package com.pictManager.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pictManager.fichier.FichierService;
import com.pictManager.model.Picture;
import com.pictManager.repository.PictureRepository;

@Service
public class PictureService {

    @Autowired
	private final PictureRepository pictureRepository;

	@Autowired
	private final FichierService fichierService;

    PictureService(PictureRepository pictureRepository, FichierService fichierService) {
		this.pictureRepository = pictureRepository;
		this.fichierService = fichierService;
	}

    public Picture save(Picture picture) {
		return this.pictureRepository.save(picture);
	}

	public void delete(Picture picture) {
		this.pictureRepository.delete(picture);
		this.fichierService.delete(picture.getFile());
	}

	public Picture getPicture(Long picture_id) throws Exception {
		Optional<Picture> picture =  this.pictureRepository.findById(picture_id);
		if (picture.isPresent()){
			return picture.get();
		}else{
			throw new Exception("Picture not found");
		}
	}

	public List<Picture> getPicturesByUserid(Long user_id){
		return this.pictureRepository.getPicturesAndSharedOnes(user_id);
	}
}
