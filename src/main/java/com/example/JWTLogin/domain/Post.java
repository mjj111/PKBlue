package com.example.JWTLogin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String postImgUrl;
    private String tag;
    private String text;

    @JsonIgnoreProperties({"postList"})
    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

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
    private boolean likesState;

    private LocalDateTime createDate;

    @Column(name = "only_friend")
    private boolean onlyFriend;

    @PrePersist
    public void createDate() {
        this.createDate = LocalDateTime.now();
    }

    @Builder
    public Post(String postImgUrl, String tag, String text, Member member, long likesCount,boolean onlyFriend) {
        this.postImgUrl = postImgUrl;
        this.tag = tag;
        this.text = text;
        this.member = member;
        this.likesCount = likesCount;
        this.onlyFriend = onlyFriend;
    }

    public void update(String tag, String text) {
        this.tag = tag;
        this.text = text;
    }

    public void updateLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public void updateLikesState(boolean likesState) {
        this.likesState = likesState;
    }

    public void makePost(long id) {
        this.id = id;
    }
}
