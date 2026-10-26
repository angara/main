-- ;;
-- ;;  MongoDB -> PostgreSQL migration, stage 4: sess (2026-10-26)
-- ;;
-- ;;  web sessions shared by the clojure app and the java (tomcat) forum.
-- ;;  Fixed attributes are columns; anything else either side ever put()
-- ;;  into a session goes to the data jsonb column (hybrid variant C).
-- ;;  As of the migration data all 8.3k documents had exactly the seven
-- ;;  known keys (no jsonb extras).
-- ;;
-- ;;  Idempotent: safe to re-run.
-- ;;

create table if not exists sess (
  sid   varchar(64) primary key,
  uid   integer,
  ct    timestamptz  not null,
  ts    timestamptz  not null,
  ip    varchar(64),
  tmp   boolean      not null default false,
  csrf  varchar(64)  not null,
  data  jsonb        not null default '{}'::jsonb
);

create index if not exists sess_uid_idx on sess (uid);
