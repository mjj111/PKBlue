package com.example.JWTLogin.web.dto.post;

import com.example.JWTLogin.domain.Comment;
import com.example.JWTLogin.domain.Member;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Data
public class PostInfoDto {

    private long id;
    private String text;
    private String tag;
    private LocalDateTime createdate;
    private String uploaderNickname;
    private long likesCount;
    private boolean likeState;
    private boolean uploader;
    private String postImgUrl;
    private List<Comment> commentList;
}