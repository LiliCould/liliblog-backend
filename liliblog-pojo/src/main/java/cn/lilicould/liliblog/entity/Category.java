package cn.lilicould.liliblog.entity;

import cn.lilicould.liliblog.entity.base.FullBaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分类表
 * @TableName category
 */
@TableName(value ="category")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class Category extends FullBaseEntity implements Serializable {
    /**
     * 分类ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 分类别名
     */
    @TableField(value = "slug")
    private String slug;

    /**
     * 分类描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 排序
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField(value = "deleted")
    private Integer deleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}