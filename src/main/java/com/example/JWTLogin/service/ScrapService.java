package com.example.JWTLogin.service;

import com.example.JWTLogin.domain.Member;
import com.example.JWTLogin.handler.CustomApiException;
import com.example.JWTLogin.repository.LikesRepository;
import com.example.JWTLogin.repository.MemberRepository;
import com.example.JWTLogin.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void postScrap(long postId, String email) {
        Member loginMember = memberRepository.findByEmail(email);
        try {
            scrapRepository.postScrap(postId, loginMember.getId());
        } catch (Exception e) {
            throw new CustomApiException("이미 스크랩 하였습니다.");
        }
    }

    @Transactional
    public void postUnscrap(long postId, String email) {
        Member loginMember = memberRepository.findByEmail(email);
        scrapRepository.postUnscrap(postId, loginMember.getId());
    }
}