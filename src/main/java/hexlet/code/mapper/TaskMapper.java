package hexlet.code.mapper;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class TaskMapper {
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private UserRepository userRepository;

    @Mapping(target = "title", source = "name")
    @Mapping(target = "content", source = "description")
    @Mapping(target = "assignee_id", source = "assignee.id")
    @Mapping(target = "status", source = "taskStatus.slug")
    @Mapping(target = "taskLabelIds", source = "labels", qualifiedByName = "labelsToLabelIds")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "assignee", source = "assignee_id", qualifiedByName = "userIdToUser")
    @Mapping(target = "taskStatus.slug", source = "status")
    @Mapping(target = "labels", source = "taskLabelIds", qualifiedByName = "labelIdsToLabels")
    public abstract Task map(TaskCreateDTO model);

    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "assignee.id", source = "assignee_id")
    @Mapping(target = "taskStatus.slug", source = "status")
    public abstract Task map(TaskDTO model);

    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    public abstract Task map(TaskUpdateDTO model);

    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "labels", source = "taskLabelIds", qualifiedByName = "labelIdsToLabels")
    public abstract void update(TaskUpdateDTO update, @MappingTarget Task destination);

    @Named("labelIdsToLabels")
    Set<Label> labelIdsToLabels(Set<Long> ids) {
        if (ids == null) {
            return new HashSet<>();
        }
        return ids.stream()
                .map(id -> labelRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found")))
                .collect(Collectors.toSet());
    }

    @Named("userIdToUser")
    User userIdToUser(Long id) {
        if (id == null) {
            return null;
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    @Named("labelsToLabelIds")
    Set<Long> labelsToLabelIds(Set<Label> labels) {
        if (labels == null) {
            return new HashSet<>();
        }
        return labels.stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
    }
}
