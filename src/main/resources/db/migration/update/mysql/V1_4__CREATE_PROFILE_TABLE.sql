CREATE TABLE IF NOT EXISTS `PROFILE` (
  `COVER_PHOTO_URL` varchar(255) DEFAULT NULL,
  `PROFILE_NAME` varchar(255) NOT NULL,
  `PROFILE_PICTURE_URL` varchar(255) DEFAULT NULL,
  `PROFILE_URL` varchar(255) DEFAULT NULL,
  `ACT_ID_USER_ID_` varchar(64) NOT NULL,
  PRIMARY KEY (`ACT_ID_USER_ID_`),
  UNIQUE KEY `UK_paki3wugbme0t0efxg9backdn` (`PROFILE_NAME`),
  UNIQUE KEY `UK_2duio1xx3y4r7726fwsm4vboo` (`PROFILE_URL`)
);

DROP TABLE IF EXISTS PERSON ;