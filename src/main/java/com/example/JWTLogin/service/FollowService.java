package com.example.JWTLogin.service;

import com.example.JWTLogin.domain.Follow;
import com.example.JWTLogin.handler.CustomApiException;
import com.example.JWTLogin.repository.FollowRepository;
import com.example.JWTLogin.web.dto.FollowDto;
import lombok.RequiredArgsConstructor;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final EntityManager em;
    private final FollowRepository followRepository;


    //팔로우
    @Transactional
    public void follow(long fromMemberId, long toMemberId){
        if(followRepository.findFollowByFromMemberIdAndToMemberId(fromMemberId, toMemberId) != null) throw new CustomApiException("이미 팔로우 하였습니다.");


        // 맞팔
        else if(followRepository.findFollowByFromMemberIdAndToMemberId(toMemberId, fromMemberId) != null){
            followRepository.follow(fromMemberId, toMemberId,true);
        }


        // 짝팔
        else {
            followRepository.follow(fromMemberId, toMemberId, false);
        }
    }


    //언팔로우
    @Transactional
    public void unFollow(long fromMemberId, long toMemberId) {
        if(followRepository.findFollowByFromMemberIdAndToMemberId(toMemberId, fromMemberId) != null){
            Follow refollow = followRepository.findFollowByFromMemberIdAndToMemberId(toMemberId, fromMemberId);
            refollow.setF4f(false);
        }
        followRepository.unFollow(fromMemberId, toMemberId);
    }


    // 팔로워 조회
    public List<FollowDto> getFollower(long profileId, long loginId) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT m.id, m.nickname, m.profile_img_url, ");
        sb.append("if ((SELECT 1 FROM follow WHERE from_member_id = ? AND to_member_id = m.id), TRUE, FALSE) AS followState, ");
        sb.append("if ((?=m.id), TRUE, FALSE) AS loginUser ");
        sb.append("FROM member m, follow f ");
        sb.append("WHERE m.id = f.from_member_id AND f.to_member_id = ?");
        // 쿼리 완성
        Query query = em.createNativeQuery(sb.toString())
                .setParameter(1, loginId)
                .setParameter(2, loginId)
                .setParameter(3, profileId);

        //JPA 쿼리 매핑 - DTO에 매핑
        JpaResultMapper result = new JpaResultMapper();
        List<FollowDto> followDtoList = result.list(query, FollowDto.class);
        return followDtoList;
    }

    //팔로잉 조회
    @Transactional
    public List<FollowDto> getFollowing(long profileId, long loginId) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT m.id, m.nickname, m.profile_img_url, ");
        sb.append("if ((SELECT 1 FROM follow WHERE from_member_id = ? AND to_member_id = m.id), TRUE, FALSE) AS followState, ");
        sb.append("if ((?=m.id), TRUE, FALSE) AS loginUser ");
        sb.append("FROM member m, follow f ");
        sb.append("WHERE m.id = f.to_member_id AND f.from_member_id = ?");


        // 쿼리 완성
        Query query = em.createNativeQuery(sb.toString())
                .setParameter(1, loginId)
                .setParameter(2, loginId)
                .setParameter(3, profileId);

        //JPA 쿼리 매핑 - DTO에 매핑
        JpaResultMapper result = new JpaResultMapper();
        List<FollowDto> followDtoList = result.list(query, FollowDto.class);
        return followDtoList;
    }

}
