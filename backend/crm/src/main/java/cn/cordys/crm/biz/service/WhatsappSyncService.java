package cn.cordys.crm.biz.service;

import cn.cordys.common.response.handler.ResultHolder;
import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.biz.domain.WhatsappOwnerConflict;
import cn.cordys.crm.biz.domain.WhatsappSyncRecord;
import cn.cordys.crm.biz.dto.SyncContactsRequest;
import cn.cordys.crm.biz.dto.SyncContactsResponse;
import cn.cordys.crm.biz.mapper.WhatsappOwnerConflictMapper;
import cn.cordys.crm.biz.mapper.WhatsappSyncRecordMapper;
import cn.cordys.crm.clue.domain.Clue;
import cn.cordys.crm.clue.dto.request.ClueAddRequest;
import cn.cordys.crm.clue.dto.request.PoolCluePickRequest;
import cn.cordys.crm.clue.service.ClueService;
import cn.cordys.crm.clue.service.PoolClueService;
import cn.cordys.crm.follow.dto.request.FollowUpRecordAddRequest;
import cn.cordys.crm.follow.service.FollowUpRecordService;
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
    private BaseMapper<Clue> clueMapper;

    @Resource
    private PoolClueService poolClueService;


    /**
     * 根据手机号是否已经存在客户联系记录
     *
     * @param phone 手机号
     * @return 是否存在客户联系记录
     */
    public ResultHolder checkPhone(String phone) {
        WhatsappSyncRecord record = whatsappSyncRecordMapper.selectByContact(phone);
        return ResultHolder.success(record);
    }

    /**
     * 同步WhatsApp联系人
     * <p>
     * 判断手机号在不在线索中，
     * - 在线索中的话in_shared_pool为true，表示在线索池，就领取
     * - 否则判断transition_type none表示线索，customer表示客户，transition_id表示客户id
     * - 增加跟踪记录
     */
    @Transactional(rollbackFor = Exception.class)
    public SyncContactsResponse syncContacts(SyncContactsRequest request, UserDTO userDTO) {
        String userId = userDTO.getId();
        String organizationId = userDTO.getLastOrganizationId();
        SyncContactsResponse response = new SyncContactsResponse();
        List<SyncContactsResponse.ContactResult> results = new ArrayList<>();

        if (request == null || request.getContacts() == null || request.getContacts().isEmpty()) {
            response.setResults(results);
            return response;
        }

        List<WhatsappSyncRecord> records = new ArrayList<>();
        List<WhatsappOwnerConflict> conflicts = new ArrayList<>();

        for (SyncContactsRequest.ContactItem item : request.getContacts()) {
            SyncContactsResponse.ContactResult result = new SyncContactsResponse.ContactResult();
            result.setContactPhone(item.getContactPhone());
            result.setSuccess(true);

            try {
                if (item.getContactPhone() == null || item.getDate() == null) {
//                    result.setErrorMessage("联系人电话或日期不能为空");
                    results.add(result);
                    continue;
                }

                String contactPhone = item.getContactPhone();
                String ownerPhone = request.getPhone();
                String type = null;
                String targetId = null;

                // 检查负责人冲突
                String conflictPhone = checkOwnerConflict(contactPhone, ownerPhone);
                if (conflictPhone != null) {
                    // 如果有冲突，记录冲突信息
                    WhatsappOwnerConflict conflict = createConflictRecord(contactPhone, ownerPhone, conflictPhone);
                    conflicts.add(conflict);
//                    result.setErrorMessage("存在负责人冲突：" + conflictPhone);
                    results.add(result);
                    continue;
                }

                // 没有冲突，正常处理同步
                // 1. 判断手机号是否在线索中
                Clue clue = getClueByPhone(item.getContactPhone());

                if (clue != null) {
                    // 在线索中的处理逻辑
                    String poolId = clue.getPoolId();
                    String transitionType = clue.getTransitionType();
                    // 判断是否有负责人, 如果没有负责人, 且有poolId, 则更新成当前的手机号的负责人
                    if (StringUtils.isBlank(clue.getOwner()) && StringUtils.isNotBlank(poolId)) {
                        // 如果没有负责人，则更新成当前的手机号的负责人
                        PoolCluePickRequest pickRequest = new PoolCluePickRequest();
                        pickRequest.setClueId(clue.getId());
                        pickRequest.setPoolId(poolId);
                        try {
                            // 领取线索
                            poolClueService.pick(pickRequest, userId, organizationId);
                        } catch (Exception e) {
                            // 记录更新失败，但不中断流程
                            System.err.println("更新线索负责人失败: " + e.getMessage());
                        }
                        // 线索
                        type = "CLUE";
                        targetId = clue.getId();
                    } else if ("CUSTOMER".equals(transitionType)) {
                        // 客户
                        type = "CUSTOMER";
                        targetId = clue.getTransitionId();
                    } else {
                        // 线索
                        type = "CLUE";
                        targetId = clue.getId();
                    }
                } else {
                    // 不在线索中的处理逻辑
                    // 不在联系人中，就增加一条线索
                    clue = createNewClue(contactPhone, userId, organizationId);
                    type = "CLUE";
                    targetId = clue.getId();
                }
                // 增加跟踪记录
                LocalDate localDate = LocalDate.parse(item.getDate());
                long timestamp = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
//                如果数据库中的根据时间存在，并且时间小于同步数据的时间，才增加跟踪记录
                if (clue.getFollowTime() == null || (clue.getFollowTime() != null && clue.getFollowTime() < timestamp)) {
                    addFollowUpRecord(targetId, type, timestamp, "WhatsApp互动", userId, organizationId);
                }

                // 构建WhatsApp同步记录
                WhatsappSyncRecord record = new WhatsappSyncRecord();
                record.setOwnerPhone(request.getPhone());
                record.setContactPhone(item.getContactPhone());
                record.setInteractDate(LocalDate.parse(item.getDate()));
                record.setType(type);
                record.setTargetId(targetId);
                // status 默认为 0，sync_time 由 SQL 的 NOW() 设置
                records.add(record);

                // 设置处理结果
                result.setType(type);
                result.setTargetId(targetId);
                result.setSuccess(true);
            } catch (Exception e) {
                result.setErrorMessage("处理失败：" + e.getMessage());
                System.err.println("处理联系人失败：" + item.getContactPhone() + "，错误：" + e.getMessage());
            } finally {
                results.add(result);
            }
        }

        if (!records.isEmpty()) {
            whatsappSyncRecordMapper.insertBatchIgnore(records);
        }

        if (!conflicts.isEmpty()) {
            whatsappOwnerConflictMapper.insertBatch(conflicts);
        }

        response.setResults(results);
        return response;
    }

    /**
     * 检查负责人冲突
     */
    private String checkOwnerConflict(String contactPhone, String currentOwnerPhone) {
        try {
            if (contactPhone.startsWith("86") || contactPhone.length() < 2) {
                return null;
            }
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
        WhatsappOwnerConflict conflict = new WhatsappOwnerConflict();
        conflict.setId(IDGenerator.nextStr());
        conflict.setContactPhone(contactPhone);
        conflict.setOwnerPhone(existingOwnerPhone);
        conflict.setConflictOwnerPhone(currentOwnerPhone);
        conflict.setConflictTime(LocalDateTime.now());
        conflict.setStatus((byte) 0);
        conflict.setCreateTime(System.currentTimeMillis());
        conflict.setUpdateTime(System.currentTimeMillis());
        conflict.setCreateUser("system");
        conflict.setUpdateUser("system");
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
     * 添加跟进记录
     */
    private void addFollowUpRecord(String resourceId, String type, Long timestamp, String content, String userId, String orgId) {
        try {
            FollowUpRecordAddRequest followRequest = new FollowUpRecordAddRequest();
            followRequest.setType(type);
            followRequest.setContent(content);

            if ("CLUE".equals(type)) {
                followRequest.setClueId(resourceId);
            } else if ("CUSTOMER".equals(type)) {
                followRequest.setCustomerId(resourceId);
                followRequest.setContactId(resourceId);
            }
            followRequest.setFollowTime(timestamp);
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