package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.NoticeQueryDto;
import com.kmbeast.pojo.entity.Notice;
import com.kmbeast.pojo.vo.NoticeListItemVO;

import java.util.List;

/**
 * 公告业务逻辑接口
 */
public interface NoticeService extends IService<Notice> {

    Result<List<NoticeListItemVO>> list(NoticeQueryDto noticeQueryDto);

    Result<String> saveEntity(Notice notice);

    Result<Notice> selectById(Integer id);

    Result<String> updateEntity(Notice notice);

}
