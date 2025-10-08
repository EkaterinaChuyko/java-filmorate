package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmDbStorage filmStorage;
    private final UserService userService;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    public List<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }

    public Film createFilm(Film film) {
        validate(film);
        if (film.getMpa() == null) {
            throw new ValidationException("MPA рейтинг обязателен");
        }
        validateMpa(film.getMpa().getId());
        if (film.getGenres() != null) {
            validateGenres(film.getGenres());
        }
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        getById(film.getId());
        validate(film);
        if (film.getMpa() == null) {
            throw new ValidationException("MPA рейтинг обязателен");
        }
        validateMpa(film.getMpa().getId());
        if (film.getGenres() != null) {
            validateGenres(film.getGenres());
        }
        return filmStorage.update(film);
    }

    public Film getById(int id) {
        return filmStorage.getById(id).orElseThrow(() -> new NoSuchElementException("Фильм не найден: " + id));
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(int filmId, int userId) {
        getById(filmId);
        userService.getById(userId);
        try {
            filmStorage.addLike(filmId, userId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Лайк уже существует");
        }
    }

    public void removeLike(int filmId, int userId) {
        getById(filmId);
        userService.getById(userId);
        try {
            filmStorage.removeLike(filmId, userId);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Лайк от пользователя " + userId + " не найден");
        }
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может быть длиннее 200 символов");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Длительность фильма должна быть положительным числом");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private void validateMpa(int mpaId) {
        mpaDbStorage.getById(mpaId).orElseThrow(() -> new IllegalArgumentException("Рейтинг MPA не найден: " + mpaId));
    }

    private void validateGenres(Set<Genre> genres) {
        for (Genre genre : genres) {
            genreDbStorage.getById(genre.getId()).orElseThrow(() -> new IllegalArgumentException("Жанр не найден: " + genre.getId()));
        }
    }
}