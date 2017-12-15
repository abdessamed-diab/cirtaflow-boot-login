CREATE TABLE IF NOT EXISTS `CF_PROFILE` (
  `ID` varchar(255) NOT NULL,
  `FC_USER_AS_BYTES` longblob,
  `FIRST_NAME` varchar(255) NOT NULL,
  `GENDER` varchar(255) NOT NULL,
  `LAST_NAME` varchar(255) NOT NULL,
  `LOCALE` varchar(255) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `PROFILE_PICTURE` longblob,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_995yh01v7sy0hjmwlus8ja7wb` (`NAME`)
);

CREATE TABLE IF NOT EXISTS `CF_PROFILE_CF_PROFILE` (
  `Profile_ID` varchar(255) NOT NULL,
  `friendList_ID` varchar(255) NOT NULL,
  UNIQUE KEY `UK_esgdyoqsgt8k0ybovalpeomtq` (`friendList_ID`),
  KEY `FK4ns8exdpl311nopb6r4kp3qeu` (`Profile_ID`),
  CONSTRAINT `FK4ns8exdpl311nopb6r4kp3qeu` FOREIGN KEY (`Profile_ID`) REFERENCES `CF_PROFILE` (`ID`),
  CONSTRAINT `FKd5pxpr51to6s2dfgxs42npvj5` FOREIGN KEY (`friendList_ID`) REFERENCES `CF_PROFILE` (`ID`)
);


DROP TABLE IF EXISTS PERSON;
