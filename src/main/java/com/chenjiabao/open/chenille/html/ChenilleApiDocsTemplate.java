package com.chenjiabao.open.chenille.html;

import lombok.NonNull;

/**
 * 接口文档模板
 * @author ChenJiaBao
 */
public class ChenilleApiDocsTemplate {

    /**
     * 获取head模板
     */
    public String getHeadTemplate(@NonNull String title){
        return """
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <link rel="icon" href="./favicon.ico" type="image/x-icon" />
                    <title>%s</title>
                    <link rel="stylesheet" href="/chenille-api-docs.css">
                </head>
               """.formatted(title);
    }

    /**
     * 获取头部栏模板
     */
    public String getHeaderTemplate(@NonNull String title){
        return """
                    <header class="header">
                        <a class="header-logo" href="#">
                            <img src="./favicon.ico" alt="logo" onerror="this.style.display='none'" />
                            %s
                        </a>
                    </header>
               """.formatted(title);
    }

    /**
     * 获取css模板
     */
    private String getCssTemplate(){
        return """
               """;
    }

}
