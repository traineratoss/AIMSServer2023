package com.atoss.idea.management.system.repository.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "rating")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rating_id")
    private Long id;

    @Column(name = "ratingNumber")
    private Integer ratingNumber;

    @ManyToOne
    @JoinColumn(name = "idea_id", referencedColumnName = "idea_id")
    @JsonBackReference(value = "idea-ratings")
    private Idea idea;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @JsonBackReference(value = "user-ratings")
    private User user;
}
