package cn.cordys.crm.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SyncContactsRequest {
    @Schema(description = "同步发起者的手机号")
    private String phone;

    @Schema(description = "联系人列表")
    private List<ContactItem> contacts;

    @Data
    public static class ContactItem {
        @Schema(description = "联系人电话")
        private String contactPhone;

        @Schema(description = "最后互动日期")
        private String date;
    }
}
