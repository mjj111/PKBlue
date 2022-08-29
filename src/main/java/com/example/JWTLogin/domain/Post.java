package com.example.JWTLogin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String text;

    private String tag;

    @OneToMany(
            mappedBy = "post",
            cascade = {CascadeType.PERSIST,CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true
    )
    private List<PostFile> postFiles = new ArrayList<>();

    @JsonIgnoreProperties({"postList"})
    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @NotNull
    @Column(name = "only_friend")
    private boolean onlyFriend;

    @JsonIgnoreProperties({"post"})
    @OneToMany(mappedBy = "post")
    private List<Likes> likesList;

    @OrderBy("id")
    @JsonIgnoreProperties({"post"})
    @OneToMany(mappedBy = "post")
    private List<Comment> commentList;

    @Transient
    private long likesCount;

    @Transient
    private long commentCount;

    @Transient
    private boolean likesState;

    private LocalDateTime createDate;

    @PrePersist
    public void createDate() {
        this.createDate = LocalDateTime.now();
    }

    @Builder
    public Post( String tag, String text, Member member, long commentCount, long likesCount,boolean onlyFriend) {
        this.text = text;
        this.tag = tag;
        this.member = member;
        this.likesCount = likesCount;
        this.commentCount = commentCount;
        this.onlyFriend = onlyFriend;
    }

    public void setPostFileList(List<PostFile> fileList){
        this.postFiles = fileList;
    }

//    public void update(String tag, String text) {
//        this.tag = tag;
//        this.text = text;
//    }

    public void updateLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public void updateCommentCount(long commentCount){this.commentCount = commentCount;}

    public void updateLikesState(boolean likesState) {
        this.likesState = likesState;
    }

}
