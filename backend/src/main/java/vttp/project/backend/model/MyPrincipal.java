package vttp.project.backend.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class MyPrincipal implements UserDetails{

    private User user;

    public MyPrincipal(User user) {
        this.user = user;
    }

    public MyPrincipal(){}

     @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(new SimpleGrantedAuthority(user.getRole()));
    }

    
    @Override
    public String getPassword() {return user.getPassword();}    

    @Override
    public String getUsername(){return user.getRoleId();}

    @Override
    public boolean isAccountNonExpired(){return true;}

    @Override
    public boolean isAccountNonLocked(){return true;}

    @Override
    public boolean isCredentialsNonExpired(){return true;}

    @Override
    public boolean isEnabled(){return true;}

    
}
