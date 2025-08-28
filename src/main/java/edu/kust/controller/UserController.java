package edu.kust.controller;

import com.baidu.aip.face.AipFace;
import edu.kust.dto.*;
import edu.kust.entity.User;
import edu.kust.service.UserService;
import edu.kust.service.imp.UserServiceImp;
import edu.kust.util.VerificationCode;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

// @RequestBody:前端->后端
// @ResponseBody:后端->前端

@RestController // 若使用该注解，则不需要再使用@ResponseBody注解来将返回值序列化为JSON
@RequestMapping("/user")
public class UserController {

    @Autowired // 与@Resources均用于依赖注入，@Autowired更适合spring生态，但@Autowired更适合单例bean，@Resources更适合多例bean
    private UserService userService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String APP_ID = "7025056";
    public static final String API_KEY = "Qv1h2VgziPWcEhIDzEApUCuX";
    public static final String SECRET_KEY = "0dz9b9CjneqJ9FdY3DAPHlnt6fN1pSko";


    @GetMapping("/all")
    @ResponseBody
    public String showAllUsers() {

        return userService.list().toString();
    }

    // 给别人发一封邮件
    @PostMapping("/mail")
    public String sendCode(@RequestBody Email email){

        // 一封邮件需要自己的邮箱、对方邮箱、主题以及内容
        String veriCode = VerificationCode.Veri();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("1656497721@qq.com");
        message.setTo(email.getEmail());
        message.setSubject("验证码"); // 主题
        message.setText("您的验证码是："+ veriCode + "有效期 60 秒");

        // 在redis中存储，并设置有效时长
        redisTemplate.opsForValue().set(email.getEmail(), veriCode);
        redisTemplate.expire(email.getEmail(), 60, TimeUnit.SECONDS);
        // 发送邮箱
        javaMailSender.send(message);
        return "success";
    }

    @PostMapping("/register")
//    public String Register(@RequestBody User user, ,HttpServletResponse httpServletResponse)
    public String Register(@RequestBody RegisterUser registerUser){


        AipFace aipFace = new AipFace(APP_ID, API_KEY, SECRET_KEY);

        // 设置超时时间
        aipFace.setConnectionTimeoutInMillis(2000);
        aipFace.setSocketTimeoutInMillis(60000);

        // 准备图片资源
        String imageType = "BASE64";

        String redisCode = (String)redisTemplate.opsForValue().get(registerUser.getEmail());
        String registerUserCode = registerUser.getCode();

        // 获取redis中email的验证码
        if (!registerUserCode.equals(redisCode)){
            return "fail";
        }
        // 人脸注册
        JSONObject res = aipFace.addUser(registerUser.getImageBase64(), imageType, "group1", registerUser.getUsername(), null);

        System.out.println(res.toString(2));
        User user = new User();

        user.setUsername(registerUser.getUsername());
        user.setPassword(registerUser.getPassword());
        user.setEmail(registerUser.getEmail());
        user.setFlag(0);
        user.setCreatedate(java.time.LocalDateTime.now());
        user.setUpdatedate(java.time.LocalDateTime.now());
        // 获取图片的imageBase64，存入user的imagetoken
        user.setImagetoken(res.getJSONObject("result").getString("face_token"));


        System.out.println(user);

        // 验证码正确，则注册成功
        userService.save(user);

        return "success";
    }


    @PostMapping("/loginMail")
    public String loginMail(@RequestBody LoginMail loginMail){
        // 从redis中获取验证码
        String redisCode = (String) redisTemplate.opsForValue().get(loginMail.getEmail());

        return redisCode.equals(loginMail.getCode()) ? "success" : "fail";

    }

    @PostMapping("/login")
    // 实现一个登录功能，接收服务端传输来的账号密码，并返回登录成功或者失败
    public String login(@RequestBody LoginUser loginUser){
        return userService.login(loginUser) ? "success" : "fail";
    }

    @PostMapping("/loginFace")
    public String loginFace(@RequestBody LoginFace loginFace) throws IOException {
        AipFace aipFace = new AipFace(APP_ID, API_KEY, SECRET_KEY);

        // 设置超时时间
        aipFace.setConnectionTimeoutInMillis(2000);
        aipFace.setSocketTimeoutInMillis(60000);

        // 准备图片资源
        String imageType = "BASE64";

        // 人脸识别
        JSONObject res = aipFace.search(loginFace.getImagetoken(), imageType, "group1", null);

        double score = res.getJSONObject("result").getDouble("score");

        return score > 90 ? "success" : "fail";
    }

}
