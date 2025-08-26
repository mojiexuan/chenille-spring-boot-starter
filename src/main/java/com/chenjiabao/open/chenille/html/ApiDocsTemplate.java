package com.chenjiabao.open.chenille.html;

/**
 * 接口文档模板
 * @author ChenJiaBao
 */
public class ApiDocsTemplate {

    /**
     * 获取head模板
     */
    public String getHeadTemplate(){
        return """
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>接口文档</title>
                    <style>
                    %s
                    </style>
                </head>
               """.formatted(getCssTemplate());
    }

    /**
     * 获取头部栏模板
     */
    public String getHeaderTemplate(){
        return """
                    <header class="header">
                        <a class="header-logo" href="#">
                            <svg class="header-logo-svg" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                                version="1.0" width="35" height="35" viewBox="0 0 340.000000 250.000000"
                                preserveAspectRatio="xMidYMid meet" color-interpolation-filters="sRGB"
                                style="display:flex;align-items: center;justify-content: center;background: transparent;">
                                <g fill="#f15a29" class="icon-text-wrapper icon-svg-group iconsvg">
                                    <g class="iconsvg-imagesvg" opacity="1">
                                        <g>
                                            <svg x="0" y="0" filtersec="colorsb8497029387" class="image-svg-svg primary"
                                                style="overflow: visible;">
                                                <svg xmlns="http://www.w3.org/2000/svg"
                                                    viewBox="0.001270294189453125 0.002597808837890625 140.7587432861328 101.65928649902344">
                                                    <g id="a4053a00-cef2-4e7f-82a8-77fd7285be7f">
                                                        <g id="b00e9dd2-d3c9-4436-ac7c-a2bb81d95639">
                                                            <path
                                                                d="M96.36,47.81C85.74,37,79.09,24.85,73.21,25.23c-4.44.29-8,9.65-12.83,9.46S53,24.17,46.58,23.78s-23.49,23.43-25.95,30C17.15,63,26.51,69.89,33.94,64.1s12-12.54,14.86-12.54,5.5,10.8,10.71,10.8,10.41-9.25,13.21-10c5.49-1.52,15.35,11.38,17.08,21.71,2.31,13.73-13.88,28.76-46.6,27.52-26.05-1-52.13-27.77-40.24-57S40.7-1.78,70.8.15,110.61,29.92,108,41.62C105.45,52.91,99.82,51.33,96.36,47.81Z"
                                                                fill="#f15a29" />
                                                            <path
                                                                d="M101.37,58.13l-2-1.06c-2.27-1.16-3.23.35-.84,1.77.91.5,1.83,1,2.75,1.42,3.07,13.9,21.62,20.57,39.48,4.65C140.72,64.91,120,33.84,101.37,58.13Z"
                                                                fill="#8dc63f" />
                                                            <path
                                                                d="M140.72,64.91c-11.69,4.14-34-1.55-39.76-6.24C119.72,33.37,140.72,64.91,140.72,64.91Z"
                                                                fill="#4f9a00" />
                                                            <path
                                                                d="M101,58.67c5.11,4.17,23.36,9.14,35.55,7.26-10.17,1.53-26.6-2.74-34-9.23A24.39,24.39,0,0,0,101,58.67Z"
                                                                fill="#408a00" />
                                                            <path
                                                                d="M99.35,68.14c-.32-.59-.64-1.18-1-1.75-1-1.54-2-.86-1.15.61L98,68.31c-15.49,13.13,5.69,25.9,5.69,25.9C113.64,81.88,108.68,69.77,99.35,68.14Z"
                                                                fill="#8dc63f" />
                                                            <path
                                                                d="M103.65,94.21C106,86.34,101.6,71.7,98.32,68,82.11,81.24,103.65,94.21,103.65,94.21Z"
                                                                fill="#4f9a00" />
                                                            <path
                                                                d="M98.32,68c2.92,3.26,6.76,15.21,5.88,23.35.71-6.79-2.62-17.55-7.14-22.25C97.45,68.77,97.87,68.41,98.32,68Z"
                                                                fill="#408a00" />
                                                            <path
                                                                d="M87,65.69c6,15.21-9.37,30.22-40.45,29C21.78,93.78-3,68.35,8.3,40.53c5.84-14.39,15.27-25.67,27-32.95C21.13,15.06,9.75,27.8,3,44.52c-11.89,29.28,14.19,56,40.24,57C76,102.76,92.15,87.73,89.84,74A31.3,31.3,0,0,0,87,65.69Z"
                                                                fill="#f7941e" />
                                                        </g>
                                                    </g>
                                                </svg>
                                            </svg>
                                        </g>
                                    </g>
                                </g>
                                <defs v-gra="od" />
                            </svg>
                            接口文档
                        </a>
                    </header>
               """;
    }

    /**
     * 获取css模板
     */
    private String getCssTemplate(){
        return """
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
               
                *:focus {
                    outline: none;
                }
               
                html {
                    font-size: 62.5%;
                }
               
                html[data-theme="light"] {
                    --mjx-main-color: #f15a29;
                }
               
                body {
                    font-size: 1.6rem;
                    scroll-behavior: smooth;
                }
               
                a {
                    text-decoration: none;
                    color: #1f1f1f;
                }
               
                h1, h2, h3, h4, h5, h6, li, p {
                    overflow-wrap: break-word;
                }
               
                img {
                    max-width: 100%;
                    height: auto;
                    object-fit: cover;
                    object-position: center;
                    border-radius: 0.5rem;
                    transition: all 0.3s ease-in-out;
                    user-select: none;
                    -webkit-user-select: none;
                    -moz-user-select: none;
                }
               
                :root{
                    --mjx-header-height:4rem;
                    --mjx-nav-width: 100vw;
                }
               
                @media screen and (min-width: 640px) {
                    :root{
                        --mjx-header-height:5.5rem;
                        --mjx-nav-width: 30rem;
                    }
                }
               
                @media screen and (min-width: 1200px) {
                    :root{
                        --mjx-nav-width: 40rem;
                    }
                }
               
                .header {
                    position: sticky;
                    top: 0;
                    left: 0;
                    right: 0;
                    background-color: #fff;
                    z-index: 999;
                    border-bottom: 1px solid #ccc;
                    height: var(--mjx-header-height);
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding: 0 2.5rem;
                    user-select: none;
                    transition: all 0.3s ease-in-out;
                }
               
                @media screen and (min-width: 640px) {
                    .header {
                        padding: 0 5rem;
                    }
                }
               
                @media screen and (min-width: 1200px) {
                    .header {
                        height: var(--mjx-header-height);
                        padding: 0 15rem;
                    }
                }
               
                .header-logo {
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                    font-weight: bold;
                    font-size: 1.8rem;
                    line-height: 1.8rem;
                    color: var(--mjx-main-color);
                    transition: all 0.3s ease-in-out;
                }
               
                @media screen and (min-width: 1200px) {
                    .header-logo {
                        font-size: 2rem;
                        line-height: 2rem;
                    }
                }
               
                .header-logo-svg {
                    width: 3rem;
                    height: 3rem;
                    transition: all 0.3s ease-in-out;
                }
               
                @media screen and (min-width: 1200px) {
                    .header-logo-svg {
                        width: 3.5rem;
                        height: 3.5rem;
                    }
                }
               
                .main-container {
                    display: flex;
                    align-items: flex-start;
                    min-height: calc(100vh - var(--mjx-header-height));
                }
               
                .main-nav{
                    top: var(--mjx-header-height);
                    left: 0;
                    position: sticky;
                    height: calc(100vh - var(--mjx-header-height));
                    width: var(--mjx-nav-width);
                    padding: 2.5rem;
                    background-color: #F6F6F7;
                    transition: all 0.3s ease-in-out;
                    z-index: 25;
                    user-select: none;
                }
               
                @media screen and (min-width: 640px) {
                    .main-nav {
                        padding: 5rem;
                    }
                }
               
                @media screen and (min-width:1200px) {
                    .main-nav{
                        padding: 5rem 5rem 5rem 15rem;
                    }
                }
               
                .main-nav-section{
                    padding-top: 1rem;
                    padding-bottom: 2.4rem;
                    border-bottom: .1rem solid #e2e2e3;
                    transition: all 0.3s ease-in-out;
                }
               
                .main-nav-details:open .main-nav-summary .main-nav-summary-icon{
                    transform: rotate(90deg);
                }
               
                .main-nav-summary-icon{
                    transition: all 0.3s ease-in-out;
                }
               
                .main-nav-summary{
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding: .4rem 0;
                    font-size: 1.4rem;
                    line-height: 1.6rem;
                    color: #3c3c43;
                    font-weight: 700;
                    list-style-type: none;
                    cursor: pointer;
                }
               
                .main-nav-list{
                    list-style-type: none;
                }
               
                .main-nav-list-item{
                    padding: .4rem 0;
                }
               
                .main-nav-list-item-link{
                    padding: .4rem 0;
                    font-size: 1.4rem;
                    line-height: 1.6rem;
                    color: #3c3c43;
                    font-weight: 500;
                }
               
                .main-article{
                    flex: 1;
                    padding: 2rem;
                    max-width: calc(100vw - var(--mjx-nav-width));
                }
               
                @media screen and (min-width: 640px) {
                    .main-article {
                        padding: 5rem;
                    }
                }
               
                /*  */
               
                .apidoc{
                    line-height: 2.8rem;
                    --jxt-apidoc-method-bg:#999999;
                    --jxt-apidoc-path-bg:#f7f7f7;
                }
               
                .apidoc-get{
                    --jxt-apidoc-method-bg:#49CC90;
                    --jxt-apidoc-path-bg:#ECFAF4;
                }
               
                .apidoc-post{
                    --jxt-apidoc-method-bg:#61AFEF;
                    --jxt-apidoc-path-bg:#D6EBFF;
                }
               
                .apidoc-put{
                    --jxt-apidoc-method-bg:#FCA130;
                    --jxt-apidoc-path-bg:#FFF5EA;
                }
               
                .apidoc-delete{
                    --jxt-apidoc-method-bg:#E07C7C;
                    --jxt-apidoc-path-bg:#FFD6D6;
                }
               
                .apidoc-name{
                    line-height: 3.2rem;
                    font-size: 2.4rem;
                    padding: 1rem 0 2.4rem 0;
                    font-weight: bold;
                    margin-bottom: 1rem;
                }
               
                .apidoc-version{
                    font-size: 1.2rem;
                    font-weight: normal;
                    line-height: normal;
                    color: #ffffff;
                    padding: .2rem .8rem;
                    border-radius: .3rem;
                    margin-left: .8rem;
                    background-color: var(--jxt-apidoc-method-bg);
                }
               
                .apidoc-api{
                    display: flex;
                    align-items: center;
                    color: #222222;
                    font-size: 0.875em;
                    margin-bottom: 1rem;
                }
               
                .apidoc-tab{
                    border-left: .4rem solid var(--jxt-apidoc-method-bg);
                    margin-bottom: 1rem;
                    padding: 0 1rem;
                    font-weight: bold;
                }
               
                .apidoc-table-content{
                      border-collapse: collapse;
                  margin: 2rem 0;
                  display: block;
                  overflow-x: auto;
                }
               
                :where([dir="ltr"]) .apidoc-table-content th {
                  text-align: left;
                }
               
                .apidoc-table-content th,
                .apidoc-table-content td {
                  border: .1rem solid #e2e2e3;
                  padding: .8rem 1.6rem;
                }
               
                .apidoc-table-content th {
                  color: var(--jxt-text-color-blank);
                  background-color: #f6f6f7;
                  font-size: 1.4rem;
                  font-weight: 600;
                }
               
                .apidoc-table-content tr {
                  background-color: #ffffff;
                  border-top: .1rem solid #e2e2e3;
                }
               
                .apidoc-table-content td {
                  font-size: 1.4rem;
                }
               
                .apidoc-table-content tr:nth-child(2n) {
                  background-color: #f6f6f7;
                }
               
                .apidoc-description{
                    margin-bottom: 1rem;
                }
               
                .apidoc-api-method{
                    background-color: var(--jxt-apidoc-method-bg);
                    border-top-left-radius: .4rem;
                    border-bottom-left-radius: .4rem;
                    padding: .2rem 1.5rem;
                    font-weight: 700;
                    color: #ffffff;
                }
               
                .apidoc-api-path{
                    flex: 1;
                    background-color: var(--jxt-apidoc-path-bg);
                    border-top-right-radius: .4rem;
                    border-bottom-right-radius: .4rem;
                    padding: .2rem 1.5rem;
                }
               """;
    }

}
