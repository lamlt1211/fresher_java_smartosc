package com.smartosc.training.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Created by DucTD on 17/4/2020
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_code", uniqueConstraints = {@UniqueConstraint(columnNames = "code")})
public class AppCode extends BaseAudit {
    @Id
    @Column(name = "app_code_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer appCodeId;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    //1 - active , 0 - inactive, default = 1
    @Column(name = "status")
    private Integer status;
}
