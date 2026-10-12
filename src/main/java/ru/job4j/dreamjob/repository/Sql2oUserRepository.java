package ru.job4j.dreamjob.repository;

import org.springframework.stereotype.Repository;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import ru.job4j.dreamjob.model.User;

import java.sql.SQLException;
import java.util.Optional;

@Repository
public class Sql2oUserRepository implements UserRepository {

    private static final String UNIQUE_VIOLATION = "23505";

    private final Sql2o sql2o;

    public Sql2oUserRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Optional<User> save(User user) {
        try (var connection = sql2o.open()) {
            var sql = """
                    INSERT INTO users (email, name, password)
                    VALUES (:email, :name, :password)
                    """;

            var query = connection.createQuery(sql, true)
                    .addParameter("email", user.getEmail())
                    .addParameter("name", user.getName())
                    .addParameter("password", user.getPassword());

            int generatedId = query.executeUpdate()
                    .getKey(Integer.class);

            user.setId(generatedId);
            return Optional.of(user);
        } catch (Sql2oException e) {
            if (isUniqueViolation(e)) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public Optional<User> findByEmailAndPassword(
            String email, String password) {
        try (var connection = sql2o.open()) {
            var sql = """
                    SELECT id, email, name, password
                    FROM users
                    WHERE email = :email AND password = :password
                    """;

            var query = connection.createQuery(sql)
                    .addParameter("email", email)
                    .addParameter("password", password);

            var user = query.executeAndFetchFirst(User.class);
            return Optional.ofNullable(user);
        }
    }

    private boolean isUniqueViolation(Throwable exception) {
        var cause = exception;
        while (cause != null) {
            if (cause instanceof SQLException sqlException
                    && UNIQUE_VIOLATION.equals(sqlException.getSQLState())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
