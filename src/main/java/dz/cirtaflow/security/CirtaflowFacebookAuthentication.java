package dz.cirtaflow.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CirtaflowFacebookAuthentication extends AbstractAuthenticationToken{
    private static final Logger LOG= LogManager.getLogger(CirtaflowFacebookAuthentication.class);
    private UserDetails principal;


    public CirtaflowFacebookAuthentication(@NonNull  Collection<GrantedAuthority> authorities, @NonNull UserDetails principal) {
        super(authorities);
        this.principal= principal;
        this.setAuthenticated(true);
        LOG.debug("default constructor.");
    }

    @Override
    public Object getCredentials() {
        return this.getAuthorities();
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }


}
