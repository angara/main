-- ;;
-- ;;  stage 3 merge: m2p_user (from mongodb) -> users + user_auth
-- ;;
-- ;;  MUST run in the cutover window with angara-@main and angara-@tomcat
-- ;;  stopped: java updates users.atime on every request and a bulk
-- ;;  UPDATE under that traffic wedges the forum (incident 2026-10-26).
-- ;;  All heavy decisions are precomputed into m2p_user so the final
-- ;;  UPDATE is a flat single pass.
-- ;;
-- ;;  decisions:
-- ;;   - existing atime values are Irkutsk wall time misread as UTC after
-- ;;     the type change: shift -8h once (guarded by m2p_meta marker)
-- ;;   - users_email_idx (unique lower(email)) -> plain index: 54
-- ;;     new-auth users share an email with a dormant legacy account;
-- ;;     login resolves via user_auth first
-- ;;   - users_login_idx stays unique; the single colliding login ("fb:")
-- ;;     becomes "user<uid>"
-- ;;   - overlap rows: mongo wins for login/email (except colliding email
-- ;;     -> final_email NULL = keep pg value); profile fields only
-- ;;     overwritten when mongo value is not empty; atime = greatest
-- ;;   - social vk/fb keys not transferred (decision 2026-10-26)
-- ;;
-- ;;  safe to re-run inside the window.
-- ;;

begin;

create table if not exists m2p_meta (k text primary key, ts timestamptz default now());

-- 1. one-time zone repair
do $$
begin
  if not exists (select 1 from m2p_meta where k = 'atime_shift_minus_8h') then
    update users set atime = atime - interval '8 hours';
    insert into m2p_meta (k) values ('atime_shift_minus_8h');
  end if;
end
$$;

-- 2. email uniqueness -> plain lookup index
drop index if exists users_email_idx;
create index if not exists users_email_l_idx on users (lower((email)::text));

-- 3. staging columns + precompute (reads users, writes staging only)
alter table m2p_user
  add column if not exists final_login varchar(48),
  add column if not exists final_email varchar(120);

update m2p_user m set
  final_login = case when exists (select 1 from users x
                                   where lower(x.login) = lower(m.login)
                                     and x.uid <> m.uid)
                     then 'user' || m.uid else m.login end,
  final_email = case when m.email is null or m.email = ''
                        or exists (select 1 from users x
                                   where lower(x.email) = lower(m.email)
                                     and x.uid <> m.uid)
                     then null
                     else m.email end;

-- 4. insert mongo-only users
insert into users (uid, login, email, name, surname, town, note,
                   active, created, atime, ntime,
                   passhash, passcode, upic, admin, ts)
select m.uid,
       m.final_login,
       coalesce(m.email, ''),
       coalesce(m.name, ''),
       coalesce(m.surname, ''),
       m.town,
       m.note,
       true,
       m.ct,
       m.atime,
       now(),
       m.passhash,
       m.passcode,
       m.upic,
       m.admin,
       m.ts
from m2p_user m
where not exists (select 1 from users u where u.uid = m.uid)
on conflict (uid) do nothing;

-- 5. flat update of users already present in pg
update users u set
  login = m.final_login,
  email = coalesce(m.final_email, u.email),
  name    = coalesce(nullif(m.name,    ''), u.name),
  surname = coalesce(nullif(m.surname, ''), u.surname),
  town    = coalesce(m.town,  u.town),
  note    = coalesce(m.note,  u.note),
  passhash = m.passhash,
  passcode = m.passcode,
  upic     = coalesce(m.upic, u.upic),
  admin    = coalesce(m.admin, u.admin),
  ts       = m.ts,
  atime    = greatest(u.atime, coalesce(m.atime, u.atime))
from m2p_user m
where u.uid = m.uid;

-- 6. auth keys -> user_auth
insert into user_auth (auth_type, auth_id, user_id)
select split_part(k, ':', 1), split_part(k, ':', 2), m.uid::text
from m2p_user m,
     unnest(coalesce(m.auth_keys, '{}'::text[])) as k
on conflict (auth_type, auth_id) do nothing;

-- 7. single uid sequence from now on
select setval('users_uid_seq',
              (select max(uid) from users) + 1, false);

commit;
