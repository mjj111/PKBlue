package com.example.JWTLogin.web.dto.post;

import lombok.*;

import java.math.BigInteger;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Data
public class PostPreviewDto {
    private long id;
    private String postImgUrl;
    private String text;
    private long likesCount;
    //private long commentCount;

    public PostPreviewDto(BigInteger id, String postImgUrl,String text, BigInteger likesCount) {
        this.id = id.longValue();
        this.postImgUrl = postImgUrl;
        this.text = text;
        this.likesCount = likesCount.longValue();
        //this.commentCount = commentCount;
    }
}
