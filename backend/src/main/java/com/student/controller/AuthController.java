package com.student.controller;

import com.student.dto.LoginDTO;
import com.student.dto.RegisterDTO;
import com.student.service.SysUserService;
import com.student.vo.LoginVO;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService userService;

    @PostMapping("/login")
    public ResultVO<LoginVO> login(@RequestBody @Validated LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return ResultVO.success(loginVO);
    }

    @PostMapping("/register")
    public ResultVO<Void> register(@RequestBody @Validated RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return ResultVO.success();
    }
}
