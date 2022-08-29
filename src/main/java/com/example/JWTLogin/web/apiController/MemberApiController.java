package com.example.JWTLogin.web.apiController;

import com.example.JWTLogin.domain.Member;
import com.example.JWTLogin.service.FollowService;
import com.example.JWTLogin.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class MemberApiController {

    private final FollowService followService;
    private final MemberService memberService;


    // 다른 사용자 팔로워 조회
    @GetMapping("/memeber/{profileId}/follower")
    public ResponseEntity<?> getFollower(@PathVariable long profileId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email =  authentication.getName();
        Member loginMember = memberService.findByEmail(email);
        return new ResponseEntity<>(followService.getFollower(profileId,loginMember.getId()), HttpStatus.OK);
    }

    //다른 사용자 팔로잉 조회
    @GetMapping("/member/{profileId}/following")
    public ResponseEntity<?> getFollowing(@PathVariable long profileId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email =  authentication.getName();
        Member loginMember = memberService.findByEmail(email);
        return new ResponseEntity<>(followService.getFollowing(profileId, loginMember.getId()), HttpStatus.OK);
    }
}