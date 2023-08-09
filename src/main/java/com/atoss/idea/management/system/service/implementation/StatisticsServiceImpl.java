package com.atoss.idea.management.system.service.implementation;


import com.atoss.idea.management.system.repository.CommentRepository;
import com.atoss.idea.management.system.repository.IdeaRepository;
import com.atoss.idea.management.system.repository.UserRepository;
import com.atoss.idea.management.system.repository.dto.IdeaResponseDTO;
import com.atoss.idea.management.system.repository.dto.StatisticsDTO;
import com.atoss.idea.management.system.repository.entity.Comment;
import com.atoss.idea.management.system.repository.entity.Idea;
import com.atoss.idea.management.system.repository.entity.Status;
import com.atoss.idea.management.system.service.CommentService;
import com.atoss.idea.management.system.service.IdeaService;
import com.atoss.idea.management.system.service.StatisticsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ModelMapper modelMapper;

    private final IdeaService ideaService;

    private final IdeaRepository ideaRepository;

    private final CommentService commentService;

    private final UserRepository userRepository;

    private final CommentRepository commentRepository;


    /**
     * Constructor
     *
     * @param modelMapper ==
     * @param ideaService ==
     * @param ideaRepository ==
     * @param commentService ==
     * @param userRepository ==
     * @param commentRepository ==
     */
    public StatisticsServiceImpl(ModelMapper modelMapper,
                                 IdeaService ideaService,
                                 IdeaRepository ideaRepository,
                                 CommentServiceImpl commentService, UserRepository userRepository, CommentRepository commentRepository) {
        this.modelMapper = modelMapper;
        this.ideaService = ideaService;
        this.ideaRepository = ideaRepository;
        this.commentService = commentService;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public Long getSelectionRepliesNumber(String selectedDateFrom, String selectedDateTo) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Comment> criteriaQuery = cb.createQuery(Comment.class);
        Root<Comment> root = criteriaQuery.from(Comment.class);

        List<Predicate> predicatesList = new ArrayList<>();

        predicatesList.addAll(ideaService.filterByDate(selectedDateFrom, selectedDateTo, root, cb, "creationDate"));
        predicatesList.add(cb.isNotNull(root.get("parent")));

        criteriaQuery.where(predicatesList.toArray(new Predicate[0]));
        TypedQuery<Comment> repliesQuery = entityManager.createQuery(criteriaQuery);

        Long allReplies = (long) repliesQuery.getResultList().size();

        return  allReplies;
    }

    @Override
    public Long getSelectionCommentNumber(String selectedDateFrom, String selectedDateTo) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Comment> criteriaQuery = cb.createQuery(Comment.class);
        Root<Comment> root = criteriaQuery.from(Comment.class);

        List<Predicate> predicatesList = new ArrayList<>();

        predicatesList.addAll(ideaService.filterByDate(selectedDateFrom, selectedDateTo, root, cb, "creationDate"));
        predicatesList.add(cb.isNull(root.get("parent")));

        criteriaQuery.where(predicatesList.toArray(new Predicate[0]));
        TypedQuery<Comment> commentsQuery = entityManager.createQuery(criteriaQuery);

        Long allComments = (long) commentsQuery.getResultList().size();

        return allComments;
    }


    /**
     * we use this function to retrieve the most commented ideas in order to
     * do statistics based an them and to send them to be displayed
     *
     * @param mostCommentedIdeas list of idea id
     * @return list of most commented ideas
     */
    public List<IdeaResponseDTO> getMostCommentedIdeas(List<Long> mostCommentedIdeas) {

        return mostCommentedIdeas.stream().map(idea_id -> {

            Idea idea = ideaRepository.findById(idea_id).get();
            IdeaResponseDTO ideaResponseDTO = modelMapper.map(idea, IdeaResponseDTO.class);
            ideaResponseDTO.setUsername(idea.getUser().getUsername());
            ideaResponseDTO.setElapsedTime(commentService.getElapsedTime(idea.getCreationDate()));
            ideaResponseDTO.setCommentsNumber(idea.getCommentList().size());

            return ideaResponseDTO;
        }).toList();
    }

    @Override
    public StatisticsDTO getGeneralStatistics() {

        StatisticsDTO statisticsDTO = new StatisticsDTO();


        Long nrOfUsers = userRepository.count();
        Long nrOfIdeas = ideaRepository.count();
        Double ideasPerUser = Math.round((double) nrOfIdeas / (double) nrOfUsers * 100) / 100.00;
        Long implIdeas = ideaRepository.countByStatus(Status.IMPLEMENTED);
        Long draftedIdeas = ideaRepository.countByStatus(Status.DRAFT);
        Long openIdeas = ideaRepository.countByStatus(Status.OPEN);
        Long nrOfComments = commentRepository.countComments();
        Long nrOfReplies = commentRepository.countAllReplies();
        Double draftP = ((double) draftedIdeas / (double) nrOfIdeas * 100);
        Double openP =  ((double) openIdeas / (double) nrOfIdeas * 100);
        Double implP = ((double) implIdeas / (double) nrOfIdeas * 100);
        List<IdeaResponseDTO> mostCommentedIdeas = getMostCommentedIdeas(commentRepository.mostCommentedIdeas());

        statisticsDTO.setMostCommentedIdeas(mostCommentedIdeas);
        statisticsDTO.setOpenIdeas(openIdeas);
        statisticsDTO.setNrOfUsers(nrOfUsers);
        statisticsDTO.setNrOfIdeas(nrOfIdeas);
        statisticsDTO.setIdeasPerUser(ideasPerUser);
        statisticsDTO.setImplementedIdeas(implIdeas);
        statisticsDTO.setDraftIdeas(draftedIdeas);
        statisticsDTO.setTotalNrOfComments(nrOfComments);
        statisticsDTO.setTotalNrOfReplies(nrOfReplies);
        statisticsDTO.setImplP(implP);
        statisticsDTO.setDraftP(draftP);
        statisticsDTO.setOpenP(openP);

        return statisticsDTO;
    }

    @Override
    public StatisticsDTO getStatisticsByDate(String selectedDateFrom,
                                             String selectedDateTo) {

        StatisticsDTO filteredStatisticsDTO = new StatisticsDTO();

        List<Long> listOfStatusesCount = ideaRepository.countStatusByDate(selectedDateFrom,
                selectedDateTo);

        Long openIdeasCount = listOfStatusesCount.get(0);
        Long draftIdeasCount = listOfStatusesCount.get(1);
        Long implIdeasCount = listOfStatusesCount.get(2);

        Long totalIdeasCount = openIdeasCount + draftIdeasCount + implIdeasCount;

        Double draftP = ((double) draftIdeasCount / (double) totalIdeasCount * 100);
        Double openP =  ((double) openIdeasCount / (double) totalIdeasCount * 100);
        Double implP = ((double) implIdeasCount / (double) totalIdeasCount * 100);

        List<Long> listOfRepliesAndComments = commentRepository.getRepliesAndCommentsCount(selectedDateFrom, selectedDateTo);

        Long noOfReplies = listOfRepliesAndComments.get(0);
        Long noOfComments = listOfRepliesAndComments.get(1);

        List<IdeaResponseDTO> mostCommentedIdeas = getMostCommentedIdeas(
                commentRepository.mostCommentedIdeasByDate(selectedDateFrom, selectedDateTo));


        filteredStatisticsDTO.setImplP(implP);
        filteredStatisticsDTO.setOpenP(openP);
        filteredStatisticsDTO.setDraftP(draftP);
        filteredStatisticsDTO.setNrOfIdeas(totalIdeasCount);
        filteredStatisticsDTO.setDraftIdeas(draftIdeasCount);
        filteredStatisticsDTO.setOpenIdeas(openIdeasCount);
        filteredStatisticsDTO.setImplementedIdeas(implIdeasCount);
        filteredStatisticsDTO.setTotalNrOfComments(noOfComments);
        filteredStatisticsDTO.setTotalNrOfReplies(noOfReplies);
        filteredStatisticsDTO.setMostCommentedIdeas(mostCommentedIdeas);

        return filteredStatisticsDTO;
    }



}
