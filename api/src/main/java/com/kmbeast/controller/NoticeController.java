package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.NoticeQueryDto;
import com.kmbeast.pojo.entity.Notice;
import com.kmbeast.pojo.vo.NoticeListItemVO;
import com.kmbeast.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 新增公告信息
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody Notice notice) {
        return noticeService.saveEntity(notice);
    }


    /**
     * 修改公告信息
     */
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody Notice notice) {
        return noticeService.updateEntity(notice);
    }


    /**
     * 删除公告信息
     */
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        noticeService.removeById(id);
        return ApiResult.success("公告删除成功");
    }

    /**
     * 通过ID查询资讯详情信息
     */
    @GetMapping(value = "/getById/{id}")
    @ResponseBody
    public Result<Notice> getById(@PathVariable Integer id) {
        return noticeService.selectById(id);
    }

    /**
     * 查询公告信息
     *
     * @param noticeQueryDto 查询参数
     * @return Result<List < NoticeListItemVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<NoticeListItemVO>> list(@RequestBody NoticeQueryDto noticeQueryDto) {
        return noticeService.list(noticeQueryDto);
    }

}

