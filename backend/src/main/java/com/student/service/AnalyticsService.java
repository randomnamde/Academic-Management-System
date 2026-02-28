package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.AnalyticsFilterDTO;
import com.student.dto.AnalyticsOverviewDTO;
import com.student.dto.RiskStudentDTO;
import com.student.dto.TrendPointDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AnalyticsService {

    AnalyticsOverviewDTO getOverview(AnalyticsFilterDTO filter, Authentication authentication);

    List<TrendPointDTO> getAttendanceTrend(AnalyticsFilterDTO filter, String granularity, Authentication authentication);

    List<TrendPointDTO> getScoreTrend(AnalyticsFilterDTO filter, String granularity, Authentication authentication);

    Page<RiskStudentDTO> getRiskStudents(AnalyticsFilterDTO filter,
                                         String riskType,
                                         Integer page,
                                         Integer size,
                                         Authentication authentication);
}

