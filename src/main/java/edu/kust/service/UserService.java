package edu.kust.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.kust.dto.LoginUser;
import edu.kust.entity.User;

public interface UserService extends IService<User> {

    boolean login(LoginUser loginUser);
}
