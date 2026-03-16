/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.dataagent.controller;

import com.alibaba.cloud.ai.dataagent.properties.FileStorageProperties;
import com.alibaba.cloud.ai.dataagent.service.file.FileStorageService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.UploadResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

/**
 * 头像上传控制器，支持普通上传和分片断点续传
 * POST /api/file/avatar          — 普通上传（≤ imageSize）
 * POST /api/file/avatar/chunk    — 上传单个分片
 * POST /api/file/avatar/merge    — 合并所有分片，返回最终 URL
 */
@Slf4j
@RestController
@RequestMapping("/api/file/avatar")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AvatarUploadController {

    private final FileStorageProperties fileStorageProperties;
    private final FileStorageService fileStorageService;

    // ── 普通上传 ──────────────────────────────────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        validateImage(file);
        long maxSize = fileStorageProperties.getImageSize();
        if (file.getSize() > maxSize) {
            return ApiResponse.error("图片大小超限，最大允许 " + (maxSize / 1024 / 1024) + "MB");
        }
        String filePath = fileStorageService.storeFile(file, "avatars");
        String url = fileStorageService.getFileUrl(filePath);
        String filename = filePath.substring(filePath.lastIndexOf('/') + 1);
        return ApiResponse.success("上传成功", UploadResponse.ok("上传成功", url, filename));
    }

    // ── 分片上传 ──────────────────────────────────────────────────────────────

    /**
     * 上传单个分片
     * @param uploadId  本次上传的唯一标识（前端生成 UUID）
     * @param chunkIndex 当前分片序号（从 0 开始）
     * @param totalChunks 总分片数
     * @param chunk      分片数据
     */
    @PostMapping(value = "/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("chunk") MultipartFile chunk) throws IOException {

        Path tempDir = getTempDir(uploadId);
        Files.createDirectories(tempDir);
        Path chunkFile = tempDir.resolve("chunk_" + chunkIndex);
        chunk.transferTo(chunkFile.toFile());

        log.debug("Received chunk {}/{} for uploadId={}", chunkIndex + 1, totalChunks, uploadId);
        return ApiResponse.success("分片上传成功", Map.of(
                "uploadId", uploadId,
                "chunkIndex", chunkIndex,
                "received", chunkIndex + 1,
                "total", totalChunks
        ));
    }

    /**
     * 合并所有分片
     * @param body { "uploadId": "...", "totalChunks": N, "filename": "avatar.jpg" }
     */
    @PostMapping("/merge")
    public ApiResponse<UploadResponse> mergeChunks(@RequestBody Map<String, Object> body) throws IOException {
        String uploadId = (String) body.get("uploadId");
        int totalChunks = (int) body.get("totalChunks");
        String originalFilename = (String) body.getOrDefault("filename", "avatar.jpg");

        Path tempDir = getTempDir(uploadId);
        String ext = getExtension(originalFilename);
        String newFilename = "avatars/" + UUID.randomUUID() + "." + ext;

        Path uploadRoot = Paths.get(fileStorageProperties.getPath()).toAbsolutePath();
        Path destFile = uploadRoot.resolve(newFilename);
        Files.createDirectories(destFile.getParent());

        // 按序合并
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(destFile.toFile()))) {
            for (int i = 0; i < totalChunks; i++) {
                Path chunkFile = tempDir.resolve("chunk_" + i);
                if (!Files.exists(chunkFile)) {
                    return ApiResponse.error("分片 " + i + " 缺失，请重新上传");
                }
                Files.copy(chunkFile, out);
            }
        }

        // 清理临时目录
        deleteDirectory(tempDir);

        String url = fileStorageProperties.getUrlPrefix() + "/" + newFilename;
        String filename = destFile.getFileName().toString();
        log.info("Merged {} chunks into {}", totalChunks, destFile);
        return ApiResponse.success("合并成功", UploadResponse.ok("上传成功", url, filename));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void validateImage(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new IllegalArgumentException("只支持图片文件");
        }
    }

    private Path getTempDir(String uploadId) {
        return Paths.get(System.getProperty("java.io.tmpdir"), "da-chunks", uploadId);
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "jpg";
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (var stream = Files.walk(dir)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                  .map(Path::toFile)
                  .forEach(File::delete);
        }
    }
}
