DELETE FROM film_likes;
DELETE FROM friendship;
DELETE FROM film_genres;
DELETE FROM films;
DELETE FROM users;
DELETE FROM genres;
DELETE FROM mpa_rating;

ALTER TABLE film_likes ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE friendship ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE film_genres ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE genres ALTER COLUMN genre_id RESTART WITH 1;
ALTER TABLE mpa_rating ALTER COLUMN rating_id RESTART WITH 1;

MERGE INTO mpa_rating KEY(rating_id) VALUES
(1, 'G', 'Нет возрастных ограничений'),
(2, 'PG', 'Детям рекомендуется смотреть с родителями'),
(3, 'PG-13', 'Детям до 13 лет просмотр не желателен'),
(4, 'R', 'Лицам до 17 лет просмотр с взрослым'),
(5, 'NC-17', 'Лицам до 18 лет просмотр запрещён');


-- Заполнение жанров

MERGE INTO genres KEY(genre_id) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');