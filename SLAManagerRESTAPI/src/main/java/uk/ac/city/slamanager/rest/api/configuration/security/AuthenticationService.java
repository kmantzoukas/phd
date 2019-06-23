package uk.ac.city.slamanager.rest.api.configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import uk.ac.city.slamanager.rest.api.entities.User;
import uk.ac.city.slamanager.rest.api.repositories.UsersRepository;

@Component
public class AuthenticationService implements UserDetailsService {

	@Autowired
	UsersRepository usersRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username)throws UsernameNotFoundException {

		User user = usersRepository.findByUsername(username);
		
		
		if(user != null){
			return new JPAUserDetails(user);
		}
			
		else
			throw new UsernameNotFoundException(username);
			
	}

}
