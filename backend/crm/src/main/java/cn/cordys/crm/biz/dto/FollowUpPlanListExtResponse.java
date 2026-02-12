package cn.cordys.crm.biz.dto;

import cn.cordys.common.domain.BaseModuleFieldValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class FollowUpPlanListExtResponse {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "客户id")
    private String customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "计划内容")
    private String content;

    @Schema(description = "负责人")
    private String owner;

    @Schema(description = "负责人名称")
    private String ownerName;

    @Schema(description = "负责人电话")
    private String ownerPhone;

    @Schema(description = "联系人")
    private String contactId;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "计划开始时间")
    private Long estimatedTime;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "电话")
    private String phone;

    @Schema(description = "自定义字段集合")
    private List<BaseModuleFieldValue> moduleFields;

}
