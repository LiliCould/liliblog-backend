DROP DATABASE IF EXISTS liliblog;

CREATE DATABASE liliblog;

USE liliblog;

CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `github_id` BIGINT NULL COMMENT 'GitHub ID',
    `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
    `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT DEFAULT 1 NOT NULL COMMENT '角色：0-ADMIN，1-USER',
    `status` TINYINT DEFAULT 1 NOT NULL COMMENT '状态：0-禁用，1-启用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
    `update_by` BIGINT NULL COMMENT '更新者',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_status` (`status`),
    UNIQUE INDEX `uk_github_id` (`github_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `slug` VARCHAR(50) UNIQUE NULL COMMENT '分类别名',
    `description` VARCHAR(200) COMMENT '分类描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 NOT NULL COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
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
   `status` TINYINT DEFAULT 1 NOT NULL COMMENT '状态：0-审核中，1-发布，2-草稿',
   `view_count` INT DEFAULT 0 COMMENT '阅读数',
   `category_id` BIGINT COMMENT '分类ID',
   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `create_by` BIGINT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
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
    `create_by` BIGINT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
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
   `create_by` BIGINT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
   UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
   INDEX `idx_article` (`article_id`),
   INDEX `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

CREATE TABLE `comment` (
   `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
   `content` TEXT NOT NULL COMMENT '评论内容',
   `article_id` BIGINT NOT NULL COMMENT '文章ID',
   `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态,0-审核中,1-发布',
   `like_count` INT DEFAULT 0 COMMENT '点赞数',
   `ip_address` VARCHAR(45) COMMENT '评论者IP',
   `user_agent` VARCHAR(500) COMMENT '用户代理',
   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `create_by` BIGINT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
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
   `create_by` BIGINT NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
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

INSERT INTO `liliblog`.`user` (`id`, `username`, `email`, `password`, `nickname`, `avatar`, `role`, `status`, `last_login_time`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES (1, 'admin', '123@qq.com', '$2a$10$C6L3k9u8wJwviUgJxP0DYOi.wXtGuAtJChBS.5jCDZVWMDdToULJu', '管理员', NULL, 1, 1, '2026-05-09 16:40:24', '2026-05-09 00:12:16', '2026-05-09 16:40:24', 0, 0, 0);
INSERT INTO `liliblog`.`user` (`id`, `username`, `email`, `password`, `nickname`, `avatar`, `role`, `status`, `last_login_time`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES (2, 'lilicould', 'lilicould@qq.com', '$2a$10$gbGYrtKxnCnQIKT88KU1iePULdoOec4zQ0Py455XvwFC67ybDvelG', '立里可', NULL, 1, 1, NULL, '2026-05-09 17:16:33', '2026-05-09 17:16:33', 0, NULL, 0);
INSERT INTO `liliblog`.`user` (`id`, `username`, `email`, `password`, `nickname`, `avatar`, `role`, `status`, `last_login_time`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES (3, 'lilicould2', 'lilicould@aqq.com', '$2a$10$avWnDDafzU/Z7gMns6LSXuAYp5E25ivMuOimzemkj9wKApf9BELny', '立里可', NULL, 1, 1, NULL, '2026-05-09 17:40:09', '2026-05-09 17:40:09', NULL, NULL, 0);
INSERT INTO `liliblog`.`user` (`id`, `username`, `email`, `password`, `nickname`, `avatar`, `role`, `status`, `last_login_time`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES (4, 'lilicould3', 'lilicoul2d@aqq.com', '$2a$10$5P7IGT8CxperKaYdnSvRQ.aYVTStGAIw3/UbMpWyPFgLiRXZ1JSr2', '立里可3', NULL, 1, 1, NULL, '2026-05-09 17:47:33', '2026-05-09 17:47:33', NULL, NULL, 0);

-- ==================== 测试数据 ====================
-- 分类数据
INSERT INTO `category` (`id`, `name`, `slug`, `description`, `sort_order`, `status`) VALUES 
(1, '技术分享', 'tech', '编程技术与开发心得', 1, 1),
(2, '生活随笔', 'life', '日常生活记录与感悟', 2, 1),
(3, '学习笔记', 'study', '学习过程中的笔记总结', 3, 1);

-- 标签数据
INSERT INTO `tag` (`id`, `name`, `color`) VALUES 
(1, 'Java', '#FF5733'),
(2, 'SpringBoot', '#33FF57'),
(3, 'MySQL', '#3357FF'),
(4, 'Redis', '#F333FF'),
(5, '前端', '#FF33A8'),
(6, 'Vue', '#33FFF5');

-- 文章数据
INSERT INTO `article` (`id`, `title`, `slug`, `summary`, `content`, `content_html`, `cover_image`, `status`, `view_count`, `category_id`, `create_by`) VALUES 
(1, 'Spring Boot 入门教程', 'springboot-intro', '从零开始学习 Spring Boot 框架', '这是 Spring Boot 的入门内容...', '<p>这是 Spring Boot 的入门内容...</p>', NULL, 1, 100, 1, 1),
(2, 'MySQL 性能优化技巧', 'mysql-optimize', '分享一些 MySQL 数据库优化的实用技巧', 'MySQL 优化有很多方面...', '<p>MySQL 优化有很多方面...</p>', NULL, 1, 85, 1, 1),
(3, '周末爬山记', 'weekend-hiking', '记录周末去爬山的经历', '今天天气真好，去爬了附近的山...', '<p>今天天气真好，去爬了附近的山...</p>', NULL, 1, 50, 2, 2);

-- 文章标签关联
INSERT INTO `article_tag` (`article_id`, `tag_id`, `create_by`) VALUES 
(1, 1, 1),
(1, 2, 1),
(2, 3, 1),
(2, 4, 1);

-- 评论数据
INSERT INTO `comment` (`id`, `content`, `article_id`, `parent_id`, `status`, `like_count`, `ip_address`, `create_by`) VALUES 
(1, '写得很好，受益匪浅！', 1, 0, 1, 5, '127.0.0.1', 2),
(2, '确实，Spring Boot 很方便', 1, 1, 1, 2, '127.0.0.1', 3),
(3, '有没有更深入的教程？', 1, 0, 1, 1, '127.0.0.1', 4);

-- 点赞记录
INSERT INTO `like_record` (`user_id`, `target_id`, `target_type`, `create_by`) VALUES 
(2, 1, 0, 2),
(3, 1, 0, 3),
(2, 1, 1, 2);

-- 聊天消息
INSERT INTO `chat_message` (`sender_id`, `content`, `type`, `parent_id`, `status`, `ip_address`, `create_by`) VALUES 
(1, '大家好！', 'TEXT', 0, 1, '127.0.0.1', 1),
(2, '你好呀', 'TEXT', 1, 1, '127.0.0.1', 2),
(3, '欢迎欢迎', 'TEXT', 0, 1, '127.0.0.1', 3);
