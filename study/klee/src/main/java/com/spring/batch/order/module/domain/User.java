package com.spring.batch.order.module.domain;

import com.spring.batch.order.module.constants.NotificationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "notify_on")
    private boolean isNotifyOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "notify_type")
    private NotificationType notifyType;
}
