package cn.cordys.crm.biz.controller;

import cn.cordys.common.response.handler.ResultHolder;
import cn.cordys.crm.biz.dto.ContactByPhoneResponse;
import cn.cordys.crm.biz.dto.SyncContactsRequest;
import cn.cordys.crm.biz.service.BusinessService;
import cn.cordys.crm.biz.service.WhatsappSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户端")
@RestController
@RequestMapping("/biz")
public class BusinessController {

    @Resource
    private BusinessService businessService;

    @GetMapping("/contact/by-phone")
    @Operation(summary = "根据用户手机号查询联系人信息")
    public List<ContactByPhoneResponse> getContactsByUserPhone(
            @Parameter(description = "用户手机号", required = true) @RequestParam String phone) {
        return businessService.getContactsByUserPhone(phone);
    }

    @Resource
    private WhatsappSyncService whatsappSyncService;

    @PostMapping("/contact/sync-contacts")
    @Operation(summary = "同步WhatsApp联系人")
    public ResultHolder syncWhatsappContacts(
            @Parameter(description = "同步联系人请求参数", required = true) @RequestBody SyncContactsRequest request) {
        try {
            whatsappSyncService.syncContacts(request);
        } catch (RuntimeException e) {
            return ResultHolder.error("同步WhatsApp联系人失败", e.getMessage());
        }
        return ResultHolder.success("同步WhatsApp联系人成功");
    }
}