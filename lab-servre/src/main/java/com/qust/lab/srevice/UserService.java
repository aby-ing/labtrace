package com.qust.lab.srevice;

import com.qust.lab.pojo.dto.UserLoginDTO;
import com.qust.lab.pojo.vo.UserLoginVO;
import com.qust.lab.pojo.vo.UserSimpleVO;

import java.util.List;

public interface UserService {

    UserLoginVO login(UserLoginDTO dto);

    List<UserSimpleVO> listEnabledUsers();
}