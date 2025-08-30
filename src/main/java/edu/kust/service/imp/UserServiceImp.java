package edu.kust.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.kust.dto.LoginFace;
import edu.kust.dto.LoginMail;
import edu.kust.dto.LoginUser;
import edu.kust.entity.User;
import edu.kust.mapper.UserMapper;
import edu.kust.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class UserServiceImp extends ServiceImpl<UserMapper, User> implements UserService {


    @Override
    public boolean login(LoginUser loginUser) {
        // 1. 把username和password构造成sql的筛选条件
        // sql：select * from user where username = ? and password = ?

        // 单表查询使用mybatis-plus提供的条件构造器，该构造器只适用于单表查询
        QueryWrapper<User> w = new QueryWrapper<>();
        // 第一个参数填写的不是User对象的属性名，而是数据库数据表中的字段名
        w.eq("username", loginUser.getUsername());
        // eq相当于sql:username = ?
        w.eq("password", loginUser.getPassword());
        // eq相当于sql：password = ?
        // 如果构造器连续调用方法，则方法之间默认以and连接，构成的完整sql语句就是：username = ? and password = ?
        // 如果要做or关系，则需要显式调用or方法


        // 2. 把sql语句提交给mysql，获取结果
        User user = this.getOne(w);

        // 3. 将结果返回给controller
        if (!ObjectUtils.isEmpty(user)) // 如果非空
            return true;

        return false;
    }

    @Override
    public User getUserInfo(LoginUser loginUser) {
        // 构造查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginUser.getUsername());
        queryWrapper.eq("password", loginUser.getPassword());

        // 查询用户信息
        User user = this.getOne(queryWrapper);

        // 返回用户信息（如果找到的话）
        return user;
    }

    @Override
    public User getUserInfo(LoginFace loginFace) {
        // 构造查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("imagetoken", loginFace.getImagetoken());

        // 查询用户信息
        User user = this.getOne(queryWrapper);

        // 返回用户信息（如果找到的话）
        return user;
    }

    @Override
    public User getUserInfo(LoginMail loginMail) {
        // 构造查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", loginMail.getEmail());

        // 查询用户信息
        User user = this.getOne(queryWrapper);

        // 返回用户信息（如果找到的话）
        return user;
    }
}
