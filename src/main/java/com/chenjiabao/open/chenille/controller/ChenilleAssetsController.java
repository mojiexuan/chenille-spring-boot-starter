package com.chenjiabao.open.chenille.controller;

import com.chenjiabao.open.chenille.model.property.ChenilleAssets;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
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
public record ChenilleAssetsController(ChenilleAssets assets) {

    /**
     * 获取静态资源
     * @param req 请求
     * @return 响应
     */
    public ResponseEntity<Flux<DataBuffer>> buildAssetsResponse(ServerHttpRequest req) {
        String requestUri = req.getURI().getPath();
        String contextPath = req.getPath().contextPath().value();
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
            String ifNoneMatch = req.getHeaders().getFirst(HttpHeaders.IF_NONE_MATCH);
            if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
            long ifModifiedSince = req.getHeaders().getIfModifiedSince();
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
            String rangeHeader = req.getHeaders().getFirst(HttpHeaders.RANGE);

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
                    headers.set("Content-Range", "bytes */" + fileSize);
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                            .headers(headers)
                            .build();
                }

                // 设置响应头
                headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
                headers.setContentLength(end - start + 1);
                // 206
                status = HttpStatus.PARTIAL_CONTENT;
            }else {
                headers.setContentLength(fileSize);
            }

            long finalStart = start;
            long finalEnd = end;

            Flux<DataBuffer> body = DataBufferUtils.readByteChannel(
                    ()-> {
                        FileChannel channel = FileChannel.open(resolvedPath, StandardOpenOption.READ);
                        channel.position(finalStart);
                        return channel;
                    },
                    new DefaultDataBufferFactory(),
                    8192
            ).transform(flux ->
                    DataBufferUtils.takeUntilByteCount(flux, finalEnd - finalStart + 1));

            return ResponseEntity.status(status).headers(headers).body(body);
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
