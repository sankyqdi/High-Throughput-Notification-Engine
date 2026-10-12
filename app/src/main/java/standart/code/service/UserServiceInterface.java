package standart.code.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import standart.code.dto.ChangePasswordRequest;
import standart.code.dto.RegisterSenderRequest;
import standart.code.dto.RegisterUserRequest;
import standart.code.dto.UserResponseDTO;

public interface UserServiceInterface extends UserDetailsService {

    UserResponseDTO register(RegisterUserRequest request);

    @Override
    UserDetails loadUserByUsername(String username);

    UserResponseDTO getProfile(Long id);

    void changePassword(String username, ChangePasswordRequest request);

    UserResponseDTO getCurrentProfile(String username);

}
