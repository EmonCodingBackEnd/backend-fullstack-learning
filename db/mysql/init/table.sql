-- -------------------------------------------------
--   _____       _      _
--  |_   _|__ _ | |__  | |  ___
--    | | / _` || '_ \ | | / _ \
--    | || (_| || |_) || ||  __/
--    |_| \__,_||_.__/ |_| \___|
--
-- 表
-- -------------------------------------------------
create table mq_message
(
    message_id     char(32) not null,
    content        text,
    to_exchange    varchar(255) default null,
    routing_key    varchar(255) default null,
    class_type     varchar(255) default null,
    message_status int(1)       default 0 comment '0-新建，1-已发送，2-错误抵达，3-已抵达',
    create_time    datetime     default null,
    update_time    datetime     default null,
    primary key (message_id)
) engine = innodb
  default charset = utf8mb4;