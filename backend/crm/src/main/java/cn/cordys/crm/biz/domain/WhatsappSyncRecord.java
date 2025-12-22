package cn.cordys.crm.biz.domain;

import cn.cordys.common.domain.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table(name = "biz_whatsapp_sync_record")
public class WhatsappSyncRecord extends BaseModel {
    @Schema(description = "同步发起者的手机号")
    private String ownerPhone;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "最后互动日期")
    private LocalDate interactDate;

    @Schema(description = "同步时间")
    private LocalDateTime syncTime;

}