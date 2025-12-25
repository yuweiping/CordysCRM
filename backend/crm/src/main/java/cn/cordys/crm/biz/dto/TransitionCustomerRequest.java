package cn.cordys.crm.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TransitionCustomerRequest {
    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "负责人手机号")
    private String ownerPhone;

    @Schema(description = "线索ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clueId;
}