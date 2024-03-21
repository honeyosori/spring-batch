package com.spring.batch.week5.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Customer {

    private String middleInitial;
    private String lastName;
    private String addressNumber;
	private String street;
    private String address;
    private String city;
    private String state;
    private String zipCode;


}
