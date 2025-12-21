-- 客户行业
INSERT INTO `cordys-crm`.`sys_module_field` (`id`, `form_id`, `internal_key`, `name`, `type`, `mobile`, `pos`,
                                             `create_user`, `create_time`, `update_user`, `update_time`)
VALUES ('customer_category', '1267066891935751', NULL, '客户行业', 'INPUT', b'1', 3, 'admin', 1766278610182, 'admin',
        1766278610182);
INSERT INTO `cordys-crm`.`sys_module_field` (`id`, `form_id`, `internal_key`, `name`, `type`, `mobile`, `pos`,
                                             `create_user`, `create_time`, `update_user`, `update_time`)
VALUES ('customer_website', '1267066891935751', NULL, '客户公司网站', 'LINK', b'1', 4, 'admin', 1766278610182, 'admin',
        1766278610182);

INSERT INTO `cordys-crm`.`sys_module_field_blob` (`id`, `prop`)
VALUES ('customer_category',
        '{\"id\":\"customer_category\",\"name\":\"客户行业\",\"internalKey\":null,\"pos\":null,\"type\":\"INPUT\",\"mobile\":true,\"showLabel\":true,\"placeholder\":\"客户行业\",\"description\":\"\",\"readable\":true,\"editable\":true,\"fieldWidth\":1.0,\"rules\":[],\"showControlRules\":null,\"businessKey\":null,\"disabledProps\":null,\"resourceFieldId\":null,\"subTableFieldId\":null,\"defaultValue\":\"\"}');
INSERT INTO `cordys-crm`.`sys_module_field_blob` (`id`, `prop`)
VALUES ('customer_website',
        '{\"id\":\"customer_website\",\"name\":\"客户公司网站\",\"internalKey\":null,\"pos\":null,\"type\":\"LINK\",\"mobile\":true,\"showLabel\":true,\"placeholder\":null,\"description\":\"\",\"readable\":true,\"editable\":true,\"fieldWidth\":1.0,\"rules\":[],\"showControlRules\":null,\"businessKey\":null,\"disabledProps\":null,\"resourceFieldId\":null,\"subTableFieldId\":null,\"linkSource\":\"userInput\",\"openMode\":\"openInNew\"}');

-- 客户池
INSERT INTO `cordys-crm`.`customer_pool` (`id`, `scope_id`, `organization_id`, `name`, `owner_id`, `enable`, `auto`,
                                          `create_time`, `update_time`, `create_user`, `update_user`)
VALUES ('industrial_automation', '[\"1306992907927552\"]', '100001', '工业自动化', '[\"org_admin\"]', b'1', b'0',
        1766221031705, 1766221031705, 'admin', 'admin');
