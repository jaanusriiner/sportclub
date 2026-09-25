package ee.sportclub.service.joinApplication;

import ee.sportclub.persistence.joinapplication.JoinApplicationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JoinApplicationService {


    public JoinApplicationRepository getJoinApplicationRepository() {
        return ee.sportclub.controller.joinapplication.JoinApplicationRepository;
    }
}
