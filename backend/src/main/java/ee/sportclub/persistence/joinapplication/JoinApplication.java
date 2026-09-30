package ee.sportclub.persistence.joinapplication;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "join_application", schema = "sportclub")

public class JoinApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "training_group_id", nullable = false)
    private Integer trainingGroupId;

    @Column(name = "status", nullable = false, length = 3)
    private String status;
}
