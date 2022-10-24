package com.pictManager.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.pictManager.model.Picture;

@Repository
public interface PictureRepository extends CrudRepository<Picture, Long>, CustomPictureRepository {

    Optional<Picture> findById(Long id);
}
