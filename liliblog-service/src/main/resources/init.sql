/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80026 (8.0.26)
 Source Host           : localhost:3306
 Source Schema         : liliblog

 Target Server Type    : MySQL
 Target Server Version : 80026 (8.0.26)
 File Encoding         : 65001

 Date: 29/06/2026 23:05:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章标题',
  `slug` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文章别名(用于URL)',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文章摘要',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章内容',
  `content_html` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文章HTML内容',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-审核中，1-发布，2-草稿',
  `view_count` int NULL DEFAULT 0 COMMENT '阅读数',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `slug`(`slug` ASC) USING BTREE,
  INDEX `idx_title`(`title` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  FULLTEXT INDEX `ft_title_content`(`title`, `content`)
) ENGINE = InnoDB AUTO_INCREMENT = 85 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of article
-- ----------------------------
INSERT INTO `article` VALUES (1, '关于我们', 'about', '关于我们', '# 关于我们 —— 海底小纵队\n\n## 🐙 队名由来\n\n传说在代码的深海中，有一支神秘的组织。他们不抓鱼，不捞虾，专抓 Bug。每当项目陷入黑暗，他们便会亮起头顶的探照灯，高喊：\n\n> **\"海底小纵队，集合！\"**\n\n没错，我们就是——**海底小纵队**。\n\n## 👥 成员介绍\n\n### 🐠 组长：何童\n\n**称号：** 章鱼队长（八只手写代码，六条腿修 Bug）\n\n作为全队的精神支柱和 Debug 天花板，何童组长不仅写代码快，甩锅更快（不是）。每当组员陷入「这个 Bug 我昨天明明改好了啊」的玄学困境时，何组长总能一眼瞟过去：「第四行多了个分号。」\n\n> **经典语录：** \"你们先别动，让我来——好了，修好了，我改了啥？我也不知道。\"\n\n### 🦐 组员：黄宇豪\n\n**称号：** 皮皮虾（速度快，但经常走过头）\n\n黄宇豪同学写代码的速度和他的手速一样快——快到有时候脑子追不上。他负责的模块总是第一个提交，但也总是第一个跑不起来。不过没关系，他坚信：\n\n> **\"先跑起来再说，优化是明天的事。\"**\n\n名言：一个能跑的 Demo > 十个没写完的完美架构。\n\n### 🐢 组员：谭勇\n\n**称号：** 海龟老司机（稳，是真的稳）\n\n如果说黄宇豪是火箭，那谭勇就是潜水艇——慢，但稳如老狗。他写的代码就像他的性格一样：注释比代码多，测试覆盖率 100%，代码检查从来没人敢说啥（因为大家都看不懂，但确实能跑）。\n\n> **经典语录：** \"别急，让我先画个流程图。\"\n\n### 🦑 组员：张逸儒\n\n**称号：** 灯笼鱼（黑暗中唯一的光——专治各种脑洞大开）\n\n张逸儒是全队的「点子王」。大家说「要不我们加个 AI 写诗功能？」，张逸儒说「行，我来搞」。他的神奇之处在于：能把各种突发奇想，用最正经的方式实现出来，最后效果竟然还不错。\n\n> **经典语录：** \"这个能做，但我建议我们先看看时间够不够。\"\n\n## ⚡ 团队技能\n\n| 技能               | 说明                                             |\n| ------------------ | ------------------------------------------------ |\n| **Bug 制造** | ⭐⭐⭐⭐⭐（先制造再修复，形成内循环）           |\n| **需求分析** | ⭐⭐⭐⭐（主要靠猜）                             |\n| **代码审查** | ⭐⭐⭐（打开文件 → 看到改动量太大 → 关闭文件） |\n| **提测速度** | ⭐⭐⭐⭐⭐（质量另说）                           |\n| **线上救火** | ⭐⭐⭐⭐⭐（毕竟火是自己放的）                   |\n\n## 🎯 团队使命\n\n**让每一行代码都充满爱（和 Bug）。**\n\n我们的口号是：\n\n> **海底小纵队，写完就收队。** 🌊\n\n*（本文档仅供娱乐，如有雷同，那一定是你们组的日常。）*\n', '<h1 id=\"关于我们--海底小纵队\">关于我们 —— 海底小纵队</h1>\n<h2 id=\"-队名由来\">🐙 队名由来</h2>\n<p>传说在代码的深海中，有一支神秘的组织。他们不抓鱼，不捞虾，专抓 Bug。每当项目陷入黑暗，他们便会亮起头顶的探照灯，高喊：</p>\n<blockquote>\n  <p><strong>&quot;海底小纵队，集合！&quot;</strong></p>\n</blockquote>\n<p>没错，我们就是——<strong>海底小纵队</strong>。</p>\n<h2 id=\"-成员介绍\">👥 成员介绍</h2>\n<h3 id=\"-组长何童\">🐠 组长：何童</h3>\n<p><strong>称号：</strong> 章鱼队长（八只手写代码，六条腿修 Bug）</p>\n<p>作为全队的精神支柱和 Debug 天花板，何童组长不仅写代码快，甩锅更快（不是）。每当组员陷入「这个 Bug 我昨天明明改好了啊」的玄学困境时，何组长总能一眼瞟过去：「第四行多了个分号。」</p>\n<blockquote>\n  <p><strong>经典语录：</strong> &quot;你们先别动，让我来——好了，修好了，我改了啥？我也不知道。&quot;</p>\n</blockquote>\n<h3 id=\"-组员黄宇豪\">🦐 组员：黄宇豪</h3>\n<p><strong>称号：</strong> 皮皮虾（速度快，但经常走过头）</p>\n<p>黄宇豪同学写代码的速度和他的手速一样快——快到有时候脑子追不上。他负责的模块总是第一个提交，但也总是第一个跑不起来。不过没关系，他坚信：</p>\n<blockquote>\n  <p><strong>&quot;先跑起来再说，优化是明天的事。&quot;</strong></p>\n</blockquote>\n<p>名言：一个能跑的 Demo &gt; 十个没写完的完美架构。</p>\n<h3 id=\"-组员谭勇\">🐢 组员：谭勇</h3>\n<p><strong>称号：</strong> 海龟老司机（稳，是真的稳）</p>\n<p>如果说黄宇豪是火箭，那谭勇就是潜水艇——慢，但稳如老狗。他写的代码就像他的性格一样：注释比代码多，测试覆盖率 100%，代码检查从来没人敢说啥（因为大家都看不懂，但确实能跑）。</p>\n<blockquote>\n  <p><strong>经典语录：</strong> &quot;别急，让我先画个流程图。&quot;</p>\n</blockquote>\n<h3 id=\"-组员张逸儒\">🦑 组员：张逸儒</h3>\n<p><strong>称号：</strong> 灯笼鱼（黑暗中唯一的光——专治各种脑洞大开）</p>\n<p>张逸儒是全队的「点子王」。大家说「要不我们加个 AI 写诗功能？」，张逸儒说「行，我来搞」。他的神奇之处在于：能把各种突发奇想，用最正经的方式实现出来，最后效果竟然还不错。</p>\n<blockquote>\n  <p><strong>经典语录：</strong> &quot;这个能做，但我建议我们先看看时间够不够。&quot;</p>\n</blockquote>\n<h2 id=\"-团队技能\">⚡ 团队技能</h2>\n<table>\n  <thead>\n    <tr><th>技能</th><th>说明</th></tr>\n  </thead>\n  <tbody>\n    <tr><td><strong>Bug 制造</strong></td><td>⭐⭐⭐⭐⭐（先制造再修复，形成内循环）</td></tr>\n    <tr><td><strong>需求分析</strong></td><td>⭐⭐⭐⭐（主要靠猜）</td></tr>\n    <tr><td><strong>代码审查</strong></td><td>⭐⭐⭐（打开文件 → 看到改动量太大 → 关闭文件）</td></tr>\n    <tr><td><strong>提测速度</strong></td><td>⭐⭐⭐⭐⭐（质量另说）</td></tr>\n    <tr><td><strong>线上救火</strong></td><td>⭐⭐⭐⭐⭐（毕竟火是自己放的）</td></tr>\n  </tbody>\n</table>\n<h2 id=\"-团队使命\">🎯 团队使命</h2>\n<p><strong>让每一行代码都充满爱（和 Bug）。</strong></p>\n<p>我们的口号是：</p>\n<blockquote>\n  <p><strong>海底小纵队，写完就收队。</strong> 🌊</p>\n</blockquote>\n<p><em>（本文档仅供娱乐，如有雷同，那一定是你们组的日常。）</em></p>\n', 'https://oss.lilicould.cn/cover/b9bdfad8-1566-4642-a366-5c9ee2e44e78_专业团队.jpg', 1, 206, 2, '2026-05-18 11:00:15', '2026-06-09 14:36:26', 2, 2, 0);
INSERT INTO `article` VALUES (2, '水帖', 'share', '意见和交流', '# 水贴\n\n## 意见和交流\n\n欢迎在本贴评论区留下你宝贵的意见以及友善的交流！\n\n## 审核机制\n\n为了博客和谐，本博客所发的所有内容需要审核后才可被其他人查看。\n', '<h1 id=\"水贴\">水贴</h1>\n<h2 id=\"意见和交流\">意见和交流</h2>\n<p>欢迎在本贴评论区留下你宝贵的意见以及友善的交流！</p>\n<h2 id=\"审核机制\">审核机制</h2>\n<p>为了博客和谐，本博客所发的所有内容需要审核后才可被其他人查看。</p>\n', 'https://oss.lilicould.cn/cover/d884d4de-b427-4bd8-9a1e-09b7d034850b_hero-2.png', 1, 76, 2, '2026-05-19 14:34:19', '2026-05-28 22:29:07', 2, 2, 0);

-- ----------------------------
-- Table structure for article_tag
-- ----------------------------
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_article_tag`(`article_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_article`(`article_id` ASC) USING BTREE,
  INDEX `idx_tag`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 172 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章标签关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of article_tag
-- ----------------------------
INSERT INTO `article_tag` VALUES (3, 2, 3, '2026-05-16 21:05:05', 1);
INSERT INTO `article_tag` VALUES (4, 2, 4, '2026-05-16 21:05:05', 1);
INSERT INTO `article_tag` VALUES (170, 1, 31, '2026-06-09 14:36:26', 2);
INSERT INTO `article_tag` VALUES (171, 1, 30, '2026-06-09 14:36:26', 2);

-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户名',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块名称(article/comment/user/category/tag)',
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型(CREATE/UPDATE/DELETE/AUDIT)',
  `target` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标资源ID/IDS,例如批量删除操作',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标资源类型',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作描述',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP方法',
  `request_uri` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URI',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
  `execution_time` int NULL DEFAULT NULL COMMENT '执行时间(ms)',
  `status` tinyint NULL DEFAULT 1 COMMENT '操作状态(1成功/0失败)',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`create_time` ASC) USING BTREE,
  INDEX `idx_module_operation`(`module` ASC, `operation` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作审计日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of audit_log
-- ----------------------------
INSERT INTO `audit_log` VALUES (12, 'lilicould', 'article', 'CREATE', '测试文章', 'ARTICLE', '创建文章: 测试文章', 'POST', '/api/article', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36 Edg/148.0.0.0', 243, 1, NULL, '2026-05-25 13:38:15', 2);
INSERT INTO `audit_log` VALUES (13, 'admin', 'article', 'AUDIT', '83', 'ARTICLE', '审核文章:83', 'PUT', '/api/admin/article/83/1', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', 42, 1, NULL, '2026-05-25 13:38:33', 1);
INSERT INTO `audit_log` VALUES (14, 'lilicould', 'article', 'DELETE', '83', 'ARTICLE', '删除文章:83', 'DELETE', '/api/article/83', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36 Edg/148.0.0.0', 8, 1, NULL, '2026-05-25 13:38:48', 2);
INSERT INTO `audit_log` VALUES (15, 'admin', 'category', 'UPDATE', '测试审计', 'CATEGORY', '新增分类:测试审计', 'POST', '/api/admin/category', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', 8, 1, NULL, '2026-05-25 13:39:17', 1);
INSERT INTO `audit_log` VALUES (16, 'admin', 'tag', 'CREATE', '测试审计标签', 'TAG', '新增标签:测试审计标签', 'POST', '/api/admin/tag', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', 5, 1, NULL, '2026-05-25 13:39:46', 1);
INSERT INTO `audit_log` VALUES (17, 'admin', 'category', 'UPDATE', '6', 'CATEGORY', '更新分类:6', 'PUT', '/api/admin/category/6', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', 49, 1, NULL, '2026-05-25 13:40:42', 1);
INSERT INTO `audit_log` VALUES (18, 'admin', 'category', 'DELETE', '6', 'CATEGORY', '删除分类:6', 'DELETE', '/api/admin/category/6', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', 8, 1, NULL, '2026-05-25 13:40:51', 1);
INSERT INTO `audit_log` VALUES (19, 'admin', 'tag', 'DELETE', '34', 'TAG', '删除标签:34', 'DELETE', '/api/admin/tag/34', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', 10, 1, NULL, '2026-05-25 13:42:36', 1);
INSERT INTO `audit_log` VALUES (20, 'admin', 'user', 'CREATE', 'lilicloud', 'USER', '新增用户:lilicloud', 'POST', '/api/admin/user', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', 91, 1, NULL, '2026-05-25 14:05:09', 1);
INSERT INTO `audit_log` VALUES (21, 'admin', 'user', 'UPDATE', '6', 'USER', '更新用户:6', 'PUT', '/api/admin/user/id', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', 22, 1, NULL, '2026-05-25 14:05:19', 1);
INSERT INTO `audit_log` VALUES (22, 'admin', 'user', 'UPDATE', '6', 'USER', '更新用户:6', 'PUT', '/api/admin/user/id', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', 3, 1, NULL, '2026-05-25 14:05:38', 1);
INSERT INTO `audit_log` VALUES (23, 'lilicould', 'article', 'CREATE', '测试', 'ARTICLE', '创建文章: 测试', 'POST', '/api/article', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36 Edg/148.0.0.0', 311, 1, NULL, '2026-05-25 15:21:40', 2);
INSERT INTO `audit_log` VALUES (24, 'admin', 'article', 'AUDIT', '84', 'ARTICLE', '审核文章:84', 'PUT', '/api/admin/article/84/1', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', 21, 1, NULL, '2026-05-25 15:22:04', 1);
INSERT INTO `audit_log` VALUES (25, 'admin', 'user', 'UPDATE', '2', 'USER', '更新用户:2', 'PUT', '/api/admin/user/id', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0', 81, 1, NULL, '2026-06-09 14:22:25', 1);
INSERT INTO `audit_log` VALUES (26, 'user', 'article', 'UPDATE', '1', 'ARTICLE', '更新文章:1', 'PUT', '/api/article/1', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0', 218, 1, NULL, '2026-06-09 14:36:26', 2);
INSERT INTO `audit_log` VALUES (27, 'admin', 'article', 'AUDIT', '1', 'ARTICLE', '审核文章:1', 'PUT', '/api/admin/article/1/1', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0', 33, 1, NULL, '2026-06-09 14:37:34', 1);

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `slug` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类别名',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE,
  UNIQUE INDEX `slug`(`slug` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_slug`(`slug` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '技术分享', 'tech', '编程技术与开发心得', 1, 1, '2026-05-19 10:31:44', '2026-05-24 12:01:47', 0, 1, 1);
INSERT INTO `category` VALUES (2, '生活随笔', 'life', '日常生活记录与感悟', 2, 1, '2026-05-19 10:31:44', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `category` VALUES (3, '学习笔记', 'study', '学习过程中的笔记总结', 3, 1, '2026-05-19 10:31:44', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `category` VALUES (4, '生活趣事', 'life--2', '分享生活小事', 100, 1, '2026-05-24 11:59:14', '2026-05-24 12:00:22', 1, 1, 0);

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父评论ID',
  `root_id` bigint NULL DEFAULT 0 COMMENT '根评论ID',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态,0-审核中,1-发布',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论者IP',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article`(`article_id` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, '沙发！', 2, 0, 1, 1, '120.203.25.242', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36 Edg/148.0.0.0', '2026-05-19 16:14:16', 2, 0);
INSERT INTO `comment` VALUES (2, '一楼', 2, 0, 2, 1, '117.40.111.145', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36', '2026-05-19 16:46:36', 4, 0);

-- ----------------------------
-- Table structure for like_record
-- ----------------------------
DROP TABLE IF EXISTS `like_record`;
CREATE TABLE `like_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID（文章ID或评论ID）',
  `target_type` tinyint NOT NULL COMMENT '目标类型，0-文章，1-评论',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_id` ASC, `target_type` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_id` ASC, `target_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of like_record
-- ----------------------------
INSERT INTO `like_record` VALUES (1, 2, 63, 0, '2026-05-19 21:24:06', 2);
INSERT INTO `like_record` VALUES (2, 2, 74, 0, '2026-05-23 12:36:52', 2);

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `color` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#666666' COMMENT '标签颜色',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE,
  INDEX `idx_id`(`id` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tag
-- ----------------------------
INSERT INTO `tag` VALUES (1, '随笔', '#000000', '2025-12-09 23:12:07', '2026-05-24 14:37:11', 0, 1, 0);
INSERT INTO `tag` VALUES (2, 'Spring Boot', '#6DB33F', '2025-12-09 23:12:07', '2026-05-24 14:36:57', 0, 1, 0);
INSERT INTO `tag` VALUES (3, '数据库', '#4479A1', '2025-12-09 23:12:07', '2026-05-24 14:36:57', 0, 1, 0);
INSERT INTO `tag` VALUES (4, '微服务', '#FF6B6B', '2025-12-09 23:12:07', '2026-05-24 14:36:57', 0, 1, 0);
INSERT INTO `tag` VALUES (5, '前端', '#ff0123', '2026-01-15 02:11:14', '2026-05-24 14:36:57', 0, 1, 0);
INSERT INTO `tag` VALUES (6, 'vue3', '#1dbf8b', '2026-03-30 23:07:18', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (7, 'MySQL', '#00758f', '2026-04-03 21:22:39', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (8, 'Redis', '#dc382d', '2026-04-03 21:24:42', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (9, 'Docker', '#2496ed', '2026-04-03 21:25:01', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (10, '算法', '#7e57c2', '2026-04-03 21:25:19', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (11, '计算机网络', '#26a69a', '2026-04-03 21:25:36', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (12, '操作系统', '#795548', '2026-04-03 21:25:47', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (13, '面试', '#ffb300', '2026-04-03 21:26:02', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (14, '项目实战', '#00acc1', '2026-04-03 21:26:18', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (15, '开源', '#607d8b', '2026-04-03 21:26:36', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (16, '架构', '#5c6bc0', '2026-04-03 21:26:51', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (17, 'AI', '#ec407a', '2026-04-03 21:27:04', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (18, 'Android', '#3ddc84', '2026-04-03 21:27:24', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (19, 'Linux', '#fcc624', '2026-04-03 21:27:36', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (20, 'Git', '#f05032', '2026-04-03 21:29:25', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (21, 'Nginx', '#009639', '2026-04-03 21:29:38', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (22, 'Elasticsearch', '#005571', '2026-04-03 21:29:48', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (23, 'Kafka', '#231f20', '2026-04-03 21:29:58', '2026-05-19 10:31:44', 0, NULL, 0);
INSERT INTO `tag` VALUES (24, 'RabbitMQ', '#ff6600', '2026-04-03 21:30:08', '2026-05-19 10:31:45', 0, NULL, 0);
INSERT INTO `tag` VALUES (25, '前端工程化', '#2196f3', '2026-04-03 21:30:22', '2026-05-19 10:31:45', 0, NULL, 0);
INSERT INTO `tag` VALUES (26, 'TypeScript', '#3178c6', '2026-04-03 21:30:31', '2026-05-19 10:31:45', 0, NULL, 0);
INSERT INTO `tag` VALUES (27, '分布式', '#8e24aa', '2026-04-03 21:30:46', '2026-05-19 10:31:45', 0, NULL, 0);
INSERT INTO `tag` VALUES (28, '高并发', '#f44336', '2026-04-03 21:31:20', '2026-05-19 10:31:45', 0, NULL, 0);
INSERT INTO `tag` VALUES (29, '学习笔记', '#9c27b0', '2026-04-03 21:31:36', '2026-05-19 10:31:45', 0, NULL, 0);
INSERT INTO `tag` VALUES (30, '工具', '#ff9800', '2026-04-03 21:31:46', '2026-05-19 10:31:45', 0, NULL, 0);
INSERT INTO `tag` VALUES (31, '生活', '#4caf50', '2026-04-03 21:32:07', '2026-05-19 10:31:45', 0, NULL, 0);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `github_id` bigint NULL DEFAULT NULL COMMENT 'GitHub ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` tinyint NOT NULL DEFAULT 1 COMMENT '角色：0-ADMIN，1-USER',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT 0 COMMENT '创建者 默认为0-管理员',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `uk_github_id`(`github_id` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, NULL, 'admin', 'admin@admin.com', '$2a$10$gBWFF8tvbP9Ac04oDE3mrOEjAwKBYtg9LDkafoY6qFeKH84G9oJmq', '管理员', 'https://oss.lilicould.cn/avatar/7a36729b-1916-427c-824d-f22d57c8c8ac_xiaodingdang.ico', 0, 1, '2026-06-29 22:44:13', '2026-05-09 00:12:16', '2026-06-29 22:44:13', 0, NULL, 0);
INSERT INTO `user` VALUES (2, NULL, 'user', 'demo@demo.com', '$2a$10$5BZi5p9/I.Gbj0TSx0qmd.u2HuqGRXKrgYWFQKpjPVHr.VhC9Wlvi', '默认用户', 'https://oss.lilicould.cn/cover/df3523eb-ee5d-43c7-95bf-c85d78444484_xiaodingdang.png', 1, 1, '2026-06-09 23:31:00', '2026-05-09 17:16:33', '2026-06-09 23:31:00', 0, NULL, 0);

SET FOREIGN_KEY_CHECKS = 1;
