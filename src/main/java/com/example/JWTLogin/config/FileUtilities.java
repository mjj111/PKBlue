package com.example.JWTLogin.config;

import com.example.JWTLogin.domain.Post;
import com.example.JWTLogin.domain.PostFile;
import com.example.JWTLogin.handler.FileException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUtilities {

    // Paths.get()으로 운영체제에 따라서 다른 파일구분자 처리
    public final static String rootPath = Paths.get("C:", "Users", "82105", "forproject").toString();

    /**
     * MultipartFile 형태의 파일을 Attachments Entity 형태로 파싱
     */
    public static List<PostFile> parseFileInfo(List<MultipartFile> multipartFiles, Post post) throws Exception {

        // 파일이 첨부되지 않았을 경우
        if (CollectionUtils.isEmpty(multipartFiles)) {
            return Collections.emptyList();
        }

        // 파일 업로드 경로 생성
        String savePath = Paths.get(rootPath, "files").toString();
        if (!new File(savePath).exists()) {
            try {
                new File(savePath).mkdir();
            } catch (Exception e) {
                e.getStackTrace();
            }
        }

        List<PostFile> fileList = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {

            String origFilename = multipartFile.getOriginalFilename();
            if (origFilename == null || "".equals(origFilename)) continue;
            String filename = MD5Generator(FilenameUtils.getBaseName(origFilename)).toString() + "." + FilenameUtils.getExtension(origFilename);
            String filePath = Paths.get(savePath, filename).toString();

            PostFile postFile = new PostFile(post,
                    multipartFile.getOriginalFilename(),
                    filename,
                    filePath,
                    multipartFile.getSize());

            fileList.add(postFile);

            try {
                File file = new File(filePath);
                multipartFile.transferTo(file);

                // 파일 권한 설정(쓰기, 읽기)
                file.setWritable(true);
                file.setReadable(true);

            } catch (IOException e) {
                throw new FileException("[" + multipartFile.getOriginalFilename() + "] failed to save file...");

            } catch (Exception e) {
                throw new FileException("[" + multipartFile.getOriginalFilename() + "] failed to save file...");
            }
        }

        return fileList;
    }

    /**
     * 다운로드 받을 파일 생성
     */
    public static File getDownloadFile(PostFile postFile) {

        return new File(Paths.get(rootPath, "files").toString(), postFile.getFileName());
    }

    /**
     * 파일명 중복 방지를 위해 MD5(128비트 암호화 해시 함수) 파일명 생성
     *
     * @param input
     */
    public static String MD5Generator(String input) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        MessageDigest mdMD5 = MessageDigest.getInstance("MD5");
        mdMD5.update(input.getBytes("UTF-8"));

        byte[] md5Hash = mdMD5.digest();
        StringBuilder hexMD5hash = new StringBuilder();

        for(byte b : md5Hash) {
            String hexString = String.format("%02x", b);
            hexMD5hash.append(hexString);
        }

        return hexMD5hash.toString();
    }

    /**
     * MediaType 생성
     *
     * @param
     */
    public static MediaType getMediaType(String filename) {

        String contentType = FilenameUtils.getExtension(filename);
        MediaType mediaType = null;

        if (contentType.equals("png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (contentType.equals("jpeg") || contentType.equals("jpg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (contentType.equals("gif")) {
            mediaType = MediaType.IMAGE_GIF;
        }

        return mediaType;
    }
}
