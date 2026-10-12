package standart.code.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class SenderAccountResponseDTO {

    private Long id;
    private String email;
    private String host;
    private String port;
    private boolean active;
    private OffsetDateTime createdAt;

}
