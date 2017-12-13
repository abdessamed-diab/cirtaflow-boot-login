package dz.cirtaflow.web;

import dz.cirtaflow.models.act.CfActIdUserAuthority;
import dz.cirtaflow.repositories.bpmnJPARepository.AuthorityRepository;
import dz.cirtaflow.repositories.bpmnRepository.ActivitiIdentityServiceRepository;
import dz.cirtaflow.repositories.facebookRepository.FacebookRepository;
import dz.cirtaflow.security.CirtaflowFacebookAuthentication;
import dz.cirtaflow.security.CirtaflowSecurityConfigurer;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.AuthenticationManagerConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.UserOperations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

/**
 * class for handling requests mapped to "/".
 * @Author DIAB-ABDESSAMED
 */
@Controller(value = "loginController")
public class LoginController implements Serializable{
    private static final Logger LOG= LogManager.getLogger(LoginController.class);

    @Value("${cirtaflow.facebook.redirect-url}")
    private String redirectUrl;

    @Autowired
    private FacebookRepository facebookRepository;

    @Autowired
    private ActivitiIdentityServiceRepository activitiIdentityServiceRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private CirtaflowSecurityConfigurer cirtaflowSecurityConfigurer;

    /**
     * read from properties file and set the value of welcome page.
     * cirtaflow project has it s own welcome page acts like the stater entry point for the end user.
     */
    @Value("${cirtaflow.welcome-page}")
    private String welcomePage;

    /**
     * default constructor
     */
    public LoginController() {
        LOG.debug("default constructor.");
    }

    /**
     * since we have declared thymeleaf as a boot starter dependency see maven POM file,
     * spring will configure thymeleaf view resolver automatically instead of using
     * {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping}.
     * @return view name, thymeleaf default configuration prefix is templates/ ,
     * witch mean that @{@link org.thymeleaf.spring5.view.ThymeleafViewResolver }
     * will look to that directory to resolve full view name
     */
    @Secured("IS_AUTHENTICATED_ANONYMOUSLY")
    @GetMapping(value = {"/", "/login"})
    public String entryPoint() {
        LOG.debug("spring web mvc entry point.");
        Assert.notNull(SecurityContextHolder.getContext().getAuthentication(), "authentication should not be null.");

        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(authentication.isAuthenticated()   ){
            if(authentication.getClass().isAssignableFrom(AnonymousAuthenticationToken.class)){
                return "login";
            }else {
                return this.welcomePage;
            }
        }

        return this.welcomePage;

    }

    /**
     * go to index using http post method. this method handel form submit.
     * @return the view name {@link org.springframework.web.servlet.View} of the requested resource.
     */
    @Secured("IS_AUTHENTICATED_FULLY")
    @PreAuthorize(value = "hasRole('USER')")
    @PostMapping("/index")
    public String goToIndex() {
        LOG.debug("go to index.");
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(authentication, "authentication should not be null.");

//        we can omit the next statement since the presence of @Link PreAuthorize.
        if(authentication.isAuthenticated() ) {
            long result = authentication.getAuthorities().stream().filter((GrantedAuthority authority) -> {
                if(authority.getAuthority().toLowerCase().contains("user"))
                    return true;
                return false;
            }).count();
            if(result > 0)
                return this.welcomePage;
        }

        return "login";
    }

    /**
     * delegating the requested resource to Get method {@link org.springframework.http.HttpMethod}
     * @return the view name {@link org.springframework.web.servlet.View} of the requested resource.
     */
    @Secured("IS_AUTHENTICATED_FULLY")
    @PreAuthorize(value = "hasRole('USER')")
    @GetMapping("/index")
    public String goToIndexGetMapping() {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(authentication, "authentication must not be null.");
        LOG.info("user with username: "+((UserDetails)authentication.getPrincipal()).getUsername()+", is fully authenticated.");

        // welcome-page is a profile property declared in maven pom file,
        // and assigned to one of spring properties file configurer.
        return this.welcomePage;
    }

    /**
     * go to facebook login page.
     * @param scope witch user's info needs to get from facebook account.
     * @return redirect and go to facebook authentication page, if the user did not have facebook session.
     */
    @GetMapping("/facebook")
    @Secured("IS_AUTHENTICATED_ANONYMOUSLY")
    public String goToFacebookLoginPage(@RequestParam(name = "scope") String scope) {
        LOG.info("go to facebook login page.");
        Assert.notNull(this.redirectUrl, "redirect url must not be null.");
        OAuth2Parameters params= FacebookRepository.CREATE_PARAMS(scope, "login_state", this.redirectUrl);
        params.add("auth_type", "rerequest");
        return "redirect:"+this.facebookRepository.getAuthenticatedUrl(params);
    }

    /**
     * authenticate to cirtaflow app using facebook access token to get user operation
     * {@link org.springframework.social.facebook.api.UserOperations}
     * @param code is an access token.
     * @return index page if user successfully authenticated to his facebook account.
     */
    @GetMapping("/facebook/index")
    @Secured("IS_AUTHENTICATED_ANONYMOUSLY")
    public String authenticateFacebookUser(@RequestParam(name = "code") String code) {
        LOG.debug("**************************************************");
        LOG.debug("\t user is authenticated by facebook account."     );
        LOG.debug("**************************************************");

        User facebookUserProfile = this.facebookRepository.getUserOperations(code, redirectUrl).getUserProfile();
        Assert.notNull(facebookUserProfile, "user operations must not be null, see access token value...");

        if(StringUtils.isBlank(facebookUserProfile.getEmail())){
            LOG.info("we can't extract email address for user: "+facebookUserProfile.getFirstName());
            return "login?error=4"; // cirtaflow app unable to read or extract your email address.
        }

        Optional<org.activiti.engine.identity.User> optionalActivitiUser= this.activitiIdentityServiceRepository.findByEmail(facebookUserProfile.getEmail());
        org.activiti.engine.identity.User activitiUser;
        if(!optionalActivitiUser.isPresent()) {
            activitiUser = activitiIdentityServiceRepository.createNewUser(facebookUserProfile.getFirstName()+"."+facebookUserProfile.getLastName());
            activitiUser.setPassword(activitiUser.getId()); // this will be changed after calling encryption method.
            activitiUser.setFirstName(facebookUserProfile.getFirstName());
            activitiUser.setLastName(facebookUserProfile.getLastName());
            activitiUser.setEmail(facebookUserProfile.getEmail());
            activitiUser.setPassword(
                    ENCRYPT_PASSWORD(activitiUser)
            );
            activitiUser = activitiIdentityServiceRepository.saveUser(activitiUser);
            this.authorityRepository.save(new CfActIdUserAuthority("USER", activitiUser.getEmail()));
            LOG.debug("**************************************************");
            LOG.debug("\t\t add new activiti user to database.");
            LOG.debug("**************************************************");
        }else
            activitiUser= optionalActivitiUser.get();

        String authority = this.authorityRepository.findByEmail(activitiUser.getEmail()).get().getAuthority();
        List<GrantedAuthority> authorities= new ArrayList<>();
        Arrays.asList(authority.split(",")).forEach(auth -> {
            authorities.add(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return auth;
                }
            });
        });
        CirtaflowFacebookAuthentication authentication= new CirtaflowFacebookAuthentication(
                authorities,
                org.springframework.security.core.userdetails.User.withUsername(activitiUser.getId())
                        .password(activitiUser.getPassword())
                        .authorities(authorities)
                        .build()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return "redirect:/index";
    }


    private static final String ENCRYPT_PASSWORD (org.activiti.engine.identity.User user) {
        return org.springframework.security.core.userdetails.User.withDefaultPasswordEncoder()
                .password(user.getPassword())
                .username(user.getId())
                .roles("USER")
                .accountLocked(false)
                .disabled(false)
                .credentialsExpired(false)
                .build()
                .getPassword();
    }


}
