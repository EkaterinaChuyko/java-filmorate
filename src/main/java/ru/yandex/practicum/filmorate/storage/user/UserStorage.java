package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

public interface UserStorage {
    User add(User user);

    User update(User user);

    Optional<User> getById(int id);

    Collection<User> getAll();
}
