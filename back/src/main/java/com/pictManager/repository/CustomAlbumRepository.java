package com.pictManager.repository;

import java.util.List;

import com.pictManager.model.Album;

public interface CustomAlbumRepository {
    List<Album> getAlbumsAndSharedOnes(Long userId);
}
