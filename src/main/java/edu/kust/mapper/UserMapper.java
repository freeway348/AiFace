package edu.kust.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.kust.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
