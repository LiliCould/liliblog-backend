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
    INDEX `idx_status` (`status`),
    INDEX `idx_slug` (`slug`),
    INDEX `idx_name` (`name`),
    UNIQUE INDEX `uk_name` (`name`)
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
    `root_id` BIGINT DEFAULT 0 COMMENT '根评论ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态,0-审核中,1-发布',
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
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (1, 'Java', '#FFA500', '2025-12-09 23:12:07');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (2, 'Spring Boot', '#6DB33F', '2025-12-09 23:12:07');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (3, '数据库', '#4479A1', '2025-12-09 23:12:07');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (4, '微服务', '#FF6B6B', '2025-12-09 23:12:07');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (5, '前端', '#ff0123', '2026-01-15 02:11:14');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (6, 'vue3', '#1dbf8b', '2026-03-30 23:07:18');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (7, 'MySQL', '#00758f', '2026-04-03 21:22:39');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (8, 'Redis', '#dc382d', '2026-04-03 21:24:42');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (9, 'Docker', '#2496ed', '2026-04-03 21:25:01');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (10, '算法', '#7e57c2', '2026-04-03 21:25:19');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (11, '计算机网络', '#26a69a', '2026-04-03 21:25:36');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (12, '操作系统', '#795548', '2026-04-03 21:25:47');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (13, '面试', '#ffb300', '2026-04-03 21:26:02');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (14, '项目实战', '#00acc1', '2026-04-03 21:26:18');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (15, '开源', '#607d8b', '2026-04-03 21:26:36');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (16, '架构', '#5c6bc0', '2026-04-03 21:26:51');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (17, 'AI', '#ec407a', '2026-04-03 21:27:04');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (18, 'Android', '#3ddc84', '2026-04-03 21:27:24');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (19, 'Linux', '#fcc624', '2026-04-03 21:27:36');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (20, 'Git', '#f05032', '2026-04-03 21:29:25');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (21, 'Nginx', '#009639', '2026-04-03 21:29:38');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (22, 'Elasticsearch', '#005571', '2026-04-03 21:29:48');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (23, 'Kafka', '#231f20', '2026-04-03 21:29:58');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (24, 'RabbitMQ', '#ff6600', '2026-04-03 21:30:08');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (25, '前端工程化', '#2196f3', '2026-04-03 21:30:22');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (26, 'TypeScript', '#3178c6', '2026-04-03 21:30:31');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (27, '分布式', '#8e24aa', '2026-04-03 21:30:46');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (28, '高并发', '#f44336', '2026-04-03 21:31:20');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (29, '学习笔记', '#9c27b0', '2026-04-03 21:31:36');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (30, '工具', '#ff9800', '2026-04-03 21:31:46');
INSERT INTO `liliblog`.`tag` (`id`, `name`, `color`, `create_time`) VALUES (31, '生活', '#4caf50', '2026-04-03 21:32:07');


-- 文章数据
INSERT INTO `article` (`id`, `title`, `slug`, `summary`, `content`, `content_html`, `cover_image`, `status`, `view_count`, `category_id`, `create_by`) VALUES
(1, 'Spring Boot 入门教程', 'springboot-intro', '从零开始学习 Spring Boot 框架', '这是 Spring Boot 的入门内容...', '<p>这是 Spring Boot 的入门内容...</p>', NULL, 1, 100, 1, 1),
(2, 'MySQL 性能优化技巧', 'mysql-optimize', '分享一些 MySQL 数据库优化的实用技巧', 'MySQL 优化有很多方面...', '<p>MySQL 优化有很多方面...</p>', NULL, 1, 85, 1, 1),
(3, '周末爬山记', 'weekend-hiking', '记录周末去爬山的经历', '今天天气真好，去爬了附近的山...', '<p>今天天气真好，去爬了附近的山...</p>', NULL, 1, 50, 2, 2),
(4, 'Redis 缓存实战指南', 'redis-cache-practice', '深入理解 Redis 缓存机制与应用场景', 'Redis 作为高性能缓存解决方案...', '<p>Redis 作为高性能缓存解决方案...</p>', NULL, 1, 120, 1, 1),
(5, 'Docker 容器化部署最佳实践', 'docker-deployment', '如何使用 Docker 进行项目部署', 'Docker 容器化技术让部署更简单...', '<p>Docker 容器化技术让部署更简单...</p>', NULL, 1, 95, 1, 1),
(6, 'Vue3 组合式 API 详解', 'vue3-composition-api', '全面掌握 Vue3 的组合式 API', 'Vue3 引入了组合式 API，让代码组织更灵活...', '<p>Vue3 引入了组合式 API...</p>', NULL, 1, 110, 1, 2),
(7, 'Java 并发编程核心概念', 'java-concurrency', '深入理解 Java 多线程与并发编程', 'Java 并发编程是高级开发的必备技能...', '<p>Java 并发编程是高级开发的必备技能...</p>', NULL, 1, 130, 1, 1),
(8, '微服务架构设计原则', 'microservice-design', '微服务架构的设计模式与最佳实践', '微服务架构将应用拆分为多个独立服务...', '<p>微服务架构将应用拆分为多个独立服务...</p>', NULL, 1, 88, 1, 1),
(9, 'Git 常用命令速查表', 'git-commands', '开发者必备的 Git 命令大全', 'Git 是版本控制的核心工具...', '<p>Git 是版本控制的核心工具...</p>', NULL, 1, 150, 3, 1),
(10, 'Linux 系统管理基础', 'linux-basics', 'Linux 常用命令与系统管理技巧', 'Linux 是服务器端的主流操作系统...', '<p>Linux 是服务器端的主流操作系统...</p>', NULL, 1, 75, 3, 1),
(11, '算法复杂度分析入门', 'algorithm-complexity', '理解时间复杂度和空间复杂度', '算法复杂度是评估算法效率的关键指标...', '<p>算法复杂度是评估算法效率的关键指标...</p>', NULL, 1, 92, 3, 1),
(12, 'Nginx 反向代理配置指南', 'nginx-proxy', 'Nginx 反向代理与负载均衡配置', 'Nginx 作为高性能 Web 服务器...', '<p>Nginx 作为高性能 Web 服务器...</p>', NULL, 1, 68, 1, 1),
(13, 'Kafka 消息队列原理与实践', 'kafka-practice', '深入理解 Kafka 消息中间件', 'Kafka 是分布式流处理平台...', '<p>Kafka 是分布式流处理平台...</p>', NULL, 1, 105, 1, 1),
(14, 'TypeScript 类型系统详解', 'typescript-types', 'TypeScript 高级类型特性解析', 'TypeScript 的类型系统非常强大...', '<p>TypeScript 的类型系统非常强大...</p>', NULL, 1, 78, 1, 2),
(15, 'Elasticsearch 搜索引擎入门', 'elasticsearch-intro', 'Elasticsearch 基础概念与使用', 'Elasticsearch 是分布式搜索和分析引擎...', '<p>Elasticsearch 是分布式搜索和分析引擎...</p>', NULL, 1, 82, 1, 1),
(16, 'RabbitMQ 消息队列实战', 'rabbitmq-practice', 'RabbitMQ 的安装与基本使用', 'RabbitMQ 是流行的消息队列中间件...', '<p>RabbitMQ 是流行的消息队列中间件...</p>', NULL, 1, 70, 1, 1),
(17, '前端工程化体系建设', 'frontend-engineering', '构建现代化的前端工程化体系', '前端工程化包含模块化、组件化、自动化等...', '<p>前端工程化包含模块化、组件化...</p>', NULL, 1, 95, 1, 2);

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
