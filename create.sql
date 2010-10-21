-- MySQL dump 10.13  Distrib 5.1.34, for Win32 (ia32)
--
-- Host: localhost    Database: hunglishwebapp
-- ------------------------------------------------------
-- Server version	5.1.34-community

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `author`
--

DROP TABLE IF EXISTS `author`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `author` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` int(11) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `author_i_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bisen`
--

DROP TABLE IF EXISTS `bisen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bisen` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` int(11) DEFAULT NULL,
  `downvotes` bigint(20) DEFAULT NULL,
  `en_sentence` varchar(4000) DEFAULT NULL,
  `hu_sentence` varchar(4000) DEFAULT NULL,
  `is_indexed` bit(1) DEFAULT NULL,
  `line_number` int(11) DEFAULT NULL,
  `upvotes` bigint(20) DEFAULT NULL,
  `doc` bigint(20) DEFAULT NULL,
  `en_sentence_hash` bigint(20) DEFAULT NULL,
  `hu_sentence_hash` bigint(20) DEFAULT NULL,
  `is_duplicate` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_bisen_doc` (`doc`),
  KEY `bisen_en_hash` (`en_sentence_hash`),
  KEY `bisen_hu_hash` (`hu_sentence_hash`),
  UNIQUE KEY `i_bisen_uniq` (`doc`, `line_number`),
  CONSTRAINT `fk_bisen_doc` FOREIGN KEY (`doc`) REFERENCES `doc` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `doc`
--

DROP TABLE IF EXISTS `doc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `doc` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` int(11) DEFAULT NULL,
  `aligned_file_path` varchar(255) DEFAULT NULL,
  `en_raw_file_path` varchar(255) DEFAULT NULL,
  `en_title` varchar(255) not NULL,
  `hu_raw_file_path` varchar(255) DEFAULT NULL,
  `hu_title` varchar(255) DEFAULT NULL,
  `is_open_content` bit(1) DEFAULT 0,
  `old_docid` varchar(255) DEFAULT NULL,
  `author` bigint(20) not NULL,
  `genre` bigint(20) not NULL,
  `upload` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_doc_genre` (`genre`),
  KEY `fk_doc_author` (`author`),
  UNIQUE KEY `doc_i_aligned_file_path` (`aligned_file_path`),
  CONSTRAINT `fk_doc_author` FOREIGN KEY (`author`) REFERENCES `author` (`id`),
  CONSTRAINT `fk_doc_genre` FOREIGN KEY (`genre`) REFERENCES `genre` (`id`),
  CONSTRAINT `fk_doc_upload` FOREIGN KEY (`upload`) REFERENCES `upload` (`id`)  
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `genre`
--

DROP TABLE IF EXISTS `genre`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `genre` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` int(11) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `genre_i_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `upload`
--

DROP TABLE IF EXISTS `upload`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `upload` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` int(11) DEFAULT NULL,
  `en_original_name` varchar(255) not NULL,
  `en_file_path` varchar(255) DEFAULT NULL,
  -- `en_sentence` varchar(4000) DEFAULT NULL, -- not used in current implementation
  `en_title` varchar(255) DEFAULT NULL,
  `hu_original_name` varchar(255) not NULL,
  `hu_file_path` varchar(255) DEFAULT NULL,
  -- `hu_sentence` varchar(4000) DEFAULT NULL, -- not used in current implementation
  `hu_title` varchar(255) DEFAULT NULL,
  `is_approved` bit(1) DEFAULT 0,
  -- Y = processed, N = not processed, E = processed with error, L = processed without error but the result is of bad quality
  `is_processed` varchar(1) not null DEFAULT 'N',
  `author` bigint(20) DEFAULT NULL,
  `author_name` varchar(255) DEFAULT NULL,
  `genre` bigint(20) not NULL,
  PRIMARY KEY (`id`),
  KEY `fk_upload_genre` (`genre`),
  KEY `fk_upload_author` (`author`),
  KEY `upload_i_is_processed` (`is_processed`),
  CONSTRAINT `fk_upload_author` FOREIGN KEY (`author`) REFERENCES `author` (`id`),
  CONSTRAINT `fk_upload_genre` FOREIGN KEY (`genre`) REFERENCES `genre` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-03-10 23:30:13
