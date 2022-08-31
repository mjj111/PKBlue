package com.example.JWTLogin.web.dto.post;

import com.example.JWTLogin.domain.Comment;
import com.example.JWTLogin.web.dto.comment.CommentDto;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Data
public class PostDetailDto {
    private long postId; // 포스트 ID
    private long loaderId; // 생성자 ID
    private String loaderProfileImg; // 생성자 프로필 이미지
    private String loaderNickname; // 생성자 닉네임
    private List<String> postFileList; // 포스트 동영상,이미지 url
    private String text;
    private String tag;
    private long commentCount; // 댓글 수
    private long likesCount; // 좋아요 수
    private LocalDateTime createDate; // 생성일
    private boolean likesState; // 현재 좋아요 눌렀는가
    private boolean onlyFriend; // 맞팔,짝팔
    private List<CommentDto> commentDtoList; // 댓글 리스트
}
