package com.atoss.idea.management.system.utils;

import com.atoss.idea.management.system.repository.AvatarRepository;
import com.atoss.idea.management.system.repository.UserRepository;
import com.atoss.idea.management.system.repository.CommentRepository;
import com.atoss.idea.management.system.repository.CategoryRepository;
import com.atoss.idea.management.system.repository.ImageRepository;
import com.atoss.idea.management.system.repository.IdeaRepository;
import com.atoss.idea.management.system.repository.entity.Idea;
import com.atoss.idea.management.system.repository.entity.Avatar;
import com.atoss.idea.management.system.repository.entity.Image;
import com.atoss.idea.management.system.repository.entity.Category;
import com.atoss.idea.management.system.repository.entity.Comment;
import com.atoss.idea.management.system.repository.entity.Role;
import com.atoss.idea.management.system.repository.entity.Status;
import com.atoss.idea.management.system.repository.entity.User;
import com.google.common.hash.Hashing;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class InitialDataLoader implements CommandLineRunner {
    private final AvatarRepository avatarRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final IdeaRepository ideaRepository;
    private final CommentRepository commentRepository;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlValue;

    public InitialDataLoader(AvatarRepository avatarRepository,
                             UserRepository userRepository,
                             CategoryRepository categoryRepository,
                             ImageRepository imageRepository,
                             CommentRepository commentRepository,
                             IdeaRepository ideaRepository) {
        this.avatarRepository = avatarRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.commentRepository = commentRepository;
        this.ideaRepository = ideaRepository;
    }


    @Transactional
    @Override
    public void run(String... args) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        if (ddlValue.equals("create")) {
            String[] avatarFileNames = {
                "avatar1.svg",
                "avatar2.svg",
                "avatar3.svg",
                "avatar4.svg",
                "avatar5.svg",
                "avatar6.svg",
                "avatar7.svg"
            };
            Avatar defaultAvatar = new Avatar();
            int count = 0;
            String avatarFilePath = "image/avatar/";
            for (String fileName : avatarFileNames) {
                String filePath = classLoader.getResource(avatarFilePath + fileName).getPath();
                Avatar avatar = avatarRepository.save(createAvatar(filePath, fileName.split("\\.", 2)));
                if (count == 0) {
                    defaultAvatar = avatar;
                }
            }


            User user1 = createUser(true,
                    Role.ADMIN,
                    defaultAvatar,
                    "ap6548088@gmail.com",
                    "AdminPopescu1",
                    "Admin Popescu",
                    Hashing.sha256()
                            .hashString("AtossAdmin123", StandardCharsets.UTF_8)
                            .toString()
            );
            userRepository.save(user1);

            Category category1 = createCategory("Innovation");
            categoryRepository.save(category1);

            Image image1 = imageRepository.save(
                    createImage("img", ".png", classLoader.getResource("image/idea/img.png").getPath()));
            imageRepository.save(image1);

            List<Category> categories = new ArrayList<>();
            categories.add(category1);

            Idea idea1 = createIdea(
                    user1,
                    Status.OPEN,
                    "First idea",
                    "Test1",
                    image1,
                    new Date(),
                    categories
            );

            Idea idea2 = createIdea(
                    user1,
                    Status.DRAFT,
                    "Second idea",
                    "Test2",
                    null,
                    new Date(),
                    categories
            );

            Idea idea3 = createIdea(
                    user1,
                    Status.IMPLEMENTED,
                    "Third idea",
                    "Test3",
                    null,
                    new Date(),
                    categories
            );
            ideaRepository.save(idea1);
            ideaRepository.save(idea2);
            ideaRepository.save(idea3);

            Comment comment1 = createComment(
                    new Date(),
                    idea1,
                    null,
                    user1,
                    "First comment"
            );
            commentRepository.save(comment1);
        }
    }


    private static User createUser(Boolean isActive,
                                   Role role,
                                   Avatar avatar,
                                   String email,
                                   String username,
                                   String fullname,
                                   String hashPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashPassword);
        user.setEmail(email);
        user.setFullName(fullname);
        user.setRole(role);
        user.setIsActive(isActive);
        user.setAvatar(avatar);
        return user;
    }

    private static Category createCategory(
            String text
    ) {
        Category category = new Category();
        category.setText(text);
        return category;
    }


    private static Avatar createAvatar(String filename,
                                       String[] split
    ) throws IOException {
        String avatarName = new String();
        String fileType = new String();
        for (String a : split) {
            avatarName = split[0];
            fileType = split[1];
        }
        Avatar avatar = new Avatar();
        File file = new File(filename);
        byte[] imageByte = Files.readAllBytes(file.toPath());
        avatar.setData(imageByte);
        avatar.setFileName(avatarName);
        avatar.setFileType(fileType);

        return avatar;
    }

    private static Image createImage(
            String fileName,
            String fileType,
            String filePath
    ) throws IOException {
        Image image = new Image();
        File file = new File(filePath);
        byte[] imageByte = Files.readAllBytes(file.toPath());
        image.setImage(imageByte);
        image.setFileName(fileName);
        image.setFileType(fileType);
        return image;
    }

    private static Idea createIdea(User user,
                                   Status status,
                                   String text,
                                   String title,
                                   Image image,
                                   Date date,
                                   List<Category> categories
    ) {
        Idea idea = new Idea();
        idea.setUser(user);
        idea.setStatus(status);
        idea.setText(text);
        idea.setTitle(title);
        idea.setImage(image);
        idea.setDate(date);
        idea.setCategoryList(categories);
        return idea;
    }

    private static Comment createComment(
            Date date,
            Idea ideaId,
            Comment parentId,
            User user,
            String text

    ) {
        Comment comment = new Comment();
        comment.setCreationDate(date);
        comment.setIdea(ideaId);
        comment.setParent(parentId);
        comment.setUser(user);
        comment.setCommentText(text);
        return comment;
    }

}