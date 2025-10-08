package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public Film add(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public void addLike(int filmId, int userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void removeLike(int filmId, int userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Film> getPopular(int count) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}