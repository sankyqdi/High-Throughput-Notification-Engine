package standart.code.dto;

import lombok.*;
import standart.code.model.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDTO {

//    private Long id;
    private User userType;

    private String email;

    private String name;

    private String body;

}
