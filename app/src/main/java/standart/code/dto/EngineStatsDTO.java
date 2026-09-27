package standart.code.dto;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public class EngineStatsDTO {

    private long errorsCount;

    private long processCount;

    private int queueCapacity;

    private OffsetDateTime dateTime;

}
