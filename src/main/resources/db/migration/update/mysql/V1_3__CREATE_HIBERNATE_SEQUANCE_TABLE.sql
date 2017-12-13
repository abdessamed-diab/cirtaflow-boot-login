CREATE TABLE IF NOT EXISTS `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
);

INSERT INTO hibernate_sequence (next_val) values (0);
