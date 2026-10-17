package standart.code.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import standart.code.dto.AuthRequest;
import standart.code.dto.AuthResponse;
import standart.code.dto.RegisterUserRequest;
import standart.code.dto.UserResponseDTO;
import standart.code.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO register(@Valid @RequestBody RegisterUserRequest request) {

        return service.register(request);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {

        AuthResponse response = service.login(request);
        return ResponseEntity.ok(response);

    }
}
