package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.AreaQueryDto;
import com.kmbeast.pojo.dto.UserAreaQueryDto;
import com.kmbeast.pojo.entity.UserArea;
import com.kmbeast.pojo.vo.UserAreaVO;

import java.util.List;

/**
 * 用户常居住地业务逻辑接口
 */
public interface UserAreaService extends IService<UserArea> {

    Result<List<UserArea>> list(UserAreaQueryDto userAreaQueryDto);

    Result<String> update(UserArea userArea);

    Result<String> saveEntity(UserArea userArea);

    Result<List<UserAreaVO>> listUser(UserAreaQueryDto userAreaQueryDto);


}
