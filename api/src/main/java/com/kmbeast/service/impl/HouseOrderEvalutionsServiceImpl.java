package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.HouseOrderEvaluationsMapper;
import com.kmbeast.mapper.HouseOrderInfoMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseOrderEvaluationsQueryDto;
import com.kmbeast.pojo.dto.HouseOrderInfoQueryDto;
import com.kmbeast.pojo.entity.HouseOrderEvaluations;
import com.kmbeast.pojo.vo.HouseOrderEvaluationsVO;
import com.kmbeast.pojo.vo.HouseOrderInfoVO;
import com.kmbeast.service.HouseOrderEvaluationsService;
import com.kmbeast.utils.AssertUtils;
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

    /**
     * 查询预约看房评价列表
     *
     * @param queryDto 查询条件
     * @return Result<List < HouseOrderEvaluationsVO>>
     */
    @Override
    public Result<List<HouseOrderEvaluationsVO>> list(HouseOrderEvaluationsQueryDto queryDto) {
        List<HouseOrderEvaluationsVO> houseOrderEvaluationsVOS = this.baseMapper.list(queryDto);
        Integer count = this.baseMapper.listCount(queryDto);
        return ApiResult.success(houseOrderEvaluationsVOS, count);
    }

    /**
     * 预约看房评价新增
     *
     * @param houseOrderEvaluations 实体
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(HouseOrderEvaluations houseOrderEvaluations) {
        judge(houseOrderEvaluations);
        // 用户只能针对订单完成一次评论，不能重复评论
        HouseOrderEvaluationsQueryDto queryDto = new HouseOrderEvaluationsQueryDto();
        queryDto.setUserId(LocalThreadHolder.getUserId());
        queryDto.setHouseOrderInfoId(houseOrderEvaluations.getHouseOrderInfoId());
        Integer count = this.baseMapper.listCount(queryDto);
        AssertUtils.isFalse(count > 0, "不能重复评论");
        houseOrderEvaluations.setCreateTime(LocalDateTime.now()); // 设置时间
        houseOrderEvaluations.setUserId(LocalThreadHolder.getUserId()); // 设置操作者用户ID
        save(houseOrderEvaluations);
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
        AssertUtils.notNull(houseId,"房源ID不能为空");
        HouseOrderInfoQueryDto houseOrderInfoQueryDto = new HouseOrderInfoQueryDto();
        houseOrderInfoQueryDto.setHouseId(houseId);
        List<Integer> ids = houseOrderInfoMapper.getIdsByHouseId(houseId);
        if (ids.isEmpty()) { // 房屋下面连预约看房订单都没有，更谈不上产生的预约看房订单服务评价
            return ApiResult.success(new ArrayList<>());
        }
        // 构造服务评价查询条件类，将查回来的预约看房订单的ID列表设置为查询条件
        HouseOrderEvaluationsQueryDto houseOrderEvaluationsQueryDto = new HouseOrderEvaluationsQueryDto();
        houseOrderEvaluationsQueryDto.setHouseOrderInfoIds(ids);
        List<HouseOrderEvaluationsVO> houseOrderEvaluationsVOS = this.baseMapper.list(houseOrderEvaluationsQueryDto);
        return ApiResult.success(houseOrderEvaluationsVOS);
    }
}
