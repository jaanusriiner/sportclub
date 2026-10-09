package ee.sportclub.service.tone;

import ee.sportclub.controller.tone.ToneDto;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.tone.Tone;
import ee.sportclub.persistence.tone.ToneMapper;
import ee.sportclub.persistence.tone.ToneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToneService {

    private final ToneRepository toneRepository;
    private final ToneMapper toneMapper;

    public List<ToneDto> findTones() {
        Sort byIdAsc = Sort.by(Sort.Direction.ASC, "id");
        List<Tone> tones = toneRepository.findAll(byIdAsc);
        return toneMapper.toToneDtos(tones);
    }

    public Tone getValidToneBy(Integer toneId) {
        return toneRepository.findById(toneId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("toneId", toneId));
    }
}
