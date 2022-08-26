package com.example.JWTLogin.service;


import com.example.JWTLogin.domain.Follow;
import com.example.JWTLogin.domain.Member;
import com.example.JWTLogin.handler.CustomApiException;
import com.example.JWTLogin.repository.FollowRepository;
import com.example.JWTLogin.web.dto.FollowDto;
import com.example.JWTLogin.web.dto.member.MemberSignupDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class FollowServiceTest {


    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private MemberService memberService;

    private EntityManager em;

    private Member from_member;
    private Member to_member;

    @BeforeEach
    public void setUp() {
        MemberSignupDto memberSignupDto = MemberSignupDto.builder()
                .email("skaks1012316@naver.com")
                .password("audwns2021802V!")
                .nickname("명준캡짱")
                .build();

        MemberSignupDto memberSignupDto2 = MemberSignupDto.builder()
                .email("skak6@naver.com")
                .password("audwns20802V!")
                .nickname("부경파랑새")
                .build();
        memberService.save(memberSignupDto);
        memberService.save(memberSignupDto2);
        from_member = memberService.findByEmail(memberSignupDto.getEmail());
        to_member = memberService.findByEmail(memberSignupDto2.getEmail());

    }

    @Test
    public void follow_성공() throws Exception {
        //when
        Follow follow = new Follow(from_member, to_member,false);
        followRepository.save(follow);

        //then
        int how = followRepository.findFollowerCountById(to_member.getId());

        Assertions.assertThat(how).isEqualTo(1);

//    @Test
//    public void getFollower_성공() {
//        //given
//        Follow follow = new Follow(from_user, to_user);
//        followRepository.save(follow);
//
//        List<Object> followDtoList = new ArrayList<>(); // jparesultmapper의 return타입은 arraylist
//        Object[] result_object = {to_user.getId(), to_user.getName(), ".jpg", 1, 1};
//        followDtoList.add(result_object);
//
//        given(em.createNativeQuery(any())).willReturn(mock_query);
//        given(em.createNativeQuery(any()).setParameter(anyInt(), any())).willReturn(mock_query);
//        given(mock_query.getResultList()).willReturn(followDtoList);
//
//        //when
//        List<FollowDto> followDtoList_result = followService.getFollower(to_user.getId(), to_user.getId());
//
//        //then
//        assertThat(followDtoList_result.size()).isEqualTo(followDtoList.size());
//    }
//
//    @Test
//    public void getFollowing_성공() {
//        //given
//        Follow follow = new Follow(from_user, to_user);
//        followRepository.save(follow);
//
//        List<Object> followDtoList = new ArrayList<>(); // jparesultmapper의 return타입은 arraylist
//        Object[] result_object = {from_user.getId(), from_user.getName(), ".jpg", 1, 1};
//        followDtoList.add(result_object);
//
//        given(em.createNativeQuery(any())).willReturn(mock_query);
//        given(em.createNativeQuery(any()).setParameter(anyInt(), any())).willReturn(mock_query);
//        given(mock_query.getResultList()).willReturn(followDtoList);
//
//        //when
//        List<FollowDto> followDtoList_result = followService.getFollowing(from_user.getId(), from_user.getId());
//
//        //then
//        assertThat(followDtoList_result.size()).isEqualTo(followDtoList.size());
//    }
    }
}