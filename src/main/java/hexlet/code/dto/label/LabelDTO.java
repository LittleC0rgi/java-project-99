package hexlet.code.dto.label;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class LabelDTO {
    private Long id;
    private String name;
    private LocalDate createdAt;
}
