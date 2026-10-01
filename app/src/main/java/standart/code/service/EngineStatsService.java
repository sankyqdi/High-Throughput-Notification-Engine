package standart.code.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import standart.code.dto.EngineStatsDTO;
import standart.code.dto.EngineStatsRequest;
import standart.code.mapper.EngineStatsMapper;
import standart.code.model.EngineStats;
import standart.code.repository.EngineStatsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EngineStatsService implements EngineStatsServiceInterface{

    private final EngineStatsMapper engineStatsMapper;

    private final EngineStatsRepository engineStatsRepository;


    public EngineStatsDTO create(EngineStatsRequest engineStatsRequest) {

        EngineStatsDTO engineStatsDTO = engineStatsMapper.map(engineStatsRequest);

        EngineStats engineStats = engineStatsMapper.map(engineStatsDTO);

        engineStatsRepository.save(engineStats);

        return engineStatsDTO;

    }

    public List<EngineStatsDTO> getAll() {

        return engineStatsRepository.findAll()
                .stream()
                .map(engineStatsMapper::map)
                .toList();

    }

    public EngineStatsDTO getById(Long id) {

        EngineStats engineStats = engineStatsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return engineStatsMapper.map(engineStats);

    }

    public void delete(Long id) {

        EngineStats engineStats = engineStatsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        engineStatsRepository.delete(engineStats);

    }


}
