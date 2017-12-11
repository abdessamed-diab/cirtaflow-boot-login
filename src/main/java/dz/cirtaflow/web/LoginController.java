package dz.cirtaflow.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Map;

/**
 * class for handling requests mapped to "/".
 * @Author DIAB-ABDESSAMED
 */
@Controller(value = "loginController")
public class LoginController implements Serializable{
    private static final Logger LOG= LogManager.getLogger(LoginController.class);

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

}
