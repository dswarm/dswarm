# Attention! This will truncate the class, attribute, attributepath and schema tables!
# A schema and data model for bibo:Document and bibrm:ContractItem will be created

SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `dmp`
--

--
-- Truncate table before insert `ATTRIBUTE`
--

TRUNCATE TABLE `ATTRIBUTE`;
--
-- Dumping data for table `ATTRIBUTE`
--

INSERT INTO `ATTRIBUTE` (`ID`, `NAME`, `URI`) VALUES
(1, 'type', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'),
(2, 'EISSN', 'http://vocab.ub.uni-leipzig.de/bibrm/EISSN'),
(3, 'title', 'http://purl.org/dc/elements/1.1/title'),
(4, 'price', 'http://vocab.ub.uni-leipzig.de/bibrm/price'),
(5, 'otherTitleInformation', 'http://rdvocab.info/Elements/otherTitleInformation'),
(6, 'alternative', 'http://purl.org/dc/terms/alternative'),
(7, 'shortTitle', 'http://purl.org/ontology/bibo/shortTitle'),
(8, 'creator', 'http://purl.org/dc/terms/creator'),
(9, 'creator', 'http://purl.org/dc/elements/1.1/creator'),
(10, 'contributor', 'http://purl.org/dc/terms/contributor'),
(11, 'contributor', 'http://purl.org/dc/elements/1.1/contributor'),
(12, 'publicationStatement', 'http://rdvocab.info/Elements/publicationStatement'),
(13, 'placeOfPublication', 'http://rdvocab.info/Elements/placeOfPublication'),
(14, 'publisher', 'http://purl.org/dc/elements/1.1/publisher'),
(15, 'issued', 'http://purl.org/dc/terms/issued'),
(16, 'sameAs', 'http://www.w3.org/2002/07/owl#sameAs'),
(17, 'isLike', 'http://umbel.org/umbel#isLike'),
(18, 'issn', 'http://purl.org/ontology/bibo/issn'),
(19, 'eissn', 'http://purl.org/ontology/bibo/eissn'),
(20, 'lccn', 'http://purl.org/ontology/bibo/lccn'),
(21, 'oclcnum', 'http://purl.org/ontology/bibo/oclcnum'),
(22, 'isbn', 'http://purl.org/ontology/bibo/isbn'),
(23, 'medium', 'http://purl.org/dc/terms/medium'),
(24, 'hasPart', 'http://purl.org/dc/terms/hasPart'),
(25, 'isPartOf', 'http://purl.org/dc/terms/isPartOf'),
(26, 'hasVersion', 'http://purl.org/dc/terms/hasVersion'),
(27, 'isFormatOf', 'http://purl.org/dc/terms/isFormatOf'),
(28, 'precededBy', 'http://rdvocab.info/Elements/precededBy'),
(29, 'succeededBy', 'http://rdvocab.info/Elements/succeededBy'),
(30, 'language', 'http://purl.org/dc/terms/language'),
(31, '1053', 'http://iflastandards.info/ns/isbd/elements/1053'),
(32, 'edition', 'http://purl.org/ontology/bibo/edition'),
(33, 'bibliographicCitation', 'http://purl.org/dc/terms/bibliographicCitation'),
(34, 'familyName', 'http://xmlns.com/foaf/0.1/familyName'),
(35, 'givenName', 'http://xmlns.com/foaf/0.1/givenName'),
(36,'id','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#id'),
(37,'typ','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#typ'),
(38,'status','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#status'),
(39,'mabVersion','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#mabVersion'),
(40,'feld','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld'),
(41,'nr','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#nr'),
(42,'ind','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ind'),
(43,'value','http://www.w3.org/1999/02/22-rdf-syntax-ns#value'),
(44,'ns','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns'),
(45,'tf','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#tf');

--
-- Truncate table before insert `ATTRIBUTES_ATTRIBUTE_PATHS`
--

TRUNCATE TABLE `ATTRIBUTE_PATHS_ATTRIBUTES`;
--
-- Dumping data for table `ATTRIBUTES_ATTRIBUTE_PATHS`
--

INSERT INTO `ATTRIBUTE_PATHS_ATTRIBUTES` (`ATTRIBUTE_PATH_ID`, `ATTRIBUTE_ID`) VALUES
(1, 1),
(34, 1),
(37, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 7),
(8, 8),
(34, 8),
(35, 8),
(36, 8),
(9, 9),
(10, 10),
(37, 10),
(38, 10),
(39, 10),
(11, 11),
(12, 12),
(13, 13),
(14, 14),
(15, 15),
(16, 16),
(17, 17),
(18, 18),
(19, 19),
(20, 20),
(21, 21),
(22, 22),
(23, 23),
(24, 24),
(25, 25),
(26, 26),
(27, 27),
(28, 28),
(29, 29),
(30, 30),
(31, 31),
(32, 32),
(33, 33),
(35, 34),
(38, 34),
(36, 35),
(39, 35),
(40,36),
(41,37),
(42,38),
(43,39),
(44,40),
(45,40),
(46,40),
(47,40),
(48,40),
(49,40),
(50,40),
(51,40),
(52,40),
(53,40),
(46,41),
(54,41),
(47,42),
(48,43),
(51,43),
(49,44),
(50,44),
(51,44),
(52,45),
(53,45);

--
-- Truncate table before insert `ATTRIBUTE_PATH`
--

TRUNCATE TABLE `ATTRIBUTE_PATH`;
--
-- Dumping data for table `ATTRIBUTE_PATH`
--

INSERT INTO `ATTRIBUTE_PATH` (`ID`, `ATTRIBUTE_PATH`) VALUES
(1, '[1]'),
(2, '[2]'),
(3, '[3]'),
(4, '[4]'),
(5, '[5]'),
(6, '[6]'),
(7, '[7]'),
(8, '[8]'),
(9, '[9]'),
(10, '[10]'),
(11, '[11]'),
(12, '[12]'),
(13, '[13]'),
(14, '[14]'),
(15, '[15]'),
(16, '[16]'),
(17, '[17]'),
(18, '[18]'),
(19, '[19]'),
(20, '[20]'),
(21, '[21]'),
(22, '[22]'),
(23, '[23]'),
(24, '[24]'),
(25, '[25]'),
(26, '[26]'),
(27, '[27]'),
(28, '[28]'),
(29, '[29]'),
(30, '[30]'),
(31, '[31]'),
(32, '[32]'),
(33, '[33]'),
(34, '[8,1]'),
(35, '[8,34]'),
(36, '[8,35]'),
(37, '[10,1]'),
(38, '[10,34]'),
(39, '[10,35]'),
(40,'[36]'),
(41,'[37]'),
(42,'[38]'),
(43,'[39]'),
(44,'[40]'),
(45,'[40,1]'),
(46,'[40,41]'),
(47,'[40,42]'),
(48,'[40,43]'),
(49,'[40,44]'),
(50,'[40,44,1]'),
(51,'[40,44,43]'),
(52,'[40,45]'),
(53,'[40,45,1]');

--
-- Truncate table before insert `ATTRIBUTE_PATHS_SCHEMAS`
--

TRUNCATE TABLE `SCHEMAS_ATTRIBUTE_PATHS`;
--
-- Dumping data for table `ATTRIBUTE_PATHS_SCHEMAS`
--

INSERT INTO `SCHEMAS_ATTRIBUTE_PATHS` (`SCHEMA_ID`, `ATTRIBUTE_PATH_ID`) VALUES
(1, 1),
(2, 1),
(1, 2),
(1, 3),
(2, 3),
(1, 4),
(2, 5),
(2, 6),
(2, 7),
(2, 8),
(2, 9),
(2, 10),
(2, 11),
(2, 12),
(2, 13),
(2, 14),
(2, 15),
(2, 16),
(2, 17),
(2, 18),
(2, 19),
(2, 20),
(2, 21),
(2, 22),
(2, 23),
(2, 24),
(2, 25),
(2, 26),
(2, 27),
(2, 28),
(2, 29),
(2, 30),
(2, 31),
(2, 32),
(2, 33),
(2, 34),
(2, 35),
(2, 36),
(2, 37),
(2, 38),
(2, 39),
(3,40),
(3,41),
(3,42),
(3,43),
(3,44),
(3,45),
(3,46),
(3,47),
(3,48),
(3,49),
(3,50),
(3,51),
(3,52),
(3,53);

--
-- Truncate table before insert `CLASS`
--

TRUNCATE TABLE `CLASS`;
--
-- Dumping data for table `CLASS`
--

INSERT INTO `CLASS` (`ID`, `NAME`, `URI`) VALUES
(1, 'ContractItem', 'http://vocab.ub.uni-leipzig.de/bibrm/ContractItem'),
(2, 'Document', 'http://purl.org/ontology/bibo/Document'),
(3,'datensatzType','http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#datensatzType');

--
-- Truncate table before insert `CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS`
--

TRUNCATE TABLE `CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS`;
--
-- Dumping data for table `CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS`
--

INSERT INTO `CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS` (`CONTENT_SCHEMA_ID`, `ATTRIBUTE_PATH_ID`) VALUES
(1,46),(1,47);

--
-- Truncate table before insert `CONTENT_SCHEMA`
--

TRUNCATE TABLE `CONTENT_SCHEMA`;
--
-- Dumping data for table `CONTENT_SCHEMA`
--

INSERT INTO `CONTENT_SCHEMA` (`ID`, `NAME`, `KEY_ATTRIBUTE_PATHS`, `VALUE_ATTRIBUTE_PATH`) VALUES
(1,'mab content schema','[46,47]',48);

TRUNCATE TABLE `DATA_SCHEMA`;
--
-- Dumping data for table `DATA_SCHEMA`
--

INSERT INTO `DATA_SCHEMA` (`ID`, `NAME`, `CONTENT_SCHEMA`, `RECORD_CLASS`) VALUES
(1,'bibrm:ContractItem-Schema (ERM-Scenario)', null, 1),
(2,'bibo:Document-Schema (KIM-Titeldaten)', null, 2),
(3,'mabxml schema',1,3);

--
-- Truncate table before insert `DATA_MODEL`
--

TRUNCATE TABLE `DATA_MODEL`;
INSERT INTO `DATA_MODEL` (ID, NAME, DESCRIPTION, CONFIGURATION, DATA_RESOURCE, DATA_SCHEMA) VALUES
(1,'Internal Data Model ContractItem', 'Internal Data Model ContractItem', null, null, 1),
(2,'Internal Data Model BiboDocument', 'Internal Data Model BiboDocument', null, null, 2),
(3,'Internal Data Model mabxml', 'Internal Data Model mabxml', null, null, 3);

SET FOREIGN_KEY_CHECKS=1;
