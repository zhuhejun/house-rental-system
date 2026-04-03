package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.HouseOrderEvaluationsMapper;
import com.kmbeast.mapper.HouseOrderInfoMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseOrderEvaluationsQueryDto;
import com.kmbeast.pojo.em.ContentModerationStatusEnum;
import com.kmbeast.pojo.em.ContentReportTargetTypeEnum;
import com.kmbeast.pojo.em.HouseOrderStatusEnum;
import com.kmbeast.pojo.em.RoleEnum;
import com.kmbeast.pojo.entity.HouseOrderEvaluations;
import com.kmbeast.pojo.entity.HouseOrderInfo;
import com.kmbeast.service.ContentReportService;
import com.kmbeast.pojo.vo.HouseOrderEvaluationsVO;
import com.kmbeast.service.HouseOrderEvaluationsService;
import com.kmbeast.utils.AssertUtils;
import com.kmbeast.utils.ContentSafetyUtils;
import com.kmbeast.utils.SubmissionThrottleUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约看房评价业务逻辑接口实现类
 */
@Service
public class HouseOrderEvalutionsServiceImpl extends ServiceImpl<HouseOrderEvaluationsMapper, HouseOrderEvaluations> implements HouseOrderEvaluationsService {

    @Resource
    private HouseOrderInfoMapper houseOrderInfoMapper;
    @Resource
    private ContentReportService contentReportService;

    /**
     * 预约看房评价新增
     *
     * @param houseOrderEvaluations 实体
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(HouseOrderEvaluations houseOrderEvaluations) {
        judge(houseOrderEvaluations);
        SubmissionThrottleUtils.ensureAllowed("house-order-evaluations", LocalThreadHolder.getUserId());
        // 用户只能针对订单完成一次评论，不能重复评论
        HouseOrderEvaluationsQueryDto queryDto = new HouseOrderEvaluationsQueryDto();
        queryDto.setUserId(LocalThreadHolder.getUserId());
        queryDto.setHouseOrderInfoId(houseOrderEvaluations.getHouseOrderInfoId());
        Integer count = this.baseMapper.listCount(queryDto);
        AssertUtils.isFalse(count > 0, "不能重复评论");
        HouseOrderInfo houseOrderInfo = houseOrderInfoMapper.selectById(houseOrderEvaluations.getHouseOrderInfoId());
        AssertUtils.notNull(houseOrderInfo, "预约订单不存在");
        AssertUtils.equals(LocalThreadHolder.getUserId(), houseOrderInfo.getUserId(), "只能评价自己的预约记录");
        AssertUtils.equals(HouseOrderStatusEnum.STATUS_5.getType(), houseOrderInfo.getOrderStatus(), "仅已完成的预约可评价");
        ContentSafetyUtils.FilterResult filterResult = ContentSafetyUtils.filter(houseOrderEvaluations.getText());
        houseOrderEvaluations.setText(filterResult.getContent());
        houseOrderEvaluations.setCreateTime(LocalDateTime.now()); // 设置时间
        houseOrderEvaluations.setUserId(LocalThreadHolder.getUserId()); // 设置操作者用户ID
        houseOrderEvaluations.setStatus(filterResult.isHitSensitive()
                ? ContentModerationStatusEnum.PENDING_REVIEW.getType()
                : ContentModerationStatusEnum.NORMAL.getType());
        save(houseOrderEvaluations);
        if (filterResult.isHitSensitive()) {
            return ApiResult.success("服务评价已提交，因触发敏感词已进入审核");
        }
        return ApiResult.success();
    }

    private void judge(HouseOrderEvaluations houseOrderEvaluations) {
        AssertUtils.notNull(houseOrderEvaluations, "实体数据不能为空");
        AssertUtils.notNull(houseOrderEvaluations.getHouseOrderInfoId(), "预约订单ID不能为空");
        AssertUtils.hasText(houseOrderEvaluations.getText(), "评论内容不能为空");
        AssertUtils.notNull(houseOrderEvaluations.getScore(), "评分不能为空");
    }

    /**
     * 查询房源所产生的预约看房评价信息
     *
     * @param houseId 房屋ID
     * @return Result<List < HouseOrderEvaluationsVO>> 响应结果
     */
    @Override
    public Result<List<HouseOrderEvaluationsVO>> houseList(Integer houseId) {
        // 传进来的是房屋ID，通过房屋ID先去查询房屋下面产生的所有的预约看房订单数据
        AssertUtils.notNull(houseId, "房源ID不能为空");
        List<Integer> ids = houseOrderInfoMapper.getIdsByHouseId(houseId);
        if (ids.isEmpty()) { // 房屋下面连预约看房订单都没有，更谈不上产生的预约看房订单服务评价
            return ApiResult.success(new ArrayList<>());
        }
        // 构造服务评价查询条件类，将查回来的预约看房订单的ID列表设置为查询条件
        HouseOrderEvaluationsQueryDto houseOrderEvaluationsQueryDto = new HouseOrderEvaluationsQueryDto();
        houseOrderEvaluationsQueryDto.setHouseOrderInfoIds(ids);
        houseOrderEvaluationsQueryDto.setStatus(ContentModerationStatusEnum.NORMAL.getType());
        List<HouseOrderEvaluationsVO> houseOrderEvaluationsVOS = this.baseMapper.list(houseOrderEvaluationsQueryDto);
        houseOrderEvaluationsVOS.forEach(this::fillStatusText);
        return ApiResult.success(houseOrderEvaluationsVOS);
    }

    @Override
    public Result<String> deleteById(Integer id) {
        AssertUtils.notNull(id, "服务评价ID不能为空");
        HouseOrderEvaluations evaluations = this.getById(id);
        AssertUtils.notNull(evaluations, "服务评价不存在");
        boolean isAdmin = RoleEnum.ADMIN.getRole().equals(LocalThreadHolder.getRoleId());
        AssertUtils.isTrue(isAdmin || LocalThreadHolder.getUserId().equals(evaluations.getUserId()), "无删除权限");
        this.removeById(id);
        contentReportService.clearHandledReports(ContentReportTargetTypeEnum.SERVICE_EVALUATION.getType(), id);
        return ApiResult.success("预约看房评价删除成功");
    }

    @Override
    public Result<String> moderate(Integer id, Integer status) {
        AssertUtils.notNull(id, "服务评价ID不能为空");
        AssertUtils.notNull(status, "审核状态不能为空");
        AssertUtils.equals(RoleEnum.ADMIN.getRole(), LocalThreadHolder.getRoleId(), "非法操作");
        AssertUtils.isTrue(ContentModerationStatusEnum.NORMAL.getType().equals(status)
                || ContentModerationStatusEnum.BLOCKED.getType().equals(status), "仅支持通过或屏蔽");
        HouseOrderEvaluations evaluations = this.getById(id);
        AssertUtils.notNull(evaluations, "服务评价不存在");
        evaluations.setStatus(status);
        this.updateById(evaluations);
        contentReportService.clearHandledReports(ContentReportTargetTypeEnum.SERVICE_EVALUATION.getType(), id);
        return ApiResult.success(ContentModerationStatusEnum.NORMAL.getType().equals(status) ? "服务评价已通过" : "服务评价已屏蔽");
    }

    @Override
    public Result<List<HouseOrderEvaluationsVO>> list(HouseOrderEvaluationsQueryDto queryDto) {
        List<HouseOrderEvaluationsVO> houseOrderEvaluationsVOS = this.baseMapper.list(queryDto);
        houseOrderEvaluationsVOS.forEach(this::fillStatusText);
        Integer count = this.baseMapper.listCount(queryDto);
        return ApiResult.success(houseOrderEvaluationsVOS, count);
    }

    private void fillStatusText(HouseOrderEvaluationsVO vo) {
        if (vo == null || vo.getStatus() == null) {
            return;
        }
        if (ContentModerationStatusEnum.NORMAL.getType().equals(vo.getStatus())) {
            vo.setStatusText(ContentModerationStatusEnum.NORMAL.getName());
        } else if (ContentModerationStatusEnum.PENDING_REVIEW.getType().equals(vo.getStatus())) {
            vo.setStatusText(ContentModerationStatusEnum.PENDING_REVIEW.getName());
        } else if (ContentModerationStatusEnum.BLOCKED.getType().equals(vo.getStatus())) {
            vo.setStatusText(ContentModerationStatusEnum.BLOCKED.getName());
        }
    }
}
