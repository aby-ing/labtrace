package com.qust.lab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qust.lab.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}