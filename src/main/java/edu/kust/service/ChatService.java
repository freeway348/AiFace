package edu.kust.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kust.entity.ChatRequest;

public interface ChatService{
    public String chat (String question) throws JsonProcessingException;
}
