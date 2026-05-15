package cn.lilicould.liliblog.pojo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "文章详情对象")
public class ArticleDetailsVO implements Serializable {
    @Schema(description = "文章ID")
    private Long id;
    @Schema(description = "文章标题")
    private String title;
    @Schema(description = "文章别名(用于URL)")
    private String slug;
    @Schema(description = "文章摘要")
    private String summary;
    @Schema(description = "封面图片URL")
    private String coverImage;
    @Schema(description = "HTML内容")
    private String contentHtml;
    @Schema(description = "阅读数")
    private Integer viewCount;
    @Schema(description = "点赞数") // 联查点赞表
    private Integer likeCount;
    @Schema(description = "评论数") // 联查like_record表
    private Integer commentCount;
    @Schema(description = "分类") // 通过分类ID从分类表联查
    private CategoryVO category;
    @Schema(description = "标签") // 从article_tag表查
    private List<TagVO> tags;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "更新人")
    private UserInfo updater;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "作者")
    private UserInfo creator;   // 创建者
}