package com.example.JWTLogin.web.dto.post;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data
public class PostUpdateDto {

    private long postId;
    private long loaderId; // 생성자 ID
    private String loaderProfileImg; // 생성자 프로필 이미지
    private String loaderNickname; // 생성자 닉네임
    private List<String> postFileList; // 포스트 동영상,이미지 url
    private String text;
    private String tag;
    private boolean onlyFriend; // 맞팔,짝팔

}