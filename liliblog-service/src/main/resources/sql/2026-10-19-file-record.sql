-- 已有数据库升级脚本：新增文件上传记录与引用关系表
-- 全新安装直接使用 init.sql，无需执行本文件。

CREATE TABLE IF NOT EXISTS `file_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件记录ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '上传者ID',
  `file_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对象key，唯一',
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问URL',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原始文件名',
  `mime_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '按扩展名判定的MIME',
  `detected_mime` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '魔数检测出的MIME',
  `file_size` bigint NULL DEFAULT NULL COMMENT '真实字节数',
  `hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '七牛etag',
  `biz_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'file' COMMENT 'cover/avatar/image/file',
  `upload_source` tinyint NOT NULL DEFAULT 1 COMMENT '1=前端直传 2=后端中转 3=历史导入',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待确认 1成功 2失败 3过期 4拒绝',
  `error_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_file_key`(`file_key` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_status_time`(`status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_url`(`url`(191) ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件上传记录表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `file_ref` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '引用ID',
  `file_id` bigint NOT NULL COMMENT '文件记录ID',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ARTICLE_COVER/ARTICLE_IMAGE/USER_AVATAR',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ref`(`file_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件引用关系表' ROW_FORMAT = DYNAMIC;
