package com.atoss.idea.management.system.repository;

import com.atoss.idea.management.system.repository.entity.Idea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Idea, Long> {
}
