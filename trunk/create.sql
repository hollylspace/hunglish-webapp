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



DROP TABLE IF EXISTS `job_queue`;
CREATE TABLE `job_queue` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` int(11) DEFAULT NULL,
  `status` varchar(1) not null DEFAULT 'N', -- N new, not processed; S started; F finished
  `request_timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `start_timestamp` TIMESTAMP ,
  `end_timestamp` TIMESTAMP ,
  PRIMARY KEY (`id`),
  KEY `job_queue_i_status` (`status`),
  KEY `job_queue_i_ts` (`request_timestamp`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;


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
  `upvotes` bigint(20) DEFAULT NULL,
  `en_sentence` varchar(4000) DEFAULT NULL,
  `hu_sentence` varchar(4000) DEFAULT NULL,
  `line_number` int(11) DEFAULT NULL,
  `doc` bigint(20) DEFAULT NULL,
  `en_sentence_hash` bigint(20) DEFAULT NULL,
  `hu_sentence_hash` bigint(20) DEFAULT NULL,
  `is_duplicate` bit(1) DEFAULT NULL,
  `indexed_timestamp` TIMESTAMP ,
  `copyright` varchar(1) not null DEFAULT 'C',
  `approved` varchar(1) not null DEFAULT 'N',  -- TODO the indexing would not be started automatically on a new doc, but could be triggered by hand on a doc.     
  `state` varchar(1) not NULL default 'D', -- N nothing to do, D duplicate filter to do, I to be added to index, R to be reindexed, E to be ereased from index, O nothing to do but there was an Error
  PRIMARY KEY (`id`),
  KEY `fk_bisen_doc` (`doc`),
  KEY `duplicate_key` (`hu_sentence_hash`, `en_sentence_hash`, `is_duplicate`),
  KEY `state_key` (`state`),
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
  `id` bigint(20) NOT NULL, -- no more AUTO_INCREMENT, the ID will be the same as the upload ID 
  `version` int(11) DEFAULT NULL,
  `aligned_file_path` varchar(255) DEFAULT NULL,
  `en_title` varchar(255) not NULL,
  `hu_title` varchar(255) DEFAULT NULL,
  `is_open_content` bit(1) DEFAULT 0,
  `old_docid` varchar(255) DEFAULT NULL,
  `author` bigint(20) not NULL,
  `genre` bigint(20) not NULL,
  `upload` bigint(20) DEFAULT NULL,
  `copyright` varchar(1) not null DEFAULT 'C',
  `approved` varchar(1) not null DEFAULT 'N',  -- TODO the indexing would not be started automatically on a new doc, but could be triggered by hand on a doc.   
  PRIMARY KEY (`id`),
  KEY `fk_doc_genre` (`genre`),
  KEY `fk_doc_author` (`author`),
  KEY `fk_doc_upload` (`upload`),  
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

insert into genre (id, name,description) values (-10, "All",   "All") ;
insert into genre (name,description) values ("lit",   "Literature") ;
insert into genre (name,description) values ("film",  "Subtitles") ;
insert into genre (name,description) values ("mag",   "Magazine") ;
insert into genre (name,description) values ("swdoc", "Software Documentation") ;
insert into genre (name,description) values ("law",   "Law") ;


--
-- Table structure for table `upload`
--

DROP TABLE IF EXISTS `upload`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `upload` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` int(11) DEFAULT NULL,
  
  `hu_uploaded_file_path` varchar(255) DEFAULT NULL, -- this is filled by the webapp (UploadController); this is normalized name
  `en_uploaded_file_path` varchar(255) DEFAULT NULL, 
  
  `hu_original_file_name` varchar(255) not NULL, -- the file was uploaded with this name;this is filled by the webapp (UploadController)
  `en_original_file_name` varchar(255) not NULL, 
  
  `hu_original_file_size` bigint(20) , -- the file size after upload; this is filled by the webapp (UploadController)
  `en_original_file_size` bigint(20) ,

  
  `hu_raw_file_size` bigint(20) , -- the size of the file after it was converted into text; this is fiilled by control_harness.py
  `en_raw_file_size` bigint(20) ,
  
  `hu_sentence_count` bigint(20) , -- number of sentences after sen phase; this is fiilled by control_harness.py
  `en_sentence_count` bigint(20) ,

  `align_bisentence_count` bigint(20) , -- number of aligned sentences; this is fiilled by control_harness.py
   
  `is_processed` varchar(1) not null DEFAULT 'N', -- Y = processed, N = not processed, E = processed with error, L = processed without error but the result is of bad quality, P - is currently being processed
  `hu_title` varchar(255) DEFAULT NULL, -- this is user input via the webapp (UploadController)
  `en_title` varchar(255) DEFAULT NULL, -- this is user input via the webapp (UploadController)
  `author` bigint(20) DEFAULT NULL, -- chosen from existing Authors; this is user input via the webapp (UploadController)
  `author_name` varchar(255) DEFAULT NULL, -- when given, new Author will be created with this name; this is user input via the webapp (UploadController)
  `genre` bigint(20) not NULL, -- this is user input via the webapp (UploadController)
  -- `en_sentence` varchar(4000) DEFAULT NULL, -- not used in current implementation
  -- `hu_sentence` varchar(4000) DEFAULT NULL, -- not used in current implementation
  
  
  `copyright` varchar(1) not null DEFAULT 'C',
  `old_docid` varchar(255) not NULL, -- It is "" if the doc does not come from hunglish1 
  `approved` varchar(1) not null DEFAULT 'N',  -- TODO the indexing would not be started automatically on a new doc, but could be triggered by hand on a doc.   

  `created_timestamp` TIMESTAMP , -- this is filled by the webapp (UploadController) when the files was uploaded
  `harnessed_timestamp` TIMESTAMP , -- this is filled by control_harness.py when the pipe-line is completed

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
