package com.pictManager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.pictManager.model.Album;
import com.pictManager.model.QAlbum;
import com.querydsl.jpa.JPQLQuery;

public class CustomAlbumRepositoryImpl extends QuerydslRepositorySupport implements CustomAlbumRepository {

    public CustomAlbumRepositoryImpl() {
        super(Album.class);
    }

    @Override
    public List<Album> getAlbumsAndSharedOnes(Long userId) {
        QAlbum album = QAlbum.album;

        JPQLQuery<Album> albumQuery = from(album)
            .where(album.user.id.eq(userId).or(album.user_shared.any().id.eq(userId)));

        List<Album> albums = albumQuery.fetch();

        return albums;
    }

}
