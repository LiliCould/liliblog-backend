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
 * 文章表
 * @TableName article
 */
@TableName(value ="article")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class Article extends FullBaseEntity implements Serializable {
    /**
     * 文章ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 文章别名(用于URL)
     */
    @TableField(value = "slug")
    private String slug;

    /**
     * 文章摘要
     */
    @TableField(value = "summary")
    private String summary;

    /**
     * 文章内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 文章HTML内容
     */
    @TableField(value = "content_html")
    private String contentHtml;

    /**
     * 封面图片URL
     */
    @TableField(value = "cover_image")
    private String coverImage;

    /**
     * 状态,0-审核中,1-发布,2-草稿
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 阅读数
     */
    @TableField(value = "view_count")
    private Integer viewCount;

    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    private Long categoryId;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField(value = "deleted")
    private Integer deleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}