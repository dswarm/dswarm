-- MySQL dump 10.13  Distrib 5.5.40, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: dmp
-- ------------------------------------------------------
-- Server version	5.5.40-0ubuntu0.14.04.1

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
-- Current Database: `dmp`
--

USE `dmp`;

--
-- Dumping data for table `ATTRIBUTE`
--

/*!40000 ALTER TABLE `ATTRIBUTE` DISABLE KEYS */;
INSERT INTO `ATTRIBUTE` VALUES (1,'type','http://www.w3.org/1999/02/22-rdf-syntax-ns#type'),(2,'EISSN','http://vocab.ub.uni-leipzig.de/bibrm/EISSN'),(3,'title','http://purl.org/dc/elements/1.1/title'),(4,'price','http://vocab.ub.uni-leipzig.de/bibrm/price'),(5,'otherTitleInformation','http://rdvocab.info/Elements/otherTitleInformation'),(6,'alternative','http://purl.org/dc/terms/alternative'),(7,'shortTitle','http://purl.org/ontology/bibo/shortTitle'),(8,'creator','http://purl.org/dc/elements/1.1/creator'),(9,'contributor','http://purl.org/dc/elements/1.1/contributor'),(10,'familyName','http://xmlns.com/foaf/0.1/familyName'),(11,'givenName','http://xmlns.com/foaf/0.1/givenName'),(12,'creator','http://purl.org/dc/terms/creator'),(13,'contributor','http://purl.org/dc/terms/contributor'),(14,'publicationStatement','http://rdvocab.info/Elements/publicationStatement'),(15,'placeOfPublication','http://rdvocab.info/Elements/placeOfPublication'),(16,'publisher','http://purl.org/dc/elements/1.1/publisher'),(17,'issued','http://purl.org/dc/terms/issued'),(18,'sameAs','http://www.w3.org/2002/07/owl#sameAs'),(19,'isLike','http://umbel.org/umbel#isLike'),(20,'issn','http://purl.org/ontology/bibo/issn'),(21,'eissn','http://purl.org/ontology/bibo/eissn'),(22,'lccn','http://purl.org/ontology/bibo/lccn'),(23,'oclcnum','http://purl.org/ontology/bibo/oclcnum'),(24,'isbn','http://purl.org/ontology/bibo/isbn'),(25,'medium','http://purl.org/dc/terms/medium'),(26,'hasPart','http://purl.org/dc/terms/hasPart'),(27,'isPartOf','http://purl.org/dc/terms/isPartOf'),(28,'hasVersion','http://purl.org/dc/terms/hasVersion'),(29,'isFormatOf','http://purl.org/dc/terms/isFormatOf'),(30,'precededBy','http://rdvocab.info/Elements/precededBy'),(31,'succeededBy','http://rdvocab.info/Elements/succeededBy'),(32,'language','http://purl.org/dc/terms/language'),(33,'1053','http://iflastandards.info/ns/isbd/elements/1053'),(34,'edition','http://purl.org/ontology/bibo/edition'),(35,'bibliographicCitation','http://purl.org/dc/terms/bibliographicCitation'),(36,'id','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#id'),(37,'typ','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#typ'),(38,'status','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#status'),(39,'mabVersion','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#mabVersion'),(40,'feld','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld'),(41,'nr','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#nr'),(42,'ind','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ind'),(43,'value','http://www.w3.org/1999/02/22-rdf-syntax-ns#value'),(44,'tf','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#tf'),(45,'stw','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#stw'),(46,'ns','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns'),(47,'uf','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf'),(48,'code','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#code');
/*!40000 ALTER TABLE `ATTRIBUTE` ENABLE KEYS */;

--
-- Dumping data for table `ATTRIBUTE_PATH`
--

/*!40000 ALTER TABLE `ATTRIBUTE_PATH` DISABLE KEYS */;
INSERT INTO `ATTRIBUTE_PATH` VALUES (1,'[1]'),(2,'[2]'),(3,'[3]'),(4,'[4]'),(5,'[5]'),(6,'[6]'),(7,'[7]'),(8,'[8]'),(9,'[9]'),(10,'[10]'),(11,'[11]'),(12,'[12]'),(13,'[13]'),(14,'[14]'),(15,'[15]'),(16,'[16]'),(17,'[17]'),(18,'[18]'),(19,'[19]'),(20,'[20]'),(21,'[21]'),(22,'[22]'),(23,'[23]'),(24,'[24]'),(25,'[25]'),(26,'[26]'),(27,'[27]'),(28,'[28]'),(29,'[29]'),(30,'[30]'),(31,'[31]'),(32,'[32]'),(33,'[33]'),(34,'[34]'),(35,'[35]'),(36,'[36]'),(37,'[37]'),(38,'[38]'),(39,'[39]'),(40,'[40]'),(41,'[40,1]'),(42,'[40,36]'),(43,'[40,41]'),(44,'[40,42]'),(45,'[40,43]'),(46,'[40,44]'),(47,'[40,44,1]'),(48,'[40,45]'),(49,'[40,45,1]'),(50,'[40,45,43]'),(51,'[40,46]'),(52,'[40,46,1]'),(53,'[40,46,43]'),(54,'[40,47]'),(55,'[40,47,1]'),(56,'[40,47,36]'),(57,'[40,47,48]'),(58,'[40,47,43]'),(59,'[40,47,44]'),(60,'[40,47,44,1]'),(61,'[40,47,45]'),(62,'[40,47,45,1]'),(63,'[40,47,45,43]'),(64,'[40,47,46]'),(65,'[40,47,46,1]'),(66,'[40,47,46,43]');
/*!40000 ALTER TABLE `ATTRIBUTE_PATH` ENABLE KEYS */;

--
-- Dumping data for table `ATTRIBUTE_PATHS_ATTRIBUTES`
--

/*!40000 ALTER TABLE `ATTRIBUTE_PATHS_ATTRIBUTES` DISABLE KEYS */;
INSERT INTO `ATTRIBUTE_PATHS_ATTRIBUTES` VALUES (1,1),(41,1),(47,1),(49,1),(52,1),(55,1),(60,1),(62,1),(65,1),(2,2),(3,3),(4,4),(5,5),(6,6),(7,7),(8,8),(9,9),(10,10),(11,11),(12,12),(13,13),(14,14),(15,15),(16,16),(17,17),(18,18),(19,19),(20,20),(21,21),(22,22),(23,23),(24,24),(25,25),(26,26),(27,27),(28,28),(29,29),(30,30),(31,31),(32,32),(33,33),(34,34),(35,35),(36,36),(42,36),(56,36),(37,37),(38,38),(39,39),(40,40),(41,40),(42,40),(43,40),(44,40),(45,40),(46,40),(47,40),(48,40),(49,40),(50,40),(51,40),(52,40),(53,40),(54,40),(55,40),(56,40),(57,40),(58,40),(59,40),(60,40),(61,40),(62,40),(63,40),(64,40),(65,40),(66,40),(43,41),(44,42),(45,43),(50,43),(53,43),(58,43),(63,43),(66,43),(46,44),(47,44),(59,44),(60,44),(48,45),(49,45),(50,45),(61,45),(62,45),(63,45),(51,46),(52,46),(53,46),(64,46),(65,46),(66,46),(54,47),(55,47),(56,47),(57,47),(58,47),(59,47),(60,47),(61,47),(62,47),(63,47),(64,47),(65,47),(66,47),(57,48);
/*!40000 ALTER TABLE `ATTRIBUTE_PATHS_ATTRIBUTES` ENABLE KEYS */;

--
-- Dumping data for table `ATTRIBUTE_PATH_INSTANCE`
--

/*!40000 ALTER TABLE `ATTRIBUTE_PATH_INSTANCE` DISABLE KEYS */;
INSERT INTO `ATTRIBUTE_PATH_INSTANCE` VALUES (1,NULL,'SchemaAttributePathInstance',1),(2,NULL,'SchemaAttributePathInstance',2),(3,NULL,'SchemaAttributePathInstance',3),(4,NULL,'SchemaAttributePathInstance',4),(5,NULL,'SchemaAttributePathInstance',3),(6,NULL,'SchemaAttributePathInstance',5),(7,NULL,'SchemaAttributePathInstance',6),(8,NULL,'SchemaAttributePathInstance',7),(9,NULL,'SchemaAttributePathInstance',8),(10,NULL,'SchemaAttributePathInstance',9),(11,NULL,'SchemaAttributePathInstance',1),(12,NULL,'SchemaAttributePathInstance',10),(13,NULL,'SchemaAttributePathInstance',11),(14,NULL,'SchemaAttributePathInstance',12),(15,NULL,'SchemaAttributePathInstance',13),(16,NULL,'SchemaAttributePathInstance',14),(17,NULL,'SchemaAttributePathInstance',15),(18,NULL,'SchemaAttributePathInstance',16),(19,NULL,'SchemaAttributePathInstance',17),(20,NULL,'SchemaAttributePathInstance',18),(21,NULL,'SchemaAttributePathInstance',19),(22,NULL,'SchemaAttributePathInstance',20),(23,NULL,'SchemaAttributePathInstance',21),(24,NULL,'SchemaAttributePathInstance',22),(25,NULL,'SchemaAttributePathInstance',23),(26,NULL,'SchemaAttributePathInstance',24),(27,NULL,'SchemaAttributePathInstance',1),(28,NULL,'SchemaAttributePathInstance',25),(29,NULL,'SchemaAttributePathInstance',26),(30,NULL,'SchemaAttributePathInstance',27),(31,NULL,'SchemaAttributePathInstance',28),(32,NULL,'SchemaAttributePathInstance',29),(33,NULL,'SchemaAttributePathInstance',30),(34,NULL,'SchemaAttributePathInstance',31),(35,NULL,'SchemaAttributePathInstance',32),(36,NULL,'SchemaAttributePathInstance',33),(37,NULL,'SchemaAttributePathInstance',34),(38,NULL,'SchemaAttributePathInstance',35),(39,NULL,'SchemaAttributePathInstance',1),(40,NULL,'SchemaAttributePathInstance',36),(41,NULL,'SchemaAttributePathInstance',37),(42,NULL,'SchemaAttributePathInstance',38),(43,NULL,'SchemaAttributePathInstance',39),(44,NULL,'SchemaAttributePathInstance',40),(45,NULL,'SchemaAttributePathInstance',41),(46,NULL,'SchemaAttributePathInstance',42),(47,NULL,'SchemaAttributePathInstance',43),(48,NULL,'SchemaAttributePathInstance',44),(49,NULL,'SchemaAttributePathInstance',45),(50,NULL,'SchemaAttributePathInstance',46),(51,NULL,'SchemaAttributePathInstance',47),(52,NULL,'SchemaAttributePathInstance',48),(53,NULL,'SchemaAttributePathInstance',49),(54,NULL,'SchemaAttributePathInstance',50),(55,NULL,'SchemaAttributePathInstance',51),(56,NULL,'SchemaAttributePathInstance',52),(57,NULL,'SchemaAttributePathInstance',53),(58,NULL,'SchemaAttributePathInstance',54),(59,NULL,'SchemaAttributePathInstance',55),(60,NULL,'SchemaAttributePathInstance',56),(61,NULL,'SchemaAttributePathInstance',57),(62,NULL,'SchemaAttributePathInstance',58),(63,NULL,'SchemaAttributePathInstance',59),(64,NULL,'SchemaAttributePathInstance',60),(65,NULL,'SchemaAttributePathInstance',61),(66,NULL,'SchemaAttributePathInstance',62),(67,NULL,'SchemaAttributePathInstance',63),(68,NULL,'SchemaAttributePathInstance',64),(69,NULL,'SchemaAttributePathInstance',65),(70,NULL,'SchemaAttributePathInstance',66);
/*!40000 ALTER TABLE `ATTRIBUTE_PATH_INSTANCE` ENABLE KEYS */;

--
-- Dumping data for table `CLASS`
--

/*!40000 ALTER TABLE `CLASS` DISABLE KEYS */;
INSERT INTO `CLASS` VALUES (1,'ContractItem','http://vocab.ub.uni-leipzig.de/bibrm/ContractItem'),(2,'Document','http://purl.org/ontology/bibo/Document'),(3,'Person','http://xmlns.com/foaf/0.1/Person'),(4,'datensatzType','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#datensatzType');
/*!40000 ALTER TABLE `CLASS` ENABLE KEYS */;

--
-- Dumping data for table `COMPONENT`
--

/*!40000 ALTER TABLE `COMPONENT` DISABLE KEYS */;
/*!40000 ALTER TABLE `COMPONENT` ENABLE KEYS */;

--
-- Dumping data for table `CONFIGURATION`
--

/*!40000 ALTER TABLE `CONFIGURATION` DISABLE KEYS */;
/*!40000 ALTER TABLE `CONFIGURATION` ENABLE KEYS */;

--
-- Dumping data for table `CONFIGURATIONS_RESOURCES`
--

/*!40000 ALTER TABLE `CONFIGURATIONS_RESOURCES` DISABLE KEYS */;
/*!40000 ALTER TABLE `CONFIGURATIONS_RESOURCES` ENABLE KEYS */;

--
-- Dumping data for table `CONTENT_SCHEMA`
--

/*!40000 ALTER TABLE `CONTENT_SCHEMA` DISABLE KEYS */;
INSERT INTO `CONTENT_SCHEMA` VALUES (1,'mab content schema','[43,44]',36,45);
/*!40000 ALTER TABLE `CONTENT_SCHEMA` ENABLE KEYS */;

--
-- Dumping data for table `CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS`
--

/*!40000 ALTER TABLE `CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS` DISABLE KEYS */;
INSERT INTO `CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS` VALUES (1,43),(1,44);
/*!40000 ALTER TABLE `CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS` ENABLE KEYS */;

--
-- Dumping data for table `DATA_MODEL`
--

/*!40000 ALTER TABLE `DATA_MODEL` DISABLE KEYS */;
/*!40000 ALTER TABLE `DATA_MODEL` ENABLE KEYS */;

--
-- Dumping data for table `DATA_SCHEMA`
--

/*!40000 ALTER TABLE `DATA_SCHEMA` DISABLE KEYS */;
INSERT INTO `DATA_SCHEMA` VALUES (1,'bibrm:ContractItem-Schema (ERM-Scenario)','[1,2,3,4]',NULL,1),(2,'foaf:Person-Schema','[11,12,13]',NULL,3),(3,'bibo:Document-Schema (KIM-Titeldaten)','[5,6,7,8,9,10,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38]',NULL,2),(4,'mabxml schema','[39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70]',1,4);
/*!40000 ALTER TABLE `DATA_SCHEMA` ENABLE KEYS */;

--
-- Dumping data for table `FILTER`
--

/*!40000 ALTER TABLE `FILTER` DISABLE KEYS */;
/*!40000 ALTER TABLE `FILTER` ENABLE KEYS */;

--
-- Dumping data for table `FUNCTION`
--

/*!40000 ALTER TABLE `FUNCTION` DISABLE KEYS */;
/*!40000 ALTER TABLE `FUNCTION` ENABLE KEYS */;

--
-- Dumping data for table `INPUT_COMPONENTS_OUTPUT_COMPONENTS`
--

/*!40000 ALTER TABLE `INPUT_COMPONENTS_OUTPUT_COMPONENTS` DISABLE KEYS */;
/*!40000 ALTER TABLE `INPUT_COMPONENTS_OUTPUT_COMPONENTS` ENABLE KEYS */;

--
-- Dumping data for table `MAPPING`
--

/*!40000 ALTER TABLE `MAPPING` DISABLE KEYS */;
/*!40000 ALTER TABLE `MAPPING` ENABLE KEYS */;

--
-- Dumping data for table `MAPPINGS_INPUT_ATTRIBUTE_PATHS`
--

/*!40000 ALTER TABLE `MAPPINGS_INPUT_ATTRIBUTE_PATHS` DISABLE KEYS */;
/*!40000 ALTER TABLE `MAPPINGS_INPUT_ATTRIBUTE_PATHS` ENABLE KEYS */;

--
-- Dumping data for table `MAPPING_ATTRIBUTE_PATH_INSTANCE`
--

/*!40000 ALTER TABLE `MAPPING_ATTRIBUTE_PATH_INSTANCE` DISABLE KEYS */;
/*!40000 ALTER TABLE `MAPPING_ATTRIBUTE_PATH_INSTANCE` ENABLE KEYS */;

--
-- Dumping data for table `PROJECT`
--

/*!40000 ALTER TABLE `PROJECT` DISABLE KEYS */;
/*!40000 ALTER TABLE `PROJECT` ENABLE KEYS */;

--
-- Dumping data for table `PROJECTS_FUNCTIONS`
--

/*!40000 ALTER TABLE `PROJECTS_FUNCTIONS` DISABLE KEYS */;
/*!40000 ALTER TABLE `PROJECTS_FUNCTIONS` ENABLE KEYS */;

--
-- Dumping data for table `PROJECTS_MAPPINGS`
--

/*!40000 ALTER TABLE `PROJECTS_MAPPINGS` DISABLE KEYS */;
/*!40000 ALTER TABLE `PROJECTS_MAPPINGS` ENABLE KEYS */;

--
-- Dumping data for table `RESOURCE`
--

/*!40000 ALTER TABLE `RESOURCE` DISABLE KEYS */;
/*!40000 ALTER TABLE `RESOURCE` ENABLE KEYS */;

--
-- Dumping data for table `SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES`
--

/*!40000 ALTER TABLE `SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES` DISABLE KEYS */;
INSERT INTO `SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES` VALUES (1,1),(1,2),(1,3),(1,4),(3,5),(3,6),(3,7),(3,8),(3,9),(3,10),(2,11),(2,12),(2,13),(3,14),(3,15),(3,16),(3,17),(3,18),(3,19),(3,20),(3,21),(3,22),(3,23),(3,24),(3,25),(3,26),(3,27),(3,28),(3,29),(3,30),(3,31),(3,32),(3,33),(3,34),(3,35),(3,36),(3,37),(3,38),(4,39),(4,40),(4,41),(4,42),(4,43),(4,44),(4,45),(4,46),(4,47),(4,48),(4,49),(4,50),(4,51),(4,52),(4,53),(4,54),(4,55),(4,56),(4,57),(4,58),(4,59),(4,60),(4,61),(4,62),(4,63),(4,64),(4,65),(4,66),(4,67),(4,68),(4,69),(4,70);
/*!40000 ALTER TABLE `SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES` ENABLE KEYS */;

--
-- Dumping data for table `SCHEMA_ATTRIBUTE_PATH_INSTANCE`
--

/*!40000 ALTER TABLE `SCHEMA_ATTRIBUTE_PATH_INSTANCE` DISABLE KEYS */;
INSERT INTO `SCHEMA_ATTRIBUTE_PATH_INSTANCE` VALUES (1,NULL),(2,NULL),(3,NULL),(4,NULL),(5,NULL),(6,NULL),(7,NULL),(8,NULL),(9,NULL),(10,NULL),(11,NULL),(12,NULL),(13,NULL),(16,NULL),(17,NULL),(18,NULL),(19,NULL),(20,NULL),(21,NULL),(22,NULL),(23,NULL),(24,NULL),(25,NULL),(26,NULL),(27,NULL),(28,NULL),(29,NULL),(30,NULL),(31,NULL),(32,NULL),(33,NULL),(34,NULL),(35,NULL),(36,NULL),(37,NULL),(38,NULL),(39,NULL),(40,NULL),(41,NULL),(42,NULL),(43,NULL),(44,NULL),(45,NULL),(46,NULL),(47,NULL),(48,NULL),(49,NULL),(50,NULL),(51,NULL),(52,NULL),(53,NULL),(54,NULL),(55,NULL),(56,NULL),(57,NULL),(58,NULL),(59,NULL),(60,NULL),(61,NULL),(62,NULL),(63,NULL),(64,NULL),(65,NULL),(66,NULL),(67,NULL),(68,NULL),(69,NULL),(70,NULL),(14,2),(15,2);
/*!40000 ALTER TABLE `SCHEMA_ATTRIBUTE_PATH_INSTANCE` ENABLE KEYS */;

--
-- Dumping data for table `TRANSFORMATION`
--

/*!40000 ALTER TABLE `TRANSFORMATION` DISABLE KEYS */;
/*!40000 ALTER TABLE `TRANSFORMATION` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-11-17 17:24:45
