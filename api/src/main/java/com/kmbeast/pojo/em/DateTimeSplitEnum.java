package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约看房时间段枚举
 */
@Getter
@AllArgsConstructor
public enum DateTimeSplitEnum {

    SPLIT_1(1, "9:00-10:00"),
    SPLIT_2(2, "10:00-11:00"),
    SPLIT_3(3, "11:00-12:00"),
    SPLIT_4(4, "15:00-16:00"),
    SPLIT_5(5, "16:00-17:00"),
    SPLIT_6(6, "17:00-18:00");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String detail;

    /**
     * 通过状态获取状态描述
     *
     * @param type 状态
     * @return String
     */
    public static String getDetail(Integer type) {
        for (DateTimeSplitEnum value : DateTimeSplitEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }


    /**
     * 检查时间段是否已过期（相对于当前时间）
     *
     * @param slotTimeSplit    时间段编码
     * @param bookingDate 预约日期
     * @return true=可用，false=已过期
     */
    public static boolean isTimeSlotValid(String slotTimeSplit, LocalDate bookingDate) {

        LocalDateTime now = LocalDateTime.now();

        // 如果预约日期不是今天，直接返回可用
        if (!bookingDate.isEqual(now.toLocalDate())) {
            return true;
        }

        // 解析时间段的结束时间
        String endTimeStr = slotTimeSplit.split("-")[1];
        LocalTime endTime = LocalTime.parse(endTimeStr);

        // 比较当前时间是否已超过时间段结束时间
        return !now.toLocalTime().isAfter(endTime);
    }

}
