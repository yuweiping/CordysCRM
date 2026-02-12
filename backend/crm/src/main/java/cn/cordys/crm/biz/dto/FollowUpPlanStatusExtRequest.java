package cn.cordys.crm.biz.dto;

import cn.cordys.crm.follow.dto.request.FollowUpPlanStatusRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FollowUpPlanStatusExtRequest extends FollowUpPlanStatusRequest {
    @Schema(description = "所有者手机号")
    private String ownerPhone;
}
