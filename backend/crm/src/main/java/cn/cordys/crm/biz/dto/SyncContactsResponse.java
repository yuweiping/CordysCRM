package cn.cordys.crm.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * WhatsApp联系人同步响应DTO
 */
@Data
public class SyncContactsResponse {
    @Schema(description = "处理结果列表")
    private List<ContactResult> results;

    @Data
    public static class ContactResult {
        @Schema(description = "联系人电话")
        private String contactPhone;

        @Schema(description = "处理类型：CLUE(线索)或CUSTOMER(客户)")
        private String type;

        @Schema(description = "对应类型的ID")
        private String targetId;

        @Schema(description = "是否成功处理")
        private boolean success;

        @Schema(description = "错误信息（如果有）")
        private String errorMessage;
    }
}
