package ee.sportclub.persistence.facility;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FacilityImageRepository extends JpaRepository<FacilityImage, Integer> {

    @Query("select f from FacilityImage f where f.facility.id = :facilityId order by f.id desc limit 1")
    Optional<FacilityImage> findFacilityImageBy(Integer facilityId);

    @Modifying
    @Query("delete from FacilityImage f where f.facility.id = :facilityId")
    void deleteFacilityImagesBy(Integer facilityId);
}
