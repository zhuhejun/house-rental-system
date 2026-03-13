package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.FlowIndexMapper;
import com.kmbeast.mapper.HouseNewsMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.FlowIndexQueryDto;
import com.kmbeast.pojo.dto.HouseNewsQueryDto;
import com.kmbeast.pojo.entity.HouseNews;
import com.kmbeast.pojo.vo.HouseNewsListVO;
import com.kmbeast.pojo.vo.ScoreVO;
import com.kmbeast.service.HouseNewsService;
import com.kmbeast.utils.AssertUtils;
import com.kmbeast.utils.UserBasedCFUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房屋资讯业务逻辑接口实现类
 */
@Service
public class HouseNewsServiceImpl extends ServiceImpl<HouseNewsMapper, HouseNews> implements HouseNewsService {


    private final FlowIndexMapper flowIndexMapper;

    public HouseNewsServiceImpl(FlowIndexMapper flowIndexMapper) {
        this.flowIndexMapper = flowIndexMapper;
    }

    /**
     * 查询房屋资讯列表
     *
     * @param houseNewsQueryDto 查询条件
     * @return Result<List < HouseNewsListVO>>
     */
    @Override
    public Result<List<HouseNewsListVO>> list(HouseNewsQueryDto houseNewsQueryDto) {
        List<HouseNewsListVO> houseNewsListVOList = this.baseMapper.list(houseNewsQueryDto);
        Integer count = this.baseMapper.listCount(houseNewsQueryDto);
        return ApiResult.success(houseNewsListVOList, count);
    }

    /**
     * 房屋资讯新增
     *
     * @param houseNews 实体
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(HouseNews houseNews) {
        judge(houseNews);
        houseNews.setCreateTime(LocalDateTime.now()); // 设置时间
        save(houseNews);
        return ApiResult.success();
    }

    /**
     * 通过ID查询房屋资讯
     *
     * @param id 房屋ID
     * @return Result<HouseNews>
     */
    @Override
    public Result<HouseNews> selectById(Integer id) {
        AssertUtils.notNull(id, "ID不能为空");
        HouseNews houseNews = getById(id);
        return ApiResult.success(houseNews);
    }

    @Override
    public Result<String> updateEntity(HouseNews houseNews) {
        judge(houseNews);
        updateById(houseNews);
        return ApiResult.success();
    }

    private void judge(HouseNews houseNews) {
        AssertUtils.notNull(houseNews, "实体数据不能为空");
        AssertUtils.hasText(houseNews.getTitle(), "标题不能为空");
        AssertUtils.hasText(houseNews.getCover(), "封面不能为空");
        AssertUtils.hasText(houseNews.getSummary(), "摘要不能为空");
        AssertUtils.isTrue(houseNews.getTitle().length() < 30, "标题最多30个字");
        AssertUtils.isTrue(houseNews.getSummary().length() < 200, "摘要最多200个字");
    }

    /**
     * 房屋资讯推荐
     *
     * @param count 推荐的条数
     * @return Result<List < HouseNewsListVO>> 响应结果
     */
    @Override
    public Result<List<HouseNewsListVO>> recommend(Integer count) {
        AssertUtils.notNull(count, "推荐条数不能为空");
        FlowIndexQueryDto flowIndexQueryDto = new FlowIndexQueryDto();
        flowIndexQueryDto.setContentType("HOUSE_NEWS"); // 做的是房屋资讯的推荐，所以模块设置为房屋资讯模块
        // 获取所有的房屋资讯ID列表
        List<Integer> houseNewsIds = this.baseMapper.getIds();
        if (houseNewsIds.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        // 获取用户对于房屋资讯的评分
        List<ScoreVO> scores = flowIndexMapper.getScores(flowIndexQueryDto);
        if (scores.isEmpty()) {
            List<HouseNewsListVO> houseNewsListVOList = queryHouseNews(houseNewsIds.size() > count ? houseNewsIds.subList(0, count) : houseNewsIds);
            return ApiResult.success(houseNewsListVOList);
        }
        // 构建用户对于物品评分的指定数据结构
        List<UserBasedCFUtil.Score> scoreList = scores.stream().map(score -> new UserBasedCFUtil.Score(
                score.getUserId(),
                score.getContentId(),
                score.getScore()
        )).collect(Collectors.toList());
        // 构建用户对于房屋资讯的评分矩阵
        Map<Integer, Map<Integer, Double>> userItemMatrix =
                UserBasedCFUtil.buildUserItemMatrix(houseNewsIds, scoreList);
        // 创建协同过滤工具实例
        UserBasedCFUtil cfUtil = new UserBasedCFUtil(userItemMatrix);
        // 算出向指定这个用户推荐的房屋资讯
        List<Integer> itemIds = cfUtil.recommendItems(LocalThreadHolder.getUserId(), count);
        // 冷启动：用户是新用户，没有一丁点儿的互动数据，何谈兴趣？何谈推荐？
        if (itemIds.isEmpty()) {
            // 如果最初查出来的房屋资讯的ID列表，比咱们接口需要的更多，则截取，反之直接用
            List<HouseNewsListVO> houseNewsListVOList = queryHouseNews(houseNewsIds.size() > count ? houseNewsIds.subList(0, count) : houseNewsIds);
            return ApiResult.success(houseNewsListVOList);
        }
        List<HouseNewsListVO> houseNewsListVOList = queryHouseNews(itemIds);
        return ApiResult.success(houseNewsListVOList);
    }

    /**
     * 通过房屋资讯的ID列表查询房屋资讯
     *
     * @param ids 房屋资讯ID列表
     * @return List<HouseNewsListVO>
     */
    private List<HouseNewsListVO> queryHouseNews(List<Integer> ids) {
        HouseNewsQueryDto houseNewsQueryDto = new HouseNewsQueryDto();
        houseNewsQueryDto.setIds(ids);
        return this.baseMapper.list(houseNewsQueryDto);
    }

}
