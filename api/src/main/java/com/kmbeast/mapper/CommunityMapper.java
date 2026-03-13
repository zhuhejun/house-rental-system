package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.CommunityQueryDto;
import com.kmbeast.pojo.entity.Community;
import com.kmbeast.pojo.vo.CommunityVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 小区持久化接口
 */
@Mapper
public interface CommunityMapper extends BaseMapper<Community> {

    List<CommunityVO> list(CommunityQueryDto communityQueryDto);

    Integer listCount(CommunityQueryDto communityQueryDto);

}
