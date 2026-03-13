package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.mapper.NoticeMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.NoticeQueryDto;
import com.kmbeast.pojo.entity.Notice;
import com.kmbeast.pojo.vo.NoticeListItemVO;
import com.kmbeast.service.NoticeService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告业务逻辑接口实现类
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {


    /**
     * 查询公告列表
     *
     * @param noticeQueryDto 查询条件
     * @return Result<List < NoticeListItemVO>>
     */
    @Override
    public Result<List<NoticeListItemVO>> list(NoticeQueryDto noticeQueryDto) {
        List<NoticeListItemVO> noticeListItemVOS = this.baseMapper.list(noticeQueryDto);
        Integer count = this.baseMapper.listCount(noticeQueryDto);
        return ApiResult.success(noticeListItemVOS, count);
    }

    /**
     * 公告新增
     *
     * @param notice 实体
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(Notice notice) {
        judge(notice);
        notice.setCreateTime(LocalDateTime.now()); // 设置时间
        save(notice);
        return ApiResult.success();
    }

    /**
     * 通过ID查询公告
     *
     * @param id 房屋ID
     * @return Result<Notice>
     */
    @Override
    public Result<Notice> selectById(Integer id) {
        AssertUtils.notNull(id, "ID不能为空");
        Notice notice = getById(id);
        return ApiResult.success(notice);
    }

    @Override
    public Result<String> updateEntity(Notice notice) {
        judge(notice);
        updateById(notice);
        return ApiResult.success();
    }

    private void judge(Notice notice) {
        AssertUtils.hasText(notice.getTitle(), "标题不能为空");
        AssertUtils.isTrue(notice.getTitle().length() < 30, "标题最多30个字");

    }


}
