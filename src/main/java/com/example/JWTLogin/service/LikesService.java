package com.example.JWTLogin.service;

import com.example.JWTLogin.domain.Member;
import com.example.JWTLogin.domain.Post;
import com.example.JWTLogin.handler.CustomApiException;
import com.example.JWTLogin.handler.CustomValidationException;
import com.example.JWTLogin.repository.LikesRepository;
import com.example.JWTLogin.repository.MemberRepository;
import com.example.JWTLogin.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class LikesService {

    private final LikesRepository likesRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public void likes(long postId, String email) {
        Member loginMember = memberRepository.findByEmail(email);
        try {
            likesRepository.likes(postId, loginMember.getId());
            Post post = postRepository.findById(postId).orElseThrow(
                    () -> new CustomApiException("존재하지 않은 포스트입니다.")
            );
            post.updateLikesCount(post.getLikesList().size());
        } catch (Exception e) {
            throw new CustomValidationException("이미 좋아요 하였습니다.");
        }
    }

    @Transactional
    public void unLikes(long postId, String email) {
        Member loginMember = memberRepository.findByEmail(email);
        likesRepository.unLikes(postId, loginMember.getId());
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new CustomApiException("존재하지 않은 포스트입니다.")
        );
        post.updateLikesCount(post.getLikesList().size());

    }
}