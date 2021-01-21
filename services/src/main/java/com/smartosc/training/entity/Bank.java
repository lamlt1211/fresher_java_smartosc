package com.smartosc.training.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by DucTD on 17/4/2020
 */
@Entity
@Table(name = "bank")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bank extends BaseAudit {
    @Id
    @Column(name = "bank_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bankId;

    @Column(name = "code")
    private String code;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "prefix_card")
    private String prefixCard;

    //1 - active , 0 - inactive,default = 1
    @Column(name = "status")
    private Integer status;

}
