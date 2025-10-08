package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Primary
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User add(User user) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate).withTableName("users").usingGeneratedKeyColumns("user_id");

        Map<String, Object> params = Map.of("email", user.getEmail(), "login", user.getLogin(), "name", user.getName(), "birthday", user.getBirthday());

        Number id = insert.executeAndReturnKey(params);
        user.setId(id.intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE user_id=?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        List<User> list = jdbcTemplate.query("SELECT * FROM users WHERE user_id=?", this::mapRowToUser, id);
        if (list.isEmpty()) return Optional.empty();

        User user = list.get(0);
        user.setFriends(loadFriends(id));
        return Optional.of(user);
    }

    @Override
    public Collection<User> getAll() {
        List<User> users = jdbcTemplate.query("SELECT * FROM users", this::mapRowToUser);
        Map<Integer, User> map = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        loadFriendsForUsers(map);
        return users;
    }

    public void confirmFriend(int userId, int friendId) {
        String sql = "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        if (count == null || count == 0) {
            addFriend(userId, friendId);
            log.debug("Дружба создана {} -> {}", userId, friendId);
        } else {
            log.debug("Дружба уже существует {} -> {}", userId, friendId);
        }
    }

    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        log.debug("Удалена дружба: {} -> {}", userId, friendId);
    }

    public List<Integer> getFriendIds(int userId) {
        String sql = "SELECT friend_id FROM friendship WHERE user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("friend_id"), userId);
    }

    public List<Integer> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT f1.friend_id FROM friendship f1 " + "JOIN friendship f2 ON f1.friend_id = f2.friend_id " + "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("friend_id"), userId, otherId);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sql = "MERGE INTO friendship KEY(user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        log.debug("Добавлена дружба: {} -> {}", userId, friendId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("user_id"));
        u.setEmail(rs.getString("email"));
        u.setLogin(rs.getString("login"));
        u.setName(rs.getString("name"));
        u.setBirthday(rs.getDate("birthday").toLocalDate());
        return u;
    }

    private Set<Integer> loadFriends(int userId) {
        List<Integer> list = jdbcTemplate.query("SELECT friend_id FROM friendship WHERE user_id=?", (rs, rowNum) -> rs.getInt("friend_id"), userId);
        return new HashSet<>(list);
    }

    private void loadFriendsForUsers(Map<Integer, User> map) {
        if (map.isEmpty()) return;
        String inClause = map.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = "SELECT user_id, friend_id FROM friendship WHERE user_id IN (" + inClause + ")";
        jdbcTemplate.query(sql, rs -> {
            User u = map.get(rs.getInt("user_id"));
            if (u != null) u.getFriends().add(rs.getInt("friend_id"));
        });
    }
}