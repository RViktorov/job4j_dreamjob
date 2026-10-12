import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.repository.Sql2oUserRepository;
import ru.job4j.dreamjob.repository.UserRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

 class Sql2oUserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        var sql2o = new Sql2o(
                "jdbc:h2:./testdb;MODE=PostgreSQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;",
                "",
                ""
        );
        userRepository = new Sql2oUserRepository(sql2o);
    }

    @Test
    void whenSaveUserThenFindByEmailAndPassword() {
        var email = UUID.randomUUID() + "@mail.ru";
        var user = new User(0, email, "Иван", "12345");

        var savedUser = userRepository.save(user);
        var foundUser = userRepository.findByEmailAndPassword(
                email, "12345"
        );

        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getId()).isPositive();
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Иван");
    }

    @Test
    void whenSaveTwoUsersWithSameEmailThenSecondNotSaved() {
        var email = UUID.randomUUID() + "@mail.ru";

        var firstUser = new User(0, email, "Иван", "12345");
        var secondUser = new User(0, email, "Пётр", "67890");

        var firstResult = userRepository.save(firstUser);
        var secondResult = userRepository.save(secondUser);

        assertThat(firstResult).isPresent();
        assertThat(secondResult).isEmpty();
    }

    @Test
    void whenPasswordIncorrectThenUserNotFound() {
        var email = UUID.randomUUID() + "@mail.ru";
        var user = new User(0, email, "Иван", "12345");

        userRepository.save(user);

        var result = userRepository.findByEmailAndPassword(
                email, "wrong-password"
        );

        assertThat(result).isEmpty();
    }

    @Test
    void whenEmailDoesNotExistThenUserNotFound() {
        var email = UUID.randomUUID() + "@mail.ru";

        var result = userRepository.findByEmailAndPassword(
                email, "12345"
        );

        assertThat(result).isEmpty();
    }
}