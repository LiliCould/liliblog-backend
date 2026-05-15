package cn.lilicould.liliblog.pojo.dto.response;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "分页对象视图")
public class PageInfo<T> implements Serializable {

    @Schema(description = "当前页码", example = "1")
    private Long current;
    @Schema(description = "每页数量", example = "10")
    private Long size;
    @Schema(description = "总页数")
    private Long totalPage;
    @Schema(description = "总记录数")
    private Long total;
    @Schema(description = "是否有上一页")
    private Boolean hasPrevious;
    @Schema(description = "是否有下一页")
    private Boolean hasNext;
    @Schema(description = "数据列表")
    private List<T> records;

    /**
     * 从 MyBatis Plus Page 对象转换
     */
    public static <T> PageInfo<T> of(Page<T> page) {
        return PageInfo.<T>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .totalPage(page.getPages())
                .total(page.getTotal())
                .hasPrevious(page.hasPrevious())
                .hasNext(page.hasNext())
                .records(page.getRecords())
                .build();
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageInfo<T> empty(long current, long size) {
        return PageInfo.<T>builder()
                .current(current)
                .size(size)
                .total(0L)
                .totalPage(0L)
                .hasPrevious(false)
                .hasNext(false)
                .records(Collections.emptyList())
                .build();
    }
}
