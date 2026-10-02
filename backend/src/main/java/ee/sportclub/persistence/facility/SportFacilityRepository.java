package ee.sportclub.persistence.facility;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SportFacilityRepository extends JpaRepository<SportFacility, Integer> {

    @Query("select s.sport.id from SportFacility s where s.facility.id = :facilityId order by s.sport.name")
    List<Integer> findSportIdsBy(Integer facilityId);

    @Modifying
    @Query("delete from SportFacility s where s.facility.id = :facilityId")
    void deleteSportFacilitiesBy(Integer facilityId);
}
