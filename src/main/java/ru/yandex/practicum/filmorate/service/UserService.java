package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final FilmDbStorage filmStorage;

    public User createUser(User user) {
        validate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.add(user);
    }

    public User updateUser(User user) {
        getById(user.getId());
        validate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    public User getById(int id) {
        return userStorage.getById(id).orElseThrow(() -> new NoSuchElementException("Пользователь не найден: " + id));
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(int userId, int friendId) {
        User user = getById(userId);
        User friend = getById(friendId);
        log.debug("Добавление дружбы: пользователь {} -> пользователь {}", userId, friendId);
        userStorage.addFriend(userId, friendId);
        userStorage.confirmFriend(userId, friendId);
        log.debug("Дружба подтверждена");
    }

    public void removeFriend(int userId, int friendId) {
        User user = getById(userId);
        User friend = getById(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public void confirmFriend(int userId, int friendId) {
        userStorage.confirmFriend(userId, friendId);
    }

    public Collection<User> getFriends(int userId) {
        return getById(userId).getFriends().stream().map(this::getById).collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> friends1 = getById(userId).getFriends();
        Set<Integer> friends2 = getById(otherId).getFriends();

        return friends1.stream().filter(friends2::contains).map(this::getById).collect(Collectors.toList());
    }

    public List<Film> getPopular(int count) {
        String sql = "SELECT f.*, m.code_rate as mpa_name, m.description as mpa_description, " + "COUNT(fl.user_id) as likes_count " + "FROM films f " + "JOIN mpa_rating m ON f.rating_id = m.rating_id " + "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " + "GROUP BY f.film_id, m.code_rate, m.description " + "ORDER BY likes_count DESC " + "LIMIT ?";

        List<Film> films = filmStorage.getJdbcTemplate().query(sql, filmStorage::mapRowToFilm, count);

        films.forEach(film -> {
            Set<Genre> genres = filmStorage.loadGenres(film.getId());
            film.setGenres(genres);
        });

        return films;
    }

    private void validate(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("Email должен содержать '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}