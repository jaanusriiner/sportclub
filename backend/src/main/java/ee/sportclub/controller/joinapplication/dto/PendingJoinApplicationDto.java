package ee.sportclub.controller.joinapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingJoinApplicationDto implements Serializable {

    private Integer joinApplicationId;
    private Integer userId;
    private String userFullName;
    private Integer sportclubId;
    private String sportclubName;
    private Integer trainingGroupId;
    private String trainingGroupName;
    private Integer trainingGroupMemberCount;

}
