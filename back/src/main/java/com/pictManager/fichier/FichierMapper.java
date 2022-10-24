package com.pictManager.fichier;

import java.util.List;
import java.util.stream.Collectors;

import com.pictManager.model.User;
import com.pictManager.shared.EntityMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper for entity {@link Fichier} and its DTO {@link FichierDTO}
 */
@Component
public class FichierMapper implements EntityMapper<FichierDTO, Fichier> {
    @Override
    public FichierDTO toDTO(Fichier fichier) {
        FichierDTO ret = new FichierDTO();
        ret.setCreatedDate(fichier.getCreatedDate());
        ret.setId(fichier.getId());
        ret.setModifDate(fichier.getModifDate());
        ret.setNom(fichier.getNom());
        ret.setPath(fichier.getPath());
        ret.setUserId(fichier.getUser().getId());
        return ret;
    }

    @Override
    public Fichier toEntity(FichierDTO fichierDTO) {
        Fichier ret = new Fichier()
            .id(fichierDTO.getId())
            .nom(fichierDTO.getNom())
            .path(fichierDTO.getPath())
            .createdDate(fichierDTO.getCreatedDate())
            .modifDate(fichierDTO.getModifDate());
        final User tmp = new User();
        tmp.setId(fichierDTO.getUserId());
        ret.setUser(tmp);

        return ret;
    }

    @Override
    public List<Fichier> toEntity(List<FichierDTO> dto) {
        return dto.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<FichierDTO> toDTO(List<Fichier> entity) {
        return entity.stream().map(this::toDTO).collect(Collectors.toList());
    }

    Fichier fromId(Long id) {
        if (id == null) {
            return null;
        }
        Fichier fichier = new Fichier();
        fichier.setId(id);
        return fichier;
    }

}
