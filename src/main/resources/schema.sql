-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    birthday DATE NOT NULL
);

-- Таблица рейтингов MPA
CREATE TABLE IF NOT EXISTS mpa_rating (
    rating_id SERIAL PRIMARY KEY,
    code_rate VARCHAR(10) NOT NULL UNIQUE,
    description TEXT
);

-- Таблица фильмов
CREATE TABLE IF NOT EXISTS films (
    film_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL,
    rating_id INTEGER REFERENCES mpa_rating(rating_id) ON DELETE SET NULL
);

-- Таблица жанров
CREATE TABLE IF NOT EXISTS genres (
    genre_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Таблица связи фильмов и жанров (многие ко многим)
CREATE TABLE IF NOT EXISTS film_genres (
    film_id INTEGER NOT NULL REFERENCES films(film_id) ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genres(genre_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

-- Таблица дружбы между пользователями
CREATE TABLE IF NOT EXISTS friendship (
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    friend_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);

-- Таблица лайков фильмов пользователями
CREATE TABLE IF NOT EXISTS film_likes (
    film_id INTEGER NOT NULL REFERENCES films(film_id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);

-- Индексы для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_films_release_date ON films(release_date);
CREATE INDEX IF NOT EXISTS idx_friendship_user_id ON friendship(user_id);
CREATE INDEX IF NOT EXISTS idx_friendship_friend_id ON friendship(friend_id);
CREATE INDEX IF NOT EXISTS idx_film_likes_user_id ON film_likes(user_id);
CREATE INDEX IF NOT EXISTS idx_film_likes_film_id ON film_likes(film_id);