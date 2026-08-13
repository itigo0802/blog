package com.itigotti.blog.mapper;

import com.itigotti.blog.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User findByUsername(String username);

    User findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void insert(User user);

    List<User> findAll();

    void updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);
}