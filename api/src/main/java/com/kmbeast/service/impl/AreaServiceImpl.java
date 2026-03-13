package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.mapper.AreaMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.AreaQueryDto;
import com.kmbeast.pojo.entity.Area;
import com.kmbeast.service.AreaService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地区业务逻辑接口实现类
 */
@Service
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area> implements AreaService {

    /**
     * 地区查询
     *
     * @param areaQueryDto 地区查询条件
     * @return Result<List < Area>>
     */
    @Override
    public Result<List<Area>> list(AreaQueryDto areaQueryDto) {
        List<Area> list = this.baseMapper.list(areaQueryDto);
        Integer count = this.baseMapper.listCount(areaQueryDto);
        return ApiResult.success(list, count);
    }

    /**
     * 地区修改
     *
     * @param area 实体信息
     * @return Result<String>
     */
    @Override
    public Result<String> update(Area area) {
        parenDeal(area);
        updateById(area);
        return ApiResult.success("地区绑定成功");
    }

    private void parenDeal(Area area) {
        AssertUtils.hasText(area.getName(), "地区名不能为空");
        // 如果这个名字不合理，不能新增
        AreaQueryDto areaQueryDto = new AreaQueryDto();
        areaQueryDto.setName(area.getName());
        Integer count = this.baseMapper.listCount(areaQueryDto);
        AssertUtils.isTrue(count == 0, "地区名不能为空");
    }

    /**
     * 地区新增
     *
     * @param area 实体信息
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(Area area) {
        parenDeal(area);
        save(area);
        return ApiResult.success("地区新增成功");
    }
}
