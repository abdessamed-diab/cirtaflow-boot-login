package dz.cirtaflow.repositories.facebookRepository;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.social.facebook.api.FriendOperations;
import org.springframework.social.facebook.api.UserOperations;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

@Repository("facebookRepository")
@ConfigurationProperties(prefix = "cirtaflow.facebook")
@Data
public class FacebookRepository implements Serializable, InitializingBean{
    private static final Logger LOG= LogManager.getLogger(FacebookRepository.class);
    private String appSecret;
    private String appId;
    private String appNamespace;

    private FacebookConnectionFactory facebookConnectionFactory;

    /**
     * you can't inherit from this class.
     */
    public FacebookRepository() {}

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.debug("**************************************************");
        LOG.debug("\t\t init facebook connection factory."            );
        LOG.debug("**************************************************");
        this.facebookConnectionFactory= new FacebookConnectionFactory(this.appId, this.appSecret, this.appNamespace);
    }

    /**
     * build the authenticated url using {@link OAuth2Parameters} to generate the wished url.
     * @param params {@link OAuth2Parameters}
     * @return String value holds the authenticated url.
     */
    public String getAuthenticatedUrl(OAuth2Parameters params) {
        LOG.debug("generate facebook authenticated url.");
        Assert.notNull(params.getRedirectUri(), "redirect uri must not be null to create an authenticated url.");
        String authenticatedUrl= this.facebookConnectionFactory.getOAuthOperations().
                buildAuthenticateUrl(GrantType.AUTHORIZATION_CODE, params);

        return authenticatedUrl;
    }

    public FacebookTemplate getFacebookTemplate(@NonNull String code, String redirectUrl) {
        AccessGrant accessGrant = this.facebookConnectionFactory.getOAuthOperations().exchangeForAccess(code, redirectUrl, null);
        return new FacebookTemplate(accessGrant.getAccessToken(), this.appNamespace);
    }

    public UserOperations getUserOperations(@NonNull String code, String redirectUrl) {
        AccessGrant accessGrant = this.facebookConnectionFactory.getOAuthOperations().exchangeForAccess(code, redirectUrl, null);
        return new FacebookTemplate(accessGrant.getAccessToken(), this.appNamespace).userOperations();
    }

    public static OAuth2Parameters CREATE_EMPTY_PARAMS() {
        return new OAuth2Parameters();
    }

    public static OAuth2Parameters CREATE_PARAMS(@NotNull String scope) {
        OAuth2Parameters params= new OAuth2Parameters();
        params.setScope(scope);
        return params;
    }

    public static OAuth2Parameters CREATE_PARAMS(@NotNull String scope, String state) {
        OAuth2Parameters params = CREATE_PARAMS(scope);
        params.setState(state);
        return params;
    }

    public static OAuth2Parameters CREATE_PARAMS(@NotNull String scope, String state, @NotNull String redirectUri) {
        OAuth2Parameters params= CREATE_PARAMS(scope, state);
        params.setRedirectUri(redirectUri);

        return params;
    }

    /**
     * utility class for facebook urls, this cams handy when url malformed.
     */
    public static class FacebookRepositoryUtils {
        public static String ADD_REDIRECT(String path) {
            try {
                URL url= new URL(path);
                return new StringBuilder("redirect").append(":").append(path).toString();
            } catch (MalformedURLException e) {
                LOG.error(e);
                return null;
            }

        }

        // remove the curly braces appear in the OAuth2authorize
        public static String NORMALIZE_URL(String authorizeUrl) {
            return authorizeUrl.replaceAll("\\{", "").replaceAll("}", "").
                    replaceAll("%7D", "");
        }

    }

}
