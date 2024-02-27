create table test.simple_mock_accounts
(
    id              int auto_increment comment 'pk'
        primary key,
    user_id         int          null comment '유저 아이디',
    name            varchar(50)  null comment '이름',
    organization_id int          null comment '조직번호',
    emp_id          int          null comment '사번',
    job             varchar(100) null comment '직업',
    hobby           varchar(100) null comment '취미',
    join_datetime   datetime     null comment '입사일'
);

