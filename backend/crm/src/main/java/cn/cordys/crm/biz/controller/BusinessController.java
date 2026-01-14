package cn.cordys.crm.biz.controller;

import cn.cordys.common.domain.BaseModuleFieldValue;
import cn.cordys.common.response.handler.ResultHolder;
import cn.cordys.common.service.BaseService;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.biz.domain.WhatsappSyncRecord;
import cn.cordys.crm.biz.dto.*;
import cn.cordys.crm.biz.mapper.WhatsappSyncRecordMapper;
import cn.cordys.crm.biz.service.BusinessService;
import cn.cordys.crm.biz.service.WhatsappSyncService;
import cn.cordys.crm.clue.service.ClueService;
import cn.cordys.crm.customer.domain.CustomerContact;
import cn.cordys.crm.customer.dto.request.ClueTransformRequest;
import cn.cordys.crm.follow.domain.FollowUpPlan;
import cn.cordys.crm.follow.service.FollowUpPlanFieldService;
import cn.cordys.crm.follow.service.FollowUpPlanService;
import cn.cordys.crm.system.service.UserLoginService;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import cn.cordys.security.SessionUser;
import cn.cordys.security.SessionUtils;
import cn.cordys.security.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private WhatsappSyncService whatsappSyncService;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private FollowUpPlanService followUpPlanService;
    @Resource
    private BaseMapper<CustomerContact> customerContactMapper;
    @Resource
    private BaseMapper<FollowUpPlan> followUpPlanMapper;
    @Resource
    private BaseService baseService;
    @Resource
    private FollowUpPlanFieldService followUpPlanFieldService;

    @GetMapping("/contact/by-phone")
    @Operation(summary = "根据用户手机号查询要跟踪d线索信息")
    public List<ClueByPhoneResponse> getClueByUserPhone(
            @Parameter(description = "用户手机号", required = true) @RequestParam String phone) {
        return businessService.getClueByUserPhone(phone);
    }


    @PostMapping("/contact/sync-contacts")
    @Operation(summary = "同步WhatsApp联系人")
    public ResultHolder syncWhatsappContacts(
            @Parameter(description = "同步联系人请求参数", required = true) @RequestBody SyncContactsRequest request) {
        try {
            // 登录用户并获取用户ID
            UserDTO userDTO = login(request.getPhone());
            // 调用服务方法获取同步结果
            Object result = whatsappSyncService.syncContacts(request, userDTO);
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
            // 登录用户并获取用户ID
            UserDTO userDTO = login(request.getOwnerPhone());
            String userId = userDTO.getId();
            ClueTransformRequest clueTransformRequest = new ClueTransformRequest();
            clueTransformRequest.setClueId(request.getClueId());
            clueTransformRequest.setOppCreated(false);
            // 调用ClueService的transitionCustomer方法
            String customerId = clueService.transform(clueTransformRequest, userId, userDTO.getLastOrganizationId());

            // 查询对应的WhatsApp同步记录
            WhatsappSyncRecord record = whatsappSyncRecordMapper.selectByOwnerAndContact(request.getOwnerPhone(), request.getContactPhone());
            if (record != null) {
                // 更新记录的type为CUSTOMER，targetId为转换后的客户ID
                record.setType("CUSTOMER");
                record.setTargetId(customerId);
                // 更新记录
                whatsappSyncRecordMapper.update(record);
            }

            return ResultHolder.success(record);
        } catch (RuntimeException e) {
            return ResultHolder.error("线索转换为客户失败", e.getMessage());
        }
    }

    @PostMapping("/follow/plan/add")
    @Operation(summary = "添加客户跟进计划")
    public FollowUpPlan add(@Validated @RequestBody FollowUpPlanAddExtRequest request) {
        // 登录用户并获取用户ID
        UserDTO userDTO = login(request.getOwnerPhone());
//        根据联系人手机号来查询联系人id
        if (request.getContactPhone() != null) {
            LambdaQueryWrapper<CustomerContact> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerContact::getPhone, request.getContactPhone());
            List<CustomerContact> contacts = customerContactMapper.selectListByLambda(wrapper);
            if (!contacts.isEmpty()) {
                request.setContactId(contacts.get(0).getId());
            }
        }
        return followUpPlanService.add(request, userDTO.getId(), OrganizationContext.getOrganizationId());
    }

    @GetMapping("/follow/plan/list")
    @Operation(summary = "客户跟进计划列表")
    public List<FollowUpPlanListExtResponse> list(@RequestParam String ownerPhone) {
        // 根据ownerPhone登录获取用户信息
        UserDTO userDTO = login(ownerPhone);
        String userId = userDTO.getId();

        // 使用BaseMapper根据userId查询FollowUpPlan数据
        LambdaQueryWrapper<FollowUpPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUpPlan::getOwner, userId);

        wrapper.eq(FollowUpPlan::getStatus, "PREPARED");
        // 添加过滤条件：estimatedTime大于当前时间
        wrapper.gt(FollowUpPlan::getEstimatedTime, System.currentTimeMillis());

        List<FollowUpPlan> followUpPlans = followUpPlanMapper.selectListByLambda(wrapper);
        List<String> contactIds = followUpPlans.stream().map(FollowUpPlan::getContactId).toList();
        Map<String, String> contactPhoneMap = baseService.getContactPhone(contactIds);
        List<String> ids = followUpPlans.stream().map(FollowUpPlan::getId).toList();
        Map<String, List<BaseModuleFieldValue>> resourceFieldMap = followUpPlanFieldService.getResourceFieldMap(ids, true);

        // 转换为FollowUpPlanListResponse列表
        List<FollowUpPlanListExtResponse> list = followUpPlans.stream().map(plan -> {
            FollowUpPlanListExtResponse response = new FollowUpPlanListExtResponse();
            // 获取自定义字段
            List<BaseModuleFieldValue> planCustomerFields = resourceFieldMap.get(plan.getId());
            response.setModuleFields(planCustomerFields);
            // 复制基础字段
            response.setId(plan.getId());
            response.setCustomerId(plan.getCustomerId());
            response.setContent(plan.getContent()); // 确保content字段被正确设置
            response.setOwner(plan.getOwner());
            response.setContactId(plan.getContactId()); // 确保contactId字段被正确设置，以便后续获取phone
            response.setEstimatedTime(plan.getEstimatedTime()); // 确保estimatedTime字段被正确设置
            response.setStatus(plan.getStatus());
            response.setOwnerPhone(ownerPhone);
            response.setPhone(contactPhoneMap.get(plan.getContactId()));
            return response;
        }).toList();

        return list;
    }

    @GetMapping("/follow/plan/delete/{id}")
    @Operation(summary = "删除跟进计划")
    public void delete(@PathVariable String id) {
        followUpPlanService.delete(id);
    }

    private UserDTO login(String ownerPhone) {
        // 根据手机号获取用户ID
        UserDTO userDTO = userLoginService.authenticateUser(ownerPhone);
        String sessionId = SessionUtils.getSessionId();
        SessionUser sessionUser = SessionUser.fromUser(userDTO, sessionId);

// 3. 将用户信息放入session
        SessionUtils.putUser(sessionUser);
        return userDTO;
    }
}