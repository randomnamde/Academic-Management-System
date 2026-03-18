package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.BackupStrategy;
import com.student.entity.DataBackup;
import com.student.entity.DataRestore;
import com.student.mapper.BackupStrategyMapper;
import com.student.security.CurrentUserService;
import com.student.service.DataBackupService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/backup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SCHOOL_ADMIN')")
public class DataBackupController {

    private final DataBackupService dataBackupService;
    private final BackupStrategyMapper backupStrategyMapper;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResultVO<DataBackup> createBackup(
            @RequestParam String backupName,
            @RequestParam(defaultValue = "MANUAL") String backupType,
            Authentication authentication) {
        String operatorNo = currentUserService.getCurrentTeacherNo(authentication);
        DataBackup backup = dataBackupService.createBackup(backupName, backupType, operatorNo);
        return ResultVO.success(backup);
    }

    @PostMapping("/restore/{backupId}")
    public ResultVO<Boolean> restoreBackup(
            @PathVariable Long backupId,
            Authentication authentication) {
        String operatorNo = currentUserService.getCurrentTeacherNo(authentication);
        boolean success = dataBackupService.restoreFromBackup(backupId, operatorNo);
        return ResultVO.success(success);
    }

    @GetMapping("/list")
    public ResultVO<Page<DataBackup>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<DataBackup> pager = new Page<>(page, size);
        Page<DataBackup> result = dataBackupService.page(pager,
            new LambdaQueryWrapper<DataBackup>()
                .orderByDesc(DataBackup::getCreateTime));
        return ResultVO.success(result);
    }

    @GetMapping("/restore/list")
    public ResultVO<Page<DataRestore>> restoreList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<DataRestore> pager = new Page<>(page, size);
        return ResultVO.success(pager);
    }

    @GetMapping("/strategy/list")
    public ResultVO<List<BackupStrategy>> listStrategy() {
        List<BackupStrategy> strategies = backupStrategyMapper.selectList(null);
        return ResultVO.success(strategies);
    }

    @PostMapping("/strategy")
    public ResultVO<Void> createStrategy(@RequestBody BackupStrategy strategy) {
        backupStrategyMapper.insert(strategy);
        return ResultVO.success();
    }

    @PutMapping("/strategy/{id}")
    public ResultVO<Void> updateStrategy(@PathVariable Long id, @RequestBody BackupStrategy strategy) {
        strategy.setId(id);
        backupStrategyMapper.updateById(strategy);
        return ResultVO.success();
    }

    @DeleteMapping("/strategy/{id}")
    public ResultVO<Void> deleteStrategy(@PathVariable Long id) {
        backupStrategyMapper.deleteById(id);
        return ResultVO.success();
    }
}
