package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate).withTableName("films").usingGeneratedKeyColumns("film_id");

        Map<String, Object> params = Map.of("name", film.getName(), "description", film.getDescription(), "release_date", film.getReleaseDate(), "duration", film.getDuration(), "rating_id", film.getMpa().getId());

        Number id = insert.executeAndReturnKey(params);
        film.setId(id.intValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());
        updateGenres(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        String sql = "SELECT f.*, m.code_rate as mpa_name, m.description as mpa_description " + "FROM films f JOIN mpa_rating m ON f.rating_id = m.rating_id " + "WHERE f.film_id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);
        if (films.isEmpty()) return Optional.empty();

        Film film = films.get(0);
        film.setGenres(loadGenres(id));
        film.setLikes(loadLikes(id));
        return Optional.of(film);
    }

    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT f.*, m.code_rate as mpa_name, m.description as mpa_description " + "FROM films f JOIN mpa_rating m ON f.rating_id = m.rating_id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);

        Map<Integer, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, f -> f));
        loadGenresForFilms(filmMap);
        loadLikesForFilms(filmMap);

        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " + "m.rating_id as mpa_id, m.code_rate as mpa_name, m.description as mpa_description, " + "COUNT(fl.user_id) as likes_count " + "FROM films f " + "JOIN mpa_rating m ON f.rating_id = m.rating_id " + "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " + "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, " + "m.rating_id, m.code_rate, m.description " + "ORDER BY likes_count DESC " + "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        films.forEach(f -> f.setGenres(loadGenres(f.getId())));
        return films;
    }

    public Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("rating_id"));
        mpa.setName(rs.getString("mpa_name"));
        mpa.setDescription(rs.getString("mpa_description"));
        film.setMpa(mpa);

        return film;
    }

    public Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("genre_id"));
        genre.setName(rs.getString("name"));
        return genre;
    }

    public Set<Genre> loadGenres(int filmId) {
        String sql = "SELECT g.genre_id, g.name FROM film_genres fg " + "JOIN genres g ON fg.genre_id = g.genre_id " + "WHERE fg.film_id = ? ORDER BY g.genre_id";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre, filmId);
        return new HashSet<>(genres);
    }

    private Set<Integer> loadLikes(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("user_id"), filmId));
    }

    private void saveGenres(int filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batch = genres.stream().distinct().map(g -> new Object[]{filmId, g.getId()}).collect(Collectors.toList());
        jdbcTemplate.batchUpdate(sql, batch);
    }

    private void updateGenres(int filmId, Set<Genre> genres) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        if (genres != null && !genres.isEmpty()) saveGenres(filmId, genres);
    }

    private void loadGenresForFilms(Map<Integer, Film> filmMap) {
        if (filmMap.isEmpty()) return;
        String inClause = filmMap.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = "SELECT fg.film_id, g.genre_id, g.name FROM film_genres fg " + "JOIN genres g ON fg.genre_id = g.genre_id " + "WHERE fg.film_id IN (" + inClause + ") ORDER BY fg.film_id, g.genre_id";
        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Film film = filmMap.get(filmId);
            if (film != null) film.getGenres().add(mapRowToGenre(rs, 0));
        });
    }

    private void loadLikesForFilms(Map<Integer, Film> filmMap) {
        if (filmMap.isEmpty()) return;
        String inClause = filmMap.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + inClause + ")";
        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Film film = filmMap.get(filmId);
            if (film != null) film.getLikes().add(rs.getInt("user_id"));
        });
    }
}