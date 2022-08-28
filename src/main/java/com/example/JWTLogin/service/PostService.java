package com.example.JWTLogin.service;

import com.example.JWTLogin.domain.Member;
import com.example.JWTLogin.domain.Post;
import com.example.JWTLogin.repository.*;
import com.example.JWTLogin.web.dto.post.*;
import lombok.RequiredArgsConstructor;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PostService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final LikesRepository likesRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;
    private final EntityManager em;

    @Value("${post.path}")
    private String uploadUrl;


    //포스트 등록
    @Transactional
    public void save(PostUploadDto postUploadDto, MultipartFile multipartFile, String email) {
        Member loginMember = memberRepository.findByEmail(email);
        UUID uuid = UUID.randomUUID();
        String imgFileName = uuid + "_" + multipartFile.getOriginalFilename();

        Path imageFilePath = Paths.get(uploadUrl + imgFileName);
        try {
            Files.write(imageFilePath, multipartFile.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        postRepository.save(Post.builder()
                .postImgUrl(imgFileName)
                .tag(postUploadDto.getTag())
                .text(postUploadDto.getText())
                .member(loginMember)
                .likesCount(0)
                .onlyFriend(postUploadDto.isOnlyFriend())
                .build());
    }

    // 포스트 상세보기
    @Transactional
    public PostInfoDto getPostInfoDto(long postId, String email) {
        Member loginMember = memberRepository.findByEmail(email);

        PostInfoDto postInfoDto = new PostInfoDto();
        postInfoDto.setId(postId);

        Post post = postRepository.findById(postId).get();
        postInfoDto.setTag(post.getTag());
        postInfoDto.setText(post.getText());
        postInfoDto.setPostImgUrl(post.getPostImgUrl());
        postInfoDto.setCreatedate(post.getCreateDate());

        //포스트 정보 요청시 포스트 엔티티의 likesCount, likesState, CommentList를 설정해준다.
        postInfoDto.setLikesCount(post.getLikesList().size());
        post.getLikesList().forEach(likes -> {
            if(likes.getMember().getId() == loginMember.getId()) postInfoDto.setLikeState(true);
        });
        postInfoDto.setCommentList(post.getCommentList());

        //포스트 주인의 정보를 가져온다.
        Member member = memberRepository.findById(post.getMember().getId()).get();

        postInfoDto.setUploaderNickname(member.getNickname());
        if(loginMember.getId() == post.getMember().getId()) postInfoDto.setUploader(true);
        else postInfoDto.setUploader(false);

        return postInfoDto;
    }


    // 포스트 상세 조회
    @Transactional
    public PostDto getPostDto(long postId) {
        //예외 처리 필요 -> post의 작성자가 아닌 사람이 해당 페이지에 접근하여 수정하려고 한다면??
        Post post = postRepository.findById(postId).get();

        PostDto postDto = PostDto.builder()
                .id(postId)
                .tag(post.getTag())
                .text(post.getText())
                .postImgUrl(post.getPostImgUrl())
                .build();

        return postDto;
    }

    //포스트 업데이트
    @Transactional
    public void update(PostUpdateDto postUpdateDto) {
        Post post = postRepository.findById(postUpdateDto.getId()).get();
        post.update(postUpdateDto.getTag(), postUpdateDto.getText());
    }

    // 포스트 지우기
    @Transactional
    public void delete(long postId) {
        Post post = postRepository.findById(postId).get();

        //관련된 likes의 정보 먼저 삭제해 준다.
        likesRepository.deleteLikesByPost(post);

        //관련된 scrap의 정보 먼저 삭제해 준다.
        scrapRepository.deleteScrapByPost(post);

        //관련된 Comment의 정보 먼저 삭제해 준다.
        commentRepository.deleteCommentsByPost(post);

        //관련 파일 저장 위치에서 삭제해 준다.
        File file = new File(uploadUrl + post.getPostImgUrl());
        file.delete();

        postRepository.deleteById(postId);
    }

    // 메인스토리 (짝팔로우) 목록 조회
    @Transactional
    public Page<Post> getMainPost(String email, Pageable pageable) {
        Member loginMemeber = memberRepository.findByEmail(email);
        Page<Post> postList = postRepository.mainStory(loginMemeber.getId(), pageable);

        postList.forEach(post -> {
            post.updateLikesCount(post.getLikesList().size());
            post.getLikesList().forEach(likes -> {
                if(likes.getMember().getId() == loginMemeber.getId()) post.updateLikesState(true);
            });
        });

        return postList;
    }

    // 서브스토리 포스트 목록 조회
    @Transactional
    public Page<Post> getSubPost(String email, Pageable pageable) {
        Member loginMemeber = memberRepository.findByEmail(email);
        Page<Post> postList = postRepository.subStory(loginMemeber.getId(), pageable);

        postList.forEach(post -> {
            post.updateLikesCount(post.getLikesList().size());
            post.getLikesList().forEach(likes -> {
                if(likes.getMember().getId() == loginMemeber.getId()) post.updateLikesState(true);
            });
        });

        return postList;
    }

    // 태그 검색 목록 조회
    @Transactional
    public Page<Post> getTagPost(String tag, String email, Pageable pageable) {
        Member loginMemeber = memberRepository.findByEmail(email);
        Page<Post> postList = postRepository.searchResult(tag, pageable);

        postList.forEach(post -> {
            post.updateLikesCount(post.getLikesList().size());
            post.getLikesList().forEach(likes -> {
                if(likes.getMember().getId() == loginMemeber.getId()) post.updateLikesState(true);
            });
        });
        return postList;
    }

    // 좋아요한 포스트 목록 조회
    @Transactional
    public Page<PostPreviewDto> getLikesPost(String email, Pageable pageable) {
        Member loginMemeber = memberRepository.findByEmail(email);
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT p.id, p.post_img_url,p.text, COUNT(p.id) as likesCount ");
        sb.append("FROM likes l, post p ");
        sb.append("WHERE l.post_id = p.id ");
        sb.append("AND p.id IN (SELECT p.id FROM likes l, post p WHERE l.member_id = ? AND p.id = l.post_id) ");
        sb.append("GROUP BY p.id ");
        sb.append("ORDER BY p.id");

        // 쿼리 완성
        Query query = em.createNativeQuery(sb.toString())
                .setParameter(1, loginMemeber.getId());

        //JPA 쿼리 매핑 - DTO에 매핑
        JpaResultMapper result = new JpaResultMapper();
        List<PostPreviewDto> postLikesList = result.list(query, PostPreviewDto.class);

        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > postLikesList.size() ? postLikesList.size() : (start + pageable.getPageSize());

        if(start > postLikesList.size()) return new PageImpl<PostPreviewDto>(postLikesList.subList(0, 0), pageable, 0);

        Page<PostPreviewDto> postLikesPage = new PageImpl<>(postLikesList.subList(start, end), pageable, postLikesList.size());
        return postLikesPage;
    }


    // 스크랩한 포스트 목록 조회
    @Transactional
    public Page<PostPreviewDto> getScrapPost(String email, Pageable pageable) {
        Member loginMemeber = memberRepository.findByEmail(email);
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT p.id, p.post_img_url, p.text, COUNT(p.id) as likesCount ");
        sb.append("FROM scrap s, post p ");
        sb.append("WHERE s.post_id = p.id ");
        sb.append("AND p.id IN (SELECT p.id FROM scrap s, post p WHERE s.member_id = ? AND p.id = l.post_id) ");
        sb.append("GROUP BY p.id ");
        sb.append("ORDER BY p.id");

        // 쿼리 완성
        Query query = em.createNativeQuery(sb.toString())
                .setParameter(1, loginMemeber.getId());

        //JPA 쿼리 매핑 - DTO에 매핑
        JpaResultMapper result = new JpaResultMapper();
        List<PostPreviewDto> postLikesList = result.list(query, PostPreviewDto.class);

        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > postLikesList.size() ? postLikesList.size() : (start + pageable.getPageSize());

        if(start > postLikesList.size()) return new PageImpl<PostPreviewDto>(postLikesList.subList(0, 0), pageable, 0);

        Page<PostPreviewDto> postLikesPage = new PageImpl<>(postLikesList.subList(start, end), pageable, postLikesList.size());
        return postLikesPage;
    }


//    // 인기 게시글
//    @Transactional
//    public List<PostPreviewDto> getPopularPost() {
//        StringBuffer sb = new StringBuffer();
//        sb.append("SELECT p.id, p.post_img_url, COUNT(p.id) as likesCount ");
//        sb.append("FROM likes l, post p ");
//        sb.append("WHERE l.post_id = p.id ");
//        sb.append("AND p.id IN (SELECT p.id FROM likes l, post p WHERE p.id = l.post_id) ");
//        sb.append("GROUP BY p.id ");
//        sb.append("ORDER BY likesCount DESC, p.id ");
//        sb.append("LIMIT 12 ");
//
//        // 쿼리 완성
//        Query query = em.createNativeQuery(sb.toString());
//
//        //JPA 쿼리 매핑 - DTO에 매핑
//        JpaResultMapper result = new JpaResultMapper();
//        List<PostPreviewDto> postPreviewDtoList = result.list(query, PostPreviewDto.class);
//
//        return postPreviewDtoList;
//    }
}
