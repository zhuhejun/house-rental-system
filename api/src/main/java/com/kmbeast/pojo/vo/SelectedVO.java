package com.kmbeast.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选择器VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectedVO {
    private Integer value;
    private String label;
}
