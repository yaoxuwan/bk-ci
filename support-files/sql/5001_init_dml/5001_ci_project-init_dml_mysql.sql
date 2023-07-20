USE devops_ci_project;
SET NAMES utf8mb4;

-- 服务初始化
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('代码库(Code)', 'Code', 2, '/codelib/', '/codelib/', 'amd', '', '/codelib/codelib.css', '/codelib/codelib.js', b'1', b'1', 'path', 'ok', 'system', '2019-06-05 17:12:47', 'system', '2019-06-05 17:12:47', b'0', '', '', 99, 'codelib', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('流水线(Pipeline)', 'Pipeline', 2, '/pipelines/', '/pipeline/', 'iframe', '/pipeline/', '', '', b'1', b'1', 'path', 'ok', 'system', '2019-06-05 17:12:47', NULL, '2019-06-05 17:12:47', b'0', NULL, NULL, 97, 'pipeline', '^\/console\/pipeline\/[^\/]+\/list(\/)?(allPipeline|myPipeline|collect)?$,^\/console\/pipeline\/[^\/]+\/[^\/]+\/detail\/[^\/]+(\/executeDetail)?$,^\/console\/pipeline\/[^\/]+\/[^\/]+\/history$');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('制品库(Artifactory)', 'Artifactory', 2, '/artifactory/', '/artifactory/', 'amd', '', '/artifactory/artifactory.css', '/artifactory/artifactory.js', b'1', b'1', 'path', 'planning', 'system', '2019-06-05 17:12:48', 'system', '2019-06-05 17:12:48', b'1', '', '', 96, 'artifactory', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('凭证管理(Ticket)', 'Ticket', 8, '/ticket/', '/ticket/', 'amd', '', '/ticket/ticket.css', '/ticket/ticket.js', b'1', b'1', 'path', 'ok', 'system', '2019-06-05 17:13:26', 'system', '2019-06-05 17:13:26', b'0', '', '', 95, 'ticket', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('环境管理(Env)', 'Env', 4, '/environment/', '/environment/', 'amd', '', '/environment/environment.css', '/environment/environment.js', b'1', b'1', 'path', 'ok', 'system', '2019-06-05 17:13:27', 'system', '2019-06-05 17:13:27', b'0', '', '', 94, 'environment', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('研发商店(Store)', 'Store', 8, '/store/', '/store/', 'amd', '', '/store/store.css', '/store/store.js', b'0', b'1', 'path', 'ok', 'system', '2019-06-05 17:13:29', 'system', '2019-06-05 17:13:29', b'0', '', '', 93, 'store', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('质量红线(Gate)', 'Gate', 3, '/quality/', '/quality/', 'amd', '', '/quality/quality.css', '/quality/quality.js', b'1', b'1', 'path', 'ok', 'system', '2019-06-05 17:13:29', 'system', '2019-06-05 17:13:29', b'0', '', '', 92, 'quality', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('代码检查(CodeCC)', 'CodeCC', 2, '/codecc/', '/codecc/', 'iframe', '/codecc/', '', '', b'1', b'1', 'path', 'planning', 'system', '2019-06-05 17:12:47', NULL, '2019-06-05 17:12:47', b'1', NULL, NULL, 89, 'codecc', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('度量数据(Measure)', 'Measure', 2, '/measure/', '/measure/', 'iframe', '/measure/', '', '', b'1', b'1', 'path', 'planning', 'system', '2019-06-05 17:12:47', NULL, '2019-06-05 17:12:47', b'1', NULL, NULL, 91, 'measure', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('敏捷开发(Teamwork)', 'Teamwork', 1, '/teamwork/', '/teamwork/', 'amd', '', '/teamwork/teamwork.css', '/teamwork/teamwork.js', b'1', b'1', 'path', 'planning', 'system', '2019-06-05 17:12:48', 'system', '2019-06-05 17:12:48', b'1', '', '', 90, 'teamwork', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('编译加速(Turbo)', 'Turbo', 2, '/turbo/', '/turbo/', 'amd', '', '', '', b'1', b'1', 'path', 'planning', 'system', '2019-06-05 17:12:47', NULL, '2019-06-05 17:12:47', b'1', NULL, NULL, 89, 'turbo', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('制品库(Repo)', 'Repo', 2, '/repo/', '/repo/', 'iframe', 'https://bkrepo.yourdomain.com/ui/', '', '', b'1', b'1', 'path', 'planning', 'system', '2021-06-17 10:58:25', 'system', '2021-06-17 10:58:25', b'1', '', '', 96, 'artifactory', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('Metrics 看板(Metrics)', 'Metrics', 8, '/metrics/', '/metrics/', 'iframe', '/metrics/', '', '', b'1', b'1', 'path', 'new', 'system', '2022-10-24 21:11:48', 'system', '2022-10-24 21:11:48', b'0', '', '', 99, 'metrics', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('项目管理(manage)', 'Project', 8, '/manage/', '/manage/', 'iframe', '/manage/', '', '', b'1', b'1', 'path', 'ok', 'system', '2023-04-06 11:13:26', 'system', '2023-04-06 11:13:26', b'0', '', '', 97, '', '');
INSERT IGNORE INTO `T_SERVICE` (`name`, `english_name`, `service_type_id`, `link`, `link_new`, `inject_type`, `iframe_url`, `css_url`, `js_url`, `show_project_list`, `show_nav`, `project_id_type`, `status`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `gray_css_url`, `gray_js_url`, `weight`, `logo_url`, `web_socket`) VALUES ('权限管理(permission)', 'Permission', 8, '/permission/', '/permission/', 'iframe', '/permission/', '', '', b'0', b'1', 'path', 'ok', 'system', '2023-04-06 11:13:26', 'system', '2023-04-06 11:13:26', b'0', '', '', 97, '', '');

-- 服务分类初始化
REPLACE INTO `T_SERVICE_TYPE` (`id`, `title`, `english_title`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `weight`) VALUES (1, '项目管理', 'Case', 'system', '2019-06-05 17:13:55', 'system', '2019-06-05 17:13:55', b'1', NULL);
REPLACE INTO `T_SERVICE_TYPE` (`id`, `title`, `english_title`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `weight`) VALUES (2, '开发', 'Develop', 'system', '2018-12-06 20:49:24', 'system', '2018-12-06 20:49:24', b'0', NULL);
REPLACE INTO `T_SERVICE_TYPE` (`id`, `title`, `english_title`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `weight`) VALUES (3, '测试', 'Test', 'system', '2019-06-05 17:13:58', 'system', '2019-06-05 17:13:58', b'0', NULL);
REPLACE INTO `T_SERVICE_TYPE` (`id`, `title`, `english_title`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `weight`) VALUES (4, '部署', 'Deploy', 'system', '2019-06-05 17:13:59', 'system', '2019-06-05 17:13:59', b'0', NULL);
REPLACE INTO `T_SERVICE_TYPE` (`id`, `title`, `english_title`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `weight`) VALUES (5, '运营', 'Operation', 'system', '2019-06-05 17:14:00', 'system', '2019-06-05 17:14:00', b'1', NULL);
REPLACE INTO `T_SERVICE_TYPE` (`id`, `title`, `english_title`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `weight`) VALUES (6, '安全', 'Security', 'system', '2019-06-05 17:14:01', 'system', '2019-06-05 17:14:01', b'1', NULL);
REPLACE INTO `T_SERVICE_TYPE` (`id`, `title`, `english_title`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `weight`) VALUES (7, '研发商店', 'RD Store', 'system', '2019-06-05 17:14:02', 'system', '2019-06-05 17:14:02', b'1', NULL);
REPLACE INTO `T_SERVICE_TYPE` (`id`, `title`, `english_title`, `created_user`, `created_time`, `updated_user`, `updated_time`, `deleted`, `weight`) VALUES (8, '管理工具', 'Management', 'system', '2019-06-05 17:14:02', 'system', '2019-06-05 17:14:02', b'0', NULL);


-- 数据源初始化
REPLACE INTO `T_DATA_SOURCE`(`ID`, `MODULE_CODE`, `DATA_SOURCE_NAME`, `FULL_FLAG`, `CLUSTER_NAME`) VALUES ('eae3670d3716427881c93fde46e28534', 'PROCESS', 'ds_0', b'0', 'prod');
REPLACE INTO `T_DATA_SOURCE`(`ID`, `MODULE_CODE`, `DATA_SOURCE_NAME`, `FULL_FLAG`, `CLUSTER_NAME`) VALUES ('a71b4a3cbbc84a5b8f8386f9d9cd0001', 'METRICS', 'ds_0', b'0', 'prod');

-- ID管理配置初始化
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('ATOM_FAIL_SUMMARY_DATA', 1, 100, '插件失败汇总数据ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('ATOM_FAIL_DETAIL_DATA', 1, 1000, '插件失败详情数据ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('ATOM_OVERVIEW_DATA', 1, 100, '插件概览数据ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('PIPELINE_STAGE_DETAIL_DATA', 1, 2000, '流水线stage详情数据ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('PIPELINE_FAIL_SUMMARY_DATA', 1, 500, '流水线失败汇总数据ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('PIPELINE_FAIL_DETAIL_DATA', 1, 1000, '流水线失败详情数据ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('PIPELINE_OVERVIEW_DATA', 1, 100, '流水线概览数据ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('METRICS_ERROR_CODE_INFO', 1, 50, 'metris错误码ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('METRICS_ERROR_TYPPE_DICT', 1, 10, 'metris错误类型ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('METRICS_PROJECT_PIPELINE_LABEL_INFO', 1, 50, 'metris项目标签ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('METRICS_PROJECT_THIRD_PLATFORM_DATA', 1, 100, 'metris项目下第三方平台ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('ATOM_DISPLAY_CONFIG', 1, 100, '项目下展示插件配置ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('T_ATOM_INDEX_STATISTICS_DAILY', 1, 100, '插件每日指标数据ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('AUTH_RESOURCE', 1, 100, '权限资源');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('ATOM_MONITOR_DATA_DAILY', 1, 100, '插件每日监控数据ID管理');
REPLACE INTO `T_LEAF_ALLOC`(`BIZ_TAG`, `MAX_ID`, `STEP`, `DESCRIPTION`) VALUES ('METRICS_PROJECT_ATOM_RELEVANCY_INFO', 1, 50, 'metris项目插件关联表ID管理');