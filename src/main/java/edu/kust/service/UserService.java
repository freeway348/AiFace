package edu.kust.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.kust.dto.LoginFace;
import edu.kust.dto.LoginMail;
import edu.kust.dto.LoginUser;
import edu.kust.entity.User;

public interface UserService extends IService<User> {

    boolean login(LoginUser loginUser);
    User getUserInfo(LoginUser loginUser);
    User getUserInfo(LoginFace loginFace);
    User getUserInfo(LoginMail loginMail);
}
