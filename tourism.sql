/*
 Navicat Premium Data Transfer

 Source Server         : design
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : tourism

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 09/05/2026 17:42:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for balance_change
-- ----------------------------
DROP TABLE IF EXISTS `balance_change`;
CREATE TABLE `balance_change`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uid` int UNSIGNED NOT NULL,
  `change_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'RECHARGE/PURCHASE/REFUND/ADMIN_ADJUST/WITHDRAW_REQUEST/PARTIAL_REFUND',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `change_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '正数表示增加，负数表示减少',
  `before_balance` decimal(12, 2) NULL DEFAULT NULL,
  `after_balance` decimal(12, 2) NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0待处理,1已完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_balance_change_uid_time`(`uid` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `fk_balance_change_uid` FOREIGN KEY (`uid`) REFERENCES `user` (`uid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账户余额变动流水' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for client
-- ----------------------------
DROP TABLE IF EXISTS `client`;
CREATE TABLE `client`  (
  `cno` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cname` varchar(8) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `csex` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `cage` int NULL DEFAULT NULL,
  `caddress` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `cphone` varchar(12) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `cdate` datetime(6) NULL DEFAULT NULL,
  `cremark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `uid` int UNSIGNED NULL DEFAULT NULL,
  `balance` double NULL DEFAULT NULL,
  PRIMARY KEY (`cno`) USING BTREE,
  INDEX `client_uid`(`uid` ASC) USING BTREE,
  CONSTRAINT `client_uid` FOREIGN KEY (`uid`) REFERENCES `user` (`uid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for cultural_product
-- ----------------------------
DROP TABLE IF EXISTS `cultural_product`;
CREATE TABLE `cultural_product`  (
  `productCode` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `productName` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `displayMode` char(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `highlight` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NULL DEFAULT NULL,
  `stock` int NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`productCode`) USING BTREE,
  INDEX `mprice`(`price` ASC) USING BTREE,
  INDEX `mname`(`productName` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for message_center
-- ----------------------------
DROP TABLE IF EXISTS `message_center`;
CREATE TABLE `message_center`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uid` int NOT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ref_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_read` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_type_ref`(`uid` ASC, `message_type` ASC, `ref_id` ASC) USING BTREE,
  INDEX `idx_user_time`(`uid` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `orderId` int NOT NULL AUTO_INCREMENT,
  `CreateDate` datetime NULL DEFAULT NULL,
  `totalPrice` double NOT NULL,
  `isCompleted` tinyint(1) NULL DEFAULT NULL,
  `uid` int UNSIGNED NOT NULL DEFAULT 100000000,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `agency` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `CompleteDate` datetime NULL DEFAULT NULL,
  `pickupCode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取件码',
  `pickupToken` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取货二维码加密内容',
  `pickupStatus` tinyint NULL DEFAULT 0 COMMENT '取货状态(0未取货/1已取货)',
  `pickupTime` datetime NULL DEFAULT NULL COMMENT '取货时间',
  `orderStatus` int NULL DEFAULT 0 COMMENT '0待支付 1待取货 2已关闭 3已完成',
  `refundReason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `refundOriginStatus` int NULL DEFAULT NULL,
  `refundRequestTime` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`orderId`) USING BTREE,
  INDEX `uid`(`uid` ASC) USING BTREE,
  CONSTRAINT `order_uid` FOREIGN KEY (`uid`) REFERENCES `user` (`uid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 278 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for orderdetails
-- ----------------------------
DROP TABLE IF EXISTS `orderdetails`;
CREATE TABLE `orderdetails`  (
  `detailsId` int NOT NULL AUTO_INCREMENT,
  `orderId` int NOT NULL,
  `productCode` char(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `purchaseNum` int NULL DEFAULT NULL,
  `price` decimal(10, 2) NULL DEFAULT NULL,
  `productName` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`detailsId`) USING BTREE,
  INDEX `order_id`(`orderId` ASC) USING BTREE,
  INDEX `order_mname`(`productName` ASC) USING BTREE,
  INDEX `order_mno`(`productCode` ASC) USING BTREE,
  INDEX `order_price`(`price` ASC) USING BTREE,
  CONSTRAINT `order_id` FOREIGN KEY (`orderId`) REFERENCES `order` (`orderId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `order_mname` FOREIGN KEY (`productName`) REFERENCES `cultural_product` (`productName`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `order_mno` FOREIGN KEY (`productCode`) REFERENCES `cultural_product` (`productCode`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `order_price` FOREIGN KEY (`price`) REFERENCES `cultural_product` (`price`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 466 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `uid` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `authority` int UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`uid`) USING BTREE,
  UNIQUE INDEX `user_username`(`username` ASC) USING BTREE,
  INDEX `uid`(`uid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100000086 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for withdraw_request
-- ----------------------------
DROP TABLE IF EXISTS `withdraw_request`;
CREATE TABLE `withdraw_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uid` int UNSIGNED NOT NULL,
  `amount` decimal(12, 2) NOT NULL,
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待审核,1已通过,2已驳回',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `review_uid` int NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_withdraw_uid_time`(`uid` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_withdraw_status_time`(`status` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `fk_withdraw_uid` FOREIGN KEY (`uid`) REFERENCES `user` (`uid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '余额提现申请单' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
