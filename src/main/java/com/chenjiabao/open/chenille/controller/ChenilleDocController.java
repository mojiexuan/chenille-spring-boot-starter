package com.chenjiabao.open.chenille.controller;

import com.chenjiabao.open.chenille.docs.render.ChenilleApiDocRender;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口文档
 *
 * @author ChenJiaBao
 */
@RestController
@RequestMapping("${chenille.config.docs.path:/docs}")
public record ChenilleDocController(ChenilleApiDocRender chenilleApiDocRender) {

    @GetMapping
    public ResponseEntity<String> getHtmlDocs() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(chenilleApiDocRender.render("/"));
    }

    @GetMapping("/{path}")
    public ResponseEntity<String> getHtmlDocsByPath(
            @PathVariable(value = "path")
            String path) {
        if (path == null) {
            path = "/";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(chenilleApiDocRender.render(path));
    }

}
