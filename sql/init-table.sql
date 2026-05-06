CREATE DATABASE IF NOT EXISTS `campus_life` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `campus_life`;

CREATE TABLE sys_user (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          student_no VARCHAR(32) NOT NULL UNIQUE COMMENT '学号',
                          username VARCHAR(50) NOT NULL COMMENT '用户名',
                          password VARCHAR(100) NOT NULL COMMENT '密码',
                          phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                          email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                          avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
                          college VARCHAR(100) DEFAULT NULL COMMENT '学院',
                          major VARCHAR(100) DEFAULT NULL COMMENT '专业',
                          role VARCHAR(32) NOT NULL DEFAULT 'STUDENT' COMMENT '角色',
                          status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE market_item (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             seller_id BIGINT NOT NULL COMMENT '卖家ID',
                             title VARCHAR(100) NOT NULL COMMENT '标题',
                             description TEXT COMMENT '描述',
                             price DECIMAL(10,2) NOT NULL COMMENT '价格',
                             category VARCHAR(50) DEFAULT NULL COMMENT '分类',
                             cover_image VARCHAR(255) DEFAULT NULL COMMENT '封面图',
                             images TEXT COMMENT '图片，JSON或逗号分隔',
                             contact_info VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
                             status VARCHAR(32) NOT NULL DEFAULT 'ON_SALE' COMMENT '状态',
                             view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
                             create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE help_task (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           publisher_id BIGINT NOT NULL COMMENT '发布者ID',
                           accepter_id BIGINT DEFAULT NULL COMMENT '接单者ID',
                           title VARCHAR(100) NOT NULL COMMENT '任务标题',
                           description TEXT COMMENT '任务描述',
                           task_type VARCHAR(50) NOT NULL COMMENT '任务类型',
                           reward DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '悬赏金额',
                           pickup_address VARCHAR(255) DEFAULT NULL COMMENT '取货/开始地址',
                           delivery_address VARCHAR(255) DEFAULT NULL COMMENT '送达地址',
                           deadline DATETIME DEFAULT NULL COMMENT '截止时间',
                           contact_info VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
                           status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED' COMMENT '任务状态',
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE club (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      name VARCHAR(100) NOT NULL COMMENT '社团名称',
                      description TEXT COMMENT '社团介绍',
                      logo VARCHAR(255) DEFAULT NULL COMMENT '社团Logo',
                      admin_id BIGINT NOT NULL COMMENT '社团负责人ID',
                      contact_info VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE activity (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          club_id BIGINT DEFAULT NULL COMMENT '所属社团ID',
                          publisher_id BIGINT NOT NULL COMMENT '发布者ID',
                          title VARCHAR(100) NOT NULL COMMENT '活动标题',
                          description TEXT COMMENT '活动描述',
                          location VARCHAR(255) DEFAULT NULL COMMENT '活动地点',
                          cover_image VARCHAR(255) DEFAULT NULL COMMENT '封面图',
                          start_time DATETIME NOT NULL COMMENT '开始时间',
                          end_time DATETIME NOT NULL COMMENT '结束时间',
                          signup_deadline DATETIME DEFAULT NULL COMMENT '报名截止时间',
                          max_participants INT DEFAULT NULL COMMENT '最大报名人数',
                          current_participants INT NOT NULL DEFAULT 0 COMMENT '当前报名人数',
                          status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态',
                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE activity_registration (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       activity_id BIGINT NOT NULL,
                                       user_id BIGINT NOT NULL,
                                       status VARCHAR(32) NOT NULL DEFAULT 'REGISTERED',
                                       create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       UNIQUE KEY uk_activity_user (activity_id, user_id)
);

CREATE TABLE notification (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              receiver_id BIGINT NOT NULL COMMENT '接收人ID',
                              title VARCHAR(100) NOT NULL COMMENT '标题',
                              content VARCHAR(500) NOT NULL COMMENT '内容',
                              type VARCHAR(32) NOT NULL COMMENT '通知类型',
                              business_id BIGINT DEFAULT NULL COMMENT '业务ID',
                              business_type VARCHAR(32) DEFAULT NULL COMMENT '业务类型',
                              read_status TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读，1已读',
                              create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);