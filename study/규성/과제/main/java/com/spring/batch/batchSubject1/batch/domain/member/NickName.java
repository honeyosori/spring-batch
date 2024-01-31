package com.spring.batch.batchSubject1.batch.domain.member;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NickName {

    @Column(name = "nickName", nullable = false, unique = true)
    private String value;

}
