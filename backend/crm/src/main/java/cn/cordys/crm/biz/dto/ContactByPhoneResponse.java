package cn.cordys.crm.biz.dto;

import cn.cordys.crm.clue.domain.Clue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 根据手机号查询联系人响应DTO
 *
 * @author jianxing
 */
@Data
public class ContactByPhoneResponse extends Clue {

    @Schema(description = "客户分类")
    private String category;

    @Schema(description = "客户网站")
    private String website;
}