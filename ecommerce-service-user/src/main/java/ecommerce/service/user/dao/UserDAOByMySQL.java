package ecommerce.service.user.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import ecommerce.service.client.base.User;

@Mapper
public interface UserDAOByMySQL {

    @Select("SELECT * FROM users LIMIT #{start}, #{size}")
    @Results(id="userResults", value={
            @Result(property="id",   column="id"),
            @Result(property="username",  column="username"),
            @Result(property="password", column="password"),
            @Result(property="sessionId", column="session_id"),
    })
    public List<User> queryUsers(User userExample, int start, int size);

    @Select("SELECT * FROM users WHERE id = #{id} LIMIT 1")
    @ResultMap("userResults")
    public User getUserById(long id);
    
    @Select("SELECT * FROM users WHERE username = #{username} LIMIT 1")
    @ResultMap("userResults")
    public User getUserByName(String username);
    
    @Select("SELECT * FROM users WHERE session_id = #{sessionId} LIMIT 1")
    @ResultMap("userResults")
    public User getUserBySessionId(String sessionId);

    @Insert("INSERT INTO users(id,username,password,session_id) SELECT #{id}, #{username}, #{password}, #{sessionId}"
    		+ " FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM users WHERE username=#{username} OR id=#{id} LIMIT 1)")
    @Results(id="insert", value={
            @Result(property="id", column="id", javaType = Boolean.class),
        })
    public boolean insert(User user);
    
    @Update("UPDATE users SET username=#{username},password=#{password},session_id=#{sessionId} WHERE id=#{id} LIMIT 1")
    @Results(id="update", value={
            @Result(property="id", column="id", javaType = Boolean.class),
        })
    public boolean update(User user);


}
