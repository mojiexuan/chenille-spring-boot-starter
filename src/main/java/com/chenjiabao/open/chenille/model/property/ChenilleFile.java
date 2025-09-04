package com.chenjiabao.open.chenille.model.property;

import lombok.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleFile {
    private boolean enabled = true;
    // 支持的文件格式
    private List<String> format = Arrays.asList(".png", ".jpg", ".jpeg", ".bmp");
    private Long maxSize = 1024 * 1024 * 10L;
    private String path = "/public/upload";

    public void setPath(String path) {
        if (path != null) {
            this.path = path.replaceAll("[/\\\\]+$", "");
        } else {
            this.path = null;
        }
    }
}
