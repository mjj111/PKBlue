package com.example.JWTLogin.service;

import com.example.JWTLogin.domain.Member;
import com.example.JWTLogin.handler.CustomApiException;
import com.example.JWTLogin.handler.CustomValidationException;
import com.example.JWTLogin.repository.FollowRepository;
import com.example.JWTLogin.repository.MemberRepository;
import com.example.JWTLogin.web.dto.member.MemberProfileDto;
import com.example.JWTLogin.web.dto.member.MemberSignupDto;
import com.example.JWTLogin.web.dto.member.MemberUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    public final MemberRepository memberRepository;
    public final PasswordEncoder passwordEncoder;
    public final FollowRepository followRepository;
    public final MailService mailService;


    // 회원가입
    @Transactional
    public void save(MemberSignupDto signupDto){

        duplicateEmailCheck(signupDto.getEmail());
        duplicateNickname(signupDto.getNickname());
        if(mailService.haveCheckEmail(signupDto.getEmail())){
            Member saveMember = Member.builder()
                    .email(signupDto.getEmail())
                    .password(passwordEncoder.encode(signupDto.getPassword()))
                    .nickname(signupDto.getNickname())
                    .introduce(null)
                    .profileImgUrl(null)
                    .roles(Collections.singletonList("ROLE_USER")) // 최초 가입시 USER 로 설정
                    .build();
            memberRepository.save(saveMember);
        }
        else{
            throw new CustomApiException("이메일 인증이 필요합니다.");
        }
    }


    // 프로필 수정
    @Value("${profileImg.path}")
    private String uploadFolder;
    @Transactional
    public void update(MemberUpdateDto memberUpdateDto, MultipartFile multipartFile, String email) {
        Member loginMember = memberRepository.findByEmail(email);

        if(!multipartFile.isEmpty()) { //파일이 업로드 되었는지 확인
            String imageFileName = loginMember.getId() + "_" + multipartFile.getOriginalFilename();
            Path imageFilePath = Paths.get(uploadFolder + imageFileName);
            try {
                if (loginMember.getProfileImgUrl() != null) { // 이미 프로필 사진이 있을경우
                    File file = new File(uploadFolder + loginMember.getProfileImgUrl());
                    file.delete(); // 원래파일 삭제
                }
                Files.write(imageFilePath, multipartFile.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            loginMember.updateProfileImgUrl(imageFileName);
        }

        loginMember.updateProfile(
                memberUpdateDto.getNickname(),
                memberUpdateDto.getIntroduce()
        );
    }

    // 회원 프로필 조회
    public MemberProfileDto getMemberProfileDto(long toId, String loginMemberEmail) {
        MemberProfileDto memberProfileDto = new MemberProfileDto();

        Member toMember = memberRepository.findById(toId).orElseThrow(() -> { return new CustomValidationException("찾을 수 없는 유저입니다.");});
        memberProfileDto.setProfileImgUrl(toMember.getProfileImgUrl());
        memberProfileDto.setNickname(toMember.getNickname());
        memberProfileDto.setIntroduce(toMember.getIntroduce());

        // loginEmail 활용하여 currentId가 로그인된 사용자 인지 확인
        Member loginMember = findByEmail(loginMemberEmail);
        memberProfileDto.setLoginMember(loginMember.getId() == toMember.getId());

        // 현재 loginMember가 toMember를 구독 했는지 확인
        memberProfileDto.setFollow(followRepository.findFollowByFromMemberIdAndToMemberId(loginMember.getId(), toMember.getId()) != null);

        //게시물 수
        memberProfileDto.setPostCount(toMember.getPostList().size());


        //toMember의 팔로워, 팔로잉 수를 확인한다.
        memberProfileDto.setMemberFollowerCount(followRepository.findFollowerCountById(toMember.getId()));
        memberProfileDto.setMemberFollowingCount(followRepository.findFollowingCountById(toMember.getId()));

        return memberProfileDto;
    }

    public Member findByEmail(String email){
        Member findMember = memberRepository.findByEmail(email);
        return findMember;
    }

    // 이메일 중복 체크
    public boolean duplicateEmailCheck(String email){
        if(memberRepository.findByEmail(email) != null){
            throw new CustomValidationException("이미 사용하고 있는 아이디입니다.");
        }
        return true;
    }
    // 닉네임 중복체크
    public boolean duplicateNickname(String nickname){
        try{
            memberRepository.findByNickname(nickname);
        } catch(Exception e){
            throw new CustomValidationException("이미 존재하는 닉네임입니다.");
        }
        return true;
    }


    // 비밀번호 변경
    @Transactional
    public void changePassword(String email, String password){
        Member loginMember = memberRepository.findByEmail(email);
        String changePSW = passwordEncoder.encode(password);
        loginMember.changePSW(changePSW);
    }

    // 비밀번호 랜덤변경 + 6자리 난수
    @Transactional
    public void changeRandomPassword(String email){
        Member loginMember = memberRepository.findByEmail(email);
        Random random = new Random();
        int createNum = 0;
        String ranNum ="";
        int letter = 6;
        String password = "";

        for(int i = 0; i< letter; i++){
            createNum = random.nextInt(9);
            ranNum = Integer.toString(createNum);
            password += ranNum;
        }

        String changePSW = passwordEncoder.encode(password);
        loginMember.changePSW(changePSW);
        try{
            mailService.createMessage(email,password); // 이메일 전송함.
        } catch (Exception e){
            throw new CustomApiException("메일 전송에 실패하였습니다.");
        }
    }

    // 비밀번호 찾기 메일 전송
    @Transactional
    public void searchPasswordSMTP(String email){
        if(memberRepository.findByEmail(email)!=null){
            try{
                mailService.createMessage(email); // 이메일 전송함.
            } catch (Exception e){
                throw new CustomApiException("이메일 인증에 실패하였습니다.");
            }
        }
        else{
            throw new CustomApiException("등록되지 않은 회원입니다. 다시 확인 부탁드립니다.");
        }
    }

}
