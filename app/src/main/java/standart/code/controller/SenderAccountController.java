package standart.code.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import standart.code.constant.ApiRoutes;
import standart.code.dto.RegisterSenderRequest;
import standart.code.dto.SenderAccountResponseDTO;
import standart.code.model.User;
import standart.code.service.SenderAccountService;

import java.util.List;

@RestController
@RequestMapping(ApiRoutes.API_REGISTER)
@RequiredArgsConstructor
public class SenderAccountController {

    private final SenderAccountService senderAccountService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerSender(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody RegisterSenderRequest request) {

        senderAccountService.registerAccount(request, user);

        return ResponseEntity.ok().build();

    }

    @GetMapping
    public ResponseEntity<List<SenderAccountResponseDTO>> getAllSenders (@AuthenticationPrincipal User user) {

        return ResponseEntity.ok(senderAccountService.getAll(user));

    }

    @GetMapping("/{id}")
    public ResponseEntity<SenderAccountResponseDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(senderAccountService.getById(id, user));

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSender(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {

        senderAccountService.deleteById(id, user);

    }

}
