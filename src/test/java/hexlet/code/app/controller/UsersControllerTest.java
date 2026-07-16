package hexlet.code.app.controller;

import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;

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
public class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void testFindOne() throws Exception {
        var testUser = new User();
        testUser.setEmail("john@google.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser = userRepository.save(testUser);

        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value("john@google.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    public void testFindAll() throws Exception {
        var user1 = new User();
        user1.setEmail("john@google.com");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        userRepository.save(user1);

        var user2 = new User();
        user2.setEmail("jack@yahoo.com");
        user2.setFirstName("Jack");
        user2.setLastName("Jons");
        userRepository.save(user2);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email",
                        containsInAnyOrder("john@google.com", "jack@yahoo.com")))
                .andExpect(jsonPath("$[*].firstName",
                        containsInAnyOrder("John", "Jack")))
                .andExpect(jsonPath("$[*].lastName",
                        containsInAnyOrder("Doe", "Jons")))
                .andExpect(jsonPath("$[*].createdAt").exists());
    }

    @Test
    public void testCreateOne() throws Exception {
        var data = new HashMap<>();
        data.put("email", "jack@google.com");
        data.put("firstName", "Jack");
        data.put("lastName", "Jons");
        data.put("password", "some-password");

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jack@google.com"))
                .andExpect(jsonPath("$.firstName").value("Jack"))
                .andExpect(jsonPath("$.lastName").value("Jons"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());

        var user = userRepository.findByEmail("jack@google.com")
                .orElseThrow();
        assertThat(user.getFirstName()).isEqualTo("Jack");
        assertThat(user.getLastName()).isEqualTo("Jons");
    }

    @Test
    public void testUpdateOne() throws Exception {
        var testUser = new User();
        testUser.setEmail("jack@google.com");
        testUser.setFirstName("Jack");
        testUser.setLastName("Jons");
        testUser = userRepository.save(testUser);

        var data = new HashMap<>();
        data.put("email", "jack@yahoo.com");
        data.put("firstName", "firstName");
        data.put("lastName", "lastName");

        var request = put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value("jack@yahoo.com"))
                .andExpect(jsonPath("$.firstName").value("firstName"))
                .andExpect(jsonPath("$.lastName").value("lastName"))
                .andExpect(jsonPath("$.createdAt").exists());

        var updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getFirstName()).isEqualTo("firstName");
        assertThat(updatedUser.getLastName()).isEqualTo("lastName");
        assertThat(updatedUser.getEmail()).isEqualTo("jack@yahoo.com");
    }

    @Test
    public void testUpdateOneWithInvalidData() throws Exception {
        var testUser = new User();
        testUser.setEmail("jack@google.com");
        testUser.setFirstName("Jack");
        testUser.setLastName("Jons");
        testUser = userRepository.save(testUser);

        var data = new HashMap<>();
        data.put("email", "invalid-email");
        data.put("firstName", "firstName");
        data.put("lastName", "lastName");

        var request = put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        var user = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(user.getEmail()).isEqualTo("jack@google.com");
    }

    @Test
    public void testDeleteOne() throws Exception {
        var testUser = new User();
        testUser.setEmail("jack@google.com");
        testUser.setFirstName("Jack");
        testUser.setLastName("Jons");
        testUser = userRepository.save(testUser);

        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }

}
