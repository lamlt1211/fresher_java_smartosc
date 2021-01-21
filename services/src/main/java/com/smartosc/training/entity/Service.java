package com.smartosc.training.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Created by DucTD on 17/4/2020
 */
@Entity
@Table(name = "service")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service extends BaseAudit {
    @Id
    @Column(name = "service_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    //1 - chuyển mạch 0 - không phải chuyển mạch
    @Column(name = "trans_status")
    private Integer transStatus;

    //1 - active , 0 - inactive, default = 1
    @Column(name = "status")
    private Integer status;

}
