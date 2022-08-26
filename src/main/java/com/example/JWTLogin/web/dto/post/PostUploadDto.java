package com.example.JWTLogin.web.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
@Data
public class PostUploadDto {

    private String text;
    private String tag;
    private boolean onlyFriend;
}