package com.example.JWTLogin.web.dto.member;


import com.example.JWTLogin.domain.Member;
import lombok.*;

/**
 * 회원의 프로필을 조회시
 * 조회하는 프로필의 본인인지에 대한 여부, 팔로잉 상태
 * 회원의 정보, 게시글 수, 팔로워 수, 팔로잉 수를 보낸다.
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data
public class MemberProfileDto {
    private String ProfileImgUrl; // 프로필 사진
    private String nickname; // 닉네임
    private String introduce; // 자기소개
    private boolean loginMember; // true면 계정 주인, false면 타계정이 해당 프로필 조회 중  -> 자기계정일 경우 팔로우가 보이지 않도록
    private boolean follow; // true면 해당 계정 팔로잉 중/ false면 해당 계정 일반 조회 중
    private int postCount; // 게시글 개수
    private int memberFollowerCount; // 팔로워 수
    private int memberFollowingCount; // 팔로잉 수
}
