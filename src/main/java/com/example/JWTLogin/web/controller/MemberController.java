package com.example.JWTLogin.web.controller;

import com.example.JWTLogin.config.security.JwtTokenProvider;
import com.example.JWTLogin.domain.Member;
import com.example.JWTLogin.handler.CustomValidationException;
import com.example.JWTLogin.service.MailService;
import com.example.JWTLogin.service.MemberService;
import com.example.JWTLogin.web.dto.member.MemberProfileDto;
import com.example.JWTLogin.web.dto.member.MemberSignupDto;
import com.example.JWTLogin.web.dto.member.MemberUpdateDto;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.*;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final MailService mailService;


    @GetMapping("/join/checkmail")
    public void join(@RequestParam String email) {
        String joinEmail = email + "@pukyong.ac.kr";
        try {
            mailService.createMessage(joinEmail);
        }catch (Exception e) {
            throw new CustomValidationException("메일 전송 실패");
        }
    }

    @PostMapping("/join/checkmail")
    public boolean join(@RequestParam String email, String code) {
        String joinEmail = email + "@pukyong.ac.kr";
        return mailService.checkEmail(joinEmail,code);
    }


    // 회원가입
    //웰컴(회원가입 성공) -> Json으로 보낸 email과 닉네임을 환영합니다로 만들어달라 프론트에 부탁
    @PostMapping("/join")
    public String join(@Valid @RequestBody MemberSignupDto memberSignupDto, RedirectAttributes redirectAttributes) {
        String joinEmail = memberSignupDto.getEmail() + "@pukyong.ac.kr";
        if(!mailService.haveCheckEmail(joinEmail)){
            throw new CustomValidationException("인증 되지 않은 이메일입니다. ");
        }
        memberSignupDto.setEmail(joinEmail);
        memberService.save(memberSignupDto);
        redirectAttributes.addAttribute("email",memberSignupDto.getEmail());
        redirectAttributes.addAttribute("nickname", memberSignupDto.getNickname());
        return "redirect:/welcom"; //회원가입에 성공한 페이지
    }

    // 로그인
    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> user, RedirectAttributes redirectAttributes) {
        Member member = memberService.findByEmail(user.get("email"));
        if (!passwordEncoder.matches(user.get("password"), member.getPassword())) {
            throw new CustomValidationException("잘못된 비밀번호입니다.");
        }
        //email로 JWT 할당
        String jwt = jwtTokenProvider.createToken(member.getEmail(), member.getRoles(),member.getUsername());
        redirectAttributes.addAttribute("jwt", jwt);
        return "redirect:/member/profile"; // 게시글이 보이는 페이지로 수정할 예정
    }

    //사용자 정보 수정 페이지로 이동 + 수정할 데이터폼 전달
    @GetMapping("/member/update/profile")
    public MemberUpdateDto updateMemberProfile(@RequestParam int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email =  authentication.getName();
        Member loginMember = memberService.findByEmail(email);
        if(loginMember.getId() != id){
            throw new CustomValidationException("잘못된 비밀번호입니다.");
        }
        MemberUpdateDto updateDto = new MemberUpdateDto(loginMember);
        return updateDto;
    }


    //사용자 정보 업데이트
    @PostMapping("/member/update/profile")
    public String updateMember(@Valid @RequestBody MemberUpdateDto memberUpdateDto, @RequestParam("profileImgUrl") MultipartFile multipartFile,
                             RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email =  authentication.getName();
        Member loginMember = memberService.findByEmail(email);
        memberService.update(memberUpdateDto, multipartFile, email);
        redirectAttributes.addAttribute("id", loginMember.getId());
        return "redirect:/member/profile";
    }


    //사용자 프로필
    @GetMapping("/member/profile")
    public MemberProfileDto profile(@RequestParam long toid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginMemberEmail =  authentication.getName();
        MemberProfileDto memberProfileDto = memberService.getMemberProfileDto(toid, loginMemberEmail);
        return memberProfileDto;
    }


    // 비밀번호 변경
    @PostMapping("/member/update/password")
    public String updateMemberPassword(@RequestBody String changePassword,RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email =  authentication.getName();
        Member loginMemeber = memberService.findByEmail(email);
        memberService.changePassword(email,changePassword);
        redirectAttributes.addAttribute("id", loginMemeber.getId());
        return "redirect:/member/profile";
    }


}