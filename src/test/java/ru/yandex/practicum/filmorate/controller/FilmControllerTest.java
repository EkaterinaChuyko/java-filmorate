package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    @Test
    void createFilm_withValidData_shouldPass() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(2000,1,1));
        film.setDuration(120);

        Film created = controller.create(film);
        assertNotNull(created.getId());
    }

    @Test
    void createFilm_withEmptyName_shouldThrow() {
        Film film = new Film();
        film.setName("");
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> controller.create(film));
        assertEquals("Название фильма не может быть пустым", ex.getMessage());
    }

    @Test
    void createFilm_withLongDescription_shouldThrow() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("A".repeat(201));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> controller.create(film));
        assertEquals("Описание фильма не может быть длиннее 200 символов", ex.getMessage());
    }

    @Test
    void createFilm_withNegativeDuration_shouldThrow() {
        Film film = new Film();
        film.setName("Name");
        film.setDuration(-5);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> controller.create(film));
        assertEquals("Длительность фильма должны быть положительным числом", ex.getMessage());
    }

    @Test
    void createFilm_withTooEarlyReleaseDate_shouldThrow() {
        Film film = new Film();
        film.setName("Name");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> controller.create(film));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", ex.getMessage());
    }
}