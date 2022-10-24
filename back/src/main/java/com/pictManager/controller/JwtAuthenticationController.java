package com.pictManager.controller;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.MediaType;

import com.pictManager.config.JwtTokenUtil;
import com.pictManager.model.JwtResponse;
import com.pictManager.model.User;
import com.pictManager.service.UserService;

@RestController
@CrossOrigin
public class JwtAuthenticationController {

	private final Logger log = LoggerFactory.getLogger(JwtAuthenticationController.class);

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody Map<String,Object> body) throws Exception {
		String login = body.get("login").toString();
		String password = body.get("password").toString();
		Objects.requireNonNull(login);
		Objects.requireNonNull(password);

		try {
			User findUser = authenticate(login, password);
			return ResponseEntity.ok(new JwtResponse(jwtTokenUtil.generateToken(findUser)));
		}catch (Exception e){
			if (e.getMessage() == null) {
				return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \"Login incorect\"}");
			}else{
				return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
			}
		}
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<?> register(@RequestBody User user) throws Exception {
		log.debug("user = {}", user);
		try {
			register(user.getUsername(),  user.getEmail(), user.getPassword());
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{ \"message\": \"REGISTERED\"}");
		}catch (Exception e){
			return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body("{ \"message\" : \""+e.getMessage()+"\"}");
		}
	}

	@RequestMapping(value = "/islogged", method = RequestMethod.GET)
	public ResponseEntity<?> isLogged(@RequestHeader(name="Authorization") String token) throws Exception {
		final User user = this.userService.getUserByUsername(this.jwtTokenUtil.getUsernameFromToken(token.substring(7)));
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(user);
	}

	private User authenticate(String login, String password) throws Exception {
		try {
			User user = userService.getUserByEmail(login);
			if (user == null) {
				user = userService.getUserByUsername(login);
			}
			Objects.requireNonNull(user);
			if (new BCryptPasswordEncoder().matches(password, user.getPassword())) {
				return user;
			}else{
				throw new Exception("WRONG PASSWORD");
			}
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}

	private void register(String username, String email, String password) throws Exception {
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);
		Objects.requireNonNull(email);

		try {
			password = encodePassword(password);
			userService.register(new User(username, email, password));
		} catch (Exception e) {
			log.debug("regiser exception {} -> {}", e.getCause(), e.getMessage());
			throw new Exception("USERNAME OR EMAIL ALREADY EXIST", e);
		}
	}

	private String encodePassword(String password){
		return new BCryptPasswordEncoder().encode(password);
	}
}
