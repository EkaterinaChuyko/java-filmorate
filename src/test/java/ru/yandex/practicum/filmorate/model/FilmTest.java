package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {

    @Test
    void shouldCreateFilmWithValidData() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertEquals("Test Film", film.getName());
        assertEquals("Test Description", film.getDescription());
        assertEquals(120, film.getDuration());
        assertTrue(film.getLikes().isEmpty());
    }

    @Test
    void shouldAddLikeToFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDuration(120);

        film.getLikes().add(1);
        film.getLikes().add(2);

        assertEquals(2, film.getLikes().size());
        assertTrue(film.getLikes().contains(1));
        assertTrue(film.getLikes().contains(2));
    }

    @Test
    void shouldSetAndGetMpa() {
        Film film = new Film();
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("PG-13");

        film.setMpa(mpa);

        assertEquals(1, film.getMpa().getId());
        assertEquals("PG-13", film.getMpa().getName());
    }

    @Test
    void shouldSetAndGetGenres() {
        Film film = new Film();
        Genre genre1 = new Genre();
        genre1.setId(1);
        genre1.setName("Комедия");

        Genre genre2 = new Genre();
        genre2.setId(2);
        genre2.setName("Драма");

        film.getGenres().add(genre1);
        film.getGenres().add(genre2);

        assertEquals(2, film.getGenres().size());
    }
}