package com.example.JWTLogin.service;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.example.JWTLogin.domain.MailCode;
import com.example.JWTLogin.repository.MailCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailService {

    private final MailCodeRepository mailCodeRepository;
    private final JavaMailSender emailSender;
    public static final String ePw = createKey();

    @Transactional
    public void createMessage(String to)throws Exception{
        MimeMessage  message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, to);//보내는 대상
        message.setSubject("백경 회원가입 인증번호가 도착했습니다.");//제목

        String msgg="";
        msgg+= "<div style='margin:100px;'>";
        msgg+= "<h1> 안녕하세요  국립부경대학교 커뮤니티 백경입니다! </h1>";
        msgg+= "<br>";
        msgg+= "<p>아래 코드를 회원가입 창으로 돌아가 입력해 주시면 회원가입이 진행됩니다.<p>";
        msgg+= "<br>";
        msgg+= "<p>회원 가입에 진심으로 감사드리며 쾌적한 커뮤니티가 되도록 함께 노력합시다!<p>";
        msgg+= "<br>";
        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgg+= "<h3 style='color:blue;'>회원가입 코드입니다.</h3>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "CODE : <strong>";
        msgg+= ePw+"</strong><div><br/> ";
        msgg+= "</div>";
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("skatks1016@naver.com","김명준"));//보내는 사람

        MailCode mailCode = new MailCode();
        mailCode.setCode(ePw);
        mailCode.setEmail(to);
        mailCode.setState(false);
        mailCodeRepository.save(mailCode);
        emailSender.send(message);
    }

    //		인증코드 만들기
    public static String createKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 8; i++) { // 인증코드 8자리
            int index = rnd.nextInt(3); // 0~2 까지 랜덤

            switch (index) {
                case 0:
                    key.append((char) ((int) (rnd.nextInt(26)) + 97));
                    //  a~z  (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    key.append((char) ((int) (rnd.nextInt(26)) + 65));
                    //  A~Z
                    break;
                case 2:
                    key.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }
        return key.toString();
    }

    // 이메일 인증
    @Transactional
    public boolean checkEmail(String email, String code){
        if(mailCodeRepository.findByEmail(email).getCode().equals(code)){
            MailCode mailCode = mailCodeRepository.findByEmail(email);
            mailCode.setState(true);
            return true;
        }
        return false;
    }

    public boolean haveCheckEmail(String email){
        return mailCodeRepository.findByEmail(email).isState();
    }
}