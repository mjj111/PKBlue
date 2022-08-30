package com.example.JWTLogin.repository;

import com.example.JWTLogin.domain.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface PostFileRepository extends JpaRepository<PostFile, Long> {

    List<PostFile> findAllByPostId(Long postId);

    @Modifying
    @Query(value = "DELETE FROM PostFile p " +
                  "WHERE p.id IN (:deleteFileList)")
    void deleteByPostFileIdList(@Param("deleteFileList") List<Long> deleteFileList);
}

