-- ;;
-- ;;  MongoDB -> PostgreSQL migration, stage 1 (2026-10-26)
-- ;;
-- ;;  tourserv   : 200 docs from mongo collection "tourserv"
-- ;;               (relational columns, hybrid plan variant C)
-- ;;  notify_user: job queue, was empty at cutover - no data transfer
-- ;;
-- ;;  Idempotent: safe to re-run.
-- ;;

create table if not exists tourserv (
  id      varchar(24) primary key,     -- mongo ObjectId, hex string (kept in page anchors)
  uid     integer,                     -- absent in mongo data, reserved
  ct      timestamptz,
  ts      timestamptz,
  type    varchar(10)  not null,       -- auto / equip / apart / guide
  town    varchar(60),                 -- town id from tourserv.const TOWNS
  addr    varchar(120),
  title   varchar(200) not null,
  descr   text,
  link    varchar(200),
  payload text,
  price   text,
  email   varchar(120),
  phone   varchar(200),                -- real data: up to 4 numbers in one field (83 chars)
  person  varchar(120),
  note    text,
  flags   text[]       not null default '{}'
);

create index if not exists tourserv_uid_idx        on tourserv (uid);
create index if not exists tourserv_type_idx       on tourserv (type);
create index if not exists tourserv_type_town_idx  on tourserv (type, town);


create table if not exists notify_user (
  id     varchar(24) primary key,      -- random 24-hex string
  ct     bigint      not null,         -- created, ms epoch
  inst   bigint      not null,         -- run-at timestamp, ms epoch
  ts     bigint,                       -- claimed-at, ms epoch
  status varchar(10) not null,         -- wait / process
  data   jsonb       not null          -- job payload (type, ids, deferred...)
);

create index if not exists notify_user_status_inst_idx on notify_user (status, inst);
