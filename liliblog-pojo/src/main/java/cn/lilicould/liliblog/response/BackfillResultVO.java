package cn.lilicould.liliblog.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 历史文件回填结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "历史文件回填结果")
public class BackfillResultVO implements Serializable {

    @Schema(description = "是否为预演模式")
    private boolean dryRun;

    @Schema(description = "本次扫描到的对象数")
    private int totalObjects;

    @Schema(description = "新插入的 file_record 数")
    private int insertedRecords;

    @Schema(description = "已存在的 file_record 数")
    private int existingRecords;

    @Schema(description = "新创建的 LEGACY_KEEP 保护引用数")
    private int protectedRefs;

    @Schema(description = "扫描的文章数")
    private int scannedArticles;

    @Schema(description = "建立的文章文件引用数")
    private int articleRefs;

    @Schema(description = "扫描的用户数")
    private int scannedUsers;

    @Schema(description = "建立的头像引用数")
    private int avatarRefs;

    @Schema(description = "URL 找不到文件记录的数量")
    private int missingReferences;

    @Schema(description = "是否还有超过 maxObjects 未处理的对象")
    private boolean hasMore;
}
