package ee.sportclub.service.training;

import ee.sportclub.controller.training.dto.TrainingGroupOverviewDto;
import ee.sportclub.controller.training.dto.TrainingGroupOverviewPageDto;
import ee.sportclub.persistence.training.TrainingDateOverviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingDateOverviewRepository trainingDateOverviewRepository;

    public TrainingGroupOverviewPageDto findTrainings(Integer requestUserId, Integer areaId, Integer sportId, Integer trainerId, LocalDate dateFrom, LocalTime timeFrom, Integer page, Integer size) {
        // API lehenumbrid algavad 1-st, Springi PageRequest 0-st; page < 1 käsitletakse kui esimest lehte
        int pageIndex = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(pageIndex, size);
        Page<TrainingGroupOverviewDto> trainingGroupOverviewDtoPage = trainingDateOverviewRepository.findTrainingGroupOverviewDtosBy(requestUserId, areaId, sportId, trainerId, dateFrom, timeFrom, pageable);
        return createTrainingGroupOverviewPageDto(trainingGroupOverviewDtoPage);
    }

    private static TrainingGroupOverviewPageDto createTrainingGroupOverviewPageDto(Page<TrainingGroupOverviewDto> trainingGroupOverviewDtoPage) {
        TrainingGroupOverviewPageDto trainingGroupOverviewPageDto = new TrainingGroupOverviewPageDto();
        trainingGroupOverviewPageDto.setTrainings(trainingGroupOverviewDtoPage.getContent());
        trainingGroupOverviewPageDto.setTotalElements(trainingGroupOverviewDtoPage.getTotalElements());
        trainingGroupOverviewPageDto.setTotalPages(trainingGroupOverviewDtoPage.getTotalPages());
        return trainingGroupOverviewPageDto;
    }
}
