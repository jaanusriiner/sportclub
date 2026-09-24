package ee.sportclub.service.training;

import ee.sportclub.controller.training.dto.TrainingGroupOverviewDto;
import ee.sportclub.persistence.training.TrainingDateOverviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingDateOverviewRepository trainingDateOverviewRepository;

    public List<TrainingGroupOverviewDto> findTrainings(Integer requestUserId, Integer areaId, Integer sportId, Integer trainerId, LocalDate dateFrom, LocalTime timeFrom) {
        return trainingDateOverviewRepository.findTrainingGroupOverviewDtosBy(requestUserId, areaId, sportId, trainerId, dateFrom, timeFrom);
    }
}
