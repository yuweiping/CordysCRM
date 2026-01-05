package cn.cordys.crm.biz.dto;

import cn.cordys.crm.follow.dto.request.FollowUpPlanPageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FollowUpPlanPageExtRequest extends FollowUpPlanPageRequest {

    @Schema(description = "所有者手机号")
    private String ownerPhone;

}
