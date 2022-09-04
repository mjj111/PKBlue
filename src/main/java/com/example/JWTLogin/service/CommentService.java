package com.example.JWTLogin.service;


import com.example.JWTLogin.domain.Comment;
import com.example.JWTLogin.domain.Member;
import com.example.JWTLogin.domain.Post;
import com.example.JWTLogin.repository.CommentRepository;
import com.example.JWTLogin.repository.MemberRepository;
import com.example.JWTLogin.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public void addComment (String text, long postId, String email) {
        Post post = postRepository.findById(postId).get();
        Member loginMember = memberRepository.findByEmail(email);
        Comment comment = Comment.builder()
                .text(text)
                .post(post)
                .member(loginMember)
                .build();
        commentRepository.save(comment);
        post.updateCommentCount(post.getCommentList().size());
    }


    @Transactional
    public void deleteComment(long id) {
        commentRepository.deleteById(id);
    }


}