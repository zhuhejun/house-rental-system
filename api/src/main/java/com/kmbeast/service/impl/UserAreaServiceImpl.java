package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.UserAreaMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.UserAreaQueryDto;
import com.kmbeast.pojo.entity.UserArea;
import com.kmbeast.pojo.vo.UserAreaVO;
import com.kmbeast.service.UserAreaService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户常居住地业务逻辑接口实现类
 */
@Service
public class UserAreaServiceImpl extends ServiceImpl<UserAreaMapper, UserArea> implements UserAreaService {

    /**
     * 用户常居住地查询
     *
     * @param userAreaQueryDto 用户常居住地查询条件
     * @return Result<List < UserArea>>
     */
    @Override
    public Result<List<UserArea>> list(UserAreaQueryDto userAreaQueryDto) {
        List<UserArea> list = this.baseMapper.list(userAreaQueryDto);
        Integer count = this.baseMapper.listCount(userAreaQueryDto);
        return ApiResult.success(list, count);
    }

    /**
     * 常居住地修改
     *
     * @param userArea 实体信息
     * @return Result<String>
     */
    @Override
    public Result<String> update(UserArea userArea) {
        updateById(userArea);
        return ApiResult.success("常居住地绑定成功");
    }

    /**
     * 常居住地新增
     *
     * @param userArea 实体信息
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(UserArea userArea) {
        UserAreaQueryDto UserAreaQueryDto = new UserAreaQueryDto();
        UserAreaQueryDto.setUserId(LocalThreadHolder.getUserId());
        Integer count = this.baseMapper.listCount(UserAreaQueryDto);
        AssertUtils.isTrue(count == 0, "请不要重复新增常居住地");
        userArea.setUserId(LocalThreadHolder.getUserId()); // 设置用户ID
        save(userArea);
        return ApiResult.success("常居住地新增成功");
    }

    /**
     * 查询用户自己的常居住地
     * @param userAreaQueryDto 查询条件类
     * @return Result<List<UserAreaVO>>
     */
    @Override
    public Result<List<UserAreaVO>> listUser(UserAreaQueryDto userAreaQueryDto) {
        List<UserAreaVO> list = this.baseMapper.listUser(userAreaQueryDto);
        return ApiResult.success(list);
    }
}
