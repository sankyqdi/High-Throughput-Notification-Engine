package standart.code.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDTO {

//    private Long id;

    private String email;

    private String name;

    private String body;

}
