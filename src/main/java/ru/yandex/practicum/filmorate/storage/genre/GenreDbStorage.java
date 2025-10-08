package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getAll() {
        return jdbcTemplate.query("SELECT * FROM genres ORDER BY genre_id", this::mapRowToGenre);
    }

    public Optional<Genre> getById(int id) {
        List<Genre> list = jdbcTemplate.query("SELECT * FROM genres WHERE genre_id=?", this::mapRowToGenre, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("genre_id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}