package com.student.controller;

import com.student.dto.LoginDTO;
import com.student.dto.RegisterDTO;
import com.student.dto.RuntimeMetricsDTO;
import com.student.entity.SysUser;
import com.student.service.SysUserService;
import com.student.vo.LoginVO;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

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

    @GetMapping("/runtime-metrics")
    public ResultVO<RuntimeMetricsDTO> getRuntimeMetrics() {
        RuntimeMetricsDTO metrics = new RuntimeMetricsDTO();
        long start = System.nanoTime();

        metrics.setGatewayConcurrency(ManagementFactory.getThreadMXBean().getThreadCount());
        userService.lambdaQuery().eq(SysUser::getStatus, 1).count();
        long delayMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        metrics.setSyncDelayMs(Math.max(delayMs, 1L));
        metrics.setNodeStatus(delayMs <= 200 ? "Stable" : "Busy");
        return ResultVO.success(metrics);
    }
}
