package com.github.t1.jeejsonpatchdemo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Student {
    private Long id;
    private String firstName;
    private String lastName;
    private Gender gender;
    private String school;
}
