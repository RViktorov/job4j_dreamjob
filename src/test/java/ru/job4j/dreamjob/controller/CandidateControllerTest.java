package ru.job4j.dreamjob.controller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CandidateControllerTest {

    private CandidateService candidateService;
    private CityService cityService;
    private CandidateController candidateController;
    private MultipartFile testFile;

    @BeforeEach
    void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);

        candidateController = new CandidateController(
                candidateService, cityService
        );

        testFile = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                new byte[]{1, 2, 3}
        );
    }

    @Test
    void whenRequestCandidateListThenGetPageWithCandidates() {
        var candidate1 = new Candidate();
        var candidate2 = new Candidate();
        var expectedCandidates = List.of(candidate1, candidate2);

        when(candidateService.findAll()).thenReturn(expectedCandidates);

        var model = new ConcurrentModel();

        var view = candidateController.getAll(model);

        assertThat(view).isEqualTo("candidates/list");
        assertThat(model.getAttribute("candidates"))
                .isEqualTo(expectedCandidates);
    }

    @Test
    void whenRequestCreationPageThenGetPageWithCities() {
        var expectedCities = List.of(
                new City(1, "Москва"),
                new City(2, "Санкт-Петербург")
        );

        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();

        var view = candidateController.getCreationPage(model);

        assertThat(view).isEqualTo("candidates/create");
        assertThat(model.getAttribute("cities"))
                .isEqualTo(expectedCities);
    }

    @Test
    void whenCreateCandidateThenRedirectToCandidates()
            throws IOException {

        var candidate = new Candidate();

        var candidateCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileCaptor = ArgumentCaptor.forClass(FileDto.class);

        var model = new ConcurrentModel();

        var view = candidateController.create(
                candidate, testFile, model
        );

        assertThat(view).isEqualTo("redirect:/candidates");

        verify(candidateService).save(
                candidateCaptor.capture(),
                fileCaptor.capture()
        );

        assertThat(candidateCaptor.getValue())
                .isSameAs(candidate);

        assertThat(fileCaptor.getValue().getName())
                .isEqualTo("resume.pdf");

        assertThat(fileCaptor.getValue().getContent())
                .containsExactly(1, 2, 3);
    }

    @Test
    void whenFileReadFailsOnCreateThenGetErrorPage()
            throws IOException {

        var brokenFile = mock(MultipartFile.class);

        when(brokenFile.getOriginalFilename())
                .thenReturn("resume.pdf");

        when(brokenFile.getBytes())
                .thenThrow(new IOException("Ошибка чтения файла"));

        var model = new ConcurrentModel();

        var view = candidateController.create(
                new Candidate(), brokenFile, model
        );

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message"))
                .isEqualTo("Ошибка чтения файла");

        verifyNoInteractions(candidateService);
    }

    @Test
    void whenCandidateExistsThenGetCandidatePage() {
        var candidate = new Candidate();

        var cities = List.of(new City(1, "Москва"));

        when(candidateService.findById(1))
                .thenReturn(Optional.of(candidate));

        when(cityService.findAll()).thenReturn(cities);

        var model = new ConcurrentModel();

        var view = candidateController.getById(model, 1);

        assertThat(view).isEqualTo("candidates/one");
        assertThat(model.getAttribute("candidate"))
                .isSameAs(candidate);
        assertThat(model.getAttribute("cities"))
                .isEqualTo(cities);
    }

    @Test
    void whenCandidateDoesNotExistThenGetErrorPage() {
        when(candidateService.findById(100))
                .thenReturn(Optional.empty());

        var model = new ConcurrentModel();

        var view = candidateController.getById(model, 100);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message"))
                .isEqualTo(
                        "Резюме с указанным идентификатором не найдено"
                );

        verify(cityService, never()).findAll();
    }

    @Test
    void whenUpdateCandidateThenRedirectToCandidates()
            throws IOException {

        var candidate = new Candidate();

        when(candidateService.update(any(), any()))
                .thenReturn(true);

        var model = new ConcurrentModel();

        var view = candidateController.update(
                candidate, testFile, model
        );

        assertThat(view).isEqualTo("redirect:/candidates");

        var fileCaptor = ArgumentCaptor.forClass(FileDto.class);

        verify(candidateService).update(
                same(candidate),
                fileCaptor.capture()
        );

        assertThat(fileCaptor.getValue().getName())
                .isEqualTo("resume.pdf");

        assertThat(fileCaptor.getValue().getContent())
                .containsExactly(1, 2, 3);
    }

    @Test
    void whenUpdateNonexistentCandidateThenGetErrorPage() {
        when(candidateService.update(any(), any()))
                .thenReturn(false);

        var model = new ConcurrentModel();

        var view = candidateController.update(
                new Candidate(), testFile, model
        );

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message"))
                .isEqualTo(
                        "Резюме с указанным идентификатором не найдено"
                );
    }

    @Test
    void whenDeleteExistingCandidateThenRedirect() {
        when(candidateService.deleteById(1)).thenReturn(true);

        var model = new ConcurrentModel();

        var view = candidateController.delete(1, model);

        assertThat(view).isEqualTo("redirect:/candidates");

        verify(candidateService).deleteById(1);
    }

    @Test
    void whenDeleteNonexistentCandidateThenGetErrorPage() {
        when(candidateService.deleteById(100)).thenReturn(false);

        var model = new ConcurrentModel();

        var view = candidateController.delete(100, model);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message"))
                .isEqualTo(
                        "Резюме с указанным идентификатором не найдено"
                );

        verify(candidateService).deleteById(100);
    }
}