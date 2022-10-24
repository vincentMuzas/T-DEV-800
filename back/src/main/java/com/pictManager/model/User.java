package com.pictManager.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pm_user")
public class User implements Serializable {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @Column(name = "email", length = 255, unique=true)
    private String email;

    @Setter
    @Column(name = "password", length = 255)
    private String password;

    @Getter
    @Setter
    @Column(name = "username", length = 50, unique=true)
    private String username;

    public User(){}

    public User(String username, String email, String password){
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @JsonIgnore
    @JsonProperty(value = "password")
    public String getPassword() {
        return this.password;
    }

    @Override
    public String toString(){
        return "User={"+
            "Username : " + this.username +
            ", Email : " + this.email +
            ", id : " + this.id +
            "}";
    }
}
