package com.kmbeast.controller;

import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.entity.ContentReport;
import com.kmbeast.service.ContentReportService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/content-report")
public class ContentReportController {

    @Resource
    private ContentReportService contentReportService;

    @PostMapping("/save")
    @ResponseBody
    public Result<String> save(@RequestBody ContentReport contentReport) {
        return contentReportService.save(contentReport);
    }

    @GetMapping("/pending-summary")
    @ResponseBody
    public Result<Map<String, Integer>> pendingSummary() {
        return contentReportService.pendingSummary();
    }
}
