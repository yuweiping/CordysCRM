package cn.cordys.crm.biz.controller;

import cn.cordys.common.response.handler.ResultHolder;
import cn.cordys.crm.biz.domain.WhatsappSyncRecord;
import cn.cordys.crm.biz.dto.ContactByPhoneResponse;
import cn.cordys.crm.biz.dto.SyncContactsRequest;
import cn.cordys.crm.biz.dto.TransitionCustomerRequest;
import cn.cordys.crm.biz.dto.TransitionCustomerResponse;
import cn.cordys.crm.biz.mapper.WhatsappSyncRecordMapper;
import cn.cordys.crm.biz.service.BusinessService;
import cn.cordys.crm.biz.service.WhatsappSyncService;
import cn.cordys.crm.clue.service.ClueService;
import cn.cordys.crm.customer.dto.request.ClueTransformRequest;
import cn.cordys.crm.system.service.UserLoginService;
import cn.cordys.security.SessionUser;
import cn.cordys.security.SessionUtils;
import cn.cordys.security.UserDTO;
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
    @Resource
    private ClueService clueService;
    @Resource
    private WhatsappSyncRecordMapper whatsappSyncRecordMapper;
    @Resource
    private UserLoginService userLoginService;

    @GetMapping("/contact/by-phone")
    @Operation(summary = "根据用户手机号查询要跟踪d线索信息")
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
            // 调用服务方法获取同步结果
            Object result = whatsappSyncService.syncContacts(request);
            // 将结果封装在ResultHolder中返回
            return ResultHolder.success(result);
        } catch (RuntimeException e) {
            return ResultHolder.error("同步WhatsApp联系人失败", e.getMessage());
        }
    }

    @PostMapping("/clue/transition-customer")
    @Operation(summary = "线索转换为客户")
    public ResultHolder transitionClueToCustomer(@RequestBody TransitionCustomerRequest request) {
        try {
            // 根据手机号获取用户ID
            UserDTO userDTO = userLoginService.authenticateUser(request.getOwnerPhone());
            String sessionId = SessionUtils.getSessionId();
            SessionUser sessionUser = SessionUser.fromUser(userDTO, sessionId);

// 3. 将用户信息放入session
            SessionUtils.putUser(sessionUser);
            if (userDTO == null) {
                return ResultHolder.error(-1, "根据手机号未找到用户");
            }
            String userId = userDTO.getId();
            ClueTransformRequest clueTransformRequest = new ClueTransformRequest();
            clueTransformRequest.setClueId(request.getClueId());
            clueTransformRequest.setOppCreated(false);
            // 调用ClueService的transitionCustomer方法
            String customerId = clueService.transform(clueTransformRequest, userId, userDTO.getLastOrganizationId());

            // 查询对应的WhatsApp同步记录
            WhatsappSyncRecord record = whatsappSyncRecordMapper.selectByOwnerAndContact(request.getOwnerPhone(), request.getContactPhone());
            if (record != null) {
                // 更新记录的type为CONTACT，targetId为转换后的客户ID
                record.setType("CONTACT");
                record.setTargetId(customerId);
                // 更新记录
                whatsappSyncRecordMapper.update(record);
            }

            // 创建返回结果
            TransitionCustomerResponse response = new TransitionCustomerResponse();
            response.setContactPhone(request.getContactPhone());
            response.setType("CONTACT");
            response.setTargetId(customerId); // 这里应该是转换后的客户ID

            return ResultHolder.success(response);
        } catch (RuntimeException e) {
            return ResultHolder.error("线索转换为客户失败", e.getMessage());
        }
    }
}
