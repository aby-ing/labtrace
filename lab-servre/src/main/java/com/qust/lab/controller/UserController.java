package com.qust.lab.controller;

import com.qust.lab.common.result.Result;
import com.qust.lab.pojo.dto.UserLoginDTO;
import com.qust.lab.pojo.vo.UserLoginVO;
import com.qust.lab.pojo.vo.UserSimpleVO;
import com.qust.lab.srevice.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(
            @Valid @RequestBody UserLoginDTO dto
    ) {
        return Result.success(
                userService.login(dto)
        );
    }

    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader("Authorization") String authorization
    ) {
        userService.logout(authorization);
        return Result.success(null);
    }

    @GetMapping
    public Result<List<UserSimpleVO>> listEnabledUsers() {
        return Result.success(
                userService.listEnabledUsers()
        );
    }
}
