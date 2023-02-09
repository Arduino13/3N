-- MySQL dump 10.16  Distrib 10.1.48-MariaDB, for debian-linux-gnueabihf (armv7l)
--
-- Host: localhost    Database: soc_server
-- ------------------------------------------------------
-- Server version	10.1.48-MariaDB-0+deb9u2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admins`
--

DROP TABLE IF EXISTS `admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admins` (
  `ID` varchar(36) COLLATE cp1250_czech_cs NOT NULL,
  `NAME` text COLLATE cp1250_czech_cs NOT NULL,
  `HASH` text COLLATE cp1250_czech_cs NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1250 COLLATE=cp1250_czech_cs;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `homework`
--

DROP TABLE IF EXISTS `homework`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `homework` (
  `ID` varchar(36) COLLATE cp1250_czech_cs NOT NULL,
  `NAME` text COLLATE cp1250_czech_cs NOT NULL,
  `DATE` text COLLATE cp1250_czech_cs NOT NULL,
  `DATE_COMPLETED` text COLLATE cp1250_czech_cs NOT NULL,
  `WORDS` text COLLATE cp1250_czech_cs NOT NULL,
  `WORDS_COMPLETED` text COLLATE cp1250_czech_cs NOT NULL,
  `COMPLETED` text COLLATE cp1250_czech_cs NOT NULL,
  `HOMEWORK_ID` text COLLATE cp1250_czech_cs NOT NULL,
  `STUDENT_ID` text COLLATE cp1250_czech_cs NOT NULL,
  `CLASS_ID` text COLLATE cp1250_czech_cs NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1250 COLLATE=cp1250_czech_cs;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rankList`
--

DROP TABLE IF EXISTS `rankList`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rankList` (
  `ID` varchar(36) COLLATE cp1250_czech_cs NOT NULL,
  `number_of_tests` text COLLATE cp1250_czech_cs NOT NULL,
  `number_of_success` text COLLATE cp1250_czech_cs NOT NULL,
  `number_of_fail` text COLLATE cp1250_czech_cs NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=cp1250 COLLATE=cp1250_czech_cs;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `students` (
  `ID` varchar(36) COLLATE cp1250_czech_cs NOT NULL,
  `NAME` text COLLATE cp1250_czech_cs NOT NULL,
  `CLASS_ID` text COLLATE cp1250_czech_cs NOT NULL,
  `HASH` text COLLATE cp1250_czech_cs NOT NULL,
  `email` text COLLATE cp1250_czech_cs NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1250 COLLATE=cp1250_czech_cs;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `teachers`
--

DROP TABLE IF EXISTS `teachers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `teachers` (
  `ID` varchar(36) COLLATE cp1250_czech_cs NOT NULL,
  `NAME` text COLLATE cp1250_czech_cs NOT NULL,
  `CLASSES` text COLLATE cp1250_czech_cs NOT NULL,
  `HASH` text COLLATE cp1250_czech_cs NOT NULL,
  `email` text COLLATE cp1250_czech_cs NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=cp1250 COLLATE=cp1250_czech_cs;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tests`
--

DROP TABLE IF EXISTS `tests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tests` (
  `ID` varchar(36) COLLATE cp1250_czech_cs NOT NULL,
  `DATE` text COLLATE cp1250_czech_cs NOT NULL,
  `WORDS` text COLLATE cp1250_czech_cs NOT NULL,
  `WRONG_WORDS` text COLLATE cp1250_czech_cs NOT NULL,
  `CLASS_ID` text COLLATE cp1250_czech_cs NOT NULL,
  `HOMEWORK_ID` text COLLATE cp1250_czech_cs NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1250 COLLATE=cp1250_czech_cs;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vocabulary`
--

DROP TABLE IF EXISTS `vocabulary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vocabulary` (
  `ID` varchar(36) COLLATE cp1250_czech_cs NOT NULL,
  `FROM_v` text COLLATE cp1250_czech_cs NOT NULL,
  `TO_v` text COLLATE cp1250_czech_cs NOT NULL,
  `CLASS_ID` text COLLATE cp1250_czech_cs NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1250 COLLATE=cp1250_czech_cs;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `web_pages`
--

DROP TABLE IF EXISTS `web_pages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `web_pages` (
  `ID` varchar(36) COLLATE cp1250_czech_cs NOT NULL,
  `ADDRESS` text COLLATE cp1250_czech_cs NOT NULL,
  `NAME` text COLLATE cp1250_czech_cs NOT NULL,
  `CLASS_ID` text COLLATE cp1250_czech_cs NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1250 COLLATE=cp1250_czech_cs;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-07-25 20:03:41
