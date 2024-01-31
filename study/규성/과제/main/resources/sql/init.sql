DROP TABLE IF EXISTS MEMBER;
CREATE TABLE IF NOT EXISTS Member(
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    last_login_at           DATETIME(6),
    nick_name               VARCHAR(100) NOT NULL,
    status                  VARCHAR(20)  NOT NULL,
    member_role             VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id)
);