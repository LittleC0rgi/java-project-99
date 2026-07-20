package hexlet.code.app.controller;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.repository.TaskStatusRepository;
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
@RequestMapping("/api/task_statuses")
@RequiredArgsConstructor
public class TaskStatusesController {
    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TaskStatusDTO create(@Valid @RequestBody TaskStatusCreateDTO data) {
        var status = taskStatusMapper.map(data);
        taskStatusRepository.save(status);
        return taskStatusMapper.map(status);
    }

    @GetMapping()
    ResponseEntity<List<TaskStatusDTO>> findAll() {
        var statuses = taskStatusRepository.findAll();
        var result = statuses.stream().map(taskStatusMapper::map).toList();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(statuses.size())).body(result);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    TaskStatusDTO findOne(@PathVariable Long id) {
        var status = taskStatusRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not Found"));
        return taskStatusMapper.map(status);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    TaskStatusDTO update(@Valid @RequestBody TaskStatusUpdateDTO data, @PathVariable Long id) {
        var user = taskStatusRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not Found"));
        taskStatusMapper.update(data, user);
        taskStatusRepository.save(user);
        return taskStatusMapper.map(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        taskStatusRepository.deleteById(id);
    }
}
