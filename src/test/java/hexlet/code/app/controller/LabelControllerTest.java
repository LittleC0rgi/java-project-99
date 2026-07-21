package hexlet.code.app.controller;

import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelUpdateDTO;
import hexlet.code.app.model.Label;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.AuthenticationTestUtils;
import hexlet.code.app.util.UserGenerator;
import hexlet.code.app.utils.NamedRoutes;
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
public class LabelControllerTest {
    private static final String PATH = NamedRoutes.LABELS;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

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
        labelRepository.deleteAll();
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

        var testLabel = new Label();
        testLabel.setName("Bug");
        var savedLabel = labelRepository.save(testLabel);

        mockMvc.perform(get(PATH + "/{id}", savedLabel.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedLabel.getId()))
                .andExpect(jsonPath("$.name").value(savedLabel.getName()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    public void testFindAll() throws Exception {
        var token = loginAsNewUser();

        var testLabel = new Label();
        testLabel.setName("bug");
        var savedLabel = labelRepository.save(testLabel);
        var testLabel2 = new Label();
        testLabel2.setName("feature");
        var savedLabel2 = labelRepository.save(testLabel2);

        var expectedNames = new String[]{savedLabel.getName(), savedLabel2.getName()};

        mockMvc.perform(get(PATH)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(expectedNames)))
                .andExpect(jsonPath("$[*].createdAt").exists());
    }

    @Test
    public void testCreateOne() throws Exception {
        var token = loginAsNewUser();

        var label = new LabelCreateDTO();
        label.setName("new label");

        var request = post(PATH)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(label));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(label.getName()))
                .andExpect(jsonPath("$.createdAt").exists());

        var savedLabel = labelRepository.findAll().stream()
                .filter(l -> l.getName().equals(label.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(savedLabel.getName()).isEqualTo(label.getName());
    }

    @Test
    public void testUpdateOne() throws Exception {
        var token = loginAsNewUser();

        var testLabel = new Label();
        testLabel.setName("oldLabel");
        var savedLabel = labelRepository.save(testLabel);

        var updatedData = new LabelUpdateDTO();
        updatedData.setName("Bug");

        var request = put(PATH + "/{id}", savedLabel.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedLabel.getId()))
                .andExpect(jsonPath("$.name").value(updatedData.getName()))
                .andExpect(jsonPath("$.createdAt").exists());

        var updatedLabel = labelRepository.findById(savedLabel.getId()).orElseThrow();
        assertThat(updatedLabel.getName()).isEqualTo(updatedData.getName());
    }

    @Test
    public void testDeleteOne() throws Exception {
        var token = loginAsNewUser();

        var testLabel = new Label();
        testLabel.setName("toDelete");
        var savedLabel = labelRepository.save(testLabel);

        mockMvc.perform(delete(PATH + "/{id}", savedLabel.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(labelRepository.findById(savedLabel.getId())).isEmpty();
    }
}
