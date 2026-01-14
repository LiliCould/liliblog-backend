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
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    `slug` VARCHAR(50) UNIQUE NOT NULL COMMENT '分类别名',
    `description` VARCHAR(200) COMMENT '分类描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
    `name` VARCHAR(30) NOT NULL UNIQUE COMMENT '标签名称',
    `slug` VARCHAR(30) UNIQUE NOT NULL COMMENT '标签别名',
    `color` VARCHAR(7) DEFAULT '#ff9900' COMMENT '标签颜色',
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

INSERT INTO user_tb
(`username`, `email`, `password`, `nickname`, `avatar`, `role`, `status`, `last_login_time`)
VALUES
('LiliCould', '123456@qq.com', '$2a$10$lSWPDDR1nX.Isnyh95B67uF/zvp08JzwD04LQJpjAlSq58XaO1qkq', '立里可', NULL, 'VISITOR', 1, '2025-11-23 14:09:56'),
('HeTong', '3364724213@qq.com', '$2a$10$lSWPDDR1nX.Isnyh95B67uF/zvp08JzwD04LQJpjAlSq58XaO1qkq.', '何童', NULL, 'VISITOR', 1, '2025-11-23 13:55:01'),
('ZhangJian', '1234721234@qq.com', '$2a$10$lSWPDDR1nX.Isnyh95B67uF/zvp08JzwD04LQJpjAlSq58XaO1qkq', '张健', NULL, 'VISITOR', 1, '2025-11-23 13:58:01'),
('WuTao', '1234711124@qq.com', '$2a$10$lSWPDDR1nX.Isnyh95B67uF/zvp08JzwD04LQJpjAlSq58XaO1qkq', '吴涛', NULL, 'VISITOR', 1, '2025-11-23 13:58:00'),
('HuangCan', '1234722114@qq.com', '$2a$10$lSWPDDR1nX.Isnyh95B67uF/zvp08JzwD04LQJpjAlSq58XaO1qkq', '黄灿', NULL, 'VISITOR', 1, '2025-11-23 13:58:01'),
('PengZiying', '12233344455@qq.com', '$2a$10$lSWPDDR1nX.Isnyh95B67uF/zvp08JzwD04LQJpjAlSq58XaO1qkq', '彭紫莹', NULL, 'VISITOR', 1, '2025-11-23 13:58:00'),
('XuYanting', '2222334455@qq.com', '$2a$10$lSWPDDR1nX.Isnyh95B67uF/zvp08JzwD04LQJpjAlSq58XaO1qkq', '徐艳婷', NULL, 'VISITOR', 0, '2025-11-23 13:58:37');

INSERT INTO category (name, slug, description,status, create_time)
VALUES
('技术博客', 'tech-blog', '技术类文章', 1, '2025-11-24 00:00:00'),
('编程语言', 'programming-languages', '各种编程语言介绍', 1, '2025-11-24 00:00:00'),
('开发工具', 'development-tools', '开发工具和框架', 1, '2025-11-24 00:00:00'),
('云计算', 'cloud-computing', '云计算相关技术', 1, '2025-11-24 00:00:00');


INSERT INTO article
(title, slug, summary, content, content_html, cover_image, status, view_count, like_count, comment_count, is_top, is_recommend, author_id, category_id, create_time, update_time, publish_time)
VALUES
('Spring Boot实战指南', 'spring-boot-practice', '深入学习Spring Boot开发技巧', '<p>本文详细介绍Spring Boot的核心特性和最佳实践</p>', '<p>本文详细介绍Spring Boot的核心特性和最佳实践</p>', 'https://example.com/spring-cover.jpg', 'PUBLISHED', 150, 23, 8, 0, 1, 1, 1, '2025-11-21 10:00:00', '2025-11-21 10:00:00', '2025-11-21 10:00:00'),
('MySQL性能优化技巧', 'mysql-performance-tips', '提升数据库查询效率的方法', '<p>分享MySQL性能调优的实用技巧和经验</p>', '<p>分享MySQL性能调优的实用技巧和经验</p>', 'https://example.com/mysql-cover.jpg', 'PUBLISHED', 200, 35, 12, 0, 1, 2, 2, '2025-11-20 09:30:00', '2025-11-20 09:30:00', '2025-11-20 09:30:00'),
('Java多线程编程', 'java-multithreading', '掌握并发编程的核心概念', '<p>深入解析Java多线程编程的基础知识和高级特性</p>', '<p>深入解析Java多线程编程的基础知识和高级特性</p>', 'https://example.com/java-thread-cover.jpg', 'PUBLISHED', 180, 28, 15, 1, 1, 3, 3, '2025-11-19 14:20:00', '2025-11-19 14:20:00', '2025-11-19 14:20:00'),
('前端框架对比分析', 'frontend-framework-comparison', 'Vue、React和Angular的全面比较', '<p>对主流前端框架进行详细的功能和性能对比</p>', '<p>对主流前端框架进行详细的功能和性能对比</p>', 'https://example.com/frontend-cover.jpg', 'PUBLISHED', 250, 42, 20, 0, 1, 4, 4, '2025-11-18 11:15:00', '2025-11-18 11:15:00', '2025-11-18 11:15:00'),
('微服务架构设计', 'microservices-architecture', '构建可扩展的微服务系统', '<p>探讨微服务架构的设计原则和实施策略</p>', '<p>探讨微服务架构的设计原则和实施策略</p>', 'https://example.com/microservices-cover.jpg', 'PUBLISHED', 300, 55, 25, 1, 1, 4, 3, '2025-11-17 16:40:00', '2025-11-17 16:40:00', '2025-11-17 16:40:00'),
('RESTful API设计规范', 'restful-api-design', '创建高质量的API接口', '<p>遵循RESTful原则设计和实现Web API的最佳实践</p>', '<p>遵循RESTful原则设计和实现Web API的最佳实践</p>', 'https://example.com/rest-api-cover.jpg', 'PUBLISHED', 220, 38, 18, 0, 1,4, 4, '2025-11-16 08:50:00', '2025-11-16 08:50:00', '2025-11-16 08:50:00'),
('Docker容器化实践', 'docker-containerization', '使用Docker简化应用部署', '<p>介绍Docker的基本概念和实际应用案例</p>', '<p>介绍Docker的基本概念和实际应用案例</p>', 'https://example.com/docker-cover.jpg', 'PUBLISHED', 190, 32, 14, 0, 1, 2, 3, '2025-11-15 13:30:00', '2025-11-15 13:30:00', '2025-11-15 13:30:00'),
('Kubernetes集群管理', 'kubernetes-management', '高效管理容器化应用', '<p>详解Kubernetes的核心组件和运维技巧</p>', '<p>详解Kubernetes的核心组件和运维技巧</p>', 'https://example.com/kubernetes-cover.jpg', 'PUBLISHED', 280, 48, 22, 1, 1, 3, 3, '2025-11-14 10:25:00', '2025-11-14 10:25:00', '2025-11-14 10:25:00'),
('GraphQL入门教程', 'graphql-beginner', '从零开始学习GraphQL', '<p>GraphQL作为现代API技术的全面介绍</p>', '<p>GraphQL作为现代API技术的全面介绍</p>', 'https://example.com/graphql-cover.jpg', 'PUBLISHED', 170, 26, 10, 0, 1, 2, 3, '2025-11-13 15:10:00', '2025-11-13 15:10:00', '2025-11-13 15:10:00'),
('Node.js高性能开发', 'nodejs-high-performance', '构建高效的JavaScript应用', '<p>Node.js在高并发场景下的优化策略</p>', '<p>Node.js在高并发场景下的优化策略</p>', 'https://example.com/nodejs-cover.jpg', 'PUBLISHED', 210, 36, 16, 0, 1, 1, 4, '2025-11-12 09:45:00', '2025-11-12 09:45:00', '2025-11-12 09:45:00'),
('Python数据分析实战', 'python-data-analysis', '使用Python处理大数据', '<p>Python在数据科学领域的应用案例</p>', '<p>Python在数据科学领域的应用案例</p>', 'https://example.com/python-data-cover.jpg', 'PUBLISHED', 240, 45, 19, 1, 1, 2, 3, '2025-11-11 14:35:00', '2025-11-11 14:35:00', '2025-11-11 14:35:00'),
('Go语言并发编程', 'go-concurrency', 'Go语言的goroutine和channel', '<p>Go语言并发模型的深入解析</p>', '<p>Go语言并发模型的深入解析</p>', 'https://example.com/go-concurrency-cover.jpg', 'PUBLISHED', 185, 30, 13, 0, 1, 3, 3, '2025-11-10 11:20:00', '2025-11-10 11:20:00', '2025-11-10 11:20:00'),
('云计算基础概念', 'cloud-computing-basics', '了解云服务的核心原理', '<p>云计算技术的基本概念和发展趋势</p>', '<p>云计算技术的基本概念和发展趋势</p>', 'https://example.com/cloud-cover.jpg', 'PUBLISHED', 260, 40, 21, 0, 1, 3, 4, '2025-11-09 16:15:00', '2025-11-09 16:15:00', '2025-11-09 16:15:00'),
('DevOps实践指南', 'devops-practices', '实现持续集成和部署', '<p>DevOps工作流的最佳实践和工具链</p>', '<p>DevOps工作流的最佳实践和工具链</p>', 'https://example.com/devops-cover.jpg', 'PUBLISHED', 310, 52, 28, 1, 1, 1, 4, '2025-11-08 08:40:00', '2025-11-08 08:40:00', '2025-11-08 08:40:00'),
('机器学习入门', 'machine-learning-intro', '初学者的机器学习指南', '<p>机器学习的基本概念和算法介绍</p>', '<p>机器学习的基本概念和算法介绍</p>', 'https://example.com/ml-cover.jpg', 'PUBLISHED', 230, 39, 17, 0, 1, 2, 3, '2025-11-07 13:55:00', '2025-11-07 13:55:00', '2025-11-07 13:55:00'),
('区块链技术解析', 'blockchain-technology', '理解区块链的工作原理', '<p>区块链技术的底层机制和应用场景</p>', '<p>区块链技术的底层机制和应用场景</p>', 'https://example.com/blockchain-cover.jpg', 'PUBLISHED', 195, 33, 14, 0, 1, 3, 4, '2025-11-06 10:10:00', '2025-11-06 10:10:00', '2025-11-06 10:10:00'),
('人工智能发展趋势', 'ai-trends', 'AI技术的未来方向', '<p>人工智能领域最新的研究进展和产业应用</p>', '<p>人工智能领域最新的研究进展和产业应用</p>', 'https://example.com/ai-cover.jpg', 'PUBLISHED', 270, 47, 23, 1, 1, 2, 4, '2025-11-05 15:25:00', '2025-11-05 15:25:00', '2025-11-05 15:25:00'),
('网络安全防护策略', 'cybersecurity-strategies', '保护系统免受攻击', '<p>常见的网络安全威胁和防御措施</p>', '<p>常见的网络安全威胁和防御措施</p>', 'https://example.com/security-cover.jpg', 'PUBLISHED', 205, 37, 16, 0, 1, 1, 3, '2025-11-04 09:50:00', '2025-11-04 09:50:00', '2025-11-04 09:50:00'),
('大数据处理技术', 'big-data-processing', '高效处理海量数据', '<p>大数据处理框架和技术栈的选择与应用</p>', '<p>大数据处理框架和技术栈的选择与应用</p>', 'https://example.com/big-data-cover.jpg', 'PUBLISHED', 290, 50, 26, 1, 1, 2, 2, '2025-11-03 14:45:00', '2025-11-03 14:45:00', '2025-11-03 14:45:00'),
('软件架构设计模式', 'software-architecture-patterns', '构建可维护的系统架构', '<p>常见的软件架构模式和设计原则</p>', '<p>常见的软件架构模式和设计原则</p>', 'https://example.com/architecture-cover.jpg', 'PUBLISHED', 245, 43, 20, 0, 1, 2, 1, '2025-11-02 11:30:00', '2025-11-02 11:30:00', '2025-11-02 11:30:00');


INSERT INTO tag (name, slug, color) VALUES
('Java', 'java', '#FFA500'),
('Spring Boot', 'spring-boot', '#6DB33F'),
('数据库', 'database', '#4479A1'),
('微服务', 'microservices', '#FF6B6B');
