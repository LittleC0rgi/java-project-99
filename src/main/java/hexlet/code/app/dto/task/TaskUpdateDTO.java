package hexlet.code.app.dto.task;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {
    private String title;
    private String content;
    private Set<Long> taskLabelIds;
}
