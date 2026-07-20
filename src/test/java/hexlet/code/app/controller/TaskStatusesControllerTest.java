package hexlet.code.app.controller;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.AuthenticationTestUtils;
import hexlet.code.app.util.UserGenerator;
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

import static hexlet.code.app.util.AuthenticationTestUtils.DEFAULT_PASSWORD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
public class TaskStatusesControllerTest {
    private static final String PATH = "/api/task_statuses";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

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

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }

    protected String loginAsNewUser() throws Exception {
        var testUser = userGenerator.generateOne();
        testUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        testUser = userRepository.save(testUser);
        return authenticationTestUtils.login(testUser);
    }

    @Test
    public void testFindOne() throws Exception {
        var token = loginAsNewUser();

        var testStatus = new TaskStatus();
        testStatus.setName(faker.funnyName().name());
        testStatus.setSlug("test_slug");
        var savedStatus = taskStatusRepository.save(testStatus);

        mockMvc.perform(get(PATH + "/{id}", savedStatus.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedStatus.getId()))
                .andExpect(jsonPath("$.name").value(savedStatus.getName()))
                .andExpect(jsonPath("$.slug").value(savedStatus.getSlug()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    public void testFindAll() throws Exception {
        var token = loginAsNewUser();

        var testStatus = new TaskStatus();
        testStatus.setName("Draft");
        testStatus.setSlug("draft");
        var savedStatus = taskStatusRepository.save(testStatus);
        var testStatus2 = new TaskStatus();
        testStatus2.setName("ToReview");
        testStatus2.setSlug("to_review");
        var savedStatus2 = taskStatusRepository.save(testStatus2);


        var expectedNames = new String[]{savedStatus.getName(), savedStatus2.getName()};
        var expectedSlugs = new String[]{savedStatus.getSlug(), savedStatus2.getSlug()};

        mockMvc.perform(get(PATH)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(expectedNames)))
                .andExpect(jsonPath("$[*].slug", containsInAnyOrder(expectedSlugs)))
                .andExpect(jsonPath("$[*].createdAt").exists());
    }

    @Test
    public void testCreateOne() throws Exception {
        var token = loginAsNewUser();

        var taskStatus = new TaskStatusCreateDTO();
        taskStatus.setName("New");
        taskStatus.setSlug("new");

        var request = post(PATH)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskStatus));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(taskStatus.getName()))
                .andExpect(jsonPath("$.slug").value(taskStatus.getSlug()))
                .andExpect(jsonPath("$.createdAt").exists());

        var savedStatus = taskStatusRepository.findBySlug(taskStatus.getSlug())
                .orElseThrow();
        assertThat(savedStatus.getName()).isEqualTo(taskStatus.getName());
        assertThat(savedStatus.getSlug()).isEqualTo(taskStatus.getSlug());
    }

    @Test
    public void testUpdateOne() throws Exception {
        var token = loginAsNewUser();

        var testStatus = new TaskStatus();
        testStatus.setName("oldStatus");
        testStatus.setSlug("old_status");

        var savedStatus = taskStatusRepository.save(testStatus);

        var updatedData = new TaskStatusUpdateDTO();
        updatedData.setName("newStatus");

        var request = put(PATH + "/{id}", savedStatus.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedStatus.getId()))
                .andExpect(jsonPath("$.name").value(updatedData.getName()))
                .andExpect(jsonPath("$.slug").value(savedStatus.getSlug()))
                .andExpect(jsonPath("$.createdAt").exists());

        var updatedStatus = taskStatusRepository.findById(savedStatus.getId()).orElseThrow();
        assertThat(updatedStatus.getName()).isEqualTo(updatedData.getName());
    }

    @Test
    public void testDeleteOne() throws Exception {
        var token = loginAsNewUser();

        var testStatus = new TaskStatus();
        testStatus.setName("status");
        testStatus.setSlug("status");
        var savedStatus = taskStatusRepository.save(testStatus);

        mockMvc.perform(delete(PATH + "/{id}", savedStatus.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.findById(savedStatus.getId())).isEmpty();
    }
}
