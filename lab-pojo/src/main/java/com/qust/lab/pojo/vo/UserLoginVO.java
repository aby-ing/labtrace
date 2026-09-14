package com.qust.lab.pojo.vo;

public class UserLoginVO {

    // JWT 登录令牌
    private String token;

    // 用户 ID
    private Long userId;

    // 登录账号
    private String username;

    // 真实姓名
    private String realName;

    // 用户角色
    private String role;

    public UserLoginVO() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
