package kr.giljabi.api.auth;


import kr.giljabi.api.entity.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserPrincipal implements UserDetails {
	
	private static final long serialVersionUID = 1L;
    private String userId;
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String userId,
                         String password) {
        this.userId = userId;
        this.password = password;
    }
 
    public static UserPrincipal build(UserInfo user) {
        return new UserPrincipal(
                user.getUserid(),
				user.getPassword()
        );
    }

	@Override
    public String getUsername() {
        return userId;
    }

    @Override
    public String getPassword() {
        return password;
    }
 
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
 
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
 
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
 
    @Override
    public boolean isEnabled() {
        return true;
    }
}