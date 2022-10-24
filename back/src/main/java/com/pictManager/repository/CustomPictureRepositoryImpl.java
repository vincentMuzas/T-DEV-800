package com.pictManager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.pictManager.model.Picture;
import com.pictManager.model.QPicture;
import com.querydsl.jpa.JPQLQuery;

public class CustomPictureRepositoryImpl extends QuerydslRepositorySupport implements CustomPictureRepository {

    public CustomPictureRepositoryImpl() {
        super(Picture.class);
    }

    @Override
    public List<Picture> getPicturesAndSharedOnes(Long userId) {
        QPicture picture = QPicture.picture;

        JPQLQuery<Picture> pictureQuery = from(picture)
            .where(picture.user.id.eq(userId).or(picture.user_shared.any().id.eq(userId)));

        List<Picture> pictures = pictureQuery.fetch();

        return pictures;
    }

}
