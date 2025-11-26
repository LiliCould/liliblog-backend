package com.lilicould.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 点赞记录实体类
 * @author lilicould
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeRecord {
    private Long id;
    private Long userId;
    private Long targetId;
    private enum targetType {
        ARTICLE,
        COMMENT
    }
    private Date createTime;
}
