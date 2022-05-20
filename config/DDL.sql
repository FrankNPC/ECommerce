DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL default '0',
  `parent_id` bigint(20) NOT NULL default '0',
  `user_id` bigint(20) NOT NULL default '0',
  `product_id` bigint(20) NOT NULL default '0',
  `receipt_amount` bigint(20) NOT NULL default '0',
  `paid_amount` bigint(20) NOT NULL default '0',
  `external_order_id` char(32) NOT NULL default '0',
  `status` int(11) NOT NULL default '0',
  `create_time` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `parent_id` USING BTREE (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL default '0',
  `username` char(32) NOT NULL default '',
  `password` char(32) NOT NULL default '',
  `session_id` char(32) NOT NULL default '',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `session_id` USING BTREE (`session_id`),
  UNIQUE KEY `username` USING BTREE (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL default '0',
  `price` bigint(20) NOT NULL default '0',
  `quantity` bigint(20) NOT NULL default '0',
  `status` int(11) NOT NULL default '0',
  `category_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `category_id` USING BTREE (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

