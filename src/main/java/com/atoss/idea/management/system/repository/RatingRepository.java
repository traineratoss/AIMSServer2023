package com.atoss.idea.management.system.repository;

import com.atoss.idea.management.system.repository.entity.Idea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    /**
     * gets the top rated ideas id's since the beginning of the app
     *
     * @return a list containing idea-id's of the most commented ideas
     */
    @Query(value = "SELECT idea_id"
            +
            " FROM rating"
            +
            " GROUP BY idea_id"
            +
            " ORDER BY AVG(rating_number) DESC"
            +
            " LIMIT 5", nativeQuery = true)
    List<Long> topRatedIdeas();

    /**
     * gets the top rated ideas averages since the beginning of the app
     *
     * @return a list containing all the averages
     */
    @Query(value = "SELECT AVG(rating_number)"
            +
            " FROM rating"
            +
            " GROUP BY idea_id"
            +
            " ORDER BY AVG(rating_number) DESC"
            +
            " LIMIT 5", nativeQuery = true)
    List<Double> topRatedIdeasAverages();
}
