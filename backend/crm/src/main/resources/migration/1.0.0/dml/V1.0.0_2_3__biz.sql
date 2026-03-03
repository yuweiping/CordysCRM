-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

INSERT INTO `sys_module_field` (`id`, `form_id`, `internal_key`, `name`, `type`, `mobile`, `pos`,
                                `create_user`, `create_time`, `update_user`, `update_time`)
VALUES ('category', '1422785226219526', NULL, '所属行业', 'INPUT', b'1', 7, 'admin', 1768834751072, 'admin',
        1768834751072);
INSERT INTO `sys_module_field` (`id`, `form_id`, `internal_key`, `name`, `type`, `mobile`, `pos`,
                                `create_user`, `create_time`, `update_user`, `update_time`)
VALUES ('country', '1422785226219526', 'clueArea', '国家', 'LOCATION', b'1', 13, 'admin', 1768834751078, 'admin',
        1768834751078);
INSERT INTO `sys_module_field` (`id`, `form_id`, `internal_key`, `name`, `type`, `mobile`, `pos`,
                                `create_user`, `create_time`, `update_user`, `update_time`)
VALUES ('profilePicture', '1422785226219530', NULL, '头像', 'INPUT', b'1', 6, 'admin', 1768300892478, 'admin',
        1768300892478);
INSERT INTO `sys_module_field` (`id`, `form_id`, `internal_key`, `name`, `type`, `mobile`, `pos`,
                                `create_user`, `create_time`, `update_user`, `update_time`)
VALUES ('website', '1422785226219526', NULL, '公司网站', 'LINK', b'1', 8, 'admin', 1768834751072, 'admin',
        1768834751072);

INSERT INTO `sys_module_field_blob` (`id`, `prop`)
VALUES ('category',
        '{\"id\":\"category\",\"name\":\"所属行业\",\"internalKey\":null,\"pos\":null,\"type\":\"INPUT\",\"mobile\":true,\"showLabel\":true,\"placeholder\":\"\",\"description\":\"\",\"readable\":true,\"editable\":true,\"fieldWidth\":1.0,\"rules\":[],\"showControlRules\":null,\"businessKey\":null,\"disabledProps\":null,\"resourceFieldId\":null,\"subTableFieldId\":null,\"defaultValue\":\"\"}');
INSERT INTO `sys_module_field_blob` (`id`, `prop`)
VALUES ('country',
        '{\"id\":\"country\",\"name\":\"国家\",\"internalKey\":\"clueArea\",\"pos\":null,\"type\":\"LOCATION\",\"mobile\":true,\"showLabel\":true,\"placeholder\":\"\",\"description\":null,\"readable\":true,\"editable\":true,\"fieldWidth\":1.0,\"rules\":[],\"showControlRules\":null,\"businessKey\":null,\"disabledProps\":null,\"resourceFieldId\":null,\"subTableFieldId\":null,\"locationType\":\"C\"}');
INSERT INTO `sys_module_field_blob` (`id`, `prop`)
VALUES ('profilePicture',
        '{\"id\":\"profilePicture\",\"name\":\"头像\",\"internalKey\":null,\"pos\":null,\"type\":\"INPUT\",\"mobile\":true,\"showLabel\":true,\"placeholder\":null,\"description\":\"\",\"readable\":true,\"editable\":false,\"fieldWidth\":1.0,\"rules\":[],\"showControlRules\":null,\"businessKey\":null,\"disabledProps\":null,\"resourceFieldId\":null,\"subTableFieldId\":null,\"defaultValue\":\"\"}');
INSERT INTO `sys_module_field_blob` (`id`, `prop`)
VALUES ('website',
        '{\"id\":\"website\",\"name\":\"公司网站\",\"internalKey\":null,\"pos\":null,\"type\":\"LINK\",\"mobile\":true,\"showLabel\":true,\"placeholder\":\"\",\"description\":\"\",\"readable\":true,\"editable\":true,\"fieldWidth\":1.0,\"rules\":[],\"showControlRules\":null,\"businessKey\":null,\"disabledProps\":null,\"resourceFieldId\":null,\"subTableFieldId\":null,\"linkSource\":\"userInput\",\"openMode\":\"openInNew\"}');


SET SESSION innodb_lock_wait_timeout = DEFAULT;