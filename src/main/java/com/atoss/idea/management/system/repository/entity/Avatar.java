package com.atoss.idea.management.system.repository.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "avatar")
public class Avatar {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "avatar_id")
    private Long id;

    @Column(name = "data", length = 2000)
    private byte[] data;

    @OneToMany(mappedBy = "avatar")
    @JsonManagedReference(value = "users-avatar")
    private List<User> users;

    @Column(name = "file_name")
    private String fileName;

    /**
     * Constructs a new Avatar instance with the specified file name and data.
     *
     * This constructor initializes an Avatar object with the provided file name and binary data.
     *
     * @param fileName The name of the avatar file.
     * @param data     The binary data representing the avatar image.
     */
    public Avatar(String fileName, byte[] data) {
        this.fileName = fileName;
        this.data = data;
    }
}
