-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE `biz_company_info`
(
    `id`             int                                                           NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `countryZh`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '国家（中文）',
    `province`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
    `city`           varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
    `name`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公司名称',
    `website`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '网站',
    `domain`         varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '域名',
    `phone`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '电话',
    `category`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '类别',
    `keyword`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关键词',
    `pageUrl`        text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '页面URL',
    `exist_whatsapp` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '是否存在WhatsApp',
    `has_send_crm`   tinyint                                                       DEFAULT NULL COMMENT '是否发送CRM',
    `created_at`     timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_phone` (`phone`) USING BTREE,
    KEY              `idx_category` (`category`) USING BTREE,
    KEY              `idx_keyword` (`keyword`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=635 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='原始公司信息表';

CREATE TABLE `biz_whatsapp_owner_conflict`
(
    `id`                   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT 'id',
    `contact_phone`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人电话',
    `owner_phone`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '当前负责人手机号',
    `conflict_owner_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '冲突负责人手机号',
    `conflict_time`        datetime                                                      NOT NULL COMMENT '冲突时间',
    `status`               tinyint(1) NOT NULL DEFAULT '0' COMMENT '冲突状态: 0=待处理, 1=已处理',
    `create_time`          bigint                                                        NOT NULL COMMENT '创建时间',
    `update_time`          bigint                                                        NOT NULL COMMENT '更新时间',
    `create_user`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '创建人',
    `update_user`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE,
    KEY                    `idx_contact_phone` (`contact_phone`) USING BTREE,
    KEY                    `idx_owner_phone` (`owner_phone`) USING BTREE,
    KEY                    `idx_conflict_owner_phone` (`conflict_owner_phone`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='WhatsApp负责人冲突表';

CREATE TABLE `biz_whatsapp_sync_record`
(
    `id`            bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `owner_phone`   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属用户手机号（发起同步的账号）',
    `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系人电话',
    `interact_date` date                                                         NOT NULL COMMENT '最后互动日期（YYYY-MM-DD）',
    `sync_time`     datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '同步时间',
    `type`          varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci          DEFAULT NULL COMMENT '类型：CLUE(线索)或CUSTOMER(客户)',
    `target_id`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci          DEFAULT NULL COMMENT '对应类型的ID',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_owner_contact_date` (`owner_phone`,`contact_phone`,`interact_date`) USING BTREE,
    UNIQUE KEY `uk_owner_contact` (`owner_phone`,`contact_phone`) USING BTREE,
    KEY             `idx_owner_phone` (`owner_phone`) USING BTREE,
    KEY             `idx_contact_id` (`contact_phone`) USING BTREE,
    KEY             `idx_interact_date` (`interact_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1789 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='WhatsApp 联系人同步记录';
-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
