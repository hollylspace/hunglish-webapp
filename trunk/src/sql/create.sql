DROP TABLE IF EXISTS `bisen`;

DROP TABLE IF EXISTS `upload`;
DROP TABLE IF EXISTS `doc`;

DROP TABLE IF EXISTS `genre`;
DROP TABLE IF EXISTS `author`;


CREATE TABLE  `genre` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `genre_i_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE  `author` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `author_i_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;



CREATE TABLE  `upload` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `genre_id` bigint(20) NOT NULL,
  `author_id` bigint(20) NOT NULL,
  `is_processed` varchar(1) default 'N',
  `title` varchar(255) NOT NULL,
  `hu_file_path` varchar(255) default NULL,
  `en_file_path` varchar(255) default NULL,
  `hu_sentence` varchar(4000) default NULL,
  `en_sentence` varchar(4000) default NULL,
  PRIMARY KEY (`id`),
  KEY `upload_fk_genre` (`genre_id`),
  KEY `upload_fk_author` (`author_id`),
  KEY `upload_i_is_processed` (`is_processed`),
  CONSTRAINT `upload_fki_genre` FOREIGN KEY (`genre_id`) REFERENCES `genre` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `upload_fki_author` FOREIGN KEY (`author_id`) REFERENCES `author` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE  `doc` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `genre_id` bigint(20) NOT NULL,
  `author_id` bigint(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `is_open_content` varchar(1) default 'N',
  `hu_raw_file_path` varchar(255) default NULL,
  `en_raw_file_path` varchar(255) default NULL,
  `aligned_file_path` varchar(255) default NULL,
  PRIMARY KEY (`id`),
  KEY `doc_fk_genre` (`genre_id`),
  KEY `doc_fk_author` (`author_id`),
  UNIQUE KEY `doc_i_aligned_file_path` (`aligned_file_path`),
  CONSTRAINT `doc_fki_genre` FOREIGN KEY (`genre_id`) REFERENCES `genre` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `doc_fki_author` FOREIGN KEY (`author_id`) REFERENCES `author` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE  `bisen` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_indexed` varchar(1) default 'N',
  `doc_id` bigint(20) NOT NULL,
  `line_number` bigint(20) NOT NULL,
  `upvotes` bigint(20) default 0,
  `downvotes` bigint(20) default 0,
  `hu_sentence` varchar(4000) NOT NULL,
  `en_sentence` varchar(4000) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `bisen_fk_doc` (`doc_id`),
  CONSTRAINT `bisen_fki_doc` FOREIGN KEY (`doc_id`) REFERENCES `doc` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

