package com.qust.lab.srevice.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qust.lab.mapper.UserMapper;
import com.qust.lab.pojo.dto.UserLoginDTO;
import com.qust.lab.pojo.entity.User;
import com.qust.lab.pojo.vo.UserLoginVO;
import com.qust.lab.srevice.UserService;
import com.qust.lab.utils.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qust.lab.pojo.vo.UserSimpleVO;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public UserServiceImpl(
            UserMapper userMapper,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        // 1. 检查请求对象
        if (dto == null) {
            throw new IllegalArgumentException("登录数据不能为空");
        }

        // 2. 检查账号和密码
        if (dto.getUsername() == null
                || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        if (dto.getPassword() == null
                || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        // 3. 根据用户名查询用户
        User user = userMapper.selectOne(
                new QueryWrapper<User>()
                        .eq("username", dto.getUsername())
        );

        // 4. 账号不存在或密码错误
        if (user == null
                || !passwordEncoder.matches(
                dto.getPassword(),
                user.getPassword()
        )) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 5. 检查用户状态
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new IllegalArgumentException("用户已被禁用");
        }

        // 6. 生成 JWT
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        // 7. 组装登录返回对象
        UserLoginVO vo = new UserLoginVO();

        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());

        return vo;
    }
    @Override
    public List<UserSimpleVO> listEnabledUsers() {
        List<User> users = userMapper.selectList(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getStatus, 1)
                        .orderByAsc(User::getId)
        );

        return users.stream()
                .map(this::convertToSimpleVO)
                .toList();
    }
    private UserSimpleVO convertToSimpleVO(User user) {
        UserSimpleVO vo = new UserSimpleVO();

        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());

        return vo;
    }
}