package uk.ac.city.slamanager.rest.api.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import uk.ac.city.slamanager.rest.api.entities.User;
import uk.ac.city.slamanager.rest.api.repositories.UsersRepository;

import java.util.List;

@RestController
public class UserRESTController {
	
	final static Logger log = Logger.getLogger(UserRESTController.class);
	
	@Autowired
	UsersRepository repository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@RequestMapping(value= "/rest/api/users", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<List<User>> getAllUsers() {
		
		List<User> users = null;
		
		try {
			users = (List<User>) repository.findAll();
			log.info("Fetched all users. Total number of users fetched is " + users.size());
			return new ResponseEntity<List<User>>(users,HttpStatus.OK);
		} catch (Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<List<User>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/rest/api/users/{id}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<User> getUserById(@PathVariable Integer id) {
		
		User user = null;
		
		try {
			user = repository.findById(id);
			
			if(user == null){
				log.info("User with id " + id + " was not found in the database");
				return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
			}
				
			else{
				log.info("Fetching " + user.toString());
				return new ResponseEntity<User>(user, HttpStatus.OK);
			}
			
		} catch (Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<User>(user, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/rest/api/users/login/{username},{password}", method = RequestMethod.GET)
	public ResponseEntity<User> login(
			@PathVariable(value = "username") String username,
			@PathVariable(value = "password") String password) {

		User user = null;

		try {

			user = repository.findByUsername(username);

			if (user != null && passwordEncoder.matches(password, user.getPassword())) {
				return new ResponseEntity<User>(user, HttpStatus.OK);
			}else{
				return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
			}

		} catch (Exception e) {
			return new ResponseEntity<User>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

    @RequestMapping(value= "/rest/api/users", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<User> createUser(@RequestBody User user) {

		User newUser = null;
		try {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			newUser = repository.save(user);
			return new ResponseEntity<User>(newUser, HttpStatus.CREATED);
		} catch (Exception e) {
			// TODO server side logging needs to be added
			return new ResponseEntity<User>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
}