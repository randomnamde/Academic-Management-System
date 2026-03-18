package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.DataBackup;

public interface DataBackupService extends IService<DataBackup> {

    DataBackup createBackup(String backupName, String backupType, String operatorNo);

    boolean restoreFromBackup(Long backupId, String operatorNo);
}
