package standart.code.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import standart.code.dto.AuthRequest;
import standart.code.dto.AuthResponse;
import standart.code.dto.RegisterUserRequest;
import standart.code.dto.UserResponseDTO;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @Transactional
    public UserResponseDTO register(RegisterUserRequest request) {

    return userService.register(request);

    }

    public AuthResponse login(AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails user = userService.loadUserByUsername(request.email());

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, request.email());

    }
}
