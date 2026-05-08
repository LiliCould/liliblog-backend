DROP DATABASE IF EXISTS liliblog;

CREATE DATABASE liliblog;

USE liliblog;

CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
    `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT DEFAULT 1 COMMENT '角色：0-ADMIN，1-USER',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
    `update_by` BIGINT NULL COMMENT '更新者',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `slug` VARCHAR(50) UNIQUE NULL COMMENT '分类别名',
    `description` VARCHAR(200) COMMENT '分类描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
    `update_by` BIGINT NULL COMMENT '更新者',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

CREATE TABLE `article` (
   `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
   `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
   `slug` VARCHAR(200) UNIQUE NULL COMMENT '文章别名(用于URL)',
   `summary` VARCHAR(500) COMMENT '文章摘要',
   `content` LONGTEXT NOT NULL COMMENT '文章内容',
   `content_html` LONGTEXT COMMENT '文章HTML内容',
   `cover_image` VARCHAR(255) COMMENT '封面图片URL',
   `status` TINYINT DEFAULT 0 COMMENT '状态,0-审核中,1-发布,2-草稿',
   `view_count` INT DEFAULT 0 COMMENT '阅读数',
   `category_id` BIGINT COMMENT '分类ID',
   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
   `update_by` BIGINT NULL COMMENT '更新者',
   `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
   INDEX `idx_title` (`title`),
   INDEX `idx_status` (`status`),
   INDEX `idx_category` (`category_id`),
   FULLTEXT INDEX `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

CREATE TABLE `tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    `name` VARCHAR(30) NOT NULL UNIQUE COMMENT '标签名称',
    `color` VARCHAR(7) DEFAULT '#666666' COMMENT '标签颜色',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
    `update_by` BIGINT NULL COMMENT '更新者',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX `idx_id` (`id`),
    INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

CREATE TABLE `article_tag` (
   `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
   `article_id` BIGINT NOT NULL COMMENT '文章ID',
   `tag_id` BIGINT NOT NULL COMMENT '标签ID',
   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
   UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
   INDEX `idx_article` (`article_id`),
   INDEX `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

CREATE TABLE `comment` (
   `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
   `content` TEXT NOT NULL COMMENT '评论内容',
   `author_id` BIGINT NOT NULL COMMENT '评论者ID',
   `article_id` BIGINT NOT NULL COMMENT '文章ID',
   `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态,0-审核中,1-发布',
   `like_count` INT DEFAULT 0 COMMENT '点赞数',
   `ip_address` VARCHAR(45) COMMENT '评论者IP',
   `user_agent` VARCHAR(500) COMMENT '用户代理',
   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
   `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
   INDEX `idx_article` (`article_id`),
   INDEX `idx_parent` (`parent_id`),
   INDEX `idx_status` (`status`),
   INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE `like_record` (
   `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
   `user_id` BIGINT NOT NULL COMMENT '用户ID',
   `target_id` BIGINT NOT NULL COMMENT '目标ID（文章ID或评论ID）',
   `target_type` TINYINT NOT NULL COMMENT '目标类型，0-文章，1-评论',
   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
   UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
   INDEX `idx_user` (`user_id`),
   INDEX `idx_target` (`target_id`, `target_type`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

use liliblog;
-- ==================== 聊天室相关表 ====================
CREATE TABLE `chat_message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` ENUM('TEXT', 'IMAGE', 'FILE', 'SYSTEM') DEFAULT 'TEXT' COMMENT '消息类型',
    `parent_id` BIGINT DEFAULT 0 COMMENT '回复的消息ID（0表示非回复）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
    `ip_address` VARCHAR(45) COMMENT '发送者IP',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `create_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
    INDEX `idx_sender` (`sender_id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_parent` (`parent_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';