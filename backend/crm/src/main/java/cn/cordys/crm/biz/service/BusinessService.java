package cn.cordys.crm.biz.service;

import cn.cordys.crm.biz.dto.ClueByPhoneResponse;
import cn.cordys.crm.biz.mapper.ExtBizContactMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务服务类
 *
 * @author jianxing
 */
@Service
public class BusinessService {

    @Resource
    private ExtBizContactMapper extBizContactMapper;

    /**
     * 根据用户手机号查询联系人信息
     *
     * @param phone 用户手机号
     * @return 联系人列表
     */
    public List<ClueByPhoneResponse> getClueByUserPhone(String phone) {
        return extBizContactMapper.getClueByUserPhone(phone);
    }
}