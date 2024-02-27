package io.spring.batch.helloworld.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "simple_mock_accounts")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "id")
    private Integer id;

    @Column(name= "user_id")
    private Integer userId;

    @Column(name= "name")
    private String name;

    @Column(name= "organization_id")
    private Integer organizationId;

    @Column(name= "emp_id")
    private Integer empId;

    @Column(name= "job")
    private String job;

    @Column(name= "hobby")
    private String hobby;

    @Column(name= "join_datetime")
    private ZonedDateTime joinDatetime;

}
