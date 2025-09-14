package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage);
        controller = new UserController(service);
    }

    @Test
    void createUser_withValidData_shouldSucceed() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = controller.create(user);
        assertNotNull(created.getId());
        assertEquals("testuser", created.getName());
    }

    @Test
    void createUser_withInvalidEmail_shouldThrow() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("testuser");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> controller.create(user));
        assertEquals("Email должен содержать '@'", ex.getMessage());
    }

    @Test
    void createUser_withBlankLogin_shouldThrow() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("   ");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> controller.create(user));
        assertEquals("Логин не может быть пустым или содержать пробелы", ex.getMessage());
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

    @Test
    void addAndRemoveFriends_shouldWork() {
        User user1 = controller.create(createUser("user1", "user1@mail.com"));
        User user2 = controller.create(createUser("user2", "user2@mail.com"));

        controller.addFriend(user1.getId(), user2.getId());
        Collection<User> friends1 = controller.getFriends(user1.getId());
        Collection<User> friends2 = controller.getFriends(user2.getId());

        assertEquals(1, friends1.size());
        assertEquals(user2.getId(), friends1.iterator().next().getId());
        assertEquals(1, friends2.size());
        assertEquals(user1.getId(), friends2.iterator().next().getId());

        controller.removeFriend(user1.getId(), user2.getId());
        assertTrue(controller.getFriends(user1.getId()).isEmpty());
        assertTrue(controller.getFriends(user2.getId()).isEmpty());
    }

    @Test
    void getCommonFriends_shouldWork() {
        User user1 = controller.create(createUser("user1", "user1@mail.com"));
        User user2 = controller.create(createUser("user2", "user2@mail.com"));
        User user3 = controller.create(createUser("user3", "user3@mail.com"));

        controller.addFriend(user1.getId(), user3.getId());
        controller.addFriend(user2.getId(), user3.getId());

        Collection<User> common = controller.getCommonFriends(user1.getId(), user2.getId());
        assertEquals(1, common.size());
        assertEquals(user3.getId(), common.iterator().next().getId());
    }

    private User createUser(String login, String email) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setBirthday(LocalDate.of(2000,1,1));
        return user;
    }
}