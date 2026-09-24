package ee.sportclub.persistence.training;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Entity
@Immutable
@Table(name = "v_training_date_overview", schema = "sportclub")
public class TrainingDateOverview {
    @Id
    @Column(name = "training_date_id")
    private Integer trainingDateId;

    @Column(name = "training_group_id")
    private Integer trainingGroupId;

    @Column(name = "sport_id")
    private Integer sportId;

    @Column(name = "sport_name")
    private String sportName;

    @Column(name = "facility_id")
    private Integer facilityId;

    @Column(name = "facility_name")
    private String facilityName;

    @Column(name = "area_id")
    private Integer areaId;

    @Column(name = "trainer_id")
    private Integer trainerId;

    @Column(name = "trainer_name")
    private String trainerName;

    @Column(name = "sportclub_id")
    private Integer sportclubId;

    @Column(name = "sportclub_name")
    private String sportclubName;

    @Column(name = "skill_level_id")
    private Integer skillLevelId;

    @Column(name = "skill_level_name")
    private String skillLevelName;

    @Column(name = "training_date")
    private LocalDate trainingDate;

    @Column(name = "training_time")
    private LocalTime trainingTime;

    @Column(name = "status")
    private String status;

    @Column(name = "user_count")
    private Integer userCount;

    @Column(name = "max_size")
    private Integer maxSize;

}
