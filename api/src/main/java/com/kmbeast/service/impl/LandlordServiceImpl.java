package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.LandlordMapper;
import com.kmbeast.mapper.UserMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.LandlordQueryDto;
import com.kmbeast.pojo.em.IsAuditEnum;
import com.kmbeast.pojo.em.RoleEnum;
import com.kmbeast.pojo.entity.Landlord;
import com.kmbeast.pojo.entity.User;
import com.kmbeast.pojo.vo.LandlordVO;
import com.kmbeast.service.LandlordService;
import com.kmbeast.utils.AssertUtils;
import com.kmbeast.utils.IdCardValidator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房东业务逻辑接口实现类
 */
@Service
public class LandlordServiceImpl extends ServiceImpl<LandlordMapper, Landlord> implements LandlordService {

    @Resource
    private UserMapper userMapper;

    /**
     * 查询房东信息列表
     *
     * @param landlordQueryDto 查询条件类
     * @return Result<List < LandlordVO>>
     */
    @Override
    public Result<List<LandlordVO>> list(LandlordQueryDto landlordQueryDto) {
        List<LandlordVO> landlordVOS = this.baseMapper.list(landlordQueryDto);
        Integer count = this.baseMapper.listCount(landlordQueryDto);
        return ApiResult.success(landlordVOS, count);
    }

    /**
     * 修改信息
     *
     * @param landlord 实体信息
     * @return Result<String>
     */
    @Override
    public Result<String> update(Landlord landlord) {
        // 只能是管理员进行房东信息的审核
        if (landlord.getIsAudit()) {
            User user = userMapper.getUserById(LocalThreadHolder.getUserId());// 通过用户ID查询用户信息
            AssertUtils.equals(user.getRole(), RoleEnum.ADMIN.getRole(), "无操作权限");
        }
        updateById(landlord);
        return ApiResult.success("修改成功");
    }

    /**
     * 申请成为房东
     *
     * @param landlord 实体信息
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(Landlord landlord) {
        // 1. 参数校验
        AssertUtils.hasText(landlord.getIdcardFront(), "请上传身份证正面照");
        AssertUtils.hasText(landlord.getIdcardOpposite(), "请上传身份证反面照");
        AssertUtils.hasText(landlord.getIdcard(), "请上传身份证号");
        // 校验身份证号的合法性
        // AssertUtils.isTrue(IdCardValidator.validate(landlord.getIdcard()), "请输入合法的身份证号");
        // 2. 一个人只能有一条房东申请记录
        LandlordQueryDto landlordQueryDto = new LandlordQueryDto();
        landlordQueryDto.setUserId(LocalThreadHolder.getUserId());
        Integer count = this.baseMapper.listCount(landlordQueryDto);
        AssertUtils.isTrue(count == 0, "请勿重复申请");
        // 3. 补充初始数据
        landlord.setUserId(LocalThreadHolder.getUserId()); // 设置申请人用户ID
        landlord.setCreateTime(LocalDateTime.now()); // 设置申请时间
        landlord.setIsAudit(IsAuditEnum.STATUS_2.getType()); // 初始设置为未审核状态
        save(landlord);
        return ApiResult.success("申请成功，请等待审核");
    }

    /**
     * 查询用户自己的申请记录
     *
     * @param landlordQueryDto 查询条件类
     * @return Result<LandlordVO>
     */
    @Override
    public Result<LandlordVO> listUser(LandlordQueryDto landlordQueryDto) {
        landlordQueryDto.setUserId(LocalThreadHolder.getUserId()); // 设置上用户ID，数据隔离，经过这样处理，用户只能查看到自己名下的房东申请记录
        List<LandlordVO> landlordVOS = this.baseMapper.list(landlordQueryDto);
        if (landlordVOS.isEmpty()) {
            return ApiResult.success();
        }
        LandlordVO landlordVO = landlordVOS.get(0);
        String idcard = landlordVO.getIdcard();
        // 身份证号脱敏处理
        String idcardCode = idcard.substring(0, 6) + "****" + idcard.substring(idcard.length() - 2);
        landlordVO.setIdcard(idcardCode);
        return ApiResult.success(landlordVO);
    }

}
