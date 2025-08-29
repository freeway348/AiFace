package edu.kust.entity;

import lombok.Data;
import org.apache.logging.log4j.message.Message;

import java.util.List;

@Data
public class ChatRequest {

    // 需要的模型
    private String model;
    // 需要的信息
    private List<Message> messages;
    // 流式模式
    private boolean stream = false;

    // 内部类
    @Data
    public static class Message {
        // 确定需要的角色
        private String role;
        // 问答内容
        private String content;
    }

}
