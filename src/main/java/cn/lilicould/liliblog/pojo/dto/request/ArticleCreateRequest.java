package cn.lilicould.liliblog.pojo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "文章保存参数")
public class ArticleCreateRequest implements Serializable {
    @NotBlank(message = "文章标题不能为空")
    @Schema(description = "文章标题",example = "关于AOP的使用")
    private String title;

    @NotBlank(message = "文章别名不能为空")
    @Schema(description = "文章别名(用于URL)",example = "the-use-method-of-aop")
    private String slug;

    @Schema(description = "文章摘要")
    @NotBlank(message = "文章摘要不能为空")
    private String summary;

    @Schema(description = "封面图片URL")
    private String coverImage;

    @Schema(description = "文章状态，0-审核中，1-发布（实际仍会被系统置为审核中）,2-草稿")
    @NotNull(message = "文章状态不能为空")
    private Integer status;

    @NotBlank(message = "文章内容不能为空")
    @Schema(description = "markdown内容")
    private String content;

    @NotNull(message = "分类ID不能为空")
    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "标签ID列表", example = "[1, 2, 3]")
    private List<Long> tags;
}
