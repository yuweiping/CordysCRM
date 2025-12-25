package cn.cordys.crm.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TransitionCustomerResponse {
    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "类型：CLUE(线索)或CUSTOMER(客户)")
    private String type;

    @Schema(description = "对应类型的ID")
    private String targetId;
}