package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;
    
    @TableField(exist = false)
    private String studentNo;
    
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String className;

    private Long courseArrangementId;
    
    @TableField(exist = false)
    private String courseName;
    
    @TableField(exist = false)
    private String teacherName;
    
    @TableField(exist = false)
    private String semester;
    
    private BigDecimal usualScore;
    
    private BigDecimal midtermScore;
    
    private BigDecimal finalScore;
    
    private BigDecimal totalScore;
    
    private BigDecimal gpa;
    
    private LocalDateTime examTime;
    
    private Status status;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public enum Status {
        NORMAL, MAKEUP, RETAKE
    }
    
    public void calculateTotalScore() {
        if (usualScore != null && midtermScore != null && finalScore != null) {
            this.totalScore = usualScore.multiply(new BigDecimal("0.2"))
                    .add(midtermScore.multiply(new BigDecimal("0.3")))
                    .add(finalScore.multiply(new BigDecimal("0.5")));
            
            // 计算GPA
            if (totalScore.compareTo(new BigDecimal("90")) >= 0) {
                this.gpa = new BigDecimal("4.0");
            } else if (totalScore.compareTo(new BigDecimal("85")) >= 0) {
                this.gpa = new BigDecimal("3.7");
            } else if (totalScore.compareTo(new BigDecimal("82")) >= 0) {
                this.gpa = new BigDecimal("3.3");
            } else if (totalScore.compareTo(new BigDecimal("78")) >= 0) {
                this.gpa = new BigDecimal("3.0");
            } else if (totalScore.compareTo(new BigDecimal("75")) >= 0) {
                this.gpa = new BigDecimal("2.7");
            } else if (totalScore.compareTo(new BigDecimal("72")) >= 0) {
                this.gpa = new BigDecimal("2.3");
            } else if (totalScore.compareTo(new BigDecimal("68")) >= 0) {
                this.gpa = new BigDecimal("2.0");
            } else if (totalScore.compareTo(new BigDecimal("64")) >= 0) {
                this.gpa = new BigDecimal("1.5");
            } else if (totalScore.compareTo(new BigDecimal("60")) >= 0) {
                this.gpa = new BigDecimal("1.0");
            } else {
                this.gpa = new BigDecimal("0.0");
            }
        }
    }
}
