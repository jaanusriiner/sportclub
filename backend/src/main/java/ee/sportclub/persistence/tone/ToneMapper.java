package ee.sportclub.persistence.tone;

import ee.sportclub.controller.tone.ToneDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ToneMapper {

    @Mapping(source = "id", target = "toneId")
    @Mapping(source = "name", target = "toneName")
    ToneDto toToneDto(Tone tone);

    List<ToneDto> toToneDtos(List<Tone> tones);
}
