package hexlet.code.app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDTO {
    @Email
    private String email;

    @Size(min = 1, max = 200)
    private String firstName;

    @Size(min = 1, max = 200)
    private String lastName;
}
