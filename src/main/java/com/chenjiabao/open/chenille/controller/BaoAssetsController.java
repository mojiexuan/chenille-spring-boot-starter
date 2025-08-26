package com.chenjiabao.open.chenille.controller;

import com.chenjiabao.open.chenille.model.property.Assets;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.*;
import org.springframework.util.MimeType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

/**
 * 静态资源控制器
 * 拦截外部静态资源访问，拼接本地系统路径并返回文件
 * 流式传输，性能高效，支持视频进度条拖拽
 * <p>
 * // @RequestMapping(value = "/public/**",method = { RequestMethod.GET,RequestMethod.HEAD })
 * 在你的静态资源控制器类上添加类似此注解
 *
 * @author 陈佳宝 mail@chenjiabao.com
 */
public record BaoAssetsController(Assets assets) {

    /**
     * 获取静态资源
     * @param req 请求
     * @return 响应
     */
    public ResponseEntity<StreamingResponseBody> getAssetsPublic(HttpServletRequest req) {
        String requestUri = req.getRequestURI();
        String contextPath = req.getContextPath();
        int publicPathStart = contextPath.length() + ("/" + assets.getPath() + "/").length();
        if (publicPathStart > requestUri.length()) {
            return ResponseEntity.notFound().build();
        }
        String requestedEncodedPath = requestUri.substring(publicPathStart);
        String requestedPath = URLDecoder.decode(requestedEncodedPath, StandardCharsets.UTF_8)
                .replaceAll("/+", "/");

        // 路径安全检查
        Path publicDir = Paths.get(System.getProperty("user.dir"), assets.getPath()).normalize();
        Path resolvedPath = publicDir.resolve(requestedPath).normalize();

        try {
            // 解析符号链接的真实路径
            Path realPath = resolvedPath.toRealPath();
            if (!realPath.startsWith(publicDir.toRealPath())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

        try {
            // 检查文件是否存在
            if (!Files.exists(resolvedPath) || Files.isDirectory(resolvedPath)) {
                return ResponseEntity.notFound().build();
            }

            // 获取文件属性
            BasicFileAttributes attrs = Files.readAttributes(resolvedPath, BasicFileAttributes.class);
            long lastModified = attrs.lastModifiedTime().toMillis();
            long fileSize = attrs.size();
            String eTag = "\"" + lastModified + "-" + fileSize + "\"";

            // 处理条件请求（304 Not Modified）
            String ifNoneMatch = req.getHeader("If-None-Match");
            if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
            long ifModifiedSince = req.getDateHeader("If-Modified-Since");
            if (ifModifiedSince >= (lastModified / 1000) * 1000) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }

            // 处理 Range 请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(getContentType(resolvedPath)));
            headers.set("Accept-Ranges", "bytes");
            headers.setCacheControl("public, max-age=86400, must-revalidate");
            headers.setLastModified(lastModified);
            headers.setETag(eTag);

            long start = 0;
            long end = fileSize - 1;
            HttpStatus status = HttpStatus.OK;
            String rangeHeader = req.getHeader("Range");

            // 流式读取文件
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                // 解析范围并设置响应头
                String rangeValue = rangeHeader.substring(6);
                // 不支持多范围
                if (rangeValue.contains(",")) {
                    headers.set("Content-Range", "bytes */" + fileSize);
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                            .headers(headers)
                            .build();
                }

                String[] ranges = rangeValue.split("-");
                try {
                    start = Long.parseLong(ranges[0]);
                    if (ranges.length > 1 && !ranges[1].isEmpty()) {
                        end = Long.parseLong(ranges[1]);
                    } else {
                        // 处理类似 bytes=0- 的请求
                        end = fileSize - 1;
                    }
                } catch (NumberFormatException e) {
                    // 处理无效的 Range 头
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                // 范围检查
                if (start < 0 || end >= fileSize || start > end) {
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                            .headers(headers)
                            .build();
                }

                // 设置响应头
                headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
                headers.setContentLength(end - start + 1);
                // 206
                status = HttpStatus.PARTIAL_CONTENT;
            }

            StreamingResponseBody responseBody = getStreamingResponseBody(start, end, resolvedPath);
            return ResponseEntity.status(status).headers(headers).body(responseBody);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchFileException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Nonnull
    private static StreamingResponseBody getStreamingResponseBody(long start, long end, Path resolvedPath) {
        final long finalStart = start;
        final long finalEnd = end;
        // 客户端中断连接，无需处理
        return outputStream -> {
            try (RandomAccessFile raf = new RandomAccessFile(resolvedPath.toFile(), "r")) {
                raf.seek(finalStart);
                long bytesToRead = finalEnd - finalStart + 1;
                byte[] buffer = new byte[8 * 1024];
                while (bytesToRead > 0) {
                    int read = raf.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead));
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(buffer, 0, read);
                    bytesToRead -= read;
                }
            } catch (ClientAbortException ex) {
                // 客户端中断连接，无需处理
            }
        };
    }

    /**
     * 获取文件类型
     *
     * @param filePath 文件路径
     * @return 类型
     */
    private String getContentType(Path filePath) {
        try {
            String filename = filePath.getFileName().toString().toLowerCase();
            Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(filename);
            return mediaType.map(MimeType::toString)
                    .orElseGet(() -> {
                        // 自定义常见类型后备方案
                        if (filename.endsWith(".css")) {
                            return "text/css";
                        }
                        if (filename.endsWith(".js")) {
                            return "application/javascript";
                        }
                        if (filename.endsWith(".html")) {
                            return "text/html; charset=UTF-8";
                        }
                        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
                    });

        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}
