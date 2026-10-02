package ee.sportclub.persistence.user;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface UserTrainingGroupRepository extends JpaRepository<UserTrainingGroup, Integer> {
    @Query("select u.trainingGroup.id from UserTrainingGroup u where u.user.id = :id")
    Set<Integer> findTrainingGroupIdsByUserId(Integer id);

    @Query("""
            select (count(u) > 0) from UserTrainingGroup u
            where u.trainingGroup.id = :trainingGroupId and u.user.id = :userId""")
    boolean userIsTrainingGroupMember(Integer trainingGroupId, Integer userId);

    @Modifying
    @Query("delete from UserTrainingGroup u where u.trainingGroup.id = :trainingGroupId")
    void deleteUserTrainingGroupsBy(Integer trainingGroupId);

}
