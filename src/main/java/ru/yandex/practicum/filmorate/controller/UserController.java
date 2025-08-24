package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validate(user, false);
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь с таким id не найден");
        }
        validate(user, true);
        User existing = users.get(user.getId());
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (user.getLogin() != null) existing.setLogin(user.getLogin());
        if (user.getName() != null) existing.setName(user.getName());
        if (user.getBirthday() != null) existing.setBirthday(user.getBirthday());
        log.info("Обновлёны данные пользователя: {}", existing);
        return existing;
    }

    private void validate(User user, boolean isUpdate) {
        if (!isUpdate || user.getEmail() != null) {
            if (user.getEmail() == null || !user.getEmail().contains("@")) {
                throw new ValidationException("Email должен содержать '@'");
            }
        }
        if (!isUpdate || user.getLogin() != null) {
            if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                throw new ValidationException("Логин не может быть пустым или содержать пробелы");
            }
        }
        if (!isUpdate || user.getBirthday() != null) {
            if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
        }
    }

    private int getNextId() {
        return users.keySet().stream().mapToInt(id -> id).max().orElse(0) + 1;
    }
}