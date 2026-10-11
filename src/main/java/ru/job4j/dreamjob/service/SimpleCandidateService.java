package ru.job4j.dreamjob.service;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Service;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.repository.CandidateRepository;

import java.util.Collection;
import java.util.Optional;

@ThreadSafe
@Service
public class SimpleCandidateService implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final FileService fileService;

    public SimpleCandidateService(CandidateRepository candidateRepository,
                                  FileService fileService) {
        this.candidateRepository = candidateRepository;
        this.fileService = fileService;
    }

    @Override
    public Candidate save(Candidate candidate, FileDto file) {
        saveNewFile(candidate, file);
        return candidateRepository.save(candidate);
    }

    private void saveNewFile(Candidate candidate, FileDto file) {
        var savedFile = fileService.save(file);
        candidate.setFileId(savedFile.getId());
    }

    @Override
    public boolean update(Candidate candidate, FileDto file) {
        var oldCandidateOptional = candidateRepository.findById(candidate.getId());

        if (oldCandidateOptional.isEmpty()) {
            return false;
        }

        var oldCandidate = oldCandidateOptional.get();
        int oldFileId = oldCandidate.getFileId();

        candidate.setFileId(oldFileId);

        if (file.getContent().length == 0) {
            return candidateRepository.update(candidate);
        }

        saveNewFile(candidate, file);

        boolean updated = candidateRepository.update(candidate);

        if (!updated) {
            fileService.deleteById(candidate.getFileId());
            return false;
        }

        if (oldFileId != 0) {
            fileService.deleteById(oldFileId);
        }

        return true;
    }

    @Override
    public boolean deleteById(int id) {
        var candidateOptional = candidateRepository.findById(id);

        if (candidateOptional.isEmpty()) {
            return false;
        }

        var candidate = candidateOptional.get();

        boolean deleted = candidateRepository.deleteById(id);

        if (deleted && candidate.getFileId() != 0) {
            fileService.deleteById(candidate.getFileId());
        }

        return deleted;
    }

    @Override
    public Optional<Candidate> findById(int id) {
        return candidateRepository.findById(id);
    }

    @Override
    public Collection<Candidate> findAll() {
        return candidateRepository.findAll();
    }
}