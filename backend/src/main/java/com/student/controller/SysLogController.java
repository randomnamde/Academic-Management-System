package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.SysLog;
import com.student.service.SysLogService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys-log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService sysLogService;

    @GetMapping
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<Page<SysLog>> list(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "20") Integer size,
                                       @RequestParam(required = false) String userId,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) String operation) {
        Page<SysLog> pageParam = new Page<>(page, size);
        Page<SysLog> result = sysLogService.lambdaQuery()
                .eq(userId != null, SysLog::getUserId, userId)
                .eq(status != null, SysLog::getStatus, status)
                .like(operation != null && !operation.isBlank(), SysLog::getOperation, operation)
                .orderByDesc(SysLog::getCreateTime)
                .page(pageParam);
        return ResultVO.success(result);
    }
}

