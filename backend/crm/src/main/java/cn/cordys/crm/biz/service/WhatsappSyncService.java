package cn.cordys.crm.biz.service;

import cn.cordys.crm.biz.domain.WhatsappSyncRecord;
import cn.cordys.crm.biz.dto.SyncContactsRequest;
import cn.cordys.crm.biz.mapper.WhatsappSyncRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * WhatsApp联系人同步服务
 */
@Service
public class WhatsappSyncService {

    @Resource
    private WhatsappSyncRecordMapper whatsappSyncRecordMapper;

    /**
     * 同步WhatsApp联系人
     *
     * @param request 同步请求参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncContacts(SyncContactsRequest request) {
        if (request == null || request.getContacts() == null || request.getContacts().isEmpty()) {
            return;
        }

        List<WhatsappSyncRecord> records = new ArrayList<>();
        for (SyncContactsRequest.ContactItem item : request.getContacts()) {
            if (item.getContactPhone() == null || item.getDate() == null) {
                continue;
            }

            WhatsappSyncRecord record = new WhatsappSyncRecord();
            record.setOwnerPhone(request.getPhone());
            record.setContactPhone(item.getContactPhone());
            record.setInteractDate(LocalDate.parse(item.getDate()));
            // status 默认为 0，sync_time 由 SQL 的 NOW() 设置
            records.add(record);
        }

        if (!records.isEmpty()) {
            whatsappSyncRecordMapper.insertBatchIgnore(records);
        }
    }
}