package com.pictManager.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pictManager.model.Album;
import com.pictManager.repository.AlbumRepository;

@Service
public class AlbumService {

    @Autowired
	private AlbumRepository albumRepository;

    public Album save(Album album) {
		return this.albumRepository.save(album);
	}

	public Album getAlbum(Long album_id) throws Exception {
		Optional<Album> album =  this.albumRepository.findById(album_id);
		if (album.isPresent()){
			return album.get();
		}else{
			throw new Exception("Album not found");
		}
	}

	public List<Album> getAlbums(Long user_id){
		return this.albumRepository.getAlbumsAndSharedOnes(user_id);
	}

	public void delete(Album album) {
		this.albumRepository.delete(album);
	}
}
