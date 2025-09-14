package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(int userId, int friendId) {
        User user = getById(userId);
        User friend = getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Collection<User> getFriends(int userId) {
        User user = getById(userId);
        return user.getFriends().stream().map(this::getById).collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> friends1 = getById(userId).getFriends();
        Set<Integer> friends2 = getById(otherId).getFriends();

        return friends1.stream().filter(friends2::contains).map(this::getById).collect(Collectors.toList());
    }

    private void validate(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("Email должен содержать '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(java.time.LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}