package com.example.JWTLogin.repository;

import com.example.JWTLogin.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value =
            "SELECT * " +
            "FROM post " +
            "WHERE member_id IN (SELECT to_member_id FROM FOLLOW WHERE from_member_id = :fromMemberId) " +
            "AND only_friend = :FALSE " +
            "ORDER BY id DESC", nativeQuery = true)
    Page<Post> mainStory(long fromMemberId, Pageable pageable);

    @Query(value =
            "SELECT * " +
            "FROM post " +
            "WHERE member_id " +
                    "IN (SELECT to_member_id FROM FOLLOW WHERE from_member_id = :fromMemberId AND f4f = :TRUE ) " +
                    "AND only_friend = :True  " +
            "ORDER BY id DESC", nativeQuery = true)
    Page<Post> subStory(long fromMemberId, Pageable pageable);

    @Query(value =
            "SELECT * " +
            "FROM post " +
            "WHERE tag LIKE :tag " +
            "OR tag LIKE CONCAT('%,',:tag,',%') " +
            "OR tag LIKE CONCAT('%,',:tag) " +
            "OR tag LIKE CONCAT(:tag,',%') " +
            "ORDER BY id DESC", nativeQuery = true)
    Page<Post> searchResult(String tag, Pageable pageable);

    @Query(value =
            "SELECT p "+
            "FROM likes l, post p "+
            "WHERE l.post_id = p.id "+
            "AND p.id IN (SELECT p.id FROM likes l, post p WHERE l.member_id = :loginMemberId AND p.id = l.post_id) "+
            "GROUP BY p.id " +
            "ORDER BY p.id DESC ", nativeQuery = true)
    Page<Post> getLikesPost(long loginMemberId, Pageable pageable);

    @Query(value =
            "SELECT p "+
            "FROM scrap s, post p "+
            "WHERE s.post_id = p.id "+
            "AND p.id IN (SELECT p.id FROM scrap s, post p WHERE s.member_id = :loginMemberId AND p.id = s.post_id) "+
            "GROUP BY p.id " +
            "ORDER BY p.id DESC ", nativeQuery = true)
    Page<Post> getScrapPost(long loginMemberId, Pageable pageable);

    @Query(value =
            "SELECT p "+
            "FROM post p "+
            "WHERE p.likes_count > 20 "+
            "p.only_friend = FALSE "+
            "GROUP BY p.id " +
            "ORDER p.id DESC ", nativeQuery = true)
    Page<Post> getPopularPost( Pageable pageable);


}