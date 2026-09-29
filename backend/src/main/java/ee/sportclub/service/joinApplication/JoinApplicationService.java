package ee.sportclub.service.joinApplication;

import ee.sportclub.controller.joinapplication.dto.JoinApplicationRequest;
import ee.sportclub.controller.joinapplication.dto.JoinApplicationResponse;
import ee.sportclub.persistence.joinapplication.JoinApplicationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JoinApplicationService {

private final JoinApplicationRepository joinApplicationRepository;
public JoinApplicationResponse submitApplication(Integer trainingGroupId, JoinApplicationRequest request) {
    return new JoinApplicationResponse("Taotlus esitatud");
}
}
