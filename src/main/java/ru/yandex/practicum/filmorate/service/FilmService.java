package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId).orElseThrow(() -> new NoSuchElementException("Фильм не найден: " + filmId));
        film.getLikes().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        filmStorage.getById(filmId).ifPresent(f -> f.getLikes().remove(userId));
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getAll().stream().sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size())).limit(count).collect(Collectors.toList());
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        return filmStorage.getById(id).orElseThrow(() -> new NoSuchElementException("Фильм не найден: " + id));
    }

    public Film addFilm(Film film) {
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getById(film.getId()).isEmpty()) {
            throw new NoSuchElementException("Фильм не найден: " + film.getId());
        }
        return filmStorage.update(film);
    }
}