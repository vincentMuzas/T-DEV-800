package com.pictManager.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pictManager.config.JwtTokenUtil;
import com.pictManager.fichier.Fichier;
import com.pictManager.fichier.FichierService;
import com.pictManager.model.Picture;
import com.pictManager.model.User;
import com.pictManager.service.PictureService;
import com.pictManager.service.UserService;

@RestController
@CrossOrigin
public class PictureController {

	private final Logger log = LoggerFactory.getLogger(PictureController.class);

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserService userService;

    @Autowired
	private PictureService pictureService;

	@Autowired
	private FichierService fichierService;

    @RequestMapping(value = "/picture", method = RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity<Picture> createPicture(@RequestParam("file") MultipartFile file, @RequestHeader(name="Authorization") String token) throws Exception {
		log.debug("REST request to save a Picture with file : {}, size : {}", file.getName(), file.getSize());
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
        final Fichier fichier = this.fichierService.save(file, user);
        final Picture picture = pictureService.save(new Picture(user, fichier));
		return new ResponseEntity<Picture>(picture, HttpStatus.OK);
	}

	@RequestMapping(value = "/picture/{picture_id}", method = RequestMethod.GET)
	public ResponseEntity<?> getPicture(@PathVariable Long picture_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		Objects.requireNonNull(user);
		Objects.requireNonNull(picture_id);
		Picture picture = pictureService.getPicture(picture_id);
		Objects.requireNonNull(picture);
		try{
			if (!(picture.getUser() == user || picture.getUser_shared().contains(user))){
				throw new Exception("Unautorized to see this album");
			}
			if (picture.getUser() != user){
				picture.setMyPicture(false);
			}
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(picture);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/picture/{picture_id}/tag", method = RequestMethod.PUT)
	public ResponseEntity<?> addTag(@RequestBody Map<String,Object> body, @PathVariable Long picture_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final String tag = body.get("tag").toString();
		final Picture picture = pictureService.getPicture(picture_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		Objects.requireNonNull(tag, "Tag shouldn't be empty");
		Objects.requireNonNull(picture);
		Objects.requireNonNull(user);

		try{
			isAllowed(user, picture);
			picture.addTag(tag);
			pictureService.save(picture);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/picture/{picture_id}/tag", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteTag(@RequestBody Map<String,Object> body, @PathVariable Long picture_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final String tag = body.get("tag").toString();
		final Picture picture = pictureService.getPicture(picture_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		Objects.requireNonNull(tag, "Tag shouldn't be empty");
		Objects.requireNonNull(picture);
		Objects.requireNonNull(user);

		try{
			isAllowed(user, picture);
			picture.deleteTag(tag);
			pictureService.save(picture);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/picture/{picture_id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deletePicture(@PathVariable Long picture_id) throws Exception {
		try{
			Picture picture = pictureService.getPicture(picture_id);
			pictureService.delete(picture);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			log.debug("ERROR: {} -> {}", e.getMessage(), e.getCause());
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/pictures", method = RequestMethod.GET)
	public ResponseEntity<?> getPictures( @RequestHeader(name="Authorization") String token) throws Exception {
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
        Objects.requireNonNull(user);
		try{
			List<Picture> pictures = pictureService.getPicturesByUserid(user.getId());
			for (Picture picture : pictures){
				if (picture.getUser() != user){
					picture.setMyPicture(false);
				}
			}
			return new ResponseEntity<>(pictures, HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/picture/share/{picture_id}", method = RequestMethod.PUT)
	public ResponseEntity<?> sharePicture(@RequestBody Map<String,Object> body, @PathVariable Long picture_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final String name = body.get("name").toString();
		final Picture picture = pictureService.getPicture(picture_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		final User userToShare = this.userService.getUserByUsername(name);
		Objects.requireNonNull(name, "Name shouldn't be empty");
		Objects.requireNonNull(picture);
		Objects.requireNonNull(user);
		Objects.requireNonNull(userToShare, "Wrong Username to share");

		try{
			isAllowed(user, picture);
			picture.share(userToShare);
			pictureService.save(picture);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/picture/share/{picture_id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> unshareAlbum(@RequestBody Map<String,Object> body, @PathVariable Long picture_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final String name = body.get("name").toString();
		final Picture picture = pictureService.getPicture(picture_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		final User userToShare = this.userService.getUserByUsername(name);
		Objects.requireNonNull(name, "Name shouldn't be empty");
		Objects.requireNonNull(picture);
		Objects.requireNonNull(user);
		Objects.requireNonNull(userToShare, "Wrong Username to share");

		try{
			isAllowed(user, picture);
			picture.unshare(userToShare);
			pictureService.save(picture);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}
	public void isAllowed(User user, Picture picture) throws Exception{
		if (picture.getUser() != user){
			throw new Exception("Unauthorized for this picture");
		}
	}
}
