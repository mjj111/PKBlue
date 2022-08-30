package com.example.JWTLogin.service;

import com.example.JWTLogin.config.FileUtilities;
import com.example.JWTLogin.domain.*;
import com.example.JWTLogin.handler.CustomApiException;
import com.example.JWTLogin.repository.*;
import com.example.JWTLogin.web.dto.post.*;
import lombok.RequiredArgsConstructor;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

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
    private final PostFileRepository postFileRepository;
    private final FollowRepository followRepository;


    //포스트 등록
    @Transactional
    public long save(PostUploadDto postUploadDto, List<MultipartFile> multipartFiles, String email) {
        Member loginMember = memberRepository.findByEmail(email);
        Post beSavedPost = new Post(postUploadDto.getTag(),
                                postUploadDto.getText(),
                                loginMember,
                                0,
                                0,
                                postUploadDto.isOnlyFriend());
        Post haveSavedPost = postRepository.save(beSavedPost);
        try{
            List<PostFile> postFileList = FileUtilities.parseFileInfo(multipartFiles, haveSavedPost);
            // 파일이 존재할 경우
            if (!postFileList.isEmpty()) {
                postFileList.forEach(postFile -> postFileRepository.save(postFile));
                haveSavedPost.setPostFileList(postFileList);
            }

        } catch (Exception e){
            throw new CustomApiException("파일 업로드 실패");
        }

        return haveSavedPost.getId();

    }
/**
 *
 *
 * 여기까지 함
 *
 */
    /
    // 포스트 상세 보기
    public PostDetailDto getMainFeed(long postId, String email) {
        Post wantedPost = postRepository.findById(postId).get();
        PostDetailDto postDetailDto = new PostDetailDto();

        postDetailDto.setPostId(postId);
        postDetailDto.setText(wantedPost.getText());
        postDetailDto.setTag(wantedPost.getTag());
        postDetailDto.setCommentCount(wantedPost.getCommentCount());
        postDetailDto.setLikesCount(wantedPost.getLikesCount());
        postDetailDto.setCreateDate(wantedPost.getCreateDate());
        postDetailDto.setOnlyFriend(wantedPost.isOnlyFriend());
        postDetailDto.setCommentList(wantedPost.getCommentList());


        Member loginMember = memberRepository.findByEmail(email);
        List<Likes> LikeList = wantedPost.getLikesList();

        postDetailDto.setLikesState(false);

        for(Likes like : LikeList){
            if(like.getMember().getId() == loginMember.getId()){
                postDetailDto.setLikesState(true);
            }
        }

        postDetailDto.setLoaderId(wantedPost.getMember().getId());
        postDetailDto.setLoaderNickname(wantedPost.getMember().getNickname());
        postDetailDto.setLoaderProfileImg(wantedPost.getMember().getProfileImgUrl());

        // 맞팔용 게시글일 경우 맞팔이 아니면 조회하지 못한다.
        if(wantedPost.isOnlyFriend()){
            if(followRepository.findFollowByFromMemberIdAndToMemberId(wantedPost.getMember().getId(),loginMember.getId()) == null){
                throw new CustomApiException("친한 친구용 게시글입니다, 접근 권한이 없습니다.");
            }
        }

        return postDetailDto;
    }


    // 포스트 업데이트 태그와 내용 수정 가능
    @Transactional
    public void update(PostUpdateDto postUpdateDto) {
        Post post = postRepository.findById(postUpdateDto.getPostId()).get();
        post.update(postUpdateDto.getTag(), postUpdateDto.getText());
    }

    // 포스트 지우기
    @Transactional
    public void delete(long postId, String email) {
        Member loginMember = memberRepository.findByEmail(email);
        Post post = postRepository.findById(postId).get();
        if(loginMember.getId() != post.getMember().getId()){
            throw new CustomApiException("게시글 생성자 본인이 아닙니다.");
        }

        //관련된 likes의 정보 먼저 삭제해 준다.
        likesRepository.deleteLikesByPost(post);

        //관련된 scrap의 정보 먼저 삭제해 준다.
        scrapRepository.deleteScrapByPost(post);

        //관련된 Comment의 정보 먼저 삭제해 준다.
        commentRepository.deleteCommentsByPost(post);

        //관련 파일 저장 위치에서 삭제해 준다.
        List<Long> postFileIdList = new ArrayList<Long>();

        for(PostFile postFile : post.getPostFiles()){
            postFileIdList.add(postFile.getId());
        }
        postFileRepository.deleteByPostFileIdList(postFileIdList);

        postRepository.deleteById(postId);
    }

    // 메인스토리 (짝팔로우) 목록 조회
    @Transactional
    public Page<PostDto> getMainFeed(String email, Pageable pageable) {
        Member loginMemeber = memberRepository.findByEmail(email);
        Page<Post> postList = postRepository.mainStory(loginMemeber.getId(), pageable);
        postList.forEach(post -> {
            post.updateLikesCount(post.getLikesList().size());
            post.updateCommentCount(post.getCommentList().size());}
        );
        Page<PostDto> postDtoPage = new PostDto().toDtoList(postList); // likeState false 고정, 추후 수정예정
        return postDtoPage;
    }

    // 서브스토리(맞팔로우) 포스트 목록 조회
    @Transactional
    public Page<PostDto> getSubFeed(String email, Pageable pageable) {
        Member loginMemeber = memberRepository.findByEmail(email);
        Page<Post> postList = postRepository.subStory(loginMemeber.getId(), pageable);
        postList.forEach(post -> {
            post.updateLikesCount(post.getLikesList().size());
            post.updateCommentCount(post.getCommentList().size());}
        );
        Page<PostDto> postDtoPage = new PostDto().toDtoList(postList); // likeState false 고정, 추후 수정예정
        return postDtoPage;
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
    public Page<PostDetailDto> getLikesPost(String email, Pageable pageable) {
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
        List<PostDetailDto> postLikesList = result.list(query, PostDetailDto.class);

        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > postLikesList.size() ? postLikesList.size() : (start + pageable.getPageSize());

        if(start > postLikesList.size()) return new PageImpl<PostDetailDto>(postLikesList.subList(0, 0), pageable, 0);

        Page<PostDetailDto> postLikesPage = new PageImpl<>(postLikesList.subList(start, end), pageable, postLikesList.size());
        return postLikesPage;
    }


    // 스크랩한 포스트 목록 조회
    @Transactional
    public Page<PostDetailDto> getScrapPost(String email, Pageable pageable) {
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
        List<PostDetailDto> postLikesList = result.list(query, PostDetailDto.class);

        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > postLikesList.size() ? postLikesList.size() : (start + pageable.getPageSize());

        if(start > postLikesList.size()) return new PageImpl<PostDetailDto>(postLikesList.subList(0, 0), pageable, 0);

        Page<PostDetailDto> postLikesPage = new PageImpl<>(postLikesList.subList(start, end), pageable, postLikesList.size());
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
