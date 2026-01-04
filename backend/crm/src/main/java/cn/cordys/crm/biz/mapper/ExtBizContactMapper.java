package cn.cordys.crm.biz.mapper;

import cn.cordys.crm.biz.dto.ClueByPhoneResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务联系人Mapper
 *
 * @author jianxing
 */
public interface ExtBizContactMapper {

    /**
     * 根据用户手机号查询联系人信息
     *
     * @param phone 用户手机号
     * @return 联系人列表
     */
    List<ClueByPhoneResponse> getClueByUserPhone(@Param("phone") String phone);
}
