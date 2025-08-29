package edu.kust.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kust.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/chat")
    public String chat(@RequestBody String question) {


        if (question.equals("") || Objects.isNull(question))
        {
            return "fail";
        }

        try{
            System.out.println(question);
            return chatService.chat(question);

        }catch(JsonProcessingException e){
            return "error";
        }
    }
}
