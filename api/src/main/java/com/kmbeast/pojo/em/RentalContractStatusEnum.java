package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 租赁合同状态枚举
 */
@Getter
@AllArgsConstructor
public enum RentalContractStatusEnum {

    STATUS_1(1, "待管理员审核"),
    STATUS_2(2, "待租客确认"),
    STATUS_3(3, "待支付押金"),
    STATUS_4(4, "已生效"),
    STATUS_5(5, "已驳回"),
    STATUS_6(6, "已拒绝"),
    STATUS_7(7, "已取消"),
    STATUS_8(8, "已到期"),
    STATUS_9(9, "退租申请中"),
    STATUS_10(10, "待退租审核"),
    STATUS_11(11, "待退押"),
    STATUS_12(12, "待审核退押"),
    STATUS_13(13, "已退租");

    private final Integer type;

    private final String detail;

    public static String getDetail(Integer type) {
        for (RentalContractStatusEnum value : RentalContractStatusEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }
}
