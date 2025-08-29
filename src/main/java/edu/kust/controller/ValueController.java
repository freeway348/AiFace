package edu.kust.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValueController {


    @Value("${deepseek.api.url}") // 获取yml配置中的 url
    private String url;

    private ValueController(){
        System.out.println("url:" + url);
    }

    @GetMapping("url")
    private String getUrl(){
        return url;
    }

}
