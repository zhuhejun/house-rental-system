package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.AreaQueryDto;
import com.kmbeast.pojo.dto.LandlordQueryDto;
import com.kmbeast.pojo.entity.Area;
import com.kmbeast.pojo.entity.Landlord;
import com.kmbeast.pojo.vo.LandlordVO;

import java.util.List;

/**
 * 房东业务逻辑接口
 */
public interface LandlordService extends IService<Landlord> {

    Result<List<LandlordVO>> list(LandlordQueryDto landlordQueryDto);

    Result<String> update(Landlord landlord);

    Result<String> saveEntity(Landlord landlord);

    Result<LandlordVO> listUser(LandlordQueryDto landlordQueryDto);

}
