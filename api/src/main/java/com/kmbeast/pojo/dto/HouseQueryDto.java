package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房屋查询条件类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HouseQueryDto extends QueryDto {
    /**
     * 房屋名
     */
    private String name;
    /**
     * 房东ID，外键，关联的是房东信息表，标识是哪个房东发布的房屋信息
     */
    private Integer landlordId;
    /**
     * 地区ID，外键，关联的是地区信息表，标识房屋信息从属于哪个地区
     */
    private Integer areaId;
    /**
     * 小区ID，外键，关联的是小区信息表，标识的是房屋从属于哪个小区
     */
    private Integer communityId;
    /**
     * 房屋类型ID，外键，比如商品房，通过枚举类定义
     */
    private Integer typeId;
    /**
     * 最小面积
     */
    private Double minSizeNumber;
    /**
     * 最大面积
     */
    private Double maxSizeNumber;
    /**
     * 方向ID，外键，比如坐北朝南，通过枚举类定义
     */
    private Integer directionId;
    /**
     * 户型ID，外键，比如一室一厅，通过枚举类定义
     */
    private Integer sizedId;
    /**
     * 最小租金
     */
    private BigDecimal minRent;
    /**
     * 最大租金
     */
    private BigDecimal maxRent;
    /**
     * 押金方式ID，外键，比如押一付一，通过定义枚举类
     */
    private Integer depositMethodId;
    /**
     * 房屋状态（1：待租；2：下架），通过定义枚举类设置
     */
    private Integer status;
    /**
     * 是否临近地铁（0：非临近；1：临近）
     * 0:false;1:true
     */
    private Boolean isSubway;
    /**
     * 装修状态ID，外键，比如毛坯房
     */
    private Integer fitmentStatusId;
    /**
     * 租赁类型（1：整租；2：合租）
     */
    private Integer rentalType;
    /**
     * 房屋ID列表
     */
    private List<Integer> ids;
    /**
     * 行为类型
     */
    private Integer type;
    /**
     * 查询的天数
     */
    private Integer days;
}
