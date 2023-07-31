package com.atoss.idea.management.system.controller;

import java.util.List;
import com.atoss.idea.management.system.repository.dto.RequestCommentDTO;
import com.atoss.idea.management.system.repository.dto.RequestCommentReplyDTO;
import com.atoss.idea.management.system.repository.dto.ResponseCommentDTO;
import com.atoss.idea.management.system.repository.dto.ResponseCommentReplyDTO;
import com.atoss.idea.management.system.service.implementation.CommentServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/aims/api/v1/ideas")
public class CommentController {

    private final CommentServiceImpl commentService;

    public CommentController(CommentServiceImpl commentService) {
        this.commentService = commentService;
    }

    @Transactional
    @PostMapping("/comments")
    @ResponseBody
    public ResponseEntity<ResponseCommentDTO> addComment(@RequestBody RequestCommentDTO newComment) {

        ResponseCommentDTO responseCommentDTO = commentService.addComment(newComment);

        return new ResponseEntity<ResponseCommentDTO>(responseCommentDTO, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/comments/reply")
    public ResponseEntity<ResponseCommentReplyDTO> addReply(@RequestBody RequestCommentReplyDTO requestCommentReplyDTO) {

        ResponseCommentReplyDTO responseCommentReplyDTO = commentService.addReply(requestCommentReplyDTO);

        return new ResponseEntity<ResponseCommentReplyDTO>(responseCommentReplyDTO, HttpStatus.OK);
    }


    @Transactional
    @GetMapping("/comments")
    public  ResponseEntity<Page<ResponseCommentDTO>> getAllCommentsByIdeaIdWithPaging(
                                                                 @RequestParam(required = true) Long ideaId,
                                                                 @RequestParam(required = true) int pageSize,
                                                                 @RequestParam(required = true) int pageNumber,
                                                                 @RequestParam(required = true) String sortCategory) {
        return new ResponseEntity<>(commentService.getAllCommentsByIdeaIdWithPaging(ideaId,
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, sortCategory))), HttpStatus.OK);
    }

    @Transactional
    @GetMapping("/comments/test")
    public List<ResponseCommentDTO> getAllCommentsByIdeaId(@RequestParam(name = "ideaId") Long ideaId) {
        return commentService.getAllCommentsByIdeaId(ideaId);
    }

    @Transactional
    @GetMapping("/comments/replies")
    public List<ResponseCommentReplyDTO> getAllRepliesByCommentId(@RequestParam(name = "commentId") Long commentId) {
        return commentService.getAllRepliesByCommentId(commentId);
    }

    @Transactional
    @GetMapping("/test")
    public String getTime(@RequestParam(name = "id") Long id) {
        return commentService.getTimeForComment(id);
    }

    @Transactional
    @DeleteMapping("/comments")
    public void deleteComment(@RequestParam(name = "commentId") Long commentId) {
        commentService.deleteComment(commentId);
    }
}
