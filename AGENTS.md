# AGENTS.md — angara/main

Clojure-приложение главной части angara.net: сайт (meteo, календарь, turserv,
API форума, поиск), аутентификация и профиль пользователя (модуль usr влит
2026-10). Стек: deps.edn, mount, Jetty/Ring/Compojure, Hiccup, MongoDB (monger),
PostgreSQL (clojure.jdbc + hikari + honeysql), telemere (логирование).

## Команды

```
make dev          # nREPL для разработки (CONFIG_EDN=../conf/dev.edn)
make build        # uberjar -> target/angara-main.jar (AOT, проверяет компиляцию)
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
- `src/mdb/` — MongoDB: коннект (mdb.core), сессии (mdb.sess), пользователи
  (mdb.user); `src/sql/` — PostgreSQL (sql.user синхронизирует psql-копию
  пользователей для старого JSP-форума)
- `src/mlib/` — общие утилиты (web-сессии через cookie `sid`, middleware,
  mail, time)
- `src/org/mindrot/jbcrypt/` — jBCrypt (precompiled class, не удалять
  allow-правило для *.class в .gitignore)
- `resources/public/` — статика из jar (раздаётся `route/resources "/"`),
  включая бывшую статику usr (`public/usr/inc`)

Сессии: один cookie `sid`, Mongo-коллекция `sess`; юзер — коллекция `user`
(строковые uid). CSRF-обвязки и соцвхода VK/FB нет (убраны при слиянии usr).
