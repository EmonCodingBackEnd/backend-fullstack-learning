-- 仓储-ware
-- 创建数据库
CREATE DATABASE IF NOT EXISTS gulimall_wms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
use gulimall_wms;

/*Table structure for table `undo_log` */
DROP TABLE IF EXISTS `undo_log`;
-- for AT mode you must to init this sql for you business database. the seata server not need it.
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';

/*Data for the table `undo_log` */

/*
SQLyog Ultimate v11.25 (64 bit)
MySQL - 5.7.27 : Database - gulimall_wms
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`gulimall_wms` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `gulimall_wms`;

/*Table structure for table `wms_purchase` */

DROP TABLE IF EXISTS `wms_purchase`;

CREATE TABLE `wms_purchase` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT comment 'id',
                                `assignee_id` bigint(20) DEFAULT NULL comment '采购人id',
                                `assignee_name` varchar(255) DEFAULT NULL comment '采购人姓名',
                                `phone` char(13) DEFAULT NULL comment '采购人电话',
                                `priority` int(4) DEFAULT NULL comment '优先级',
                                `status` int(4) DEFAULT NULL comment '状态[0新建，1已分配，2已领取，3已完成，4有异常]',
                                `ware_id` bigint(20) DEFAULT NULL comment '仓库id',
                                `amount` decimal(18,4) DEFAULT NULL comment '采购总金额',
                                `create_time` datetime DEFAULT NULL comment '创建日期',
                                `update_time` datetime DEFAULT NULL comment '更新日期',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购信息';

/*Data for the table `wms_purchase` */

/*Table structure for table `wms_purchase_detail` */

DROP TABLE IF EXISTS `wms_purchase_detail`;

CREATE TABLE `wms_purchase_detail` (
                                       `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                       `purchase_id` bigint(20) DEFAULT NULL COMMENT '采购单id',
                                       `sku_id` bigint(20) DEFAULT NULL COMMENT '采购商品id',
                                       `sku_num` int(11) DEFAULT NULL COMMENT '采购数量',
                                       `sku_price` decimal(18,4) DEFAULT NULL COMMENT '采购金额',
                                       `ware_id` bigint(20) DEFAULT NULL COMMENT '仓库id',
                                       `status` int(11) DEFAULT NULL COMMENT '状态[0新建，1已分配，2正在采购，3已完成，4采购失败]',
                                       `reason` varchar(255) DEFAULT NULL COMMENT '采购失败原因',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '采购需求详情表';

/*Data for the table `wms_purchase_detail` */

/*Table structure for table `wms_ware_info` */

DROP TABLE IF EXISTS `wms_ware_info`;

CREATE TABLE `wms_ware_info` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                 `name` varchar(255) DEFAULT NULL COMMENT '仓库名',
                                 `address` varchar(255) DEFAULT NULL COMMENT '仓库地址',
                                 `areacode` varchar(20) DEFAULT NULL COMMENT '区域编码',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库信息';

/*Data for the table `wms_ware_info` */

/*Table structure for table `wms_ware_order_task` */

DROP TABLE IF EXISTS `wms_ware_order_task`;

CREATE TABLE `wms_ware_order_task` (
                                       `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                       `order_id` bigint(20) DEFAULT NULL COMMENT 'order_id',
                                       `order_sn` varchar(255) DEFAULT NULL COMMENT 'order_sn',
                                       `consignee` varchar(100) DEFAULT NULL COMMENT '收货人',
                                       `consignee_tel` char(15) DEFAULT NULL COMMENT '收货人电话',
                                       `delivery_address` varchar(500) DEFAULT NULL COMMENT '配送地址',
                                       `order_comment` varchar(200) DEFAULT NULL COMMENT '订单备注',
                                       `payment_way` tinyint(1) DEFAULT NULL COMMENT '付款方式【 1:在线付款 2:货到付款】',
                                       `task_status` tinyint(2) DEFAULT NULL COMMENT '任务状态',
                                       `order_body` varchar(255) DEFAULT NULL COMMENT '订单描述',
                                       `tracking_no` char(30) DEFAULT NULL COMMENT '物流单号',
                                       `create_time` datetime DEFAULT NULL COMMENT 'create_time',
                                       `ware_id` bigint(20) DEFAULT NULL COMMENT '仓库id',
                                       `task_comment` varchar(500) DEFAULT NULL COMMENT '工作单备注',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存工作单';

/*Data for the table `wms_ware_order_task` */

/*Table structure for table `wms_ware_order_task_detail` */

DROP TABLE IF EXISTS `wms_ware_order_task_detail`;

CREATE TABLE `wms_ware_order_task_detail` (
                                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                              `sku_id` bigint(20) DEFAULT NULL COMMENT 'sku_id',
                                              `sku_name` varchar(255) DEFAULT NULL COMMENT 'sku_name',
                                              `sku_num` int(11) DEFAULT NULL COMMENT '购买个数',
                                              `task_id` bigint(20) DEFAULT NULL COMMENT '工作单id',
                                              `ware_id` bigint(20) DEFAULT NULL COMMENT '仓库id',
                                              `lock_status` int(1) DEFAULT NULL COMMENT '1-已锁定  2-已解锁  3-扣减',
                                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存工作单';

/*Data for the table `wms_ware_order_task_detail` */

/*Table structure for table `wms_ware_sku` */

DROP TABLE IF EXISTS `wms_ware_sku`;

CREATE TABLE `wms_ware_sku` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                `sku_id` bigint(20) DEFAULT NULL COMMENT 'sku_id',
                                `ware_id` bigint(20) DEFAULT NULL COMMENT '仓库id',
                                `stock` int(11) DEFAULT NULL COMMENT '库存数',
                                `sku_name` varchar(200) DEFAULT NULL COMMENT 'sku_name',
                                `stock_locked` int(11) DEFAULT '0' COMMENT '锁定库存',
                                PRIMARY KEY (`id`),
                                KEY `sku_id` (`sku_id`) USING BTREE,
                                KEY `ware_id` (`ware_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品库存';

/*Data for the table `wms_ware_sku` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
