package hexlet.code.app.controller;

import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
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

import java.util.HashMap;
import java.util.List;

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
    private static final String PATH = "/api/users";
    private static final String LOGIN_PATH = "/api/login";
    private static final String DEFAULT_PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private List<User> generateUsers(int size) {
        return Instancio.ofList(User.class)
                .size(size)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .create();
    }

    private String login(User user) throws Exception {
        var data = new HashMap<>();
        data.put("username", user.getEmail());
        data.put("password", DEFAULT_PASSWORD);

        var request = post(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void testFindOne() throws Exception {
        var testUser = generateUsers(1).getFirst();
        testUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        testUser = userRepository.save(testUser);

        var token = login(testUser);

        mockMvc.perform(get(PATH + "/{id}", testUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(testUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testUser.getLastName()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    public void testFindAll() throws Exception {
        var testUsers = generateUsers(5);

        var authUser = testUsers.getFirst();
        authUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        userRepository.saveAll(testUsers);

        var token = login(authUser);

        var expectedEmails = testUsers.stream().map(User::getEmail).toArray(String[]::new);
        var expectedFirstNames = testUsers.stream().map(User::getFirstName).toArray(String[]::new);
        var expectedLastNames = testUsers.stream().map(User::getLastName).toArray(String[]::new);

        mockMvc.perform(get(PATH)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder(expectedEmails)))
                .andExpect(jsonPath("$[*].firstName", containsInAnyOrder(expectedFirstNames)))
                .andExpect(jsonPath("$[*].lastName", containsInAnyOrder(expectedLastNames)))
                .andExpect(jsonPath("$[*].createdAt").exists());
    }

    @Test
    public void testCreateOne() throws Exception {
        var authUser = generateUsers(1).getFirst();
        authUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        authUser = userRepository.save(authUser);
        var token = login(authUser);

        var user = new UserCreateDTO();
        user.setEmail(faker.internet().emailAddress());
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setPassword(faker.random().toString());

        var request = post(PATH)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());

        var savedUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow();
        assertThat(savedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(user.getLastName());
    }

    @Test
    public void testUpdateOne() throws Exception {
        var testUser = generateUsers(1).getFirst();
        testUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        testUser = userRepository.save(testUser);

        var token = login(testUser);

        var updatedData = new UserUpdateDTO();
        updatedData.setEmail(faker.internet().emailAddress());
        updatedData.setFirstName(faker.name().firstName());
        updatedData.setLastName(faker.name().lastName());

        var request = put(PATH + "/{id}", testUser.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(updatedData.getEmail()))
                .andExpect(jsonPath("$.firstName").value(updatedData.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(updatedData.getLastName()))
                .andExpect(jsonPath("$.createdAt").exists());

        var updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getFirstName()).isEqualTo(updatedData.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(updatedData.getLastName());
        assertThat(updatedUser.getEmail()).isEqualTo(updatedData.getEmail());
    }

    @Test
    public void testUpdateOneWithInvalidData() throws Exception {
        var testUser = generateUsers(1).getFirst();
        testUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        testUser = userRepository.save(testUser);

        var token = login(testUser);

        var data = new HashMap<>();
        data.put("email", "invalid-email");
        data.put("firstName", "firstName");
        data.put("lastName", "lastName");

        var request = put(PATH + "/{id}", testUser.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        var user = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    public void testDeleteOne() throws Exception {
        var testUser = generateUsers(1).getFirst();
        testUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        testUser = userRepository.save(testUser);

        var token = login(testUser);

        mockMvc.perform(delete(PATH + "/{id}", testUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }
}
