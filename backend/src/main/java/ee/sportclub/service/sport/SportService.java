package ee.sportclub.service.sport;


import ee.sportclub.controller.sport.SportDto;
import ee.sportclub.persistence.sport.Sport;
import ee.sportclub.persistence.sport.SportMapper;
import ee.sportclub.persistence.sport.SportRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SportService {

    private final SportRepository sportRepository;
    private final SportMapper sportMapper;

    public List<SportDto> findSports() {

        Sort byNameAsc = Sort.by(Sort.Direction.ASC, "name");
        List<Sport> sports = sportRepository.findAll(byNameAsc);
        List<SportDto> sportDtos = sportMapper.toSportDtos(sports);
        return sportDtos;


    }

}
