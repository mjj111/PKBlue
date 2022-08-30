package com.example.JWTLogin.web.dto.post;


import com.example.JWTLogin.domain.Member;
import com.example.JWTLogin.domain.Post;
import com.example.JWTLogin.domain.PostFile;
import lombok.*;
import org.springframework.data.domain.Page;

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
    private List<PostFile> postFileList; // 포스트 동영상,이미지 url
    private String text;
    private String tag;
    private long commentCount; // 댓글 수
    private long likesCount; // 좋아요 수
    private LocalDateTime createDate; // 생성일
    private boolean likesState; // 현재 좋아요 눌렀는가
    private boolean onlyFriend; // 맞팔,짝팔
    private long loaderId; // 생성자 ID
    private String loaderProfileImg; // 생성자 프로필 이미지
    private String loaderNickname; // 생성자 닉네임


    public Page<PostDto> toDtoList(Page<Post> postList){
        Page<PostDto> postDtoList = postList.map(p -> PostDto.builder()
                .postId(p.getId())
                .postFileList(p.getPostFiles())
                .text(p.getText())
                .tag(p.getTag())
                .commentCount(p.getCommentCount())
                .likesCount(p.getLikesCount())
                .createDate(p.getCreateDate())
                .onlyFriend(p.isOnlyFriend())
                .likesState(false)
                .loaderId(p.getMember().getId())
                .loaderProfileImg(p.getMember().getProfileImgUrl())
                .loaderNickname(p.getMember().getNickname())
                .build());
        return postDtoList;
    }
}
