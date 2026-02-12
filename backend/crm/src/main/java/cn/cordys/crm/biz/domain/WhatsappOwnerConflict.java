package cn.cordys.crm.biz.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WhatsappOwnerConflict extends BaseModel {
    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "当前负责人手机号")
    private String ownerPhone;

    @Schema(description = "冲突负责人手机号")
    private String conflictOwnerPhone;

    @Schema(description = "冲突时间")
    private LocalDateTime conflictTime;

    @Schema(description = "冲突状态: 0=待处理, 1=已处理")
    private Byte status;
}
