package cn.cordys.crm.biz.mapper;

import cn.cordys.crm.biz.domain.WhatsappSyncRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WhatsappSyncRecordMapper {

    /**
     * 批量插入（忽略重复，基于唯一索引）
     */
    void insertBatchIgnore(@Param("list") List<WhatsappSyncRecord> records);

    /**
     * 根据 ownerPhone 和 contactId 查询最新记录（可选，供后续扩展）
     */
    WhatsappSyncRecord selectByOwnerAndContact(
            @Param("ownerPhone") String ownerPhone,
            @Param("contactPhone") String contactPhone
    );

    /**
     * 查询负责人冲突的记录
     */
    List<WhatsappSyncRecord> selectOwnerConflict(
            @Param("contactPhone") String contactPhone,
            @Param("currentOwnerPhone") String currentOwnerPhone
    );
}
