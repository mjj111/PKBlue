package com.example.JWTLogin.service;

import com.example.JWTLogin.config.FileUtilities;
import com.example.JWTLogin.domain.*;
import com.example.JWTLogin.handler.CustomApiException;
import com.example.JWTLogin.handler.CustomValidationException;
import com.example.JWTLogin.repository.*;
import com.example.JWTLogin.web.dto.comment.CommentDto;
import com.example.JWTLogin.web.dto.post.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PostService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final LikesRepository likesRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;
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


    // 포스트 상세 보기
    public PostDetailDto getMainFeed(long postId, String email) {
        Post wantedPost = postRepository.findById(postId).orElseThrow(() -> new CustomValidationException("존재하지 않은 포스트 입니다."));
        PostDetailDto postDetailDto = new PostDetailDto();


        // post 정보 추가
        postDetailDto.setPostId(postId);
        postDetailDto.setText(wantedPost.getText());
        postDetailDto.setTag(wantedPost.getTag());
        postDetailDto.setCommentCount(wantedPost.getCommentCount());
        postDetailDto.setLikesCount(wantedPost.getLikesCount());
        postDetailDto.setCreateDate(wantedPost.getCreateDate());
        postDetailDto.setOnlyFriend(wantedPost.isOnlyFriend());


        // comment 정보 추가
        List<CommentDto> commentDtoList = new CommentDto().toDtoList(wantedPost.getCommentList());
        postDetailDto.setCommentDtoList(commentDtoList);


        // member 정보 추가
        Member loginMember = memberRepository.findByEmail(email);
        postDetailDto.setLoaderId(wantedPost.getMember().getId());
        postDetailDto.setLoaderNickname(wantedPost.getMember().getNickname());
        postDetailDto.setLoaderProfileImg(wantedPost.getMember().getProfileImgUrl());


        //likeState 추가
        List<Likes> LikeList = wantedPost.getLikesList();
        postDetailDto.setLikesState(false);
        for(Likes like : LikeList){
            if(like.getMember().getId() == loginMember.getId()){
                postDetailDto.setLikesState(true);
            }
        }

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
    public void update(PostUpdateDto postUpdateDto,String email) {
        Member loginMember = memberRepository.findByEmail(email);
        Post post = postRepository.findById(postUpdateDto.getPostId()).orElseThrow(() -> new CustomValidationException("존재하지 않은 포스트 입니다."));
        if(post.getMember().getId() != loginMember.getId()){
            throw new CustomApiException("본인 게시글만 수정할 수 있습니다.");
        }
        post.update(postUpdateDto.getTag(), postUpdateDto.getText());
    }


    // 포스트 지우기
    @Transactional
    public void delete(long postId, String email) {
        Member loginMember = memberRepository.findByEmail(email);
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomValidationException("존재하지 않은 포스트 입니다."));
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
        List<Long> postFileIdList = new ArrayList<>();

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
        Page<PostDto> postDtoPage = new PostDto().toDtoList(postList); // likeState false 고정, 추후 수정예정
        return postDtoPage;
    }


    // 서브스토리(맞팔로우) 포스트 목록 조회
    @Transactional
    public Page<PostDto> getSubFeed(String email, Pageable pageable) {
        Member loginMemeber = memberRepository.findByEmail(email);
        Page<Post> postList = postRepository.subStory(loginMemeber.getId(), pageable);
        Page<PostDto> postDtoPage = new PostDto().toDtoList(postList); // likeState false 고정, 추후 수정예정
        return postDtoPage;
    }

    // 본인 게시 피드 포스트 목록 조회
    @Transactional
    public Page<PostDto> getSelfFeed(String email, Pageable pageable) {
        Member loginMemeber = memberRepository.findByEmail(email);
        Page<Post> postList = postRepository.selfFeed(loginMemeber.getId(), pageable);
        Page<PostDto> postDtoPage = new PostDto().toDtoList(postList); // likeState false 고정, 추후 수정예정
        return postDtoPage;
    }


    // 태그 검색 목록 조회
    @Transactional
    public Page<PostDto> getTagPost(String tag, String email, Pageable pageable) {
        Page<Post> postList = postRepository.searchResult(tag, pageable);
        Page<PostDto> postDtoPage = new PostDto().toDtoList(postList);
        return postDtoPage;
    }


    // 좋아요한 포스트 목록 조회
    @Transactional
    public Page<PostDto> getLikesPost(String email, Pageable pageable) {
        Member loginMemeber = memberRepository.findByEmail(email);
        Page<Post> postList = postRepository.getLikesPost(loginMemeber.getId(), pageable);
        Page<PostDto> postDtoPage = new PostDto().toDtoList(postList);
        return postDtoPage;
    }


    // 스크랩한 포스트 목록 조회
    @Transactional
    public Page<PostDto> getScrapPost(String email, Pageable pageable) {
        Member loginMemeber = memberRepository.findByEmail(email);
        Page<Post> postList = postRepository.getScrapPost(loginMemeber.getId(), pageable);
        Page<PostDto> postDtoPage = new PostDto().toDtoList(postList);
        return postDtoPage;
    }


    // 인기 게시글
    @Transactional
    public Page<PostDto> getPopularPost(Pageable pageable) {
        Page<Post> postList = postRepository.getPopularPost(pageable);
        Page<PostDto> postDtoPage = new PostDto().toDtoList(postList);
        return postDtoPage;
    }
}
