package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.mapper.CommunityMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.CommunityQueryDto;
import com.kmbeast.pojo.entity.Community;
import com.kmbeast.pojo.vo.CommunityVO;
import com.kmbeast.service.CommunityService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 小区业务逻辑接口实现类
 */
@Service
public class CommunityServiceImpl extends ServiceImpl<CommunityMapper, Community> implements CommunityService {

    /**
     * 小区信息查询
     *
     * @param communityQueryDto 查询条件类
     * @return Result<List < CommunityVO>>
     */
    @Override
    public Result<List<CommunityVO>> list(CommunityQueryDto communityQueryDto) {
        List<CommunityVO> communityVOS = this.baseMapper.list(communityQueryDto);
        Integer count = this.baseMapper.listCount(communityQueryDto);
        return ApiResult.success(communityVOS, count);
    }

    /**
     * 小区信息修改
     *
     * @param community 小区信息
     * @return Result<String>
     */
    @Override
    public Result<String> update(Community community) {
        paramJudge(community);
        updateById(community);
        return ApiResult.success("修改成功");
    }

    /**
     * 小区实体信息参数校验
     *
     * @param community 小区信息
     */
    private void paramJudge(Community community) {
        AssertUtils.hasText(community.getName(), "小区名不能为空");
        AssertUtils.hasText(community.getCover(), "小区封面图不能为空");
        AssertUtils.hasText(community.getCovers(), "小区实况图不能为空");
        AssertUtils.notNull(community.getAreaId(), "小区所属地区不能为空");
        AssertUtils.isTrue(community.getName().length() < 30, "小区名不能超过30个字");
    }

    /**
     * 小区新增
     *
     * @param community 小区实体信息
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(Community community) {
        paramJudge(community);
        community.setCreateTime(LocalDateTime.now());
        save(community);
        return ApiResult.success("新增成功");
    }

    /**
     * 通过ID查询小区详情信息
     *
     * @param id 小区ID
     * @return Result<CommunityVO> 响应结果
     */
    @Override
    public Result<CommunityVO> selectById(Integer id) {
        AssertUtils.notNull(id, "小区ID不能为空");
        CommunityQueryDto communityQueryDto = new CommunityQueryDto();
        communityQueryDto.setId(id);
        List<CommunityVO> communityVOS = this.baseMapper.list(communityQueryDto);
        return ApiResult.success(communityVOS.isEmpty() ? new CommunityVO() : communityVOS.get(0));
    }
}
