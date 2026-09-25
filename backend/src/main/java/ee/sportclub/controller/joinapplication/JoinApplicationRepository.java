package ee.sportclub.controller.joinapplication;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link ee.sportclub.persistence.joinapplication.JoinApplication}
 */
@Value
public class JoinApplicationRepository implements Serializable {
    Integer userId;
    Integer trainingGroupId;
    String status;
}