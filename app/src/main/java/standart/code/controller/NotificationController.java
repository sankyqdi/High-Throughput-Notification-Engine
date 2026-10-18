package standart.code.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import standart.code.constant.ApiRoutes;
import standart.code.dto.EngineStatsDTO;
import standart.code.dto.NotificationRequest;
import standart.code.model.User;
import standart.code.service.NotificationEngineService;
import standart.code.service.NotificationService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiRoutes.NOTIFICATIONS)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationEngineService notificationEngineService;

    @PostMapping(ApiRoutes.SEND_NOTIFICATION)
    public ResponseEntity<EngineStatsDTO> sendNotification(
            @Valid @RequestBody NotificationRequest notificationRequest,
            @AuthenticationPrincipal User user
    ) {

        EngineStatsDTO stats = notificationService.dataAcquisition(notificationRequest, user);
        return ResponseEntity.ok(stats);

    }

    @GetMapping("/stats")
    public ResponseEntity<EngineStatsDTO> getLiveStats() {

        EngineStatsDTO engineStatsDTO = EngineStatsDTO.builder()
                .errorsCount(notificationEngineService.getErrorsCount())
                .processCount(notificationEngineService.getProcessCount())
                .queueCapacity(notificationEngineService.getQueueCapacity())
                .currentQueueSize(notificationEngineService.getQueueSize())
                .dateTime(OffsetDateTime.now())
                .build();

        return ResponseEntity.ok(engineStatsDTO);

    }

    @GetMapping("/stats/history")
    public ResponseEntity<List<EngineStatsDTO>> getStatsHistory() {

        return ResponseEntity.ok(notificationEngineService.getMetricsHistory());

    }

}
