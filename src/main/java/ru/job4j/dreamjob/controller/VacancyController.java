package ru.job4j.dreamjob.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
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
    public String getAll(Model model) {
        model.addAttribute("vacancies", vacancyService.findAll());
        return "vacancies/list";
    }

    @GetMapping("/create")
    public String getCreationPage(Model model) {
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
    public String getById(@PathVariable int id, Model model) {
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
                         Model model) throws IOException {

        FileDto fileDto = new FileDto(
                file.getOriginalFilename(),
                file.getBytes()
        );

        var isUpdated = vacancyService.update(vacancy, fileDto);

        if (!isUpdated) {
            model.addAttribute("message", "Вакансия не найдена");
            return "errors/404";
        }

        return "redirect:/vacancies";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id, Model model) {
        var isDeleted = vacancyService.deleteById(id);

        if (!isDeleted) {
            model.addAttribute("message", "Вакансия не найдена");
            return "errors/404";
        }

        return "redirect:/vacancies";
    }
}