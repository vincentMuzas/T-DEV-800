package com.pictManager.repository;

import java.util.List;

import com.pictManager.model.Picture;

public interface CustomPictureRepository {
    List<Picture> getPicturesAndSharedOnes(Long userId);
}
