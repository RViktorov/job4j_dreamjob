package ru.job4j.dreamjob.controller;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.io.IOException;

@ThreadSafe
@Controller
@RequestMapping("/candidates")
public class CandidateController {

    private final CandidateService candidateService;
    private final CityService cityService;

    public CandidateController(CandidateService candidateService,
                               CityService cityService) {
        this.candidateService = candidateService;
        this.cityService = cityService;
    }

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("candidates", candidateService.findAll());
        return "candidates/list";
    }

    @GetMapping("/create")
    public String getCreationPage(Model model) {
        model.addAttribute("cities", cityService.findAll());
        return "candidates/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Candidate candidate,
                         @RequestParam("file") MultipartFile file,
                         Model model) {
        try {
            FileDto fileDto = new FileDto(
                    file.getOriginalFilename(),
                    file.getBytes()
            );

            candidateService.save(candidate, fileDto);

            return "redirect:/candidates";

        } catch (IOException exception) {
            model.addAttribute("message", exception.getMessage());
            return "errors/404";
        }
    }

    @GetMapping("/{id}")
    public String getById(Model model, @PathVariable int id) {
        var candidateOptional = candidateService.findById(id);

        if (candidateOptional.isEmpty()) {
            model.addAttribute(
                    "message",
                    "Резюме с указанным идентификатором не найдено"
            );
            return "errors/404";
        }

        model.addAttribute("candidate", candidateOptional.get());
        model.addAttribute("cities", cityService.findAll());

        return "candidates/one";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Candidate candidate,
                         @RequestParam("file") MultipartFile file,
                         Model model) {
        try {
            FileDto fileDto = new FileDto(
                    file.getOriginalFilename(),
                    file.getBytes()
            );

            boolean updated = candidateService.update(candidate, fileDto);

            if (!updated) {
                model.addAttribute(
                        "message",
                        "Резюме с указанным идентификатором не найдено"
                );
                return "errors/404";
            }

            return "redirect:/candidates";

        } catch (IOException exception) {
            model.addAttribute("message", exception.getMessage());
            return "errors/404";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id, Model model) {
        boolean deleted = candidateService.deleteById(id);

        if (deleted) {
            return "redirect:/candidates";
        }

        model.addAttribute(
                "message",
                "Резюме с указанным идентификатором не найдено"
        );

        return "errors/404";
    }
}