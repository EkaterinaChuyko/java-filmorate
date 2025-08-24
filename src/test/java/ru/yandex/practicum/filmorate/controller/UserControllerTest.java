package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    @Test
    void createUser_withValidData_shouldSucceed() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = controller.create(user);

        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals("testuser", created.getName()); // name будет login если null
    }

    @Test
    void createUser_withInvalidEmail_shouldThrow() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("testuser");

        ValidationException ex = Assertions.assertThrows(ValidationException.class, () -> controller.create(user));
        Assertions.assertEquals("Email должен содержать '@'", ex.getMessage());
    }

    @Test
    void createUser_withBlankLogin_shouldThrow() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("   ");

        ValidationException ex = Assertions.assertThrows(ValidationException.class, () -> controller.create(user));
        Assertions.assertEquals("Логин не может быть пустым или содержать пробелы", ex.getMessage());
    }

    @Test
    void createUser_withEmptyName_shouldUseLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("userlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(2000,1,1));

        User created = controller.create(user);

        assertEquals("userlogin", created.getName());
    }
}