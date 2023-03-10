package nyang.cat.Users.service;

import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import nyang.cat.Users.entity.Users;
import nyang.cat.Users.repository.UsersRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return usersRepository.findByEmail(username)
                    .map(this::createUserDetails)
                    .orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));
        }

    private UserDetails createUserDetails(Users users) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(users.getAuthority().toString());
            /*  User 클래스로 사용자 정의(이름,암호,권한)  */
            return new User(
                    String.valueOf(users.getUserSeq()),
                    users.getPassword(),
                    Collections.singleton(grantedAuthority)
            );
    }
}