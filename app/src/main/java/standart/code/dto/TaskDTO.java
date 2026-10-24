package standart.code.dto;

import lombok.*;
import standart.code.model.SenderAccount;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDTO {

    private SenderAccount senderAccount;

    private String email;

    private String name;

    private String subject;

    private String body;

}
