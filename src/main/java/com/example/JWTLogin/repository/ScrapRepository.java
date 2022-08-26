package com.example.JWTLogin.repository;

import com.example.JWTLogin.domain.Likes;
import com.example.JWTLogin.domain.Post;
import com.example.JWTLogin.domain.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    void deleteScrapByPost(Post post);

    @Modifying
    @Query(value = "INSERT INTO scrap(post_id, member_id) VALUES(:postId, :memberId)", nativeQuery = true)
    void postScrap(long postId, long memberId);

    @Modifying
    @Query(value = "DELETE FROM scrap WHERE post_id = :postId AND member_id = :memberId", nativeQuery = true)
    void postUnscrap(long postId, long memberId);
}