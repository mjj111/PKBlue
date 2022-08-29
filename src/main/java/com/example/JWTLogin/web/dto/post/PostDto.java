package com.example.JWTLogin.web.dto.post;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Data
public class PostDto {

    private long postId;
    private long loaderId; // 생성자 ID
    private String loaderProfileImg; // 생성자 프로필 이미지
    private String loaderNickname; // 생성자 닉네임
    private List<String> postFileList; // 포스트 동영상,이미지 url
    private String text;
    private String tag;
    private int commentCount; // 댓글 수
    private int likesCount; // 좋아요 수
    private LocalDateTime elapseTime; // 생성 경과시간
    private LocalDateTime createDate; // 생성일
    private boolean likesState; // 현재 좋아요 눌렀는가
    private boolean onlyFriend; // 맞팔,짝팔
}
