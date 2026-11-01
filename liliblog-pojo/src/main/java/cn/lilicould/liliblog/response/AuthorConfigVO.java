package cn.lilicould.liliblog.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * “关于作者”配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "关于作者配置")
public class AuthorConfigVO implements Serializable {

    @Schema(description = "作者资料")
    private Profile profile;

    @Schema(description = "社交链接列表")
    private List<SocialLink> socialLinks;

    @Schema(description = "兴趣分组列表")
    private List<InfoGroup> infoGroups;

    /**
     * 作者资料
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "作者资料")
    public static class Profile implements Serializable {

        @Schema(description = "头像地址")
        private String avatar;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "账号标识，如 LiliCould")
        private String handle;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "签名")
        private String signature;

        @Schema(description = "角标文案，如“关于作者”")
        private String badge;
    }

    /**
     * 社交链接
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "社交链接")
    public static class SocialLink implements Serializable {

        @Schema(description = "链接标识")
        private String key;

        @Schema(description = "展示名称")
        private String name;

        @Schema(description = "图标标识：github / bilibili / music / mail")
        private String icon;

        @Schema(description = "跳转地址")
        private String url;

        @Schema(description = "是否启用")
        private Boolean enabled;
    }

    /**
     * 兴趣分组
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "兴趣分组")
    public static class InfoGroup implements Serializable {

        @Schema(description = "分组标识")
        private String key;

        @Schema(description = "分组标题，如“喜欢的动漫”")
        private String text;

        @Schema(description = "分组图标")
        private String emoji;

        @Schema(description = "分组内容")
        private List<String> items;
    }
}
