-- 线索增加字段
INSERT INTO `cordys-crm`.`sys_module_field` (`id`, `form_id`, `internal_key`, `name`, `type`, `mobile`, `pos`,
                                             `create_user`, `create_time`, `update_user`, `update_time`)
VALUES ('category', '811525480652806', NULL, '所属行业', 'INPUT', b'1', 3, 'admin', 1766278610182, 'admin',
        1766278610182);
INSERT INTO `cordys-crm`.`sys_module_field` (`id`, `form_id`, `internal_key`, `name`, `type`, `mobile`, `pos`,
                                             `create_user`, `create_time`, `update_user`, `update_time`)
VALUES ('website', '811525480652806', NULL, '公司网站', 'LINK', b'1', 4, 'admin', 1766278610182, 'admin',
        1766278610182);

INSERT INTO `cordys-crm`.`sys_module_field_blob` (`id`, `prop`)
VALUES ('website',
        '{\"id\":\"website\",\"name\":\"公司网站\",\"internalKey\":null,\"pos\":null,\"type\":\"LINK\",\"mobile\":true,\"showLabel\":true,\"placeholder\":null,\"description\":\"\",\"readable\":true,\"editable\":true,\"fieldWidth\":1.0,\"rules\":[],\"showControlRules\":null,\"businessKey\":null,\"disabledProps\":null,\"resourceFieldId\":null,\"subTableFieldId\":null,\"linkSource\":\"userInput\",\"openMode\":\"openInNew\"}');
INSERT INTO `cordys-crm`.`sys_module_field_blob` (`id`, `prop`)
VALUES ('category',
        '{\"id\":\"category\",\"name\":\"所属行业\",\"internalKey\":null,\"pos\":null,\"type\":\"INPUT\",\"mobile\":true,\"showLabel\":true,\"placeholder\":null,\"description\":\"\",\"readable\":true,\"editable\":true,\"fieldWidth\":1.0,\"rules\":[],\"showControlRules\":null,\"businessKey\":null,\"disabledProps\":null,\"resourceFieldId\":null,\"subTableFieldId\":null,\"defaultValue\":\"\"}');

-- 线索池
INSERT INTO `cordys-crm`.`clue_pool` (`id`, `name`, `scope_id`, `organization_id`, `owner_id`, `enable`, `auto`,
                                      `create_time`, `update_time`, `create_user`, `update_user`)
VALUES ('industrial_automation', '工业自动化', '[\"sales_staff\"]', '100001', '[\"org_admin\"]', b'1', b'0',
        1766294899952, 1766294899952, 'admin', 'admin');

INSERT INTO `cordys-crm`.`clue_pool_recycle_rule` (`id`, `pool_id`, `operator`, `condition`, `create_time`,
                                                   `update_time`, `create_user`, `update_user`)
VALUES ('833206475563010', 'industrial_automation', 'AND', '[]', 1766294899971, 1766294899971, 'admin', 'admin');
INSERT INTO `cordys-crm`.`clue_pool_pick_rule` (`id`, `pool_id`, `limit_on_number`, `pick_number`, `limit_pre_owner`,
                                                `pick_interval_days`, `limit_new`, `new_pick_interval`, `create_user`,
                                                `create_time`, `update_user`, `update_time`)
VALUES ('833206475563009', 'industrial_automation', b'0', NULL, b'0', NULL, b'0', NULL, 'admin', 1766294899957, 'admin',
        1766294899957);
