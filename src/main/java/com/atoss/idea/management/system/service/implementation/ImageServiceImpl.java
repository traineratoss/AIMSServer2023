package com.atoss.idea.management.system.service.implementation;

import com.atoss.idea.management.system.exception.ImageNotFoundException;
import com.atoss.idea.management.system.repository.ImageRepository;
import com.atoss.idea.management.system.repository.dto.ImageDTO;
import com.atoss.idea.management.system.repository.entity.Image;
import com.atoss.idea.management.system.service.ImageService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;
    private final ModelMapper modelMapper;

    public  ImageServiceImpl(ImageRepository imageRepository, ModelMapper modelMapper) {
        this.imageRepository = imageRepository;
        this.modelMapper = modelMapper;
    }

    public ImageDTO addImage(MultipartFile file) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        Image image = new Image(fileName, file.getContentType(), file.getBytes());
        return modelMapper.map(imageRepository.save(image), ImageDTO.class);
    }

    @Transactional
    public ImageDTO getImage(Long id) throws Exception {
        if (id < 0) {
            throw new ImageNotFoundException();
        } else {
            return modelMapper.map(imageRepository.findImageById(id), ImageDTO.class);
        }

    }

    public List<ImageDTO> getAllImage() {
        return Arrays.asList(modelMapper.map(imageRepository.findAll(), ImageDTO[].class));
    }

}
