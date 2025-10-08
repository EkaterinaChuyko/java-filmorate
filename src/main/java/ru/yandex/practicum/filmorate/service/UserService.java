package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
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
    }

    public void removeFriend(int userId, int friendId) {
        getById(userId);
        getById(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getFriends(int userId) {
        return userStorage.getById(userId).orElseThrow(() -> new NoSuchElementException("Пользователь не найден: " + userId)).getFriends().stream().map(this::getById).collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> friends1 = getById(userId).getFriends();
        Set<Integer> friends2 = getById(otherId).getFriends();
        return friends1.stream().filter(friends2::contains).map(this::getById).collect(Collectors.toList());
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }

    public void confirmFriend(int userId, int friendId) {
        getById(userId);
        getById(friendId);
        userStorage.confirmFriend(userId, friendId);
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