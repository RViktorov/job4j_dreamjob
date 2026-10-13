package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void whenRequestRegistrationPageThenGetRegistrationPage() {
        var view = userController.getRegistrationPage();

        assertThat(view).isEqualTo("users/register");
    }

    @Test
    void whenRegisterNewUserThenRedirectToVacancies() {
        var user = new User();
        user.setName("Иван");
        user.setEmail("ivan@mail.ru");
        user.setPassword("12345");

        when(userService.save(user)).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();

        var view = userController.register(model, user);

        assertThat(view).isEqualTo("redirect:/vacancies");

        verify(userService).save(user);
    }

    @Test
    void whenRegisterExistingEmailThenGetErrorPage() {
        var user = new User();
        user.setEmail("ivan@mail.ru");

        when(userService.save(user)).thenReturn(Optional.empty());

        var model = new ConcurrentModel();

        var view = userController.register(model, user);

        assertThat(view).isEqualTo("errors/404");

        assertThat(model.getAttribute("message"))
                .isEqualTo(
                        "Пользователь с такой почтой уже существует"
                );
    }

    @Test
    void whenRequestLoginPageThenGetLoginPage() {
        var view = userController.getLoginPage();

        assertThat(view).isEqualTo("users/login");
    }

    @Test
    void whenLoginWithCorrectCredentialsThenSaveUserInSession() {
        var user = new User();
        user.setName("Иван");
        user.setEmail("ivan@mail.ru");
        user.setPassword("12345");

        when(userService.findByEmailAndPassword(
                "ivan@mail.ru", "12345"
        )).thenReturn(Optional.of(user));

        var request = mock(HttpServletRequest.class);
        var session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);

        var model = new ConcurrentModel();

        var view = userController.loginUser(
                user, model, request
        );

        assertThat(view).isEqualTo("redirect:/vacancies");

        verify(session).setAttribute("user", user);
    }

    @Test
    void whenLoginWithWrongCredentialsThenGetLoginPageWithError() {
        var user = new User();
        user.setEmail("ivan@mail.ru");
        user.setPassword("wrong");

        when(userService.findByEmailAndPassword(
                "ivan@mail.ru", "wrong"
        )).thenReturn(Optional.empty());

        var request = mock(HttpServletRequest.class);
        var model = new ConcurrentModel();

        var view = userController.loginUser(
                user, model, request
        );

        assertThat(view).isEqualTo("users/login");

        assertThat(model.getAttribute("error"))
                .isEqualTo("Почта или пароль введены неверно");

        verify(request, never()).getSession();
    }

    @Test
    void whenLogoutThenInvalidateSession() {
        var session = mock(HttpSession.class);

        var view = userController.logout(session);

        assertThat(view).isEqualTo("redirect:/users/login");

        verify(session).invalidate();
    }
}