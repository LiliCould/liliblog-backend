package cn.lilicould.liliblog.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 首页 Hero 区配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "首页Hero配置")
public class HeroConfigVO implements Serializable {

    @Schema(description = "打字机配置")
    private Typewriter typewriter;

    @Schema(description = "轮播图列表")
    private List<Slide> slides;

    /**
     * 打字机配置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "打字机配置")
    public static class Typewriter implements Serializable {

        @Schema(description = "是否启用")
        private Boolean enabled;

        @Schema(description = "文案列表，随机抽取")
        private List<String> phrases;

        @Schema(description = "编程语言模板列表，随机抽取")
        private List<CodeTemplate> templates;
    }

    /**
     * 编程语言模板：最终输出 = prefix + 文案 + suffix
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "编程语言模板")
    public static class CodeTemplate implements Serializable {

        @Schema(description = "模板标识")
        private String key;

        @Schema(description = "展示名称，如 Java")
        private String label;

        @Schema(description = "文案前缀")
        private String prefix;

        @Schema(description = "文案后缀")
        private String suffix;
    }

    /**
     * 轮播图
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "轮播图")
    public static class Slide implements Serializable {

        @Schema(description = "轮播图标识")
        private String key;

        @Schema(description = "图片地址")
        private String src;

        @Schema(description = "图片替代文本")
        private String alt;

        @Schema(description = "是否启用")
        private Boolean enabled;
    }
}
