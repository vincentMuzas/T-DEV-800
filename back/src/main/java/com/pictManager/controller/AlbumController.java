package com.pictManager.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import org.springframework.web.bind.annotation.RestController;

import com.pictManager.config.JwtTokenUtil;
import com.pictManager.model.Album;
import com.pictManager.model.Picture;
import com.pictManager.model.User;
import com.pictManager.service.AlbumService;
import com.pictManager.service.PictureService;
import com.pictManager.service.UserService;


@RestController
@CrossOrigin
public class AlbumController {

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserService userService;

	@Autowired
	private AlbumService albumService;

	@Autowired
	private PictureService pictureService;

    @RequestMapping(value = "/album", method = RequestMethod.POST)
	public ResponseEntity<Album> createAlbum(@RequestBody Map<String,Object> body, @RequestHeader(name="Authorization") String token) throws Exception {
        final String name = body.get("name").toString();
        final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
        Objects.requireNonNull(name);
		Objects.requireNonNull(user);

        final Album album = albumService.save(new Album(user, name));

		return new ResponseEntity<Album>(album, HttpStatus.OK);
	}

	@RequestMapping(value = "/album/{album_id}", method = RequestMethod.GET)
	public ResponseEntity<?> getAlbum(@PathVariable Long album_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		Objects.requireNonNull(user);
		Objects.requireNonNull(album_id);
		Album album = albumService.getAlbum(album_id);
		Objects.requireNonNull(album);
		try{
			if (!(album.getUser() == user || album.getUser_shared().contains(user))){
				throw new Exception("Unautorized to see this album");
			}
			if (album.getUser() != user){
				album.setMyAlbum(false);
			}
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(album);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/album/{album_id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteAlbum(@PathVariable Long album_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final Album album = albumService.getAlbum(album_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
        Objects.requireNonNull(album);
		Objects.requireNonNull(user);
		try{
			isAllowed(user, album);
			albumService.delete(album);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/album/{album_id}", method = RequestMethod.PUT)
	public ResponseEntity<?> updateAlbum(@RequestBody Map<String,Object> body, @PathVariable Long album_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final String name = body.get("name").toString();
		final Album album = albumService.getAlbum(album_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		Objects.requireNonNull(name);
		Objects.requireNonNull(album);
		try{
			isAllowed(user, album);
			album.setName(name);
			albumService.save(album);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/album/{album_id}/tag", method = RequestMethod.PUT)
	public ResponseEntity<?> addTag(@RequestBody Map<String,Object> body, @PathVariable Long album_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final String tag = body.get("tag").toString();
		final Album album = albumService.getAlbum(album_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		Objects.requireNonNull(tag, "Tag shouldn't be empty");
		Objects.requireNonNull(album);
		Objects.requireNonNull(user);

		try{
			isAllowed(user, album);
			album.addTag(tag);
			albumService.save(album);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/album/{album_id}/tag", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteTag(@RequestBody Map<String,Object> body, @PathVariable Long album_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final String tag = body.get("tag").toString();
		final Album album = albumService.getAlbum(album_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		Objects.requireNonNull(tag, "Tag shouldn't be empty");
		Objects.requireNonNull(album);
		Objects.requireNonNull(user);

		try{
			isAllowed(user, album);
			album.deleteTag(tag);
			albumService.save(album);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/albums", method = RequestMethod.GET)
	public ResponseEntity<?> getAlbum(@RequestHeader(name="Authorization") String token) throws Exception {
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
        Objects.requireNonNull(user);
		try{
			List<Album> albums = albumService.getAlbums(user.getId());
			for (Album album : albums){
				if (album.getUser() != user){
					album.setMyAlbum(false);
				}
			}
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(albums);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/album/{album_id}/{picture_id}", method = RequestMethod.PUT)
	public ResponseEntity<?> addPicture(@PathVariable Long album_id, @PathVariable Long picture_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final Album album = albumService.getAlbum(album_id);
		final Picture picture = pictureService.getPicture(picture_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		Objects.requireNonNull(album);
		Objects.requireNonNull(picture);
		Objects.requireNonNull(user);
		try{
			isAllowed(user, album);
			album.addPicture(picture);
			albumService.save(album);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/album/{album_id}/{picture_id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> removePicture(@PathVariable Long album_id, @PathVariable Long picture_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final Album album = albumService.getAlbum(album_id);
		final Picture picture = pictureService.getPicture(picture_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		Objects.requireNonNull(album);
		Objects.requireNonNull(picture);
		Objects.requireNonNull(user);
		try{
			isAllowed(user, album);
			album.removePicture(picture);
			albumService.save(album);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/album/share/{album_id}", method = RequestMethod.PUT)
	public ResponseEntity<?> shareAlbum(@RequestBody Map<String,Object> body, @PathVariable Long album_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final String name = body.get("name").toString();
		final Album album = albumService.getAlbum(album_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		final User userToShare = this.userService.getUserByUsername(name);
		Objects.requireNonNull(name, "Name shouldn't be empty");
		Objects.requireNonNull(album);
		Objects.requireNonNull(user);
		Objects.requireNonNull(userToShare, "Wrong Username to share");

		try{
			isAllowed(user, album);
			album.share(userToShare);
			albumService.save(album);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/album/share/{album_id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> unshareAlbum(@RequestBody Map<String,Object> body, @PathVariable Long album_id, @RequestHeader(name="Authorization") String token) throws Exception {
		final String name = body.get("name").toString();
		final Album album = albumService.getAlbum(album_id);
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		final User userToShare = this.userService.getUserByUsername(name);
		Objects.requireNonNull(name, "Name shouldn't be empty");
		Objects.requireNonNull(album);
		Objects.requireNonNull(user);
		Objects.requireNonNull(userToShare, "Wrong Username to share");

		try{
			isAllowed(user, album);
			album.unshare(userToShare);
			albumService.save(album);
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	public void isAllowed(User user, Album album) throws Exception{
		if (album.getUser() != user){
			throw new Exception("Unauthorized for this album");
		}
	}
}
