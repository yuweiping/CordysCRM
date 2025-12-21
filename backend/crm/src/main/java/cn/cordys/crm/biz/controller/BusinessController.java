package cn.cordys.crm.biz.controller;

import cn.cordys.crm.biz.dto.ContactByPhoneResponse;
import cn.cordys.crm.biz.service.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}