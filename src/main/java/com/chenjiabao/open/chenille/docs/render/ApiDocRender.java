package com.chenjiabao.open.chenille.docs.render;

import com.chenjiabao.open.chenille.docs.model.ApiDefinition;
import com.chenjiabao.open.chenille.docs.model.ApiParameter;
import com.chenjiabao.open.chenille.docs.scanner.ApiScanner;
import com.chenjiabao.open.chenille.html.ApiDocsTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口文档渲染
 * @author ChenJiaBao
 */
public class ApiDocRender {

    private final ApiDocsTemplate template = new ApiDocsTemplate();
    private ApiDefinition currentApi;

    /**
     * 渲染侧边导航栏
     * @return HTML 字符串
     */
    private String renderNavGroup(String path) {
        return ApiScanner.getApiGroups().stream().map(apiGroup -> {
                    String navOption = renderNavOption(path, apiGroup.getApis());
                    return """
                            <section class="main-nav-section">
                                <details class="main-nav-details">
                                    <summary class="main-nav-summary">%s
                                        <svg class="main-nav-summary-icon" width="18" height="18" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M19 12L31 24L19 36" stroke="#929295" stroke-width="4" stroke-linecap="round"
                                                stroke-linejoin="round" />
                                        </svg>
                                    </summary>
                                    <ul class="main-nav-list">
                                        %s
                                    </ul>
                                </details>
                            </section>
                            """.formatted(
                            apiGroup.getName(),
                            navOption
                    );
                })
                .collect(Collectors.joining());
    }

    /**
     * 渲染侧边导航栏选项
     */
    private String renderNavOption(String path, List<ApiDefinition> apis) {
        return apis
                .stream()
                .map(api -> {
                    // 查找对应的接口
                    boolean isActive = false;
                    if(this.currentApi == null && !path.isEmpty() && api.getUrlCode().equals(path)){
                        isActive = true;
                        this.currentApi = api;
                    }
                    return """
                            <li class="main-nav-list-item %s"><a class="main-nav-list-item-link" href="/api-docs/%s">%s</a></li>
                        """.formatted(
                            isActive ? "main-nav-list-item-active" : "",
                            api.getUrlCode(),
                            api.getSummary());
                })
                .collect(Collectors.joining());
    }

    /**
     * 渲染接口文档
     */
    private String renderApis(){
        if(this.currentApi == null){
            return "";
        }
        return """
               <section class="apidoc %s">
                    <h4 class="apidoc-name">%s<sup class="apidoc-version">%s</sup></h4>
                    <div class="apidoc-api">
                        <div class="apidoc-api-method apidoc-api-method-get">%s</div>
                        <div class="apidoc-api-path apidoc-api-path-get">%s</div>
                    </div>
                    %s
                    %s
                    %s
                    %s
               </section>
               """.formatted(
                       renderApiMethod(),
                this.currentApi.getSummary(),
                this.currentApi.getVersion(),
                this.currentApi.getMethod(),
                this.currentApi.getPath(),
                renderApiDescription(),
                renderApiPathParams(),
                renderApiQueryParams(),
                renderApiBodyParams()
        );
    }

    /**
     * 渲染路径参数
     */
    private String renderApiPathParams(){
        if(this.currentApi.getPathParam().isEmpty()){
            return "";
        }
        String tbody = this.currentApi.getPathParam()
                .stream()
                .map(this::renderTableBody)
                .collect(Collectors.joining());
        return renderTable("路径参数",tbody);
    }

    /**
     * 渲染请求体
     */
    private String renderApiBodyParams(){
        if(this.currentApi.getBodyParam().isEmpty()){
            return "";
        }
        String tbody = this.currentApi.getBodyParam()
                .stream()
                .map(this::renderTableBody)
                .collect(Collectors.joining());
        return renderTable("请求体",tbody);
    }

    /**
     * 渲染查询参数
     */
    private String renderApiQueryParams(){
        if(this.currentApi.getQueryParam().isEmpty()){
            return "";
        }
        String tbody = this.currentApi.getQueryParam()
                .stream()
                .map(this::renderTableBody)
                .collect(Collectors.joining());
        return renderTable("查询参数",tbody);
    }

    /**
     * 渲染表格
     */
    private String renderTable(String title,String tbody){
        return """
                <div class="apidoc-tab">%s</div>
                <table class="apidoc-table-content">
                    <thead>
                        <tr>
                            <th>参数名称</th>
                            <th>数据类型</th>
                            <th>是否必填</th>
                            <th>参数说明</th>
                        </tr>
                    </thead>
                    <tbody>
                        %s
                    </tbody>
                </table>
               """.formatted(title,tbody);
    }

    /**
     * 渲染表格体
     */
    private String renderTableBody(ApiParameter parameter){
        return """
               <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td class="no">否</td>
                    %s
                    <td>%s</td>
               </tr>
               """.formatted(
                       parameter.getName(),
                parameter.getType(),
                parameter.isRequired() ? "<td class=\"yes\">是</td>" : "<td class=\"no\">否</td>",
                parameter.getDescription());
    }

    /**
     * 渲染接口描述
     */
    private String renderApiDescription(){
        if(this.currentApi.getDescription() == null){
            return "";
        }
        return """
               <div class="apidoc-tab">接口描述</div>
               <div class="apidoc-description">%s</div>
               """.formatted(this.currentApi.getDescription());
    }

    /**
     * 判断接口请求方式
     */
    private String renderApiMethod(){
        return switch (this.currentApi.getMethod()) {
            case "GET" -> "apidoc-get";
            case "POST" -> "apidoc-post";
            case "PUT" -> "apidoc-put";
            case "DELETE" -> "apidoc-delete";
            default -> "";
        };
    }

    /**
     * 渲染接口文档
     * @return HTML 字符串
     */
    public String render(String path) {
        return """
               <!DOCTYPE html>
               <html lang="zh-CN" dir="ltr" data-theme="light">
               %s
               <body>
                    %s
                    <div class="main-container">
                        <nav class="main-nav">
                        %s
                        </nav>
                        <article class="main-article">
                        %s
                        </article>
                    </div>
                </body>
               </html>
               """.formatted(
                       template.getHeadTemplate(),
                template.getHeaderTemplate(),
                renderNavGroup(path),
                renderApis());
    }

}
