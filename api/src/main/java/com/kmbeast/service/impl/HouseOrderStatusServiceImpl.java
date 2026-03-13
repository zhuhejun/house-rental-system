package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.mapper.HouseOrderStatusMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseOrderStatusQueryDto;
import com.kmbeast.pojo.em.HouseOrderStatusEnum;
import com.kmbeast.pojo.entity.HouseOrderStatus;
import com.kmbeast.pojo.vo.HouseOrderStatusVO;
import com.kmbeast.service.HouseOrderStatusService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约看房状态流转业务逻辑接口实现类
 */
@Service
public class HouseOrderStatusServiceImpl extends ServiceImpl<HouseOrderStatusMapper, HouseOrderStatus> implements HouseOrderStatusService {


    /**
     * 查询预约看房状态流转
     *
     * @param queryDto 查询条件
     * @return Result<List < HouseOrderStatusVO>>
     */
    @Override
    public Result<List<HouseOrderStatusVO>> list(HouseOrderStatusQueryDto queryDto) {
        List<HouseOrderStatusVO> houseOrderStatusVOS = this.baseMapper.list(queryDto);
        Integer count = this.baseMapper.listCount(queryDto);
        statusChange(houseOrderStatusVOS);
        return ApiResult.success(houseOrderStatusVOS, count);
    }

    private void statusChange(List<HouseOrderStatusVO> houseOrderStatusVOS) {
        if (houseOrderStatusVOS != null && !houseOrderStatusVOS.isEmpty()) {
            for (HouseOrderStatusVO houseOrderStatusVO : houseOrderStatusVOS) {
                // 原始状态
                String originText = HouseOrderStatusEnum.getDetail(houseOrderStatusVO.getOriginStatus());
                houseOrderStatusVO.setOldStatusText(originText);
                // 新状态
                String newText = HouseOrderStatusEnum.getDetail(houseOrderStatusVO.getNewId());
                houseOrderStatusVO.setNewStatusText(newText);
            }
        }
    }

    /**
     * 预约看房评价新增
     *
     * @param houseOrderStatus 实体
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(HouseOrderStatus houseOrderStatus) {
        judge(houseOrderStatus);
        houseOrderStatus.setCreateTime(LocalDateTime.now()); // 设置时间
        save(houseOrderStatus);
        return ApiResult.success();
    }

    private void judge(HouseOrderStatus houseOrderStatus) {
        AssertUtils.notNull(houseOrderStatus, "实体数据不能为空");
        AssertUtils.notNull(houseOrderStatus.getHouseOrderInfoId(), "预约订单ID不能为空");
        AssertUtils.notNull(houseOrderStatus.getOriginStatus(), "旧状态不能为空");
        AssertUtils.notNull(houseOrderStatus.getNewId(), "新状态不能为空");
    }


}
