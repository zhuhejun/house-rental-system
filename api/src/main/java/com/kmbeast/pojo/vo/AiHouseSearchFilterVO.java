package com.kmbeast.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 找房解析结果
 */
@Data
public class AiHouseSearchFilterVO {

    private Integer areaId;

    private String city;

    private String district;

    private BigDecimal minRent;

    private BigDecimal maxRent;

    private Double minSizeNumber;

    private Double maxSizeNumber;

    private Integer sizedId;

    private String sizedName;

    private Integer rentalType;

    private String rentalTypeName;

    private Integer directionId;

    private String directionName;

    private Integer fitmentStatusId;

    private String fitmentStatusName;

    private Boolean isSubway;

    private List<String> facilities;
}
