package com.spring.batch.hwk.module;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class DomainDto {

    private Long virtualHostId;
    private String name;
    private String host;
    private String schema;
}
