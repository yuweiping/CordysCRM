package cn.cordys.crm.biz.mapper;

import cn.cordys.crm.biz.domain.WhatsappOwnerConflict;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WhatsappOwnerConflictMapper {


    /**
     * 批量插入冲突记录
     */
    void insertBatch(@Param("list") List<WhatsappOwnerConflict> conflicts);

    /**
     * 根据联系人电话查询所有冲突记录
     */
    List<WhatsappOwnerConflict> selectByContactPhone(@Param("contactPhone") String contactPhone);

    /**
     * 根据联系人电话和当前负责人查询冲突记录
     */
    List<WhatsappOwnerConflict> selectByContactPhoneAndOwner(@Param("contactPhone") String contactPhone, @Param("ownerPhone") String ownerPhone);


}