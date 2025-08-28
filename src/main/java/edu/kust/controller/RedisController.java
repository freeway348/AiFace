package edu.kust.controller;

import edu.kust.util.VerificationCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class RedisController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/set")
    public String save(String mail){

        redisTemplate.opsForValue().set(mail, VerificationCode.Veri());
        redisTemplate.expire(mail, 60, TimeUnit.SECONDS);
        return "ok";
    }

    @GetMapping("/get")
    public String get(String mail)
    {
        return (String) redisTemplate.opsForValue().get(mail);
    }

}
