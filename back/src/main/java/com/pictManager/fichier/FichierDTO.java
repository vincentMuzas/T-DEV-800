package com.pictManager.fichier;

import java.io.Serializable;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * A DTO for {@link com.pictManager.fichier.Fichier} entity
 */
public class FichierDTO implements Serializable{
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String nom;

    @Getter
    @Setter
    private String path;

    @Getter
    @Setter
    private Instant createdDate;

    @Getter
    @Setter
    private Instant ModifDate;

    @Getter
    @Setter
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FichierDTO)) return false;
        return id != null && id.equals(((FichierDTO)o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FichierDTO{" +
            ", nom='" + getNom() + "'" +
            ", path='" + getPath() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", modifDate='" + getModifDate() + "'" +
            ", userId=" + getUserId() +
            "}";
    }
}