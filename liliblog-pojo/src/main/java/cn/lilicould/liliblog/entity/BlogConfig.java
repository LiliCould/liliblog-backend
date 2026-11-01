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
 * 站点配置表
 * 一行一个配置分组，config_value 以 JSON 存放该分组的完整内容
 *
 * @TableName blog_config
 */
@TableName(value = "blog_config")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class BlogConfig extends FullBaseEntity implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置分组键：hero / author / site ...
     */
    @TableField(value = "config_key")
    private String configKey;

    /**
     * 分组配置内容（JSON 文本）
     * MySQL 的 json 列用 String 承接，读写时由 ObjectMapper 转换
     */
    @TableField(value = "config_value")
    private String configValue;

    /**
     * 是否启用：0-禁用，1-启用
     */
    @TableField(value = "enabled")
    private Integer enabled;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}
