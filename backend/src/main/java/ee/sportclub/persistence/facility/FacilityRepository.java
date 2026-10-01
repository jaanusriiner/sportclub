package ee.sportclub.persistence.facility;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FacilityRepository extends JpaRepository<Facility, Integer> {

    @Query("select (count(f) > 0) from Facility f where upper(f.name) = upper(:facilityName)")
    boolean facilityNameIsUnavailable(@NotEmpty @Size(min=1, max=60) String facilityName);
}
