package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.HouseNewsQueryDto;
import com.kmbeast.pojo.dto.NoticeQueryDto;
import com.kmbeast.pojo.entity.Notice;
import com.kmbeast.pojo.vo.NoticeListItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 公告持久化接口
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    List<NoticeListItemVO> list(NoticeQueryDto noticeQueryDto);

    Integer listCount(NoticeQueryDto noticeQueryDto);

}
