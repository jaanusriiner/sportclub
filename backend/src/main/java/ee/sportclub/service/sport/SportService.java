package ee.sportclub.service.sport;


import ee.sportclub.controller.sport.SportDto;
import ee.sportclub.controller.sport.SportSkillLevelDto;
import ee.sportclub.persistence.skilllevel.SkillLevel;
import ee.sportclub.persistence.skilllevel.SkillLevelRepository;
import ee.sportclub.persistence.skilllevel.SportSkillLevelMapper;
import ee.sportclub.persistence.sport.Sport;
import ee.sportclub.persistence.sport.SportMapper;
import ee.sportclub.persistence.sport.SportRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SportService {

    private final SportRepository sportRepository;
    private final SportMapper sportMapper;
    private final SkillLevelRepository skillLevelRepository;
    private final SportSkillLevelMapper sportSkillLevelMapper;

    public List<SportDto> findSports() {

        Sort byNameAsc = Sort.by(Sort.Direction.ASC, "name");
        List<Sport> sports = sportRepository.findAll(byNameAsc);
        List<SportDto> sportDtos = sportMapper.toSportDtos(sports);
        return sportDtos;


    }
//todo Jaanus Võrreldes taskiga jääb hetkel tegemata veakäsitlus, kui sportId ei eksisteeri, hetkel tagastab sel juhul tühja Listi
    public List<SportSkillLevelDto> findSportSkillLevels(Integer sportId) {
        List<SkillLevel> skillLevels = skillLevelRepository.findBySportId(sportId);
        List<SportSkillLevelDto> skillLevelDtos = sportSkillLevelMapper.tosSportSkillLevelDtos(skillLevels);
        return skillLevelDtos;
    }




}
