/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP TABLE IF EXISTS `t_account`;
CREATE TABLE `t_account` (
  `id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '主键',
  `username` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '用户名',
  `useralias` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '用户姓名',
  `password` varchar(128) COLLATE utf8_unicode_ci NOT NULL COMMENT '密码',
  `phone` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '手机号码',
  `role` tinyint(4) NOT NULL COMMENT '角色 乘客：0 司机：1',
  `avatar` varchar(1024) COLLATE utf8_unicode_ci NOT NULL DEFAULT '/web/img/default-header.jpg' COMMENT '用户头像',
  `paycode` varchar(1024) COLLATE utf8_unicode_ci NOT NULL DEFAULT '/web/img/default-paycode.jpg' COMMENT '收款码',
  `status` tinyint(4) DEFAULT NULL COMMENT '实名认证:0 未认证,1:已认证',
  `REVISION` int(11) DEFAULT NULL COMMENT '乐观锁',
  `CREATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATED_TIME` datetime DEFAULT NULL COMMENT '创建时间',
  `UPDATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATED_TIME` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='用户表';

DROP TABLE IF EXISTS `t_attachment`;
CREATE TABLE `t_attachment` (
  `id` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '主键',
  `name` varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT '文件名称',
  `url` varchar(200) COLLATE utf8_unicode_ci NOT NULL COMMENT '文件URL',
  `lenght` int(9) NOT NULL COMMENT '文件大小',
  `ext` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '文件扩展名',
  `md5` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '文件签名',
  `status` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '状态',
  `REVISION` int(11) DEFAULT NULL COMMENT '乐观锁',
  `CREATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATED_TIME` datetime DEFAULT NULL COMMENT '创建时间',
  `UPDATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATED_TIME` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS `t_authentication`;
CREATE TABLE `t_authentication` (
  `id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '主键',
  `useralias` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '用户姓名',
  `phone` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '手机号码',
  `birth` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '出生年月',
  `personal_photo` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '个人照片',
  `card_id` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '身份证号码',
  `card_id_front_photo` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '身份证正面照片',
  `card_id_back_photo` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '身份证背面照片',
  `status` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '认证状态 未认证：0认证成功：1认证失败：2',
  `REVISION` int(11) DEFAULT NULL COMMENT '乐观锁',
  `CREATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATED_TIME` datetime DEFAULT NULL COMMENT '创建时间',
  `UPDATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATED_TIME` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='用户认证';

DROP TABLE IF EXISTS `t_chat_message`;
CREATE TABLE `t_chat_message` (
  `id` varchar(64) NOT NULL COMMENT '主键',
  `conversation_id` varchar(64) NOT NULL COMMENT '会话ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `role` varchar(16) NOT NULL COMMENT '角色: user / assistant / system',
  `content` text NOT NULL COMMENT '消息内容',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天消息记录';

DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '主键',
  `passenger_id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '乘客ID',
  `passenger_stroke_id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '乘客行程ID',
  `driver_id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '司机ID',
  `driver_stroke_id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '司机行程ID',
  `distance` int(11) NOT NULL COMMENT '米',
  `estimated_time` int(11) NOT NULL COMMENT '秒',
  `cost` float NOT NULL COMMENT '价格',
  `status` tinyint(4) NOT NULL COMMENT '状态',
  `REVISION` int(11) DEFAULT NULL COMMENT '乐观锁',
  `CREATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATED_TIME` datetime DEFAULT NULL COMMENT '创建时间',
  `UPDATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATED_TIME` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `driver_passenger_union` (`driver_stroke_id`,`passenger_stroke_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='订单表';

DROP TABLE IF EXISTS `t_payment`;
CREATE TABLE `t_payment` (
  `id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '主键',
  `order_id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '订单号',
  `prepay_id` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '预支付ID',
  `pay_info` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '支付信息',
  `amount` float NOT NULL COMMENT '金额',
  `channel` tinyint(4) DEFAULT NULL COMMENT '支付渠道 支付宝：1微信：2',
  `transaction_order_num` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '交易订单号',
  `status` tinyint(4) DEFAULT NULL COMMENT '支付状态 未支付：0\r\n已支付：1\r\n未确认：2',
  `REVISION` int(11) DEFAULT NULL COMMENT '乐观锁',
  `CREATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATED_TIME` datetime DEFAULT NULL COMMENT '创建时间',
  `UPDATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATED_TIME` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='支付交易';

DROP TABLE IF EXISTS `t_stroke`;
CREATE TABLE `t_stroke` (
  `id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '主键',
  `publisher_id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '发布人ID',
  `role` tinyint(4) NOT NULL COMMENT '发布人角色 乘客：1\r\n司机：2',
  `start_geo_lng` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '起点GEO 经度',
  `start_geo_lat` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '起点GEO 维度',
  `end_geo_lng` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '终点GEO 经度',
  `end_geo_lat` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '终点GEO 纬度',
  `start_addr` varchar(1024) COLLATE utf8_unicode_ci NOT NULL COMMENT '起点地址',
  `end_addr` varchar(1024) COLLATE utf8_unicode_ci NOT NULL COMMENT '终点地址',
  `quantity` tinyint(4) NOT NULL COMMENT '数量 对于乘客来说是-同行人数\r\n对于司机来说是-空座数',
  `departure_time` datetime NOT NULL COMMENT '出发时间',
  `quick_confirm` tinyint(4) DEFAULT NULL COMMENT '快速确认 是否开启快速确认\r\n对于乘客就是-闪电确认\r\n对于司机就是-自动接单',
  `status` tinyint(4) DEFAULT NULL COMMENT '行程状态 未接单：0\r\n已接单：1\r\n已出发：2\r\n行程中：3\r\n已结束：4\r\n已超时：5',
  `REVISION` int(11) DEFAULT NULL COMMENT '乐观锁',
  `CREATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATED_TIME` datetime DEFAULT NULL COMMENT '创建时间',
  `UPDATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATED_TIME` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='行程表';

DROP TABLE IF EXISTS `t_vehicle`;
CREATE TABLE `t_vehicle` (
  `id` varchar(64) COLLATE utf8_unicode_ci NOT NULL COMMENT '主键',
  `car_number` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '车牌号',
  `car_front_photo` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '车牌前部照片',
  `car_back_photo` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '行驶证',
  `car_side_photo` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '人车同框',
  `purchase_date` date DEFAULT NULL COMMENT '购车日期',
  `phone` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT '所属人手机号码',
  `status` tinyint(4) DEFAULT NULL COMMENT '认证状态 未认证：0\r\n认证成功：1\r\n认证失败：2',
  `REVISION` int(11) DEFAULT NULL COMMENT '乐观锁',
  `CREATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATED_TIME` datetime DEFAULT NULL COMMENT '创建时间',
  `UPDATED_BY` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATED_TIME` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='车辆信息表';

INSERT INTO `t_account` (`id`, `username`, `useralias`, `password`, `phone`, `role`, `avatar`, `paycode`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1522178076463595520', 'zhangsan', '张三', '202cb962ac59075b964b07152d234b70', '13322223333', 1, 'http://wsl:9005/hitch/1783057433725-PixPin_2026-06-01_10-32-58.png', 'http://wsl:9005/hitch/1783316003434-exported_qrcode_image_600.png', 1, NULL, '1522178076463595520', '2026-07-06 00:33:24', '1522178076463595520', '2026-07-06 00:33:24');
INSERT INTO `t_account` (`id`, `username`, `useralias`, `password`, `phone`, `role`, `avatar`, `paycode`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1522611066226343936', 'lisi', 'lisi', '202cb962ac59075b964b07152d234b70', '13355556666', 1, 'http://wsl:9005/hitch/1783318708992-2079C2C155A75DD3B5339A5DFCA90433.gif', 'http://wsl:9005/hitch/1783323607142-alipay.jpg', 1, NULL, '1522611066226343936', '2026-07-06 02:40:08', '1522611066226343936', '2026-07-06 02:40:08');


INSERT INTO `t_attachment` (`id`, `name`, `url`, `lenght`, `ext`, `md5`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('', 'PixPin_2026-06-01_10-32-58.png', 'http://wsl:9005/hitch/1783057433725-PixPin_2026-06-01_10-32-58.png', 61511, 'png', '67e900c04deabed2d8199dbce5cef740', NULL, NULL, NULL, NULL, NULL, NULL);


INSERT INTO `t_authentication` (`id`, `useralias`, `phone`, `birth`, `personal_photo`, `card_id`, `card_id_front_photo`, `card_id_back_photo`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1522178076616073216', NULL, '13322223333', NULL, 'http://wsl:9005/hitch/1783057433725-PixPin_2026-06-01_10-32-58.png', NULL, 'http://wsl:9005/hitch/1783057433725-PixPin_2026-06-01_10-32-58.png', 'http://wsl:9005/hitch/1783057433725-PixPin_2026-06-01_10-32-58.png', '0', NULL, NULL, '2026-07-01 20:52:14', NULL, '2026-07-01 20:52:14');
INSERT INTO `t_authentication` (`id`, `useralias`, `phone`, `birth`, `personal_photo`, `card_id`, `card_id_front_photo`, `card_id_back_photo`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1522611066324295680', 'lisi', '13355556666', '1990-01-01', 'http://wsl:9005/hitch/1783318674420-d85a439c-1c6f-4bbb-9018-648aa1255b48.png', '372111222233336666', 'http://wsl:9005/hitch/1783318683594-screenshot-1713234778302.png', 'http://wsl:9005/hitch/1783318699741-screenshot-1713234798957.png', '1', NULL, NULL, '2026-07-03 01:32:47', NULL, '2026-07-03 01:32:47');


INSERT INTO `t_chat_message` (`id`, `conversation_id`, `user_id`, `role`, `content`, `created_time`) VALUES
('094270b5159e4ca181cc974d0b1a0fd6', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '故宫的北门是**神武门**。  \n\n📌 关键信息速览：  \n- **原名**：玄武门（清康熙年间为避讳康熙帝名“玄烨”，改称“神武门”）；  \n- **功能**：明清时期是皇室成员出入、皇帝巡幸回宫及选秀女的通道；清代晚期也作宫人出宫、太监遣散之门；  \n- **现状**：现为故宫博物院**主要出口之一**（游客参观结束后多由此离开），门上悬挂郭沫若题写的“故宫博物院”匾额；  \n- **注意**：神武门**不作为入口**，目前官方参观统一从**午门进入**。\n\n📍小贴士：  \n从神武门出来，正对景山公园南门（可登万春亭俯瞰故宫全景！）、北海公园东门，步行约5分钟；地铁8号线「北海北」站离此最近，出站走10分钟即达。\n\n需要我帮你规划「神武门→景山→北海」的轻松半日游路线吗？😊', '2026-07-07 18:13:12');
INSERT INTO `t_chat_message` (`id`, `conversation_id`, `user_id`, `role`, `content`, `created_time`) VALUES
('1f8cb4d4df464fe5ba44dbe63720d54c', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '故宫有几个门？', '2026-07-07 02:05:34');
INSERT INTO `t_chat_message` (`id`, `conversation_id`, `user_id`, `role`, `content`, `created_time`) VALUES
('2848cebc5ec747dcbd32b84928c9c280', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '给我规划个北京1日游', '2026-07-07 18:13:37');
INSERT INTO `t_chat_message` (`id`, `conversation_id`, `user_id`, `role`, `content`, `created_time`) VALUES
('336ebe47ddc1452fa8d027ea6ada943e', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '故宫一共有**4座主要宫门**，分别是：\n\n🔹 **午门**（南门）—— 正门，唯一对外开放的**游客入口**；  \n🔹 **神武门**（北门）—— 清代后妃出入及皇帝巡幸回宫之门，现为**主要出口之一**；  \n🔹 **东华门**（东门）—— 文官、太子、册封等出入通道，现也开放为**散场出口**；  \n🔹 **西华门**（西门）—— 原为皇帝赴西苑、祭天等专用，**目前不对外开放参观**（仅内部通行）。\n\n✅ 补充说明：  \n- 这4门围合紫禁城，体现“前朝后寝、左文右武”的礼制格局；  \n- 角楼（4座）和内廷小门（如顺贞门、隆宗门等）属于功能性门禁，**不计入“故宫正门”范畴**；  \n- 游客实际可进出的门目前是：**午门（进）＋神武门/东华门（出）**。\n\n需要我帮你整理「从地铁到午门」的步行指引，或对比神武门vs东华门哪个出口更方便打车/坐公交吗？😊', '2026-07-07 18:11:50'),
('43d575a0f3764f7a8f616aa625aaf0ca', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '123', '2026-07-07 02:18:45'),
('4d5ba6b12e91480b91a633596605eb93', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '故宫用几个门？', '2026-07-07 02:06:32'),
('522d6508ba0649a881f63a01da8284b6', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '哈哈，收到一串可爱的“123”信号📶～  \n小智已成功连线，随时待命！✨  \n\n如果你正想：  \n🔹 查故宫今日是否开放？  \n🔹 想知道怎么避开排队快速入园？  \n🔹 需要一份带时间点的2小时精华游览表？  \n🔹 或者……干脆让我帮你订个讲解员/租个导览器？  \n\n一句话，我马上安排！😊  \n（悄悄说：现在回复“**路线**”“**预约**”“**带娃Tips**”或任意关键词，就能秒得专属建议哦～）', '2026-07-07 16:30:22'),
('52dbb005e28447778d70425e484818d7', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '456', '2026-07-07 02:21:58'),
('545b52443ffd48ac8faa643988a1b60d', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '好的！为你定制一份**经典高效、避开人潮、兼顾文化与体验的北京1日精华游路线**（适合首次到访/时间紧凑的游客）👇\n\n---\n\n🎯 **主题：皇城根下的千年气韵 · 轻松不赶路版**  \n⏱ 建议时间：8:30 AM – 6:30 PM｜🚶‍♂️ 步行+地铁为主，全程约12km，节奏舒缓  \n\n---\n\n### 🌞 上午｜紫禁之巅 · 故宫深度精华  \n**📍 8:30–9:00** 到达【天安门广场】  \n→ 从「天安门东」地铁站A口出，先拍天安门合影（人少时段！9点前光线好、游客少）  \n→ 过安检后，经端门直达【午门】（故宫入口）  \n\n**📍 9:00–12:00** 游览【故宫中轴线+精华打卡】  \n✅ 必走动线：午门 → 太和门 → 太和殿（仰拍蟠龙柱+金砖）→ 中和殿 → 保和殿 → 乾清宫 → 交泰殿 → 坤宁宫 → 御花园（推荐钦安殿+千秋亭拍照）  \n✅ 贴士：  \n- 提前7天在「故宫博物院」小程序预约（选“上午场”，8:30入）；  \n- 租电子讲解器或听官方免费音频导览（微信搜“数字故宫”）；  \n- 御花园北侧的【摛藻堂】旁有安静长椅，可稍作休息。\n\n---\n\n### 🌇 中午｜胡同烟火 · 午餐+微体验  \n**📍 12:00–13:30** 出神武门 → 步行5分钟至【景山公园西门】登顶万春亭  \n→ 俯瞰故宫全景（北京最经典机位！建议带广角镜头📸）  \n→ 下山后步行3分钟到【什刹海·银锭桥】附近  \n✅ 午餐推荐（步行可达）：  \n▪️ **烤肉季**（百年老字号，临湖吃炙子烤肉）  \n▪️ **南门涮肉（后海店）**（铜锅地道，环境古朴）  \n▪️ 素食可选：**京兆尹**（米其林一星，需预约）或 **素心斋**\n\n---\n\n### 🌇 下午｜水畔风雅 · 什刹海慢时光  \n**📍 13:30–15:30** 什刹海环湖漫步 & 自由选择  \n✔️ 推荐动线：银锭桥 → 烟袋斜街（逛小众文创，避开主街人流）→ 宋庆龄故居（静谧园林，门票20元）→ 后海北沿乘一艘手划船（30分钟，约¥120，私密感强）  \n\n---\n\n### 🌆 傍晚｜落日余晖 · 收官之美  \n**📍 15:30–16:30** 打车/骑行至【北海公园】（距什刹海仅1公里）  \n→ 重点看：白塔、五龙亭、琼华岛，登塔远眺中南海与故宫西延线  \n→ 公园傍晚票更便宜，人少光柔，超适合收尾拍照  \n\n**📍 16:30–17:30** 返程或灵活延展：  \n▪️ 想买伴手礼？→ 步行至「西四北头条」的「京华印书局旧址」文创店  \n▪️ 想看京剧？→ 提前订「梅兰芳大剧院」下午场（14:00开演，16:30结束）  \n▪️ 想轻松返程？地铁6号线「北海北」站直达各大酒店/枢纽  \n\n---\n\n✅ **贴心附赠**：  \n📌 交通：全程推荐地铁+短途打车（高德/滴滴输入景点名即可）；  \n🎒 必带：身份证（全景点必验）、舒适鞋、充电宝、防晒帽；  \n🎫 门票提前预约：故宫（必约）、景山（¥2）、北海（¥5，学生半价）、宋庆龄故居（¥20）；  \n🌧 雨天Plan B：故宫→国家博物馆（免费，近天安门）→ 王府井APM地下「和平菓局」老北京沉浸馆  \n\n需要我：  \n🔹 把这份行程生成PDF/可打印版？  \n🔹 按你的出发地（如酒店/机场）优化交通接驳？  \n🔹 加入亲子/银发/摄影等专属需求？  \n\n随时告诉我，小智马上为你升级定制！✨😊', '2026-07-07 18:14:01'),
('5582c64d30524262b1e4ba8277acc0b8', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '北京故宫建于哪一年？', '2026-07-07 01:35:14'),
('5f9195121bbd4fdea5d55ef69460d446', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '当然可以！故宫（紫禁城）占地72万平方米，有大小宫殿七十多座、房屋九千余间，精华景点推荐你重点打卡这几个：\n\n✅ **午门**：故宫正门，气势恢宏，是“明三暗五”形制，也是古代举行颁诏、献俘等大典的地方。\n\n✅ **太和殿**：故宫最核心、等级最高的建筑，明清皇帝登基、大婚、册立皇后等重大典礼都在此举行。金砖铺地、蟠龙藻井、十一脊兽，细节震撼！\n\n✅ **中和殿 & 保和殿**：与太和殿合称“前三殿”，中和殿是皇帝去太和殿前的休憩处；保和殿是殿试场所，乾隆后科举最高考试就在此举行。\n\n✅ **乾清宫 & 坤宁宫**：内廷核心，“乾清坤宁”象征天地和谐。乾清宫曾是皇帝寝宫和理政之所；坤宁宫清代改为萨满祭祀场所。\n\n✅ **养心殿**：雍正后成为实际政治中心，皇帝在此批奏折、召见大臣，“垂帘听政”也发生于此（东暖阁），文物展出丰富。\n\n✅ **西六宫 & 东六宫**：妃嫔居所，其中储秀宫（慈禧曾居）、翊坤宫、永寿宫等保存完好，可感受宫廷生活。\n\n✅ **珍宝馆 & 钟表馆**（需另购票）：必看！收藏清代皇家珍宝与精美西洋钟表，工艺登峰造极。\n\n💡小贴士：建议从南往北游览（午门→神武门），避开人流高峰；提前7天在「故宫博物院」小程序预约门票哦～\n\n需要我帮你定制1日精华游路线或避开人流的时段建议吗？😊', '2026-07-07 01:35:45'),
('604f8ed82f5840008e66f455cd79cc69', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '午门正面有**五个门洞**，但严格来说，并不是“五个独立小门”，而是由一座主楼与两座延伸的雁翅楼（东西阙亭）围合成的“凹”字形建筑群，共设**三个门洞通道**，两侧另有两个掖门（隐蔽侧门）：\n\n✅ **中央门洞**：最宽大，专供皇帝通行；皇后大婚时可走一次；殿试前三名（状元、榜眼、探花）出宫也由此门荣归。\n\n✅ **东侧门洞（左掖门）**：文武百官上朝按“文东武西”规制，文官由此入。\n\n✅ **西侧门洞（右掖门）**：武官由此入。\n\n⚠️ 两侧雁翅楼尽头还有两个**掖门（即“左右掖门”）**，平时关闭，仅在大型典礼（如皇帝亲征、大祀）时开启，供特定人员或仪仗出入。\n\n🔸 所以总结：  \n- **常开的通行门洞：3个**（中、东、西）；  \n- **含掖门在内的总门道：5处**（因此常说“五门”或“明三暗五”）；  \n- 但外观上看，是“一座主楼 + 两座阙亭”，整体为一个恢弘建筑体，而非五个分离的小门。\n\n需要我画个简易示意图说明，或告诉你拍照打卡的最佳角度吗？😊', '2026-07-07 02:12:45'),
('6872554423634819bca11cec525c001a', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '你好呀～看来是输入小彩蛋啦！😄  \n不过小智随时在线， ready to help！  \n\n如果你有任何关于：  \n✅ 故宫游览（路线/讲解/拍照点）  \n✅ 北京交通（地铁/打车/步行导航）  \n✅ 天气穿搭、门票预约、带娃/银发出游贴士  \n✅ 或其他城市自驾/智驾游规划（比如西安兵马俑、杭州西湖线）  \n\n——尽管告诉我，我来为你定制清晰、省心、有温度的出行建议 🌟  \n\n需要现在开始规划吗？😊', '2026-07-07 02:22:01'),
('707795e7e04f41deae19df73231831c1', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '123', '2026-07-07 16:30:18'),
('85ce6e0c55f8419aad6b6cf54614da5d', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '午门有几个小门', '2026-07-07 02:12:38'),
('85ded6de24d9448090a4edcb434fcfae', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '我目前无法实时获取天气数据（需联网查询最新预报），但你可以通过以下方式**快速查看北京明日天气**：\n\n✅ 推荐方法：  \n🔹 打开手机「天气」App 或微信搜索「北京天气」官方小程序；  \n🔹 或在百度/高德地图搜索“北京天气”，首页即显示未来24小时详情（温度、降水、风力、空气质量）。\n\n🌤️ 小贴士（北京春季通用）：  \n- 近期多为晴到多云，昼夜温差大（白天15~20℃，早晚约6~10℃），建议“洋葱式穿衣”；  \n- 春季偶有杨柳飞絮或浮尘，敏感人群可备口罩；  \n- 故宫石板路较滑，若预报有小雨，记得穿防滑鞋哦！\n\n需要我帮你规划**明日故宫游的穿搭+必备清单+避开人流时段**吗？😊', '2026-07-07 16:30:37'),
('8e1309c80b854ce88aefd9f7009a93bd', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '北京故宫始建于明朝永乐四年，即公元1406年，建成于永乐十八年（1420年）。它是明清两代的皇家宫殿，至今已有600多年历史。需要我帮你规划故宫游览路线吗？😊', '2026-07-07 01:35:15'),
('92719422e74e4050ae61ffb21af12e24', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '123', '2026-07-07 02:22:16'),
('932b9f854e3f42e6887aca89749fe504', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '今天北京天气怎么样', '2026-07-07 02:27:22'),
('956b37c7a76046c58b99a395e3fa8cdf', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '好的！南京3日精华游来啦～  \n小智为你定制一条「文化深度 + 自然风光 + 美食体验」兼顾、交通顺、节奏舒缓的路线，适合首次到访的朋友（含地铁/步行指引，避开绕路坑）👇\n\n---\n\n### 🌟【南京3日智驾游·人文诗意版】  \n**特点**：地铁为主+短途打车/共享单车，每日步行≤8000步，重点景区全预约提醒！\n\n---\n\n#### **Day 1｜六朝烟水·老城核心线**  \n📍 **中山陵 → 美龄宫 → 明孝陵（石象路）→ 夫子庙·秦淮河（夜游）**  \n- 🚇 地铁2号线「下马坊站」→ 中山陵（免费，需预约！）  \n- 🌳 中山陵（免费，庄严肃穆，建议登顶俯瞰紫金山）  \n- 🚴‍♀️ 步行10分钟或电瓶车至美龄宫（“远东第一别墅”，民国浪漫必看）  \n- 🚇 地铁2号线「明孝陵站」→ 明孝陵（重点逛「石象路」秋日银杏/初冬乌桕超美，600年神道沧桑感拉满）  \n- 🌙 傍晚→夫子庙（地铁3号线「夫子庙站」），逛老门东（免费）、品鸭血粉丝汤+梅花糕，夜游秦淮河画舫（推荐19:30场次，灯光绝美✨）\n\n✅ 贴士：中山陵&明孝陵需提前在「中山陵园风景区」公众号预约；夫子庙无需预约，但画舫票建议现场早购。\n\n---\n\n#### **Day 2｜六朝文脉·城墙书卷线**  \n📍 **南京博物院（必去！）→ 金陵美术馆/老门东漫步 → 中华门瓮城 → 长干桥·大报恩寺遗址公园（夜景）**  \n- 🚇 地铁2号线「明故宫站」→ 南京博物院（⚠️ 提前7天预约！免费，民国馆+数字馆沉浸感超强）  \n- 🚶‍♂️ 步行15分钟→ 金陵美术馆（免费，小众文艺）→ 老门东（青砖黛瓦，鸡鸣汤包、蒋有记锅贴必尝）  \n- 🚇 地铁3号线「武定门站」→ 中华门城堡（现存最大明代瓮城，登城远眺，拍剪影超酷）  \n- 🌙 傍晚→大报恩寺遗址公园（门票¥70，必看琉璃塔光影秀，20:00开始，震撼！）\n\n✅ 贴士：南博周一闭馆；大报恩寺夜场更出片，穿浅色衣服拍照更上镜哦～\n\n---\n\n#### **Day 3｜江畔风雅·慢生活线**  \n📍 **阅江楼（登高揽胜）→ 静海寺·天妃宫 → 下关滨江步道 → 返程前打卡「梧桐大道」（颐和路 or 汉中路）**  \n- 🚇 地铁1号线「阅江楼站」→ 阅江楼（“江南四大名楼”之一，登楼看长江与南京长江大桥同框）  \n- 🚶‍♀️ 步行5分钟→ 静海寺（《南京条约》议约地，历史厚重感强）+ 天妃宫（郑和下西洋祈福地）  \n- 🚇 地铁5号线「盐仓桥站」→ 下关滨江步道（免费！江风拂面，看货轮穿梭，日落时分绝美🌅）  \n- 🍃 返程前：打车10分钟→ 颐和路（民国公馆区，梧桐+黄墙+咖啡馆，随手拍都是电影感）\n\n✅ 贴士：阅江楼可刷支付宝“金陵旅游卡”免票（如已购）；滨江步道傍晚人少景美，适合放松收尾。\n\n---\n\n### 📌 小智贴心附赠：\n🔹 **交通**：下载「Metro大都会」APP，刷码乘地铁；南京地铁覆盖极好，基本不堵车。  \n🔹 **美食彩蛋**：鸭子！盐水鸭、烤鸭、鸭血粉丝汤、桂花糖芋苗——每天安排1种不重样～  \n🔹 **避坑提醒**：  \n▪️ 中华门/夫子庙周边慎选“低价一日游团”；  \n▪️ 南博珍品馆（特展）需另预约，热门展建议抢早场；  \n▪️ 3月/11月最佳，避开暑期高温与梅雨季。\n\n需要我：  \n✅ 把这份行程生成PDF/带地图标注版？  \n✅ 推荐高性价比住宿区域（新街口 or 老门东附近）？  \n✅ 定制亲子版/银发版/摄影专线版？  \n\n随时告诉我，小智马上为你优化！🌿', '2026-07-07 17:58:28'),
('9b0e94052eaf47e785254eaf6b3d8365', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '123', '2026-07-07 16:29:03'),
('9ec93c85e68e492a94ff19e80c2fd382', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '故宫共有**四个主要宫门**，对应四方，严格遵循古代礼制：\n\n🔹 **南门：午门** —— 正门，形如“凹”字，俗称“五凤楼”，是故宫等级最高、最宏伟的门，明清举行大典、颁诏、献俘之地。\n\n🔹 **北门：神武门**（原名玄武门）—— 清代后妃出入及皇帝巡幸回宫之门，现为游客主要出口；故宫博物院匾额即悬挂于此。\n\n🔹 **东门：东华门** —— 皇太子、文武官员及册封用品进出通道；清代晚期也作“冰窖运冰”专用门；门钉为偶数（8×9=72颗），是故宫唯一例外（其余门均为奇数，象征阳）。\n\n🔹 **西门：西华门** —— 皇帝去西苑（今中南海、北海）、太后太妃出行常用门；清代也是阅兵、出宫祭天的通道。\n\n✅ 补充说明：  \n- 故宫外围还有**角楼**（4座）和**护城河桥**，但不算“门”；  \n- 现在游客常规入口是**午门**，出口多为**神武门**或**东华门**（尤其散场时分流用）；  \n- “故宫博物院”官方开放的参观通道目前仅这四门中的**午门、神武门、东华门**（西华门暂不对外开放参观）。\n\n需要我帮你标注地图上的门位置，或提醒哪个门离地铁最近吗？😊', '2026-07-07 02:09:34'),
('a0cecc4d950d47559331d72ec8eb2fe1', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '去南京旅游，3天，给我规划个路线', '2026-07-07 17:58:02'),
('a844b15e1c8448ce8fad724655037aaf', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '故宫一共有几个门？', '2026-07-07 18:11:44'),
('ab087a25108246de8a4635593f41b9a2', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', ' [异常] 404 Not Found from POST https://dashscope.aliyuncs.com/compatible-mode/v1/v1/chat/completions', '2026-07-07 16:27:20'),
('b28c558a525341b38097098f339e5542', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '故宫有几个门？', '2026-07-07 02:09:24'),
('b8e88b93e193405cb0903e1f8b913b51', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '故宫正门叫什么', '2026-07-07 02:10:36'),
('c214655d10e445aa8dd71bdb0f117731', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '北京明天天气怎么样', '2026-07-07 16:30:31'),
('c712de4b99d24518bfeaf31f4de4fcce', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '哈哈，又来一组可爱的“123”～  \n小智已悄悄记下：你可能在测试我、放松打趣，或者正忙中随手一按 😄  \n\n放心，我一直在这儿——  \n✅ 耐心、友好、专业；  \n✅ 知道故宫哪扇门拍龙纹最飒，  \n✅ 也清楚神武门出来右转30米就有冰镇酸梅汤 vendor 🍶  \n\n随时等你抛来真实问题～  \n是想查开放时间？找无障碍通道？还是……刚刚午门那5个门洞还没看够？😉  \n我，小智，随时待命！✨', '2026-07-07 02:22:21');
INSERT INTO `t_chat_message` (`id`, `conversation_id`, `user_id`, `role`, `content`, `created_time`) VALUES
('cb30f896569d45688a3f9603bbcd8d75', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '好的，简单介绍下故宫里有哪些值得去的点', '2026-07-07 01:35:37'),
('cd5fb9b1a67d48bb84a97ccc3219a65b', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '哈哈，收到第2个“123”啦～看来你在和小智玩暗号接龙？😄  \n（悄悄记下：你可能是位喜欢轻松氛围的旅行者✨）\n\n如果暂时没想好问什么，这里有几个「即拿即用」的小贴士送给你👇：  \n🔹 故宫门票提前7天晚8点放票（小程序搜“故宫博物院”）；  \n🔹 午门入口最近地铁站是**1号线天安门东站C口**，出站步行约5分钟；  \n🔹 神武门出来就是景山公园——登顶万春亭，俯拍故宫全景，绝美！  \n\n随时喊我，小智都在～ 🌈  \n（比如：“帮我规划上午3小时精华游” 或 “带老人怎么走最轻松？”）', '2026-07-07 02:25:44'),
('da6d42de59494f61a46e68b121ee349b', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '哈哈，收到你的“密码”啦～ 🌟  \n小智已启动「故宫守护模式」✅，随时待命为你解答、规划、避坑、暖心提醒！\n\n需要的话，我可以立刻帮你：  \n🔹 生成【故宫1日精华路线】（含最佳拍照点+最少排队时段）  \n🔹 发送【预约门票详细步骤】（含放票时间&抢票技巧）  \n🔹 提供【地铁+步行导航】（从天安门东站到午门，3分钟实景指引）  \n🔹 或——你想先了解哪个部分？我来定制专属版 😊  \n\n等你一句话，咱们出发！🚀', '2026-07-07 02:27:25'),
('dc4133f371074ca98c019677f76b7314', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '北京面积多大', '2026-07-07 16:27:20'),
('e2d117083c07455995f88dfb78ed4295', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '你好！看起来你可能不小心输入了“123”～  \n需要我帮你解答关于故宫、出行路线、交通，或者任何旅行相关的问题吗？😊  \n\n比如：  \n📍 故宫怎么预约？  \n🚇 最近的地铁站怎么走？  \n⏰ 什么时间人最少？  \n🎒 有哪些必带物品或避坑提示？  \n\n随时告诉我，小智为你贴心安排！✨', '2026-07-07 02:18:47'),
('e9f1efd542d24a1b98ae5a2ecfdadcb0', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '故宫有几个门？', '2026-07-07 01:59:16'),
('ed2acc8b8ce84122810c5b7defb847b8', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '故宫的正门叫**午门**。  \n\n它是紫禁城的南大门，也是等级最高、最雄伟的宫门，形制为“明三暗五”（正面五开间，两侧伸出两阙，合称“凹”字形），俗称“五凤楼”。  \n\n📌 小知识：  \n- “午”代表正南方，对应子午线中的“午时”（中午），取“居中向阳”之意；  \n- 古代重大典礼（如颁诏、献俘、出征）都在午门前举行；  \n- 现在游客统一从**午门入院**，是故宫博物院官方参观的唯一入口。\n\n需要我告诉你怎么从地铁1号线“天安门东”站最快走到午门吗？😊', '2026-07-07 02:10:41'),
('f2638035396f49c8b8137aef1f0a403d', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '午门有几个门洞？', '2026-07-07 02:17:29'),
('f57974bd34ae4b2d85ae3cc5ffadffed', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'assistant', '午门共有**5个门洞**，采用“**明三暗五**”的规制：\n\n🔹 **明三**：正面可见的3个门洞（中央主门 + 左右两旁门），是日常通行和典礼使用的主要通道；  \n🔹 **暗二**：隐藏在东西两侧雁翅楼（阙亭）尽头的2个掖门（左掖门、右掖门），平时关闭，仅在重大典礼（如皇帝亲征、大祀）时开启。\n\n✅ 所以——  \n🔸 **总门洞数：5个**（这是官方明确记载的数字）；  \n🔸 **日常开放通行：3个**（中、东、西）；  \n🔸 **建筑形制上，“五门”象征帝王“九五之尊”中的“五”，体现礼制权威**。\n\n小趣闻：明清时“推出午门斩首”其实是误传——实际行刑地在菜市口，午门只用于“廷杖”（当众责打大臣）😅\n\n需要我帮你区分“门洞”和“门楼”的概念，或配个简图说明吗？😊', '2026-07-07 02:17:35'),
('f657706132db49e4b112569e86c137e7', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '北门是哪个门', '2026-07-07 18:13:06'),
('fdaf9ae32f534d8eb8906dd326344d8a', '59c5248faae24a6790d39a74884fc935', '1522178076463595520', 'user', '111', '2026-07-07 02:25:40');

INSERT INTO `t_order` (`id`, `passenger_id`, `passenger_stroke_id`, `driver_id`, `driver_stroke_id`, `distance`, `estimated_time`, `cost`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1523716280942493696', '1522611066226343936', '1523716243001352192', '1522178076463595520', '1523716097349951488', 18953, 3605, 48.5, 2, NULL, '1522611066226343936', '2026-07-06 02:44:31', '1522611066226343936', '2026-07-06 02:44:31');
INSERT INTO `t_order` (`id`, `passenger_id`, `passenger_stroke_id`, `driver_id`, `driver_stroke_id`, `distance`, `estimated_time`, `cost`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1523717242977419264', '1522611066226343936', '1523716179327623168', '1522178076463595520', '1523716097349951488', 19136, 3568, 50.8, 2, NULL, '1522611066226343936', '2026-07-06 02:48:20', '1522611066226343936', '2026-07-06 02:48:20');
INSERT INTO `t_order` (`id`, `passenger_id`, `passenger_stroke_id`, `driver_id`, `driver_stroke_id`, `distance`, `estimated_time`, `cost`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1524092705772044288', '1522178076463595520', '1524091255713603584', '1522611066226343936', '1524092191806758912', 18402, 3631, 48.5, 1, NULL, '1522178076463595520', '2026-07-07 16:40:17', '1522178076463595520', '2026-07-07 16:40:17');
INSERT INTO `t_order` (`id`, `passenger_id`, `passenger_stroke_id`, `driver_id`, `driver_stroke_id`, `distance`, `estimated_time`, `cost`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1524096273396695040', '1522178076463595520', '1524094695202267136', '1522611066226343936', '1524096216329527296', 16697, 3230, 43.9, 2, NULL, '1522178076463595520', '2026-07-07 16:54:28', '1522178076463595520', '2026-07-07 16:54:28'),
('1524113452498321408', '1522611066226343936', '1524103193575202816', '1522178076463595520', '1524110387607674880', 18140, 3313, 48.5, 0, NULL, '1522611066226343936', '2026-07-07 18:02:44', '1522611066226343936', '2026-07-07 18:02:44');



INSERT INTO `t_stroke` (`id`, `publisher_id`, `role`, `start_geo_lng`, `start_geo_lat`, `end_geo_lng`, `end_geo_lat`, `start_addr`, `end_addr`, `quantity`, `departure_time`, `quick_confirm`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1523716097349951488', '1522178076463595520', 1, '116.402272', '40.001186', '116.39154', '39.869293', '北京市朝阳区奥运村街道慧忠路隧道', '北京市丰台区西罗园街道马家堡路64号', 3, '2026-07-06 10:43:00', NULL, 3, NULL, '1522178076463595520', '2026-07-06 02:43:47', '1522178076463595520', '2026-07-06 02:43:47');
INSERT INTO `t_stroke` (`id`, `publisher_id`, `role`, `start_geo_lng`, `start_geo_lat`, `end_geo_lng`, `end_geo_lat`, `start_addr`, `end_addr`, `quantity`, `departure_time`, `quick_confirm`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1523716179327623168', '1522611066226343936', 0, '116.422144', '40.004007', '116.398524', '39.863334', '北京市朝阳区大屯街道', '北京市丰台区大红门街道南三环中路82-2号', 1, '2026-07-06 10:43:00', 0, 3, NULL, '1522611066226343936', '2026-07-06 02:44:06', '1522611066226343936', '2026-07-06 02:44:06');
INSERT INTO `t_stroke` (`id`, `publisher_id`, `role`, `start_geo_lng`, `start_geo_lat`, `end_geo_lng`, `end_geo_lat`, `start_addr`, `end_addr`, `quantity`, `departure_time`, `quick_confirm`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1523716243001352192', '1522611066226343936', 0, '116.388273', '40.000922', '116.388719', '39.863334', '北京市朝阳区奥运村街道安翔北路10号', '北京市丰台区西罗园街道马家堡路84号', 1, '2026-07-06 10:44:00', 1, 3, NULL, '1522611066226343936', '2026-07-06 02:44:22', '1522611066226343936', '2026-07-06 02:44:22');
INSERT INTO `t_stroke` (`id`, `publisher_id`, `role`, `start_geo_lng`, `start_geo_lat`, `end_geo_lng`, `end_geo_lat`, `start_addr`, `end_addr`, `quantity`, `departure_time`, `quick_confirm`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1523981539385712640', '1522611066226343936', 0, '116.386268', '39.999208', '116.390725', '39.857151', '北京市朝阳区亚运村街道安翔里西路', '北京市丰台区西罗园街道马家堡南小街', 1, '2026-07-07 04:18:00', 0, 0, NULL, '1522611066226343936', '2026-07-06 20:18:33', '1522611066226343936', '2026-07-06 20:18:33'),
('1523982219924119552', '1522178076463595520', 1, '116.406488', '39.991752', '116.397673', '39.868702', '北京市朝阳区亚运村街道', '北京市丰台区西罗园街道西罗园东路', 3, '2026-07-07 04:21:00', 0, 0, NULL, '1522178076463595520', '2026-07-06 20:21:15', '1522178076463595520', '2026-07-06 20:21:15'),
('1523982466276564992', '1522178076463595520', 1, '116.387324', '40.000596', '116.386941', '39.86693', '北京市朝阳区奥运村街道安翔北路11', '北京市丰台区西罗园街道西罗园北路', 3, '2026-07-07 04:21:00', 0, 0, NULL, '1522178076463595520', '2026-07-06 20:22:14', '1522178076463595520', '2026-07-06 20:22:14'),
('1524091255713603584', '1522178076463595520', 0, '116.352446', '39.95194', '116.455164', '39.870474', '北京市海淀区北下关街道高梁桥斜街42号', '北京市朝阳区十八里店(地区)乡白墙子街', 3, '2026-07-08 00:34:00', 0, 3, NULL, '1522178076463595520', '2026-07-07 16:34:32', '1522178076463595520', '2026-07-07 16:34:32'),
('1524092191806758912', '1522611066226343936', 1, '116.396964', '39.996466', '116.419693', '39.840318', '北京市朝阳区奥运村街道国家体育场南路', '北京市丰台区大红门街道东后街8号院', 3, '2026-07-08 00:38:00', 0, 0, NULL, '1522611066226343936', '2026-07-07 16:38:15', '1522611066226343936', '2026-07-07 16:38:15'),
('1524093823147745280', '1522178076463595520', 0, '116.358195', '39.95194', '116.455164', '39.861022', '北京市西城区展览路街道娘娘庙胡同46号', '北京市丰台区成寿寺街道钟萃路', 3, '2026-07-08 00:44:00', 0, 0, NULL, '1522178076463595520', '2026-07-07 16:44:44', '1522178076463595520', '2026-07-07 16:44:44'),
('1524094695202267136', '1522178076463595520', 0, '116.37276', '39.95194', '116.425652', '39.836205', '北京市西城区新街口街道德胜门西大街70', '北京市丰台区和义街道', 3, '2026-07-08 00:48:00', 0, 3, NULL, '1522178076463595520', '2026-07-07 16:48:12', '1522178076463595520', '2026-07-07 16:48:12'),
('1524095751890051072', '1522178076463595520', 0, '116.362411', '39.957545', '116.460147', '39.868997', '北京市海淀区北太平庄街道文慧园路72号', '北京市朝阳区十八里店(地区)乡南三环东路辅路', 3, '2026-07-08 00:52:00', 0, 0, NULL, '1522178076463595520', '2026-07-07 16:52:24', '1522178076463595520', '2026-07-07 16:52:24'),
('1524096038914662400', '1522178076463595520', 0, '116.362411', '39.957545', '116.460147', '39.868997', '北京市海淀区北太平庄街道文慧园路72号', '北京市朝阳区十八里店(地区)乡南三环东路辅路', 1, '2026-07-08 00:52:00', 0, 0, NULL, '1522178076463595520', '2026-07-07 16:53:32', '1522178076463595520', '2026-07-07 16:53:32'),
('1524096216329527296', '1522611066226343936', 1, '116.424596', '39.957377', '116.350614', '39.837569', '北京市东城区和平里街道和平里西街63号', '北京市丰台区玉泉营街道京开辅路', 3, '2026-07-08 00:54:00', 0, 3, NULL, '1522611066226343936', '2026-07-07 16:54:14', '1522611066226343936', '2026-07-07 16:54:14'),
('1524103193575202816', '1522611066226343936', 0, '116.373789', '39.970409', '116.426378', '39.841349', '北京市海淀区北太平庄街道新街口外大街19号院', '北京市丰台区石榴庄街道', 3, '2026-07-08 01:21:00', 0, 1, NULL, '1522611066226343936', '2026-07-07 17:21:58', '1522611066226343936', '2026-07-07 17:21:58'),
('1524110387607674880', '1522178076463595520', 1, '116.367777', '39.996469', '116.437917', '39.86693', '北京市海淀区学院路街道学院路30号', '北京市丰台区方庄街道群星路', 3, '2026-07-08 01:50:00', 0, 0, NULL, '1522178076463595520', '2026-07-07 17:50:33', '1522178076463595520', '2026-07-07 17:50:33');

INSERT INTO `t_vehicle` (`id`, `car_number`, `car_front_photo`, `car_back_photo`, `car_side_photo`, `purchase_date`, `phone`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1522178076708347904', NULL, 'http://wsl:9005/hitch/1783060703678-x.jpg', 'http://wsl:9005/hitch/1783060708609-x.jpg', 'http://wsl:9005/hitch/1783060706359-x.jpg', NULL, '13322223333', 0, NULL, NULL, '2026-07-01 20:52:14', NULL, '2026-07-01 20:52:14');
INSERT INTO `t_vehicle` (`id`, `car_number`, `car_front_photo`, `car_back_photo`, `car_side_photo`, `purchase_date`, `phone`, `status`, `REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES
('1522611066362044416', '粤C66666', 'http://wsl:9005/hitch/1783317595851-screenshot-1713234021212.png', 'http://wsl:9005/hitch/1783317620315-screenshot-1713166753548.png', 'http://wsl:9005/hitch/1783317611167-PixPin_2025-05-07_17-01-22.png', NULL, '13355556666', 0, NULL, NULL, '2026-07-03 01:32:47', NULL, '2026-07-03 01:32:47');



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;