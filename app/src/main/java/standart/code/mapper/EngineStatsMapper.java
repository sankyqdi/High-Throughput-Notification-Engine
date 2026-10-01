package standart.code.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import standart.code.dto.EngineStatsDTO;
import standart.code.dto.EngineStatsRequest;
import standart.code.model.EngineStats;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EngineStatsMapper {

    EngineStatsDTO map(EngineStatsRequest engineStatsRequest);

    EngineStatsDTO map(EngineStats engineStats);

    EngineStats map(EngineStatsDTO engineStatsDTO);

}
