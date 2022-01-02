package org.example.opcDemo.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author PanYi
 */
@Data
public class ExcelData {
    @ExcelProperty(index = 0)
    private String code;
    @ExcelProperty(index = 1)
    private String area;
    @ExcelProperty(index = 2)
    private String room;
    @ExcelProperty(index = 3)
    private String key;
    @ExcelProperty(index = 4)
    private String type;
    private String value;
}
