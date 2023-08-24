package com.atoss.idea.management.system.service.implementation;

import com.atoss.idea.management.system.exception.IdeaNotFoundException;
import com.atoss.idea.management.system.exception.UserNotFoundException;
import com.atoss.idea.management.system.exception.FieldValidationException;
import com.atoss.idea.management.system.repository.*;
import com.atoss.idea.management.system.repository.dto.*;
import com.atoss.idea.management.system.repository.entity.*;
import com.atoss.idea.management.system.service.IdeaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Log4j2
public class IdeaServiceImpl implements IdeaService {
    ClassLoader classLoader = getClass().getClassLoader();

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> badWords = new ArrayList<>();

    private final IdeaRepository ideaRepository;

    private final ImageRepository imageRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final RatingRepository ratingRepository;

    private final ModelMapper modelMapper;

    private final CommentServiceImpl commentServiceImpl;

    /**
     * Constructor for the Idea Service Implementation
     *
     * @param ideaRepository     repository for the Idea Entity
     * @param imageRepository    repository for the Image Entity
     * @param userRepository     repository for the User Entity
     * @param categoryRepository repository for the Category Entity
     * @param ratingRepository repository for the Rating Entity
     * @param modelMapper        responsible for mapping our entities
     * @param commentServiceImpl ======
     */
    public IdeaServiceImpl(IdeaRepository ideaRepository,
                           ImageRepository imageRepository, UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           RatingRepository ratingRepository, ModelMapper modelMapper,
                           CommentServiceImpl commentServiceImpl) {
        this.ideaRepository = ideaRepository;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ratingRepository = ratingRepository;
        this.modelMapper = modelMapper;
        this.commentServiceImpl = commentServiceImpl;
    }

    private String filterBadWords(String text) {
        for (String word : badWords) {
            String pattern = "\\b" + word + "\\b";
            text = text.replaceAll("(?i)" + pattern, "*".repeat(word.length()));
        }
        return text;
    }

    private void readBadWordsFromFile(String path) {
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String word = line.trim();

                badWords.add(word);
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IdeaResponseDTO addIdea(IdeaRequestDTO idea, String username) throws UnsupportedEncodingException {

        if (idea.getTitle() == null || idea.getTitle().isEmpty()) {
            throw new FieldValidationException("Please enter a valid title for the idea.");
        }

        if (idea.getStatus() == null) {
            throw new FieldValidationException("Please enter a valid status for the idea.");
        }

        if (idea.getCategoryList() == null || idea.getCategoryList().size() <= 0) {
            throw new FieldValidationException("Please enter a valid category for the idea.");
        }

        if (idea.getText() == null || idea.getText().isEmpty()) {
            throw new FieldValidationException("Please enter a valid text for the idea.");
        }

        Idea savedIdea = new Idea();
        String wordsFilePath = "textTerms/badWords.txt";
        URL resourceUrl = classLoader.getResource(wordsFilePath);
        if (resourceUrl != null) {
            String filePath = URLDecoder.decode(resourceUrl.getFile(), "UTF-8");
            readBadWordsFromFile(filePath);
        }

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("No user found by this username."));
        savedIdea.setUser(user);
        savedIdea.setStatus(idea.getStatus());
        String filteredIdeaText = filterBadWords(idea.getText());
        savedIdea.setText(filteredIdeaText);
        String filteredIdeaTitle = filterBadWords(idea.getTitle());
        savedIdea.setTitle(filteredIdeaTitle);
        savedIdea.setCategoryList(new ArrayList<>());
        savedIdea.setCreationDate(new Date());

        if (idea.getImage() != null) {
            Image existingImage = imageRepository.findImageByFileName(idea.getImage().getFileName());

            if (existingImage == null) {
                Image newImage = modelMapper.map(idea.getImage(), Image.class);
                savedIdea.setImage(newImage);
            } else {
                savedIdea.setImage(existingImage);
            }
        }

        for (CategoryDTO categoryDTO : idea.getCategoryList()) {
            Category category = categoryRepository.findByText(modelMapper.map(categoryDTO, Category.class).getText());

            if (category == null) {
                savedIdea.getCategoryList().add(modelMapper.map(categoryDTO, Category.class));
            } else {
                savedIdea.getCategoryList().add(category);
            }
        }

        savedIdea.setRatings(new ArrayList<>());

        if (user.getIdeas() == null) {
            user.setIdeas(new ArrayList<>());
        }

        user.getIdeas().add(savedIdea);

        IdeaResponseDTO responseDTO = modelMapper.map(ideaRepository.save(savedIdea), IdeaResponseDTO.class);
        responseDTO.setUsername(username);
        return responseDTO;
    }

    @Override
    public IdeaResponseDTO getIdeaById(Long id) throws FieldValidationException {

        if (ideaRepository.findById(id).isPresent()) {
            Idea idea = ideaRepository.findById(id).get();
            IdeaResponseDTO responseDTO = modelMapper.map(idea, IdeaResponseDTO.class);
            responseDTO.setUsername(ideaRepository.findById(id).get().getUser().getUsername());
            responseDTO.setElapsedTime(commentServiceImpl.getElapsedTime(idea.getCreationDate()));
            responseDTO.setCommentsNumber(idea.getCommentList().size());
            return responseDTO;
        } else {
            throw new IdeaNotFoundException("Idea doesn't exist.");
        }
    }

    @Override
    public IdeaResponseDTO updateIdeaById(Long id, IdeaUpdateDTO ideaUpdateDTO) throws UnsupportedEncodingException {
        String wordsFilePath = "textTerms/badWords.txt";
        URL resourceUrl = classLoader.getResource(wordsFilePath);
        if (resourceUrl != null) {
            String filePath = URLDecoder.decode(resourceUrl.getFile(), "UTF-8");
            readBadWordsFromFile(filePath);
        }


        if (ideaRepository.findById(id).isPresent()) {

            Idea idea = ideaRepository.findById(id).get();

            if (ideaUpdateDTO.getText() != null) {
                idea.setText(ideaUpdateDTO.getText());
                String filteredCommentText = filterBadWords(idea.getText());
                idea.setText(filteredCommentText);
            }

            if (ideaUpdateDTO.getStatus() != null) {
                idea.setStatus(ideaUpdateDTO.getStatus());
            }

            if (ideaUpdateDTO.getImage() != null) {
                Image existingImage = imageRepository.findImageByFileName(ideaUpdateDTO.getImage().getFileName());
                if (existingImage == null) {
                    Image newImage = modelMapper.map(ideaUpdateDTO.getImage(), Image.class);
                    idea.setImage(newImage);
                } else {
                    idea.setImage(existingImage);
                }
            }

            if (ideaUpdateDTO.getTitle() != null) {
                idea.setTitle(ideaUpdateDTO.getTitle());
            }

            if (ideaUpdateDTO.getCategoryList() != null) {

                if (ideaUpdateDTO.getCategoryList().isEmpty()) {
                    throw new RuntimeException("Please select at least one category");
                }

                idea.setCategoryList(new ArrayList<>());
                List<Category> newList = new ArrayList<>();

                for (CategoryDTO category: ideaUpdateDTO.getCategoryList()) {

                    Category newCategory = categoryRepository.findByText(category.getText());
                    if (newCategory == null) {
                        Category addedCategory = new Category();
                        addedCategory.setText(category.getText());
                        categoryRepository.save(addedCategory);
                        newList.add(addedCategory);
                    } else {
                        newList.add(newCategory);
                    }

                }

                idea.setCategoryList(newList);
            }

            IdeaResponseDTO responseDTO = modelMapper.map(ideaRepository.save(idea), IdeaResponseDTO.class);
            responseDTO.setUsername(ideaRepository.findById(id).get().getUser().getUsername());
            responseDTO.setElapsedTime(commentServiceImpl.getElapsedTime(idea.getCreationDate()));
            responseDTO.setCommentsNumber(idea.getCommentList().size());
            return responseDTO;
        } else {
            throw new IdeaNotFoundException("Idea doesn't exist.");
        }
    }

    @Override
    public void deleteIdeaById(Long id) {

        if (ideaRepository.existsById(id)) {
            ideaRepository.deleteById(id);
        } else {
            throw new IdeaNotFoundException("Idea doesn't exist.");
        }
    }

    @Override
    public Page<IdeaResponseDTO> getAllIdeas(Pageable pageable) {

        if (ideaRepository.findAll().size() <= 0) {
            throw new FieldValidationException("No ideas found.");
        }

        Page<Idea> ideas = ideaRepository.findAll(pageable);

        List<IdeaResponseDTO> ideaResponseDTOs = ideas.stream()
                .map(idea -> {
                    IdeaResponseDTO responseDTO = modelMapper.map(idea, IdeaResponseDTO.class);
                    responseDTO.setUsername(idea.getUser().getUsername());
                    responseDTO.setElapsedTime(commentServiceImpl.getElapsedTime(idea.getCreationDate()));
                    responseDTO.setCommentsNumber(idea.getCommentList().size());
                    return responseDTO;
                })
                .toList();

        return new PageImpl<>(ideaResponseDTOs, pageable, ideas.getTotalElements());
    }

    @Override
    public Page<IdeaResponseDTO> getAllIdeasByUserUsername(String username, Pageable pageable) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User doesn't exist."));

        if (user.getIdeas() == null || user.getIdeas().isEmpty()) {
            throw new FieldValidationException("No ideas found.");
        }

        List<IdeaResponseDTO> ideaResponseDTOs = ideaRepository.findAllByUserUsername(username, pageable)
                .stream()
                .map(idea -> {
                    IdeaResponseDTO responseDTO = modelMapper.map(idea, IdeaResponseDTO.class);
                    responseDTO.setUsername(user.getUsername());
                    responseDTO.setElapsedTime(commentServiceImpl.getElapsedTime(idea.getCreationDate()));
                    responseDTO.setCommentsNumber(idea.getCommentList().size());
                    return responseDTO;
                })
                .toList();

        return new PageImpl<>(ideaResponseDTOs, pageable, ideaResponseDTOs.size());
    }

    @Override
    public Page<IdeaResponseDTO> filterIdeasByAll(String title,
                                        String text,
                                        List<Status> statuses,
                                        List<String> categories,
                                        List<String> users,
                                        Double ratingNumber,
                                        String selectedDateFrom,
                                        String selectedDateTo,
                                        String sortDirection,
                                        String username,
                                        Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Idea> criteriaQuery = cb.createQuery(Idea.class);
        Root<Idea> root = criteriaQuery.from(Idea.class);

        List<Predicate> predicatesList = new ArrayList<>();

        if (username == null) {
            List<Integer> allIdeasStatuses = new ArrayList<>();
            allIdeasStatuses.add(0);
            allIdeasStatuses.add(2);

            predicatesList.add(root.get("status").in(allIdeasStatuses));
        }

        if (username != null) {
            predicatesList.add(cb.equal(root.join("user").get("username"), username));
        }

        if (title != null) {
            String nonCaseSensitiveTitle = title.toLowerCase();
            predicatesList.add(cb.like(cb.lower(root.get("title")), "%" + nonCaseSensitiveTitle + "%"));
        }

        if (text != null) {
            String nonCaseSensitiveText = text.toLowerCase();
            predicatesList.add(cb.like(cb.lower(root.get("text")), "%" + nonCaseSensitiveText + "%"));
        }

        if (statuses != null && !statuses.isEmpty()) {
            predicatesList.add(root.get("status").in(statuses));
        }

        if (users != null && !users.isEmpty() && username == null) {
            predicatesList.add(root.join("user").get("username").in(users));
        }

        if (categories != null && !categories.isEmpty()) {
            predicatesList.add(root.join("categoryList").get("text").in(categories));
        }

        if (ratingNumber != null && ratingNumber > 0) {
            Subquery<Double> subquery = criteriaQuery.subquery(Double.class);
            Root<Idea> subqueryRoot = subquery.correlate(root);

            Expression<Double> avgRating = cb.avg(subqueryRoot.join("ratings").get("ratingNumber"));
            subquery.select(avgRating);

            subquery.where(cb.equal(subqueryRoot, root));

            subquery = subquery.having(cb.greaterThanOrEqualTo(avgRating, ratingNumber));

            predicatesList.add(cb.exists(subquery));
        }

        predicatesList.addAll(filterByDate(selectedDateFrom, selectedDateTo, root, cb, "creationDate"));

        List<Order> orders = new ArrayList<>();

        if (Objects.equals(sortDirection, "ASC")) {
            orders.add(cb.asc(root.get("creationDate")));
        } else {
            orders.add(cb.desc(root.get("creationDate")));
        }

        criteriaQuery.orderBy(orders);
        criteriaQuery.where(predicatesList.toArray(new Predicate[0]));
        TypedQuery<Idea> query = entityManager.createQuery(criteriaQuery);

        int totalSize = query.getResultList().size();

        if (totalSize == 0) {
            throw new FieldValidationException("No ideas found.");
        }

        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<Idea> pagedIdeas = query.getResultList();

            List<IdeaResponseDTO> allIdeasDTO = pagedIdeas.stream().map(idea -> {
                IdeaResponseDTO ideaResponseDTO = modelMapper.map(idea, IdeaResponseDTO.class);
                ideaResponseDTO.setUsername(idea.getUser().getUsername());
                ideaResponseDTO.setElapsedTime(commentServiceImpl.getElapsedTime(idea.getCreationDate()));
                ideaResponseDTO.setCommentsNumber(idea.getCommentList().size());
                return ideaResponseDTO;
            }).toList();

            return new PageImpl<>(allIdeasDTO, pageable, totalSize);
        }

        List<Idea> allIdeas = query.getResultList();

        List<IdeaResponseDTO> allIdeasUnpaged = allIdeas.stream().map(idea -> {
            IdeaResponseDTO ideaResponseDTO = modelMapper.map(idea, IdeaResponseDTO.class);
            ideaResponseDTO.setUsername(idea.getUser().getUsername());
            ideaResponseDTO.setElapsedTime(commentServiceImpl.getElapsedTime(idea.getCreationDate()));
            ideaResponseDTO.setCommentsNumber(idea.getCommentList().size());
            return ideaResponseDTO;
        }).toList();

        return new PageImpl<>(allIdeasUnpaged, Pageable.unpaged(), totalSize);
    }

    @Override
    public List<Idea> findIdeasByIds(List<Long> ideaIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Idea> criteriaQuery = cb.createQuery(Idea.class);
        Root<Idea> root = criteriaQuery.from(Idea.class);

        List<Predicate> predicatesList = new ArrayList<>();

        for (Long id:ideaIds) {
            criteriaQuery.where(cb.equal(root.get("id"), id));
        }

        return entityManager.createQuery(criteriaQuery).getResultList();

    }


    @Override
    public List<Predicate> filterByDate(String selectedDateFrom, String selectedDateTo, Root<?> root, CriteriaBuilder cb, String columnName) {

        List<Predicate> predicatesList = new ArrayList<>();

        if (selectedDateFrom != null && selectedDateTo == null) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date fromDate = simpleDateFormat.parse(selectedDateFrom + " 00:00:00");
                predicatesList.add(cb.greaterThanOrEqualTo(root.get(columnName), fromDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (selectedDateFrom == null && selectedDateTo != null) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date toDate = simpleDateFormat.parse(selectedDateTo + " 23:59:59");
                predicatesList.add(cb.lessThanOrEqualTo(root.get(columnName), toDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (selectedDateFrom != null && selectedDateTo != null) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date fromDate = simpleDateFormat.parse(selectedDateFrom + " 00:00:00");
                Date toDate = simpleDateFormat.parse(selectedDateTo + " 23:59:59");
                predicatesList.add(cb.between(root.get(columnName), fromDate, toDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return predicatesList;
    }

    @Override
    public IdeaResponseDTO addUserRatingToIdea(Long id, Double ratingNumber, String username) {

        Idea idea = ideaRepository.findById(id).get();

        Boolean alreadyAddedRating = false;

        if (idea.getUser().getUsername().equals(username)) {
            throw new FieldValidationException("Can't add rating to own idea.");
        }

        if (ratingNumber < 1D || ratingNumber > 5D) {
            throw new FieldValidationException("Rating out of bounds.");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User doesn't exist."));

        for (Rating rating : idea.getRatings()) {
            if (rating.getUser().getUsername().equals(username)) {
                alreadyAddedRating = true;
            }
        }

        if (alreadyAddedRating) {
            for (Rating rating : idea.getRatings()) {
                if (rating.getUser().getUsername().equals(username)) {
                    rating.setRatingNumber(ratingNumber);
                }
            }

            IdeaResponseDTO responseDTO = modelMapper.map(idea, IdeaResponseDTO.class);

            return responseDTO;

        } else {
            if (idea.getRatings() == null) {
                idea.setRatings(new ArrayList<>());
            }

            Rating rating = new Rating();

            rating.setIdea(idea);
            rating.setUser(user);
            rating.setRatingNumber(ratingNumber);

            idea.getRatings().add(rating);

            IdeaResponseDTO responseDTO = modelMapper.map(idea, IdeaResponseDTO.class);

            return responseDTO;
        }
    }

    @Override
    public Double getIdeaRatingAverage(Long id) {

        Idea idea = ideaRepository.findById(id).get();

        Double sumOfRatings = 0D;
        Double numberOfRatings = 0D;

        for (Rating rating: idea.getRatings()) {
            sumOfRatings += rating.getRatingNumber();
            numberOfRatings++;
        }

        if (numberOfRatings == 0) {
            return 0.0D;
        }

        Double averageRating = sumOfRatings / numberOfRatings;
        return averageRating;

    }

    @Override
    public void deleteUserRatingFromIdea(Long id, String username) {
        if (ideaRepository.existsById(id)) {
            ratingRepository.deleteByIdeaIdAndUserUsername(id, username);
        }
    }


}

