package cn.lilicould.entity;

import cn.lilicould.entity.base.CreateOnlyEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 操作审计日志表
 * @TableName audit_log
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="audit_log")
@Data
public class AuditLog extends CreateOnlyEntity implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 操作用户名
     */
    @TableField(value = "username")
    private String username;
    /**
     * 模块名称(article/comment/user/category/tag)
     */
    @TableField(value = "module")
    private String module;
    /**
     * 操作类型(CREATE/UPDATE/DELETE/AUDIT)
     */
    @TableField(value = "operation")
    private String operation;
    /**
     * 目标资源ID
     */
    @TableField(value = "target")
    private String target;
    /**
     * 目标资源类型
     */
    @TableField(value = "target_type")
    private String targetType;
    /**
     * 操作描述
     */
    @TableField(value = "description")
    private String description;
    /**
     * HTTP方法
     */
    @TableField(value = "request_method")
    private String requestMethod;
    /**
     * 请求URI
     */
    @TableField(value = "request_uri")
    private String requestUri;
    /**
     * IP地址
     */
    @TableField(value = "ip_address")
    private String ipAddress;
    /**
     * 用户代理
     */
    @TableField(value = "user_agent")
    private String userAgent;
    /**
     * 执行时间(ms)
     */
    @TableField(value = "execution_time")
    private Integer executionTime;
    /**
     * 操作状态(1成功/0失败)
     */
    @TableField(value = "status")
    private Integer status;
    /**
     * 错误信息
     */
    @TableField(value = "error_message")
    private String errorMessage;
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}