-- ;;
-- ;;  MongoDB -> PostgreSQL migration, stage 2: calendar (2026-10-26)
-- ;;
-- ;;  663 docs from mongo collection "calendar".
-- ;;  Status values: publ / NULL (never shown; mongo also had "" - unified to NULL).
-- ;;  date_1/tags absent in all mongo docs - columns omitted
-- ;;  (code never reads them; add back if the feature ever appears).
-- ;;
-- ;;  Idempotent: safe to re-run.
-- ;;

create table if not exists calendar (
  id     varchar(24) primary key,     -- mongo ObjectId, hex string
  uid    integer      not null,
  ct     timestamptz  not null,
  ts     timestamptz  not null,
  date   timestamptz  not null,       -- event date (instant; shown in Asia/Irkutsk)
  date_1 timestamptz,
  status varchar(10),                 -- publ = show on the public calendar
  title  varchar(200) not null,
  descr  text,
  thumb  varchar(60),
  link   varchar(60)
);

create index if not exists calendar_uid_idx  on calendar (uid);
create index if not exists calendar_date_idx on calendar (date desc);
