package com.example.JWTLogin.repository;

import com.example.JWTLogin.domain.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Follow findFollowByFromMemberIdAndToMemberId(long from_member_id, long to_member_id);

    @Query(value = "SELECT COUNT(*) FROM follow WHERE to_member_id = :profileId", nativeQuery = true)
    int findFollowerCountById(long profileId);

    @Query(value = "SELECT COUNT(*) FROM follow WHERE from_member_id = :profileId", nativeQuery = true)
    int findFollowingCountById(long profileId);

    @Modifying
    @Query(value = "INSERT INTO follow(from_member_id, to_member_id,f4f) VALUES(:fromId, :toId)", nativeQuery = true)
    void follow(long fromId, long toId,boolean f4f);

    @Modifying
    @Query(value = "DELETE FROM follow WHERE from_member_id = :fromId AND to_member_id = :toId", nativeQuery = true)
    void unFollow(long fromId, long toId);
}
