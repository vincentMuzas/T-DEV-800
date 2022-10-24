package com.pictManager.fichier;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pictManager.model.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fichier")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Fichier implements Serializable{
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Getter
    @Setter
    @Column(name = "nom")
    private String nom;

    @Getter
    @Setter
    @Column(name = "path")
    private String path = "";

    @Getter
    @Setter
    @Column(name = "created_date")
    private Instant createdDate;

    @Getter
    @Setter
    @Column(name = "modif_date")
    private Instant modifDate;

    @Getter
    @Setter
    @ManyToOne
    @JsonIgnoreProperties(value = "fichiers", allowSetters = true)
    private User user;

    @PrePersist
    public void beforeSave() {
        this.createdDate = Instant.now();
    }

    @PreUpdate
    public void beforeUpdate() {
        this.modifDate = Instant.now();
    }

    public Fichier id(Long id) {
        this.id = id;
        return this;
    }

    public Fichier nom(String nom) {
        this.nom = nom;
        return this;
    }

    public Fichier path(String path) {
        this.path = path;
        return this;
    }

    public Fichier createdDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Fichier modifDate(Instant modifDate) {
        this.modifDate = modifDate;
        return this;
    }

    public Fichier user(User user) {
        this.user = user;
        return this;
    }

    public String getFullPath() {
        return getPath() + getNomUnique();
    }

    public String getNomUnique() {
        return this.id + "_" + this.nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fichier)) return false;
        return id != null && id.equals(((Fichier) o ).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Fichier{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", fullpath='" + getFullPath() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", modifDate='" + getModifDate() +"'" +
            "}";
    }
}
