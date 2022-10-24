package com.pictManager.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.pictManager.model.Album;

@Repository
public interface AlbumRepository extends CrudRepository<Album, Long>, CustomAlbumRepository {

    Optional<Album> findById(Long id);
}
