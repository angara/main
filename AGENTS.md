# AGENTS.md — angara/main

Clojure-приложение главной части angara.net: сайт (meteo, календарь, turserv,
API форума, поиск), аутентификация и профиль пользователя (модуль usr влит
2026-10). Стек: deps.edn, mount, Jetty/Ring/Compojure, Hiccup, PostgreSQL
(next.jdbc + hikari + honeysql), telemere (логирование). MongoDB остался
только для сессий (mdb.sess, этап 4 плана mongo->pg).

## Команды

```
make dev          # nREPL для разработки (CONFIG_EDN=../conf/dev.edn)
make css          # tailwind -> resources/public/webapp/assets/main.css (npx)
make build        # css + uberjar -> target/angara-main.jar (AOT, проверяет компиляцию)
make deploy       # rsync jar на прод angara:/app/main
clj-kondo --lint src   # линтер (бинарник в ~/.local/bin)
```

Изменения в проде: jar кладётся в /app/main, рестарт `systemctl restart
angara-@main` (на сервере юниты называются `angara-@<имя>`). Перед деплоем
убедиться, что `make build` проходит.

## Правила кода

1. **Писать идиоматичный Clojure.** Следовать стилю существующего кода:
   ns-форма с отсортированными requires, kebab-case, threading-макросы
   (`->`/`->>`/`as->`) где читаемо, `(:require ...)` с префиксными векторами.
   Не тащить новые зависимости без необходимости. Ошибки логировать через
   telemere `log!` (или `mlib.logger/warn` в старых ns), не `println`.

2. **Проверять линтером.** После изменений запускать `clj-kondo --lint src` —
   должен быть чистым (0 errors, 0 warnings; базлайн с 2026-10).
   Компиляция проверяется `make build`.

## Структура

- `src/app/` — точка входа (app.main), конфигурация (app.config: jar-config.edn
  + $CONFIG_EDN поверх)
- `src/web/` — маршруты (web.app), Jetty (web.srv)
- `src/auth/`, `src/me/` — логин/регистрация/сброс пароля, профиль
- `src/calendar/`, `src/meteo/`, `src/tourserv/`, `src/forum/`, `src/front/` —
  разделы сайта
- `src/angara/webapp/` — форум-фронтенд из angara/webapp (reitit:
  /forum/groups|group|topic), next.jdbc+honey.sql 2 на общем пуле sql.conn;
  статика resources/public/webapp/assets, css собирается npm-шагом (tailwind/ в корне)
- `src/mdb/` — MongoDB (только сессии: mdb.sess + коннект mdb.core);
  `src/sql/` — PostgreSQL: sql.user (мастер-хранилище пользователей +
  user_auth, с 2026-10-26, было в mongo), sql.calendar, sql.tourserv,
  sql.notify (форум/почта + очередь уведомлений)
- `src/mlib/` — общие утилиты (web-сессии через cookie `sid`, middleware,
  mail, time)
- `src/org/mindrot/jbcrypt/` — jBCrypt (precompiled class, не удалять
  allow-правило для *.class в .gitignore)
- `resources/public/` — статика из jar (раздаётся `route/resources "/"`),
  включая бывшую статику usr (`public/usr/inc`)

Сессии: один cookie `sid`, Mongo-коллекция `sess` (перенос на pg — этап 4
плана mongo->pg, см. ../../docs/mongo-to-postgres-migration.md); пользователи —
таблица pg `users` (uid integer) + ключи входа в `user_auth`; в map'ах кода
`:id` — строковый uid, `:username` = колонка login. Соцвхода VK/FB нет.
