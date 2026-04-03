package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.ContentReportMapper;
import com.kmbeast.mapper.EvaluationsMapper;
import com.kmbeast.mapper.HouseOrderEvaluationsMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.em.ContentModerationStatusEnum;
import com.kmbeast.pojo.em.ContentReportTargetTypeEnum;
import com.kmbeast.pojo.entity.ContentReport;
import com.kmbeast.pojo.entity.Evaluations;
import com.kmbeast.pojo.entity.HouseOrderEvaluations;
import com.kmbeast.service.ContentReportService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentReportServiceImpl implements ContentReportService {

    private static final int AUTO_PENDING_THRESHOLD = 3;
    private static final int STATUS_PENDING = 1;
    private static final int STATUS_HANDLED = 2;

    @Resource
    private ContentReportMapper contentReportMapper;
    @Resource
    private EvaluationsMapper evaluationsMapper;
    @Resource
    private HouseOrderEvaluationsMapper houseOrderEvaluationsMapper;

    @Override
    public Result<String> save(ContentReport contentReport) {
        AssertUtils.notNull(contentReport, "举报内容不能为空");
        AssertUtils.notNull(contentReport.getTargetId(), "举报对象不能为空");
        AssertUtils.hasText(contentReport.getReason(), "请填写举报原因");
        AssertUtils.isTrue(ContentReportTargetTypeEnum.contains(contentReport.getTargetType()), "举报类型非法");

        Integer reporterId = LocalThreadHolder.getUserId();
        contentReport.setReporterId(reporterId);

        LambdaQueryWrapper<ContentReport> wrapper = new LambdaQueryWrapper<ContentReport>()
                .eq(ContentReport::getTargetType, contentReport.getTargetType())
                .eq(ContentReport::getTargetId, contentReport.getTargetId())
                .eq(ContentReport::getReporterId, reporterId);
        AssertUtils.isFalse(contentReportMapper.selectCount(wrapper) > 0, "请勿重复举报同一条内容");

        validateTarget(contentReport, reporterId);
        contentReport.setStatus(STATUS_PENDING);
        contentReport.setCreateTime(LocalDateTime.now());
        contentReportMapper.insert(contentReport);

        LambdaQueryWrapper<ContentReport> totalWrapper = new LambdaQueryWrapper<ContentReport>()
                .eq(ContentReport::getTargetType, contentReport.getTargetType())
                .eq(ContentReport::getTargetId, contentReport.getTargetId());
        if (contentReportMapper.selectCount(totalWrapper) >= AUTO_PENDING_THRESHOLD) {
            markTargetPending(contentReport);
        }
        return ApiResult.success("举报已提交，平台会尽快审核");
    }

    @Override
    public Result<Map<String, Integer>> pendingSummary() {
        Map<String, Integer> summary = new HashMap<>();
        summary.put("comment", distinctTargetCount(ContentReportTargetTypeEnum.COMMENT.getType()));
        summary.put("service", distinctTargetCount(ContentReportTargetTypeEnum.SERVICE_EVALUATION.getType()));
        summary.put("total", summary.get("comment") + summary.get("service"));
        return ApiResult.success(summary);
    }

    @Override
    public void clearHandledReports(Integer targetType, Integer targetId) {
        if (targetType == null || targetId == null) {
            return;
        }
        LambdaQueryWrapper<ContentReport> wrapper = new LambdaQueryWrapper<ContentReport>()
                .eq(ContentReport::getTargetType, targetType)
                .eq(ContentReport::getTargetId, targetId)
                .eq(ContentReport::getStatus, STATUS_PENDING);
        ContentReport update = new ContentReport();
        update.setStatus(STATUS_HANDLED);
        contentReportMapper.update(update, wrapper);
    }

    private void validateTarget(ContentReport contentReport, Integer reporterId) {
        if (ContentReportTargetTypeEnum.COMMENT.getType().equals(contentReport.getTargetType())) {
            Evaluations evaluations = evaluationsMapper.selectById(contentReport.getTargetId());
            AssertUtils.notNull(evaluations, "评论不存在");
            AssertUtils.notEquals(reporterId, evaluations.getCommenterId(), "不能举报自己发布的评论");
            return;
        }
        HouseOrderEvaluations houseOrderEvaluations = houseOrderEvaluationsMapper.selectById(contentReport.getTargetId());
        AssertUtils.notNull(houseOrderEvaluations, "服务评价不存在");
        AssertUtils.notEquals(reporterId, houseOrderEvaluations.getUserId(), "不能举报自己提交的服务评价");
    }

    private void markTargetPending(ContentReport contentReport) {
        if (ContentReportTargetTypeEnum.COMMENT.getType().equals(contentReport.getTargetType())) {
            Evaluations evaluations = evaluationsMapper.selectById(contentReport.getTargetId());
            if (evaluations != null && ContentModerationStatusEnum.NORMAL.getType().equals(evaluations.getStatus())) {
                Evaluations update = new Evaluations();
                update.setId(evaluations.getId());
                update.setStatus(ContentModerationStatusEnum.PENDING_REVIEW.getType());
                evaluationsMapper.updateById(update);
            }
            return;
        }
        HouseOrderEvaluations houseOrderEvaluations = houseOrderEvaluationsMapper.selectById(contentReport.getTargetId());
        if (houseOrderEvaluations != null && ContentModerationStatusEnum.NORMAL.getType().equals(houseOrderEvaluations.getStatus())) {
            HouseOrderEvaluations update = new HouseOrderEvaluations();
            update.setId(houseOrderEvaluations.getId());
            update.setStatus(ContentModerationStatusEnum.PENDING_REVIEW.getType());
            houseOrderEvaluationsMapper.updateById(update);
        }
    }

    private Integer distinctTargetCount(Integer targetType) {
        QueryWrapper<ContentReport> wrapper = new QueryWrapper<>();
        wrapper.select("target_id");
        wrapper.eq("target_type", targetType);
        wrapper.eq("status", STATUS_PENDING);
        List<Object> targetIds = contentReportMapper.selectObjs(wrapper);
        if (targetIds == null || targetIds.isEmpty()) {
            return 0;
        }
        return (int) targetIds.stream()
                .filter(java.util.Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toSet())
                .size();
    }
}
