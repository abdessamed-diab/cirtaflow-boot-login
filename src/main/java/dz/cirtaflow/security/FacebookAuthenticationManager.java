package dz.cirtaflow.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

public class FacebookAuthenticationManager implements AuthenticationProvider{

    public FacebookAuthenticationManager() {
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserDetails userDetails= (UserDetails) authentication.getPrincipal();
        if(userDetails.isEnabled() && userDetails.isAccountNonExpired() && userDetails.isAccountNonLocked() && userDetails.isCredentialsNonExpired()) {
            if(CollectionUtils.containsAny(authentication.getAuthorities() , userDetails.getAuthorities() ))
                authentication.setAuthenticated(true);
        } else
            authentication.setAuthenticated(false);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;

    }

    @Override
    public boolean supports(Class<?> aClass) {
        if(aClass.isAssignableFrom(AnonymousAuthenticationToken.class))
            return false;

        return true;
    }
}
