package standart.code.service;

import standart.code.dto.EngineStatsDTO;
import standart.code.dto.EngineStatsRequest;

import java.util.List;

public interface EngineStatsServiceInterface {

    EngineStatsDTO create(EngineStatsRequest engineStatsRequest);

    List<EngineStatsDTO> getAll();

    EngineStatsDTO getById(Long id);

    void delete(Long id);

}
