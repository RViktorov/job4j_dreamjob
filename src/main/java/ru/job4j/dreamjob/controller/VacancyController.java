package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.service.VacancyService;

import java.io.IOException;

@Controller
@RequestMapping("/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;
    private final CityService cityService;

    public VacancyController(VacancyService vacancyService,
                             CityService cityService) {
        this.vacancyService = vacancyService;
        this.cityService = cityService;
    }

    @GetMapping
    public String getAll(Model model, HttpSession session) {
        addUserToModel(model, session);

        model.addAttribute("vacancies", vacancyService.findAll());

        return "vacancies/list";
    }

    @GetMapping("/create")
    public String getCreationPage(Model model, HttpSession session) {
        addUserToModel(model, session);

        model.addAttribute("cities", cityService.findAll());

        return "vacancies/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Vacancy vacancy,
                         @RequestParam("file") MultipartFile file)
            throws IOException {

        FileDto fileDto = new FileDto(
                file.getOriginalFilename(),
                file.getBytes()
        );

        vacancyService.save(vacancy, fileDto);

        return "redirect:/vacancies";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable int id,
                          Model model,
                          HttpSession session) {
        addUserToModel(model, session);

        var vacancyOptional = vacancyService.findById(id);

        if (vacancyOptional.isEmpty()) {
            model.addAttribute("message", "Вакансия не найдена");
            return "errors/404";
        }

        model.addAttribute("vacancy", vacancyOptional.get());
        model.addAttribute("cities", cityService.findAll());

        return "vacancies/one";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Vacancy vacancy,
                         @RequestParam("file") MultipartFile file,
                         Model model,
                         HttpSession session) throws IOException {

        FileDto fileDto = new FileDto(
                file.getOriginalFilename(),
                file.getBytes()
        );

        var isUpdated = vacancyService.update(vacancy, fileDto);

        if (!isUpdated) {
            addUserToModel(model, session);
            model.addAttribute("message", "Вакансия не найдена");
            return "errors/404";
        }

        return "redirect:/vacancies";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id,
                         Model model,
                         HttpSession session) {
        var isDeleted = vacancyService.deleteById(id);

        if (!isDeleted) {
            addUserToModel(model, session);
            model.addAttribute("message", "Вакансия не найдена");
            return "errors/404";
        }

        return "redirect:/vacancies";
    }

    private void addUserToModel(Model model, HttpSession session) {
        var user = (User) session.getAttribute("user");

        if (user == null) {
            user = new User();
            user.setName("Гость");
        }

        model.addAttribute("user", user);
    }
}