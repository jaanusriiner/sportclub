package ee.sportclub.service.sportclub;

import ee.sportclub.controller.sportclub.dto.SportclubDto;
import ee.sportclub.controller.sportclub.dto.SportclubTrainerDto;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.sportclub.SportclubRepository;
import ee.sportclub.persistence.sportclubtrainer.SportclubTrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SportclubService {

    private final SportclubRepository sportclubRepository;
    private final SportclubTrainerRepository sportclubTrainerRepository;

    public List<SportclubDto> findSportclubs() {
        return sportclubRepository.findAll(Sort.by("name")).stream()
                .map(sportclub -> new SportclubDto(sportclub.getId(), sportclub.getName()))
                .toList();
    }

    public List<SportclubTrainerDto> findSportclubTrainers(Integer sportclubId) {
        validateSportclubExists(sportclubId);
        return sportclubTrainerRepository.findSportclubTrainerDtosBy(sportclubId);
    }

    private void validateSportclubExists(Integer sportclubId) {
        if (!sportclubRepository.existsById(sportclubId)) {
            throw new PrimaryKeyNotFoundException("sportclubId", sportclubId);
        }
    }
}
