DROP TABLE IF EXISTS `PROFILE`;
DROP TABLE IF EXISTS PERSON;

CREATE TABLE `CF_PROFILE` (
  `ID` varchar(255) NOT NULL,
  `FIRST_NAME` varchar(255) NOT NULL,
  `GENDER` varchar(255) NOT NULL,
  `LAST_NAME` varchar(255) NOT NULL,
  `LOCALE` varchar(255) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `PROFILE_PICTURE` longblob,
  PRIMARY KEY (`ID`)
);

CREATE TABLE `CF_FRIEND` (
  `ID` bigint(20) NOT NULL,
  `STATUS` varchar(255) DEFAULT NULL,
  `FRIEND_PROFILE` varchar(255) NOT NULL,
  `PROFILE` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_FRIEND_PROFILE` (`FRIEND_PROFILE`),
  KEY `FK_PROFILE_FRIENDS` (`PROFILE`),
  CONSTRAINT `FK_FRIEND_PROFILE` FOREIGN KEY (`FRIEND_PROFILE`) REFERENCES `CF_PROFILE` (`ID`),
  CONSTRAINT `FK_PROFILE_FRIENDS` FOREIGN KEY (`PROFILE`) REFERENCES `CF_PROFILE` (`ID`)
);













