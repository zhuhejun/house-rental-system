package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.CommunityQueryDto;
import com.kmbeast.pojo.entity.Community;
import com.kmbeast.pojo.vo.CommunityVO;

import java.util.List;

/**
 * 小区业务逻辑接口
 */
public interface CommunityService extends IService<Community> {

    Result<List<CommunityVO>> list(CommunityQueryDto communityQueryDto);

    Result<String> update(Community community);

    Result<String> saveEntity(Community community);

    Result<CommunityVO> selectById(Integer id);

}
