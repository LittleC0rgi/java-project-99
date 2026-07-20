package hexlet.code.app.util;

import hexlet.code.app.model.User;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserGenerator {
    @Autowired
    private Faker faker;

    public List<User> generate(int size) {
        return Instancio.ofList(User.class)
                .size(size)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .create();
    }

    public User generateOne() {
        return generate(1).getFirst();
    }
}
