package com.kmbeast.service;

import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.entity.ContentReport;

import java.util.Map;

public interface ContentReportService {

    Result<String> save(ContentReport contentReport);

    Result<Map<String, Integer>> pendingSummary();

    void clearHandledReports(Integer targetType, Integer targetId);
}
