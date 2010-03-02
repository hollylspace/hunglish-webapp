
DROP TABLE IF EXISTS `upload`;
DROP TABLE IF EXISTS `source`;
CREATE TABLE  `source` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `source_i_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;



DROP TABLE IF EXISTS `upload`;
CREATE TABLE  `upload` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `source_id` bigint(20) NOT NULL,
  `is_processed` varchar(1) default 'N',
  `author` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `hu_file_path` varchar(255) default NULL,
  `en_file_path` varchar(255) default NULL,
  `hu_sentence` varchar(4000) default NULL,
  `en_sentence` varchar(4000) default NULL,
  PRIMARY KEY (`id`),
  KEY `upload_fk_source` (`source_id`),
  KEY `upload_i_is_processed` (`is_processed`),
  CONSTRAINT `upload_fki_source` FOREIGN KEY (`source_id`) REFERENCES `source` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

