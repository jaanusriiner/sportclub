package ee.sportclub.persistence.joinapplication;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "join_application", schema = "sportclub")

public class JoinApplication {

}
