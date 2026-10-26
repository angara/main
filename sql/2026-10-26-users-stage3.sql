-- ;;
-- ;;  MongoDB -> PostgreSQL migration, stage 3: users (2026-10-26)
-- ;;
-- ;;  - staging table m2p_user (drop/recreate freely, never production data)
-- ;;  - new columns on users: passhash / passcode / upic / admin / ts
-- ;;  - users.atime -> timestamptz (java writes CURRENT_TIMESTAMP in a UTC
-- ;;    session; naive values are UTC wall time)
-- ;;  - setval users_uid_seq (mongo seq "uid" n=107307 == pg max(uid))
-- ;;
-- ;;  Idempotent parts guarded with if not exists; staging drop is intended.
-- ;;

drop table if exists m2p_user;

create table m2p_user (
  uid        integer      primary key,
  login      varchar(40),
  email      varchar(120),
  name       varchar(80),
  surname    varchar(40),
  town       varchar(80),
  note       varchar(4000),
  passhash   varchar(80),
  passcode   varchar(20),
  atime      timestamptz,
  ct         timestamptz,
  ts         timestamptz,
  upic       varchar(200),
  admin      varchar(40)[],
  auth_keys  text[]
);

alter table users
  add column if not exists passhash varchar(80),
  add column if not exists passcode varchar(20),
  add column if not exists upic     varchar(200),
  add column if not exists admin    varchar(40)[],
  add column if not exists ts       timestamptz;

-- every existing writer stores UTC wall time here (pg session TZ = UTC);
-- timestamptz keeps the same instants and makes JVM-zone writes correct
alter table users alter column atime type timestamptz using atime at time zone 'UTC';

-- sequence: single uid source from now on (was mongo seq "uid")
select setval('users_uid_seq', greatest((select max(uid) from users),
                                        (select coalesce(max(uid), 0) from m2p_user)));
