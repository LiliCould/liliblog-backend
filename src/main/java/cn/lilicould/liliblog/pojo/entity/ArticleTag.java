package cn.lilicould.liliblog.pojo.entity;

import cn.lilicould.liliblog.pojo.entity.base.CreateOnlyEntity;
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
 * 文章标签关联表
 * @TableName article_tag
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@TableName(value ="article_tag")
public class ArticleTag extends CreateOnlyEntity implements Serializable {
    /**
     * 关联ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID
     */
    @TableField(value = "article_id")
    private Long articleId;

    /**
     * 标签ID
     */
    @TableField(value = "tag_id")
    private Long tagId;


    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}