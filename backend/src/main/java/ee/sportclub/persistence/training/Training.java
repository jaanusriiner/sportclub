package ee.sportclub.persistence.training;

import ee.sportclub.persistence.facility.Facility;
import ee.sportclub.persistence.training.traininggroup.TrainingGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "training", schema = "sportclub")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "maxsize", nullable = false)
    private Integer maxsize;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Column(name = "default_start_date")
    private LocalDate defaultStartDate;

    @Column(name = "default_end_date")
    private LocalDate defaultEndDate;

    @NotNull
    @Column(name = "default_start_time", nullable = false)
    private LocalTime defaultStartTime;

    @NotNull
    @Column(name = "default_end_time", nullable = false)
    private LocalTime defaultEndTime;

    @NotNull
    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Size(max = 255)
    @NotNull
    @Column(name = "weekdays", nullable = false)
    private String weekdays;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_group_id", nullable = false)
    private TrainingGroup trainingGroup;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "default_facility_id", nullable = false)
    private Facility defaultFacility;


}