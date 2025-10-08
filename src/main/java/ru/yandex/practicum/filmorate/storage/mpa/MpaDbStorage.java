package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> getAll() {
        String sql = "SELECT * FROM mpa_rating ORDER BY rating_id";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    public Optional<Mpa> getById(int id) {
        String sql = "SELECT * FROM mpa_rating WHERE rating_id=?";
        List<Mpa> list = jdbcTemplate.query(sql, this::mapRowToMpa, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("rating_id"));
        mpa.setName(rs.getString("code_rate"));
        mpa.setDescription(rs.getString("description"));
        return mpa;
    }
}