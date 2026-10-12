package standart.code.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import standart.code.constant.ApiRoutes;
import standart.code.dto.RegisterSenderRequest;
import standart.code.service.SenderAccountService;

@RestController
@RequestMapping(ApiRoutes.API_REGISTER)
@RequiredArgsConstructor
public class SenderAccountController {

    private final SenderAccountService senderAccountService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerSender(@Valid @RequestBody RegisterSenderRequest request) {

        senderAccountService.registerAccount(request);

        return ResponseEntity.ok().build();

    }

}
