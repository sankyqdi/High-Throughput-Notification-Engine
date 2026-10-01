package standart.code.dto;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EngineStatsRequest {

    private long errorsCount;

    private long processCount;

    private int queueCapacity;

}
