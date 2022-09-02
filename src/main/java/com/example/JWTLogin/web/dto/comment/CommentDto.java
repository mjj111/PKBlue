package com.example.JWTLogin.web.dto.comment;

import com.example.JWTLogin.domain.Comment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Data
public class CommentDto {

    private String commenterImgUrl;
    private String commenterNickname;
    private LocalDateTime createDate;
    private String text;

    public List<CommentDto> toDtoList(List<Comment> commentList){
        List<CommentDto> commentDtoList = commentList.stream().map(c -> CommentDto.builder()
                .commenterImgUrl(c.getMember().getProfileImgUrl())
                .commenterNickname(c.getMember().getNickname())
                .createDate(c.getCreateDate())
                .text(c.getText())
                .build()).collect(Collectors.toList());

        return commentDtoList;
    }
}
