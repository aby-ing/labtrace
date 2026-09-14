package com.qust.lab.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.qust.lab.common.result.Result;
@RestController
public class HelloController {
    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("实验室预约平台启动成功");
    }
}
