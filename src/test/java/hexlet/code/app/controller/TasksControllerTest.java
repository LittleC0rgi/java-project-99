package hexlet.code.app.controller;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.AuthenticationTestUtils;
import hexlet.code.app.util.UserGenerator;
import hexlet.code.app.utils.NamedRoutes;
import jakarta.transaction.Transactional;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

import static hexlet.code.app.util.AuthenticationTestUtils.DEFAULT_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TasksControllerTest {
    private static final String PATH = NamedRoutes.TASKS;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    private AuthenticationTestUtils authenticationTestUtils;

    @Autowired
    private LabelRepository labelRepository;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
        labelRepository.deleteAll();
    }

    protected String loginAsNewUser() throws Exception {
        var testUser = userGenerator.generateOne();
        testUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        testUser = userRepository.save(testUser);
        return authenticationTestUtils.login(testUser);
    }

    private User createAssignee() {
        var user = userGenerator.generateOne();
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        return userRepository.save(user);
    }

    private TaskStatus createStatus(String name, String slug) {
        var status = new TaskStatus();
        status.setName(name);
        status.setSlug(slug);
        return taskStatusRepository.save(status);
    }

    private Label createLabel(String name) {
        var label = new Label();
        label.setName(name);
        return labelRepository.save(label);
    }

    @Test
    public void testFindOne() throws Exception {
        var token = loginAsNewUser();
        var assignee = createAssignee();
        var status = createStatus("ToBeFixed", "to_be_fixed");

        var testTask = new Task();
        testTask.setIndex(faker.number().numberBetween(1, 10000));
        testTask.setName("Task 1");
        testTask.setDescription("Description of task 1");
        testTask.setAssignee(assignee);
        testTask.setTaskStatus(status);
        var savedTask = taskRepository.save(testTask);

        mockMvc.perform(get(PATH + "/{id}", savedTask.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.index").value(savedTask.getIndex()))
                .andExpect(jsonPath("$.title").value(savedTask.getName()))
                .andExpect(jsonPath("$.content").value(savedTask.getDescription()))
                .andExpect(jsonPath("$.status").value(status.getSlug()))
                .andExpect(jsonPath("$.assignee_id").value(assignee.getId()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    public void testFindAll() throws Exception {
        var token = loginAsNewUser();
        var assignee = createAssignee();
        var statusToFix = createStatus("ToBeFixed", "to_be_fixed");
        var statusToReview = createStatus("ToReview", "to_review");

        var testTask = new Task();
        testTask.setIndex(3140);
        testTask.setName("Task 1");
        testTask.setDescription("Description of task 1");
        testTask.setAssignee(assignee);
        testTask.setTaskStatus(statusToFix);
        var savedTask = taskRepository.save(testTask);

        var testTask2 = new Task();
        testTask2.setIndex(3161);
        testTask2.setName("Task 2");
        testTask2.setDescription("Description of task 2");
        testTask2.setAssignee(assignee);
        testTask2.setTaskStatus(statusToReview);
        var savedTask2 = taskRepository.save(testTask2);

        var expectedTitles = new String[]{savedTask.getName(), savedTask2.getName()};

        mockMvc.perform(get(PATH)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder(expectedTitles)))
                .andExpect(jsonPath("$[*].createdAt").exists());
    }

    @Test
    @Transactional
    public void testCreateOne() throws Exception {
        var token = loginAsNewUser();
        var assignee = createAssignee();
        var status = createStatus("Draft", "draft");

        var task = new TaskCreateDTO();
        task.setIndex(12);
        task.setAssignee_id(assignee.getId());
        task.setTitle("Test title");
        task.setContent("Test content");
        task.setStatus(status.getSlug());

        var request = post(PATH)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.index").value(task.getIndex()))
                .andExpect(jsonPath("$.title").value(task.getTitle()))
                .andExpect(jsonPath("$.content").value(task.getContent()))
                .andExpect(jsonPath("$.status").value(task.getStatus()))
                .andExpect(jsonPath("$.assignee_id").value(assignee.getId()))
                .andExpect(jsonPath("$.createdAt").exists());

        var savedTask = taskRepository.findAll().stream()
                .filter(t -> t.getName().equals(task.getTitle()))
                .findFirst()
                .orElseThrow();
        assertThat(savedTask.getIndex()).isEqualTo(task.getIndex());
        assertThat(savedTask.getDescription()).isEqualTo(task.getContent());
        assertThat(savedTask.getAssignee().getId()).isEqualTo(assignee.getId());
        assertThat(savedTask.getTaskStatus().getSlug()).isEqualTo(task.getStatus());
    }

    @Test
    public void testUpdateOne() throws Exception {
        var token = loginAsNewUser();
        var assignee = createAssignee();
        var status = createStatus("Draft", "draft");

        var testTask = new Task();
        testTask.setIndex(12);
        testTask.setName("Old title");
        testTask.setDescription("Old content");
        testTask.setAssignee(assignee);
        testTask.setTaskStatus(status);
        var savedTask = taskRepository.save(testTask);

        var updatedData = new TaskUpdateDTO();
        updatedData.setTitle("New title");
        updatedData.setContent("New content");

        var request = put(PATH + "/{id}", savedTask.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.index").value(savedTask.getIndex()))
                .andExpect(jsonPath("$.title").value(updatedData.getTitle()))
                .andExpect(jsonPath("$.content").value(updatedData.getContent()))
                .andExpect(jsonPath("$.status").value(status.getSlug()))
                .andExpect(jsonPath("$.assignee_id").value(assignee.getId()))
                .andExpect(jsonPath("$.createdAt").exists());

        var updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        assertThat(updatedTask.getName()).isEqualTo(updatedData.getTitle());
        assertThat(updatedTask.getDescription()).isEqualTo(updatedData.getContent());
    }

    @Test
    public void testDeleteOne() throws Exception {
        var token = loginAsNewUser();
        var assignee = createAssignee();
        var status = createStatus("Draft", "draft");

        var testTask = new Task();
        testTask.setIndex(1);
        testTask.setName("Task to delete");
        testTask.setDescription("Content");
        testTask.setAssignee(assignee);
        testTask.setTaskStatus(status);
        var savedTask = taskRepository.save(testTask);

        mockMvc.perform(delete(PATH + "/{id}", savedTask.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(savedTask.getId())).isEmpty();
    }

    @Test
    @Transactional
    public void testCreateOneWithLabels() throws Exception {
        var token = loginAsNewUser();
        var assignee = createAssignee();
        var status = createStatus("Draft", "draft");
        var featureLabel = createLabel("feature");
        var bugLabel = createLabel("bug");

        var task = new TaskCreateDTO();
        task.setIndex(12);
        task.setAssignee_id(assignee.getId());
        task.setTitle("Test title with labels");
        task.setContent("Test content");
        task.setStatus(status.getSlug());
        task.setTaskLabelIds(Set.of(featureLabel.getId(), bugLabel.getId()));

        var request = post(PATH)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(task.getTitle()));

        var savedTask = taskRepository.findAll().stream()
                .filter(t -> t.getName().equals(task.getTitle()))
                .findFirst()
                .orElseThrow();

        var labelNames = savedTask.getLabels().stream().map(Label::getName).toList();
        assertThat(savedTask.getLabels()).hasSize(2);
        assertThat(labelNames).containsExactlyInAnyOrder(featureLabel.getName(), bugLabel.getName());
    }

    @Test
    @Transactional
    public void testUpdateOneWithLabels() throws Exception {
        var token = loginAsNewUser();
        var assignee = createAssignee();
        var status = createStatus("Draft", "draft");
        var featureLabel = createLabel("feature");
        var bugLabel = createLabel("bug");

        var testTask = new Task();
        testTask.setIndex(12);
        testTask.setName("Task with labels");
        testTask.setDescription("Content");
        testTask.setAssignee(assignee);
        testTask.setTaskStatus(status);
        testTask.setLabels(new HashSet<>(Set.of(featureLabel)));
        var savedTask = taskRepository.save(testTask);

        var updatedData = new TaskUpdateDTO();
        updatedData.setTitle("Task with labels");
        updatedData.setContent("Content");
        updatedData.setTaskLabelIds(Set.of(bugLabel.getId()));

        var request = put(PATH + "/{id}", savedTask.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()));

        var updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        var labelNames = updatedTask.getLabels().stream().map(Label::getName).toList();
        assertThat(updatedTask.getLabels()).hasSize(1);
        assertThat(labelNames).containsExactly(bugLabel.getName());
    }
}
