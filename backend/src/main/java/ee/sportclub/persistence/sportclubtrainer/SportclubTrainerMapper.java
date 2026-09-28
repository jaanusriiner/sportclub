package ee.sportclub.persistence.sportclubtrainer;

import ee.sportclub.controller.trainer.TrainerSportclubDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SportclubTrainerMapper {

    @Mapping(source = "sportclub.id", target = "sportclubId")
    @Mapping(source = "sportclub.name", target = "sportclubName")
    TrainerSportclubDto toTrainerSportClubDto(SportclubTrainer sportclubTrainer);


    List<TrainerSportclubDto> toTrainerSportclubDtos(List<SportclubTrainer> sportclubTrainers);


}