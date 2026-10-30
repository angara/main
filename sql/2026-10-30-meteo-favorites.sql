-- Per-user station IDs, independent of the shared weather API/cache.
alter table users
  add column if not exists meteo_favorites jsonb not null default '[]'::jsonb
  check (jsonb_typeof(meteo_favorites) = 'array');
