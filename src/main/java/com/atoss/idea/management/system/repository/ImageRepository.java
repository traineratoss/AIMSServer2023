package com.atoss.idea.management.system.repository;

import com.atoss.idea.management.system.repository.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Finds an Image object that is associated to its id.
     *
     * @param id the id of the image that needs to be found.
     * @return  the Image object that matches the provided image id.
     */
    Image findImageById(Long id);

    /**
     * Finds an image by its file name
     *
     * @param fileName the file name of the image
     * @return the image
     */
    Image findImageByFileName(String fileName);

    /**
     * Finds an image by an idea id
     *
     * @param id the id of the idea
     * @return the image associated to the idea
     */
    Image findByIdeaId(Long id);
}