package hexlet.code.app.controller;

import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelDTO;
import hexlet.code.app.dto.label.LabelUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.utils.NamedRoutes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(NamedRoutes.LABELS)
@RequiredArgsConstructor
public class LabelsController {
    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LabelDTO create(@Valid @RequestBody LabelCreateDTO data) {
        var label = labelMapper.map(data);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    @GetMapping()
    ResponseEntity<List<LabelDTO>> findAll() {
        var labels = labelRepository.findAll();
        var result = labels.stream().map(labelMapper::map).toList();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(labels.size())).body(result);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    LabelDTO findOne(@PathVariable Long id) {
        var status = labelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not Found"));
        return labelMapper.map(status);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    LabelDTO update(@Valid @RequestBody LabelUpdateDTO data, @PathVariable Long id) {
        var user = labelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not Found"));
        labelMapper.update(data, user);
        labelRepository.save(user);
        return labelMapper.map(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        labelRepository.deleteById(id);
    }
}
