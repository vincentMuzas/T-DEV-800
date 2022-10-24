package com.pictManager.service;

import com.pictManager.model.User;
import com.pictManager.repository.UserRepository;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;


	Logger log = LoggerFactory.getLogger(UserService.class);

	UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public void register(User user) {
		this.userRepository.save(user);
	}

	public User getUserByEmail(String email){
		return this.userRepository.findByEmail(email);
	}

	public User getUserByUsername(String username){
		return this.userRepository.findByUsername(username);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = this.getUserByUsername(username);
		if (user == null)
			throw new UsernameNotFoundException("User not found with username: " + username);
		UserDetails ret = new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
		return ret;
	}
}
