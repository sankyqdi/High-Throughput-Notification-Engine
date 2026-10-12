package standart.code.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import standart.code.constant.ApiRoutes;
import standart.code.dto.ChangePasswordRequest;
import standart.code.dto.RegisterUserRequest;
import standart.code.dto.UserResponseDTO;
import standart.code.service.UserServiceInterface;

@RestController
@RequestMapping(ApiRoutes.USER)
@RequiredArgsConstructor
public class UserController {

    private final UserServiceInterface userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register (@Valid @RequestBody RegisterUserRequest request) {

        UserResponseDTO dto = userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);

    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<UserResponseDTO> userProfile (@PathVariable Long id) {

        UserResponseDTO dto = userService.getProfile(id);

        return ResponseEntity.ok(dto);

    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentProfile (@AuthenticationPrincipal UserDetails userDetails) {

        UserResponseDTO dto = userService.getCurrentProfile(userDetails.getUsername());

        return ResponseEntity.ok(dto);

    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {

        userService.changePassword(userDetails.getUsername(), request);

        return ResponseEntity.noContent().build();

    }

}
