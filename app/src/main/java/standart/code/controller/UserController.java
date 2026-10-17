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
import standart.code.dto.UserResponseDTO;
import standart.code.model.User;
import standart.code.service.UserServiceInterface;

@RestController
@RequestMapping(ApiRoutes.USER)
@RequiredArgsConstructor
public class UserController {

    private final UserServiceInterface userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> userProfile (@PathVariable Long id) {

        UserResponseDTO dto = userService.getProfile(id);

        return ResponseEntity.ok(dto);

    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentProfile (@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getCurrentProfile(user.getUsername()));
    }

    @PatchMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {

        userService.changePassword(user.getUsername(), request);


    }

}
