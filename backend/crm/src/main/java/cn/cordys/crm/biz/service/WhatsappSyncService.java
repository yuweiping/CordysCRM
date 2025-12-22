package cn.cordys.crm.biz.service;

import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.biz.domain.WhatsappOwnerConflict;
import cn.cordys.crm.biz.domain.WhatsappSyncRecord;
import cn.cordys.crm.biz.dto.ContactByPhoneResponse;
import cn.cordys.crm.biz.dto.SyncContactsRequest;
import cn.cordys.crm.biz.mapper.WhatsappOwnerConflictMapper;
import cn.cordys.crm.biz.mapper.WhatsappSyncRecordMapper;
import cn.cordys.crm.clue.domain.Clue;
import cn.cordys.crm.clue.dto.request.ClueAddRequest;
import cn.cordys.crm.clue.dto.request.PoolCluePickRequest;
import cn.cordys.crm.clue.service.ClueService;
import cn.cordys.crm.clue.service.PoolClueService;
import cn.cordys.crm.follow.dto.request.FollowUpRecordAddRequest;
import cn.cordys.crm.follow.service.FollowUpRecordService;
import cn.cordys.crm.system.mapper.ExtUserMapper;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.mybatis.lambda.LambdaQueryWrapper;
import cn.cordys.security.UserDTO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * WhatsApp联系人同步服务
 */
@Service
public class WhatsappSyncService {

    @Resource
    private WhatsappSyncRecordMapper whatsappSyncRecordMapper;

    @Resource
    private WhatsappOwnerConflictMapper whatsappOwnerConflictMapper;

    @Resource
    private ClueService clueService;

    @Resource
    private FollowUpRecordService followUpRecordService;

    @Resource
    private BusinessService businessService;

    @Resource
    private ExtUserMapper extUserMapper;

    @Resource
    private BaseMapper<Clue> clueMapper;

    @Resource
    private PoolClueService poolClueService;
    private static final String DEFAULT_ORGANIZATION_ID = "100001";

    /**
     * 同步WhatsApp联系人
     * <p>
     * 判断手机号在不在线索中，
     * - 在线索中的话判断是否有负责人，
     * - 如果没有则从线索池中领取
     * - 增加跟踪记录
     * 不在线索中的话判断是否在联系人中
     * - 不在的联系人中，就增加一条线索，增加跟踪记录
     * - 在联系人中，增加跟踪记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncContacts(SyncContactsRequest request) {
        if (request == null || request.getContacts() == null || request.getContacts().isEmpty()) {
            return;
        }

        List<WhatsappSyncRecord> records = new ArrayList<>();
        List<WhatsappOwnerConflict> conflicts = new ArrayList<>();

        for (SyncContactsRequest.ContactItem item : request.getContacts()) {
            if (item.getContactPhone() == null || item.getDate() == null) {
                continue;
            }

            String contactPhone = item.getContactPhone();
            String ownerPhone = request.getPhone();

            // 检查负责人冲突
            String conflictPhone = checkOwnerConflict(contactPhone, ownerPhone);
            if (conflictPhone != null) {
                // 如果有冲突，记录冲突信息
                WhatsappOwnerConflict conflict = createConflictRecord(contactPhone, ownerPhone, conflictPhone);
                conflicts.add(conflict);
            } else {
                // 没有冲突，正常处理同步
                // 1. 判断手机号是否在线索中
                Clue clue = getClueByPhone(item.getContactPhone());

                if (clue != null) {
                    // 在线索中的处理逻辑
                    handleClueExists(clue, request.getPhone(), item);
                } else {
                    // 不在线索中的处理逻辑
                    handleClueNotExists(item.getContactPhone(), request.getPhone(), item);
                }

                // 构建WhatsApp同步记录
                WhatsappSyncRecord record = new WhatsappSyncRecord();
                record.setOwnerPhone(request.getPhone());
                record.setContactPhone(item.getContactPhone());
                record.setInteractDate(LocalDate.parse(item.getDate()));
                // status 默认为 0，sync_time 由 SQL 的 NOW() 设置
                records.add(record);
            }
        }

        if (!records.isEmpty()) {
            whatsappSyncRecordMapper.insertBatchIgnore(records);
        }

        if (!conflicts.isEmpty()) {
            whatsappOwnerConflictMapper.insertBatch(conflicts);
        }
    }

    /**
     * 检查负责人冲突
     */
    private String checkOwnerConflict(String contactPhone, String currentOwnerPhone) {
        try {
            // 查询当前联系人是否已经存在于同步记录中，并且负责人不同
            List<WhatsappSyncRecord> existingRecords = whatsappSyncRecordMapper.selectOwnerConflict(contactPhone, currentOwnerPhone);
            if (existingRecords != null && !existingRecords.isEmpty()) {
                return existingRecords.get(0).getOwnerPhone();
            }
            return null;
        } catch (Exception e) {
            // 如果查询失败，默认认为没有冲突，继续处理
            return null;
        }
    }

    /**
     * 创建冲突记录
     */
    private WhatsappOwnerConflict createConflictRecord(String contactPhone, String currentOwnerPhone, String existingOwnerPhone) {
        String currentUserId = getUserIdByPhone(currentOwnerPhone);
        WhatsappOwnerConflict conflict = new WhatsappOwnerConflict();
        conflict.setId(IDGenerator.nextStr());
        conflict.setContactPhone(contactPhone);
        conflict.setOwnerPhone(existingOwnerPhone);
        conflict.setConflictOwnerPhone(currentOwnerPhone);
        conflict.setConflictTime(LocalDateTime.now());
        conflict.setStatus((byte) 0);
        conflict.setCreateTime(System.currentTimeMillis());
        conflict.setUpdateTime(System.currentTimeMillis());
        conflict.setCreateUser(currentUserId);
        conflict.setUpdateUser(currentUserId);
        return conflict;

    }

    /**
     * 根据手机号查询线索
     */
    private Clue getClueByPhone(String phone) {
        try {
            // 使用Lambda查询根据手机号查询线索
            LambdaQueryWrapper<Clue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Clue::getPhone, phone);
            List<Clue> clues = clueMapper.selectListByLambda(queryWrapper);
            return clues != null && !clues.isEmpty() ? clues.get(0) : null;
        } catch (Exception e) {
            // 如果查询失败，返回null
            return null;
        }
    }

    /**
     * 处理手机号在线索中的情况
     */
    private void handleClueExists(Clue clue, String ownerPhone, SyncContactsRequest.ContactItem item) {
        String currentUserId = getUserIdByPhone(ownerPhone);
        String poolId = clue.getPoolId();

        // 判断是否有负责人, 如果没有负责人, 且有poolId, 则更新成当前的手机号的负责人
        if (StringUtils.isBlank(clue.getOwner()) && StringUtils.isNotBlank(poolId)) {
            // 如果没有负责人，则更新成当前的手机号的负责人
            PoolCluePickRequest pickRequest = new PoolCluePickRequest();
            pickRequest.setClueId(clue.getId());
            pickRequest.setPoolId(poolId);

            try {
                // 领取线索
                poolClueService.pick(pickRequest, currentUserId, DEFAULT_ORGANIZATION_ID);
            } catch (Exception e) {
                // 记录更新失败，但不中断流程
                System.err.println("更新线索负责人失败: " + e.getMessage());
            }
        }

        // 增加跟踪记录
        addFollowUpRecord(clue.getId(), "CLUE", item.getDate(), "WhatsApp互动", currentUserId, DEFAULT_ORGANIZATION_ID);
    }

    /**
     * 处理手机号不在线索中的情况
     */
    private void handleClueNotExists(String contactPhone, String ownerPhone, SyncContactsRequest.ContactItem item) {
        String currentUserId = getUserIdByPhone(ownerPhone);

        // 判断是否在联系人中
        List<ContactByPhoneResponse> contacts = businessService.getContactsByUserPhone(contactPhone);

        if (contacts == null || contacts.isEmpty()) {
            // 不在联系人中，就增加一条线索
            Clue newClue = createNewClue(contactPhone, currentUserId, DEFAULT_ORGANIZATION_ID);

            if (newClue != null) {
                // 增加跟踪记录
                addFollowUpRecord(newClue.getId(), "CLUE", item.getDate(), "WhatsApp互动 - 新增线索", currentUserId, DEFAULT_ORGANIZATION_ID);
            }
        } else {
            // 增加跟踪记录
            addFollowUpRecord(contacts.get(0).getId(), "CONTACT", item.getDate(), "WhatsApp互动", currentUserId, DEFAULT_ORGANIZATION_ID);
        }
    }

    /**
     * 根据手机号获取用户ID
     */
    private String getUserIdByPhone(String phone) {
        try {
            // 使用用户服务根据手机号查询用户
            UserDTO user = extUserMapper.selectByPhoneOrEmail(phone);
            return user != null ? user.getId() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 添加跟进记录
     */
    private void addFollowUpRecord(String resourceId, String type, String date, String content, String userId, String orgId) {
        try {
            FollowUpRecordAddRequest followRequest = new FollowUpRecordAddRequest();
            followRequest.setType(type);
            followRequest.setContent(content);

            if ("CLUE".equals(type)) {
                followRequest.setClueId(resourceId);
            } else if ("CONTACT".equals(type)) {
                followRequest.setCustomerId(resourceId);
                followRequest.setContactId(resourceId);
            }

            // 设置跟进时间（将日期字符串转换为时间戳）
            try {
                LocalDate localDate = LocalDate.parse(date);
                long timestamp = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                followRequest.setFollowTime(timestamp);
            } catch (Exception e) {
                // 如果日期格式错误，使用当前时间
                followRequest.setFollowTime(System.currentTimeMillis());
            }

            followRequest.setFollowMethod("WHATSAPP");
            followRequest.setOwner(userId);

            // 添加跟进记录
            followUpRecordService.add(followRequest, userId, orgId);
        } catch (Exception e) {
            // 记录日志，但不中断主流程
            System.err.println("添加跟进记录失败: " + e.getMessage());
        }
    }

    /**
     * 创建新线索
     */
    private Clue createNewClue(String contactPhone, String userId, String orgId) {
        try {
            ClueAddRequest addRequest = new ClueAddRequest();
            addRequest.setName("WhatsApp联系人 - " + contactPhone);
            addRequest.setContact(contactPhone);
            addRequest.setPhone(contactPhone);
            addRequest.setOwner(userId);

            return clueService.add(addRequest, userId, orgId);
        } catch (Exception e) {
            System.err.println("创建新线索失败: " + e.getMessage());
            return null;
        }
    }
}