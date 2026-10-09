package ee.sportclub.persistence.llmmodel;

import ee.sportclub.controller.llmmodel.LlmModelDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface LlmModelMapper {

    @Mapping(source = "id", target = "llmModelId")
    @Mapping(source = "provider", target = "provider")
    @Mapping(source = "name", target = "modelName")
    @Mapping(source = "maxTemperature", target = "maxTemperature")
    LlmModelDto toLlmModelDto(LlmModel llmModel);

    List<LlmModelDto> toLlmModelDtos(List<LlmModel> llmModels);
}
