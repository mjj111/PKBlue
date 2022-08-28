package com.example.JWTLogin.web.dto.member;

import com.example.JWTLogin.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


/**
 * 회원 정보 변경시
 * 비밀번호, 닉네임, 자기소개, 프로필 사진을 변경한다.
 */
@Builder
@AllArgsConstructor
@Getter
@Data
public class MemberUpdateDto {

    @Size(min=1, max=30, message = "이름은 1글자 이상, 30글자 이내로 작성해 주세요.")
    @NotBlank(message = "닉네임을 입력해 주세요.")
    private String nickname;

    private String introduce;

    private String ProfileImgUrl;

    public MemberUpdateDto(Member member){
        this.nickname = member.getNickname();
        this.introduce = member.getIntroduce();
        this.ProfileImgUrl = member.getProfileImgUrl();
    }
}
