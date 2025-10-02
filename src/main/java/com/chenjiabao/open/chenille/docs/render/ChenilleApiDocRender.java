package com.chenjiabao.open.chenille.docs.render;

import com.chenjiabao.open.chenille.docs.config.ChenilleOpenApi;
import com.chenjiabao.open.chenille.docs.config.ChenilleOpenApiContact;
import com.chenjiabao.open.chenille.docs.config.ChenilleOpenApiLicense;
import com.chenjiabao.open.chenille.docs.config.ChenilleOpenApiServer;
import com.chenjiabao.open.chenille.docs.model.ChenilleApiDefinition;
import com.chenjiabao.open.chenille.docs.model.ChenilleApiGroup;
import com.chenjiabao.open.chenille.docs.model.ChenilleApiParameter;
import com.chenjiabao.open.chenille.docs.scanner.ChenilleApiScanner;
import com.chenjiabao.open.chenille.html.ChenilleApiDocsTemplate;
import com.chenjiabao.open.chenille.model.property.ChenilleDoc;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口文档渲染
 *
 * @author ChenJiaBao
 */
public class ChenilleApiDocRender {

    private final ChenilleDoc chenilleDoc;
    private final ChenilleApiDocsTemplate template;
    private final ChenilleOpenApi chenilleOpenApi;
    private ChenilleApiGroup currentGroup;
    private ChenilleApiDefinition currentApi;

    public ChenilleApiDocRender(ChenilleDoc chenilleDoc,
                                ChenilleApiDocsTemplate template,
                                ChenilleOpenApi chenilleOpenApi) {
        this.chenilleDoc = chenilleDoc;
        this.template = template;
        this.chenilleOpenApi = chenilleOpenApi;
    }

    /**
     * 渲染侧边导航栏
     *
     * @return HTML 字符串
     */
    private String renderNavGroup(String path) {
        return ChenilleApiScanner.getChenilleApiGroups()
                .stream()
                .map(apiGroup -> {
                    String navOption = renderNavOption(path, apiGroup);
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
    private String renderNavOption(String path, ChenilleApiGroup apiGroup) {
        return apiGroup.getApis()
                .stream()
                .map(api -> {
                    // 查找对应的接口
                    boolean isActive = false;
                    if (this.currentApi == null && !path.isEmpty() && api.getUrlCode().equals(path)) {
                        isActive = true;
                        if (!"/".equals(path)) {
                            this.currentGroup = apiGroup;
                            this.currentApi = api;
                        }
                    }
                    return """
                                <li class="main-nav-list-item %s"><a class="main-nav-list-item-link" href="%s">%s</a></li>
                            """.formatted(
                            isActive ? "main-nav-list-item-active" : "",
                            chenilleDoc.getPath() + ("/".equals(api.getUrlCode()) ? "" : "/" + api.getUrlCode()),
                            api.getSummary());
                })
                .collect(Collectors.joining());
    }

    /**
     * 渲染服务列表
     */
    private String renderServers(List<ChenilleOpenApiServer> servers) {
        if (servers == null || servers.isEmpty()) {
            return "";
        }
        return """
                <table>
                        <thead>
                            <tr>
                                <th>环境</th>
                                <th>Base URL</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                      </table>
                """.formatted(servers.stream().map(server ->
                """
                        <tr>
                            <td>%s</td>
                            <td>%s</td>
                        </tr>
                        """.formatted(server.getServerEnv().getDescription(), server.getUrl()))
                        .collect(Collectors.joining()));
    }

    /**
     * 渲染服务条款
     */
    private String renderTermsOfService(String termsOfService) {
        if (termsOfService == null || termsOfService.isEmpty()) {
            return "";
        }
        return """
                <p><a href="%s" target="_blank">服务条款</a></p>
                """.formatted(termsOfService);
    }

    /**
     * 渲染许可证
     */
    private String renderLicense(ChenilleOpenApiLicense license) {
        if (license == null) {
            return "";
        }
        return """
                <p><a href="%s" target="_blank">%s</a></p>
                """.formatted(license.getUrl(), license.getName());
    }

    /**
     * 渲染联系我们
     */
    private String renderContact(ChenilleOpenApiContact contact) {
        if (contact == null) {
            return "";
        }
        return """
                <h4>联系我们</h4>
                    <ul>
                       %s
                       %s
                </ul>
                """.formatted(contact.getEmail() != null ? """
                        <li>邮箱：<a href="mailto:%s">%s</a></li>
                        """.formatted(contact.getEmail(), contact.getEmail()) : "",
                contact.getUrl() != null ? """
                        <li>链接：<a href="%s" target="_blank" >%s</a></li>
                        """.formatted(contact.getUrl(), contact.getUrl()) : "");
    }

    /**
     * 渲染接口文档
     */
    private String renderApis() {
        if (this.currentApi == null) {
            if (chenilleOpenApi == null) {
                return "";
            }
            return """
                    <article class="article-container main-article">
                       <h1>%s<sup class="main-article-version">%s</sup></h1>
                       <p>%s</p>
                       %s
                       %s
                       %s
                       %s
                    </article>
                    """.formatted(
                    this.chenilleOpenApi.getTitle(),
                    this.chenilleOpenApi.getVersion(),
                    this.chenilleOpenApi.getDescription(),
                    renderServers(this.chenilleOpenApi.getServers()),
                    renderTermsOfService(this.chenilleOpenApi.getTermsOfService()),
                    renderLicense(this.chenilleOpenApi.getLicense()),
                    renderContact(this.chenilleOpenApi.getContact()));
        }
        return """
                <article class="article-container api-doc %s">
                     <div class="api-doc-title">
                        <h1 class="api-doc-name">%s<sup class="api-doc-version">%s</sup></h1>
                        <button class="api-doc-btn" onclick="handleDebugClick()">
                             <svg width="24" height="24" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                                      <path d="M15 24V11.8756L25.5 17.9378L36 24L25.5 30.0622L15 36.1244V24Z"
                                        fill="none"
                                        stroke="#333"
                                        stroke-width="2"
                                        stroke-linejoin="round"/>
                             </svg>
                             调试
                        </button>
                     </div>
                     <div class="api-doc-api">
                         <div class="api-doc-api-method api-doc-api-method-get">%s</div>
                         <div class="api-doc-api-path api-doc-api-path-get">%s</div>
                     </div>
                     %s
                     %s
                     %s
                     %s
                </article>
                """.formatted(
                renderApiMethod(),
                this.currentApi.getSummary(),
                "V " + this.currentApi.getVersion(),
                this.currentApi.getMethod(),
                this.currentGroup.getPath() + this.currentApi.getPath(),
                renderApiDescription(),
                renderApiPathParams(),
                renderApiQueryParams(),
                renderApiBodyParams()
        );
    }

    /**
     * 渲染路径参数
     */
    private String renderApiPathParams() {
        if (this.currentApi.getPathParam().isEmpty()) {
            return "";
        }
        String tbody = this.currentApi.getPathParam()
                .stream()
                .map(this::renderTableBody)
                .collect(Collectors.joining());
        return renderTable("路径参数", tbody);
    }

    /**
     * 渲染请求体
     */
    private String renderApiBodyParams() {
        if (this.currentApi.getBodyParam().isEmpty()) {
            return "";
        }
        String tbody = this.currentApi.getBodyParam()
                .stream()
                .map(this::renderTableBody)
                .collect(Collectors.joining());
        return renderTable("请求体", tbody);
    }

    /**
     * 渲染查询参数
     */
    private String renderApiQueryParams() {
        if (this.currentApi.getQueryParam().isEmpty()) {
            return "";
        }
        String tbody = this.currentApi.getQueryParam()
                .stream()
                .map(this::renderTableBody)
                .collect(Collectors.joining());
        return renderTable("查询参数", tbody);
    }

    /**
     * 渲染表格
     */
    private String renderTable(String title, String tbody) {
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
                """.formatted(title, tbody);
    }

    /**
     * 渲染表格体
     */
    private String renderTableBody(ChenilleApiParameter parameter) {
        return """
                <tr>
                     <td>%s</td>
                     <td>%s</td>
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
    private String renderApiDescription() {
        if (this.currentApi.getDescription() == null) {
            return "";
        }
        return """
                <div class="api-doc-tab">接口描述</div>
                <div class="api-doc-description">%s</div>
                """.formatted(this.currentApi.getDescription());
    }

    /**
     * 判断接口请求方式
     */
    private String renderApiMethod() {
        return switch (this.currentApi.getMethod()) {
            case "GET" -> "api-doc-get";
            case "POST" -> "api-doc-post";
            case "PUT" -> "api-doc-put";
            case "DELETE" -> "api-doc-delete";
            default -> "";
        };
    }

    /**
     * 渲染接口文档
     *
     * @return HTML 字符串
     */
    public String render(String path) {
        String html = """
                <!DOCTYPE html>
                <html lang="zh-CN" dir="ltr" data-theme="Light">
                %s
                <body>
                     %s
                     <div class="main-container">
                         <nav class="main-nav">
                         %s
                         </nav>
                         %s
                     </div>
                 </body>
                </html>
                """.formatted(
                template.getHeadTemplate(chenilleOpenApi != null ? (chenilleOpenApi.getTitle() != null ? chenilleOpenApi.getTitle() : "Chenille 文档") : "Chenille 文档"),
                template.getHeaderTemplate(chenilleOpenApi != null ? (chenilleOpenApi.getTitle() != null ? chenilleOpenApi.getTitle() : "Chenille 文档") : "Chenille 文档"),
                renderNavGroup(path),
                renderApis());
        this.currentGroup = null;
        this.currentApi = null;
        return html;
    }

}
