package cn.lilicould.liliblog.pojo.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "标签查询参数")
public class UserQuery extends BaseQuery implements Serializable {

    // 精准匹配项目
    @Schema(description = "用户ID")
    private Long id;
    @Schema(description = "身份，0-管理员，1-用户")
    private Integer role;
    @Schema(description = " 状态")
    private Integer status;

    // 模糊匹配项目
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "email")
    private String email;

    // 时间范围匹配项目
    @Schema(description = "注册开始时间", type = "string",format = "date-time",example = "2026-05-09 14:51:06")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;
    @Schema(description = "注册结束时间", type = "string",format = "date-time",example = "2026-05-22 14:51:06")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;
    @Schema(description = "上次登录开始时间", type = "string",format = "date-time",example = "2026-05-09 14:51:06")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTimeStart;
    @Schema(description = "上次登录结束时间", type = "string",format = "date-time",example = "2026-05-22 14:51:06")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTimeEnd;
    @Schema(description = "更新开始时间", type = "string",format = "date-time",example = "2026-05-09 14:51:06")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTimeStart;
    @Schema(description = "更新结束时间", type = "string",format = "date-time",example = "2026-05-22 14:51:06")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTimeEnd;
}
