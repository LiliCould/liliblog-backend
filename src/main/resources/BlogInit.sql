DROP DATABASE IF EXISTS liliblog;

CREATE DATABASE liliblog;

USE liliblog;

CREATE TABLE `user_tb` (
                           `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                           `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
                           `email` VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
                           `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
                           `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
                           `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
                           `role` ENUM('ADMIN','VISITOR') DEFAULT 'VISITOR' COMMENT '角色',
                           `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                           `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
                           `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           INDEX `idx_username` (`username`),
                           INDEX `idx_email` (`email`),
                           INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `category` (
                            `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
                            `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
                            `slug` VARCHAR(50) UNIQUE NOT NULL COMMENT '分类别名',
                            `description` VARCHAR(200) COMMENT '分类描述',
                            `parent_id` BIGINT DEFAULT 0 COMMENT '父级分类ID',
                            `sort_order` INT DEFAULT 0 COMMENT '排序',
                            `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                            `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            INDEX `idx_parent` (`parent_id`),
                            INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

CREATE TABLE `article` (
                           `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
                           `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
                           `slug` VARCHAR(200) UNIQUE COMMENT '文章别名(用于URL)',
                           `summary` VARCHAR(500) COMMENT '文章摘要',
                           `content` LONGTEXT NOT NULL COMMENT '文章内容',
                           `content_html` LONGTEXT COMMENT '文章HTML内容',
                           `cover_image` VARCHAR(255) COMMENT '封面图片',
                           `status` ENUM('DRAFT', 'PUBLISHED', 'HIDDEN', 'DELETED') DEFAULT 'DRAFT' COMMENT '状态',
                           `view_count` INT DEFAULT 0 COMMENT '阅读数',
                           `like_count` INT DEFAULT 0 COMMENT '点赞数',
                           `comment_count` INT DEFAULT 0 COMMENT '评论数',
                           `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶',
                           `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐',
                           `author_id` BIGINT NOT NULL COMMENT '作者ID',
                           `category_id` BIGINT COMMENT '分类ID',
                           `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `publish_time` DATETIME COMMENT '发布时间',
                           FOREIGN KEY (`author_id`) REFERENCES `user_tb`(`id`),
                           FOREIGN KEY (`category_id`) REFERENCES `category`(`id`),
                           INDEX `idx_title` (`title`),
                           INDEX `idx_status` (`status`),
                           INDEX `idx_author` (`author_id`),
                           INDEX `idx_category` (`category_id`),
                           INDEX `idx_publish_time` (`publish_time`),
                           INDEX `idx_is_top` (`is_top`),
                           FULLTEXT INDEX `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

CREATE TABLE `tag` (
                       `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
                       `name` VARCHAR(30) NOT NULL COMMENT '标签名称',
                       `slug` VARCHAR(30) UNIQUE NOT NULL COMMENT '标签别名',
                       `color` VARCHAR(7) DEFAULT '#666666' COMMENT '标签颜色',
                       `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

CREATE TABLE `article_tag` (
                               `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
                               `article_id` BIGINT NOT NULL COMMENT '文章ID',
                               `tag_id` BIGINT NOT NULL COMMENT '标签ID',
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE,
                               FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE,
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
                           `status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'APPROVED' COMMENT '审核状态', # 默认通过审核
                           `like_count` INT DEFAULT 0 COMMENT '点赞数',
                           `ip_address` VARCHAR(45) COMMENT '评论者IP',
                           `user_agent` VARCHAR(500) COMMENT '用户代理',
                           `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           FOREIGN KEY (`author_id`) REFERENCES `user_tb`(`id`),
                           FOREIGN KEY (`article_id`) REFERENCES `article`(`id`),
                           INDEX `idx_article` (`article_id`),
                           INDEX `idx_parent` (`parent_id`),
                           INDEX `idx_status` (`status`),
                           INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE `like_record` (
                               `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                               `user_id` BIGINT NOT NULL COMMENT '用户ID',
                               `target_id` BIGINT NOT NULL COMMENT '目标ID（文章ID或评论ID）',
                               `target_type` ENUM('ARTICLE', 'COMMENT') NOT NULL COMMENT '目标类型',
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
                               FOREIGN KEY (`user_id`) REFERENCES `user_tb`(`id`),
                               FOREIGN KEY (`target_id`) REFERENCES `article`(`id`),
                               FOREIGN KEY (`target_id`) REFERENCES `comment`(`id`),
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
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`sender_id`) REFERENCES `user_tb`(`id`),
    INDEX `idx_sender` (`sender_id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_parent` (`parent_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';




