
# Angara.Net: main page app

Includes functionality of the former angara/usr module (auth, /me profile),
merged 2026-10: single process, single jar, single Jetty.

Not transferred from usr: VK/FB social login, csrf middleware.

Избранные метеостанции: для вошедших пользователей на `/meteo/` доступен
переключатель `☆` / `★` справа от времени. Коды станций сохраняются массивом
JSONB в `users.meteo_favorites`. При следующей загрузке страницы сервер ставит
избранные станции первыми, сохраняя исходный порядок внутри обеих групп.
Отметка сохраняется и при временном отсутствии станции в списке активных.
`POST /meteo/favorite` принимает JSON `{st, favorite}` (boolean), требует
авторизацию и заголовок `x-csrf-token` с токеном текущей сессии.

Перед запуском новой версии применить к БД приложения миграцию
`sql/2026-10-30-meteo-favorites.sql`.
Проверка на локальной тестовой PostgreSQL (создаёт и удаляет собственную схему):

```sh
METEO_TEST_JDBC_URL=jdbc:postgresql://127.0.0.1:PORT/postgres?user=USER \
  clojure -Sdeps '{:paths ["src" "resources" "test"]}' -M -m meteo-favorites-test
```


На Angara.Net запускается в тестовом режиме Календарь событий.
В него можно добавлять свои мероприятия:
1. Создать тему на форуме
2. Добавить картинку в первое сообщение
3. Нажать в заголовке темы [в календарь]
4. Указать актуальныю дату мероприятия
5. Поставить галочку "Показывать"


```
https://rp5.ru/%D0%90%D1%80%D1%85%D0%B8%D0%B2_%D0%BF%D0%BE%D0%B3%D0%BE%D0%B4%D1%8B_%D0%B2_%D0%A5%D0%B0%D0%BC%D0%B0%D1%80-%D0%94%D0%B0%D0%B1%D0%B0%D0%BD%D0%B5

http://pogodaiklimat.ru/weather.php?id=30818
```
