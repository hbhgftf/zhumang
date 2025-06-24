package zysy.iflytek.zhumang.user.mapper;

import zysy.iflytek.zhumang.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

@Mapper
public interface IUserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User selectByOpenid(String openid);

    @Insert("INSERT INTO user(openid, session_key, created_time, role, email, password) " +
            "VALUES(#{openid}, #{sessionKey}, #{createdTime}, #{role}, #{email}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}
