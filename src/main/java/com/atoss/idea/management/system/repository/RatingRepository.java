package com.atoss.idea.management.system.repository;

import com.atoss.idea.management.system.repository.entity.Idea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Idea, Long> {

    /**
     * deletes a rating by idea id and the username of a user
     *
     * @param ideaId the id of the idea
     * @param username the user's username
     */
    @Modifying
    @Query("DELETE FROM Rating r "
            + "WHERE r.user = (SELECT u FROM User u WHERE u.username = :username) "
            + "AND r.idea = (SELECT i FROM Idea i WHERE i.id = :ideaId)")
    void deleteByIdeaIdAndUserUsername(@Param("ideaId") Long ideaId, @Param("username") String username);
}
