package cn.cordys.crm.biz.dto;

import cn.cordys.crm.follow.dto.request.FollowUpPlanAddRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FollowUpPlanAddExtRequest extends FollowUpPlanAddRequest {
    @Schema(description = "所有者手机号")
    private String ownerPhone;
    @Schema(description = "联系人手机号")
    private String contactPhone;
}
