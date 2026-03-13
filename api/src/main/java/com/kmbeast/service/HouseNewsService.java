package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseNewsQueryDto;
import com.kmbeast.pojo.entity.HouseNews;
import com.kmbeast.pojo.vo.HouseNewsListVO;

import java.util.List;

/**
 * 房屋资讯业务逻辑接口
 */
public interface HouseNewsService extends IService<HouseNews> {

    Result<List<HouseNewsListVO>> list(HouseNewsQueryDto houseNewsQueryDto);

    Result<String> saveEntity(HouseNews houseNews);

    Result<HouseNews> selectById(Integer id);

    Result<String> updateEntity(HouseNews houseNews);

    Result<List<HouseNewsListVO>> recommend(Integer count);
}
