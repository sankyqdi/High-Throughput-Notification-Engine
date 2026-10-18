package standart.code.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class EngineStatsDTO {

    private long errorsCount;

    private long processCount;

    private int queueCapacity;

    private long currentQueueSize;

    private long throughput;

    private int addedInThisBatch;

    private OffsetDateTime dateTime;

}
