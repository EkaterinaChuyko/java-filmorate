package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage) {
            @Override
            public User getById(int id) {
                User user = new User();
                user.setId(id);
                return user;
            }
        };

        FilmService filmService = new FilmService(filmStorage, userService);
        controller = new FilmController(filmService);
    }

    @Test
    void createFilm_withValidData_shouldPass() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = controller.create(film);
        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
    }

    @Test
    void createFilm_withEmptyName_shouldThrow() {
        Film film = new Film();
        film.setName("");
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Название фильма не может быть пустым", ex.getMessage());
    }

    @Test
    void createFilm_withLongDescription_shouldThrow() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("A".repeat(201));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Описание фильма не может быть длиннее 200 символов", ex.getMessage());
    }

    @Test
    void createFilm_withNegativeDuration_shouldThrow() {
        Film film = new Film();
        film.setName("Name");
        film.setDuration(-5);

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Длительность фильма должны быть положительным числом", ex.getMessage());
    }

    @Test
    void createFilm_withTooEarlyReleaseDate_shouldThrow() {
        Film film = new Film();
        film.setName("Name");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", ex.getMessage());
    }

    @Test
    void addLike_andGetPopular_shouldWork() {
        Film film1 = controller.create(createFilm("Film 1", 100));
        Film film2 = controller.create(createFilm("Film 2", 120));
        Film film3 = controller.create(createFilm("Film 3", 90));

        controller.addLike(film1.getId(), 1);
        controller.addLike(film1.getId(), 2);
        controller.addLike(film2.getId(), 1);

        List<Film> popular = controller.getPopular(2);
        assertEquals(2, popular.size());
        assertEquals(film1.getId(), popular.get(0).getId());
        assertEquals(film2.getId(), popular.get(1).getId());
    }

    private Film createFilm(String name, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description for " + name);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(duration);
        return film;
    }
}