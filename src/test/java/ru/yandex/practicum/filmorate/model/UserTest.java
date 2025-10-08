package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithValidData() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1998, 5, 5));

        assertEquals("test@mail.com", user.getEmail());
        assertEquals("testlogin", user.getLogin());
        assertEquals("Test User", user.getName());
        assertTrue(user.getFriends().isEmpty());
    }

    @Test
    void shouldSetLoginAsNameWhenNameIsNull() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setName(null);

        assertNull(user.getName());
    }

    @Test
    void shouldAddFriend() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");

        user.getFriends().add(1);
        user.getFriends().add(2);

        assertEquals(2, user.getFriends().size());
        assertTrue(user.getFriends().contains(1));
        assertTrue(user.getFriends().contains(2));
    }

    @Test
    void shouldSetBirthday() {
        User user = new User();
        LocalDate birthday = LocalDate.of(1995, 5, 15);
        user.setBirthday(birthday);

        assertEquals(birthday, user.getBirthday());
    }
}
