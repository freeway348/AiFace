package edu.kust.service.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kust.entity.ChatRequest;
import edu.kust.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Service // 实例化
public class ChatServiceImp implements ChatService {
    @Value("${deepseek.api.model}")
    private String model;

    @Value("${deepseek.api.url}")
    private String url;

    @Override
    public String chat(String question) throws JsonProcessingException {
        // 此项业务方法处理逻辑：把从本地页面获取的内容发送给deepseek，然后把deepseek的信息返回给项目的前端页面
        ChatRequest.Message message = new ChatRequest.Message();
        message.setRole("assistant");
        message.setContent(question);

        ChatRequest chatRequest  = new ChatRequest();
        chatRequest.setModel(model);
        chatRequest.setMessages(Collections.singletonList(message)); // 参考Javase文档集合篇

        // 把数据发送给deepseek
        // RestTemplate是Springboot提供的一个工具
        RestTemplate restTemplate = new RestTemplate(); // 创建 http请求对象

        Map<Object, String> maps = restTemplate.postForObject(url, chatRequest, Map.class); // 发送的是post请求

        ObjectMapper objectMapper = new ObjectMapper(); // 处理json数据

        String json = objectMapper.writeValueAsString(maps);

        System.out.println(json);

        return json;

    }
}
