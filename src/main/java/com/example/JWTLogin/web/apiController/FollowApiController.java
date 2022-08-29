package com.example.JWTLogin.web.apiController;

import com.example.JWTLogin.domain.Member;
import com.example.JWTLogin.service.FollowService;
import com.example.JWTLogin.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class FollowApiController {

    private final FollowService followService;
    private final MemberService memberService;

    // 팔로우
    @PostMapping("/follow/{toMemberId}")
    public ResponseEntity<?> followMember(@PathVariable long toMemberId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email =  authentication.getName();
        Member fromMember = memberService.findByEmail(email); // 현재 본인
        followService.follow(fromMember.getId(), toMemberId);
        return new ResponseEntity<>("팔로우 성공", HttpStatus.OK);
    }

    // 언팔로우
    @DeleteMapping("/follow/{toMemberId}")
    public ResponseEntity<?> unFollowMember(@PathVariable long toMemberId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email =  authentication.getName();
        Member fromMember = memberService.findByEmail(email); // 현재 본인
        followService.unFollow(fromMember.getId(), toMemberId);
        return new ResponseEntity<>("팔로우 취소 ", HttpStatus.OK);
    }
}