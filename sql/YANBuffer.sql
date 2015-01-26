SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE `yanb_ulist_buffs` (
  `ulist_id` int(10) unsigned NOT NULL,
  `ulist_buff_ident` varchar(255) NOT NULL,
  PRIMARY KEY (`ulist_id`,`ulist_buff_ident`),
  CONSTRAINT `yanb_ulist_buffs_ibfk_1` FOREIGN KEY (`ulist_id`) REFERENCES `yanb_ulists` (`ulist_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `yanb_ulists` (
  `ulist_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `ulist_char_id` int(10) unsigned NOT NULL,
  `ulist_name` varchar(255) NOT NULL,
  PRIMARY KEY (`ulist_id`),
  UNIQUE KEY `ulist_char_id` (`ulist_char_id`,`ulist_name`),
  CONSTRAINT `yanb_ulists_ibfk_1` FOREIGN KEY (`ulist_char_id`) REFERENCES `characters` (`charId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

