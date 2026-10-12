package standart.code.dto;

import lombok.Builder;
import standart.code.model.Role;

@Builder
public record UserResponseDTO (
        Long id,
        String email,
        Role role,
        boolean enabled
) {
}
