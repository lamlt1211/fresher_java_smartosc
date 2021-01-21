package com.smartosc.training.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Entity
@Table(name = "users")
@Getter
@Setter
public class Users implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    @Column(name = "fullname", length = 45)
    private String fullName;
    @Column(name = "email", length = 45)
    private String email;
    @Column(name = "username", length = 45)
    private String userName;
    @Column(name = "password", length = 60)
    private String password;
    @CreationTimestamp
    @Column(name = "createdat", updatable = false)
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updatedat")
    private Date updatedAt;
    @Column(name = "status", columnDefinition = "int default 1")
    private int status;
    @Column(name = "enabled", columnDefinition = "boolean default false")
    private boolean enabled;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "users")
    private List<Orders> orders;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "users")
    private List<Address> addresses;
    
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "users_role",
            joinColumns = { @JoinColumn(name = "user_id") },
            inverseJoinColumns = {@JoinColumn(name = "role_id") })
    private List<Role> roles;

}
