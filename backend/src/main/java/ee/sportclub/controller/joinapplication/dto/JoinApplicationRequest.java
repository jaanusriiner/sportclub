package ee.sportclub.controller.joinapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * DTO for {@link ee.sportclub.persistence.joinapplication.JoinApplication}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinApplicationRequest implements Serializable {
    private Integer userId;
    private Integer trainingGroupId;
    private String status;
}