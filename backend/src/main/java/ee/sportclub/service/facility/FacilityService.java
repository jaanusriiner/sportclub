package ee.sportclub.service.facility;

import ee.sportclub.controller.facility.dto.FacilityDto;
import ee.sportclub.persistence.facility.Facility;
import ee.sportclub.persistence.facility.FacilityMapper;
import ee.sportclub.persistence.facility.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityMapper facilityMapper;

    public List<FacilityDto> findFacilities() {
        Sort byNameAsc = Sort.by(Sort.Direction.ASC, "name");
        List<Facility> facilities = facilityRepository.findAll(byNameAsc);
        return facilityMapper.toFacilityDtos(facilities);
    }

}
