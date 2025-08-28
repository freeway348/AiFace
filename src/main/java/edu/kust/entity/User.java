package edu.kust.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("user")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String email;

    private String imagetoken;

    private Integer flag;

    private LocalDateTime createdate;

    private LocalDateTime updatedate;


    // 有参+无参

    public  User(){}
    public User(Integer id, String username, String password, String email, String imagetoken) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.imagetoken = imagetoken;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", imagetoken='" + imagetoken + '\'' +
                '}';
    }
}
