package dz.cirtaflow.web;

import dz.cirtaflow.models.act.CfActIdUserAuthority;
import dz.cirtaflow.models.cirtaflow.Friend;
import dz.cirtaflow.models.cirtaflow.Profile;
import dz.cirtaflow.repositories.bpmnJPARepository.AuthorityRepository;
import dz.cirtaflow.repositories.bpmnJPARepository.FriendProfileRepository;
import dz.cirtaflow.repositories.bpmnJPARepository.ProfileRepository;
import dz.cirtaflow.repositories.bpmnRepository.ActivitiIdentityServiceRepository;
import dz.cirtaflow.repositories.facebookRepository.FacebookRepository;
import dz.cirtaflow.security.CirtaflowFacebookAuthentication;
import org.activiti.engine.identity.Picture;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.UserOperations;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.*;

/**
 * class for handling requests mapped to "/".
 * @Author DIAB-ABDESSAMED
 */
@Controller(value = "loginController")
public class LoginController implements Serializable, InitializingBean{
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
    private ProfileRepository profileRepository;

    @Autowired
    private FriendProfileRepository friendProfileRepository;

    /**
     * read from properties file and set the value of welcome page.
     * cirtaflow project has it s own welcome page acts like the stater entry point for the end user.
     */
    @Value("${cirtaflow.welcome-page}")
    private String welcomePage;

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.debug("init LoginHelper static class ");
        LoginHelper.setActivitiIdentityServiceRepository(this.activitiIdentityServiceRepository);
        LoginHelper.setAuthorityRepository(this.authorityRepository);
        LoginHelper.setProfileRepository(this.profileRepository);
        LoginHelper.setFriendProfileRepository(this.friendProfileRepository);
    }

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
    public String authenticateFacebookUser(@RequestParam(name = "code") String code, HttpSession httpSession) {


        FacebookTemplate facebookTemplate = this.facebookRepository.getFacebookTemplate(code, redirectUrl);
        User facebookUserProfile = facebookTemplate.userOperations().getUserProfile();
        Assert.notNull(facebookUserProfile, "user operations must not be null, see access token value...");

        if(StringUtils.isBlank(facebookUserProfile.getEmail())){
            LOG.info("we can't extract email address for user: "+facebookUserProfile.getFirstName());
            return "/login?error=4"; // cirtaflow app unable to read or extract your email address.
        }

        Optional<org.activiti.engine.identity.User> optionalActivitiUser= this.activitiIdentityServiceRepository.findByEmail(facebookUserProfile.getEmail());
        org.activiti.engine.identity.User activitiUser;
        String authority;

        if(!optionalActivitiUser.isPresent()) {
            activitiUser = LoginHelper.CONVERT_AND_SAVE_ACTIVITI_USER(facebookUserProfile);
            authority    = LoginHelper.ASSOCIATE_AUTHORITY_FOR_GIVEN_ACTIVITI_USER(facebookUserProfile.getEmail()).getAuthority();
        }else {
            activitiUser = optionalActivitiUser.get();
            authority    =  this.authorityRepository.findByEmail(activitiUser.getEmail()).get().getAuthority();
        }

        LoginHelper.SET_ACTIVITI_USER_PICTURE(activitiUser , facebookTemplate.userOperations().getUserProfileImage());
        LoginHelper.ADD_PROFILE_USING_FACEBOOK_PROFILE(activitiUser, facebookUserProfile, facebookTemplate.friendOperations().getFriendProfiles()  );
        LoginHelper.DO_AUTHENTICATE_NEW_USER(activitiUser , authority);

        httpSession.setAttribute("userOperations", facebookTemplate.userOperations());
        return "redirect:/index";
    }

    @Secured("IS_AUTHENTICATED_FULLY")
    @RequestMapping("/profile")
    public ModelAndView goToProfile(HttpSession httpSession) {
        Assert.notNull(httpSession.getAttribute("userOperations"), "there is no code in current session");
        LOG.debug("go to profile.");
        UserOperations userOperations= (UserOperations) httpSession.getAttribute("userOperations");
        ModelAndView modelAndView= new ModelAndView("user/profile");
        modelAndView.addObject("email", userOperations.getUserProfile().getEmail());

        if(userOperations.getUserProfile().getCover() != null)
            modelAndView.addObject("coverPhoto", userOperations.getUserProfile().getCover().getSource());

        String base64= Base64.getEncoder().encodeToString(userOperations.getUserProfileImage());
        modelAndView.addObject("profilePicture", base64);
        return modelAndView;
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


    private final static class LoginHelper {
        private static ActivitiIdentityServiceRepository ACTIVITI_IDENTITY_SERVICE_REPOSITORY;
        private static AuthorityRepository AUTHORITY_REPOSITORY;
        private static ProfileRepository PROFILE_REPOSITORY;
        private static FriendProfileRepository FRIEND_PROFILE_REPOSITORY;
        private LoginHelper() {
        }

         static org.activiti.engine.identity.User CONVERT_TO_ACTIVITI_USER(User facebookUserProfile) {
            org.activiti.engine.identity.User activitiUser = ACTIVITI_IDENTITY_SERVICE_REPOSITORY.createNewUser(facebookUserProfile.getId());
            Assert.notNull(activitiUser, "activiti identity service can not create new user. perhaps given user already exists in the system");
            activitiUser.setPassword(facebookUserProfile.getFirstName()+"."+facebookUserProfile.getLastName()); // this will be changed after calling encryption method.
            activitiUser.setFirstName(facebookUserProfile.getFirstName());
            activitiUser.setLastName(facebookUserProfile.getLastName());
            activitiUser.setEmail(facebookUserProfile.getEmail());
            activitiUser.setPassword(
                    ENCRYPT_PASSWORD(activitiUser)
            );

            LOG.debug("**************************************************");
            LOG.debug("\t\t add new activiti user to database.");
            LOG.debug("**************************************************");
            return activitiUser;
        }

         static org.activiti.engine.identity.User CONVERT_AND_SAVE_ACTIVITI_USER(@NonNull User facebookUserProfile) {
            org.activiti.engine.identity.User activitiUser = CONVERT_TO_ACTIVITI_USER(facebookUserProfile);
            activitiUser = ACTIVITI_IDENTITY_SERVICE_REPOSITORY.saveUser(activitiUser);
            return activitiUser;
        }

        static boolean ASSOCIATE_AUTHORITY_FOR_GIVEN_ACTIVITI_USER(@NonNull  org.activiti.engine.identity.User activitiUser) {
            Assert.notNull(activitiUser.getEmail(), "email address should not be null.");
            return AUTHORITY_REPOSITORY.save(
                    new CfActIdUserAuthority("USER", activitiUser.getEmail()    )
            ).getId() != null;
        }

        static CfActIdUserAuthority ASSOCIATE_AUTHORITY_FOR_GIVEN_ACTIVITI_USER(@NonNull String email) {
            return AUTHORITY_REPOSITORY.save(
                    new CfActIdUserAuthority("USER", email    )
            );
        }

        static void DO_AUTHENTICATE_NEW_USER(org.activiti.engine.identity.User activitiUser, String authority) {
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
        }

        static void SET_ACTIVITI_USER_PICTURE(org.activiti.engine.identity.User activitiUser, byte[] imageAsByteArray) {
            Picture activitiUserPicture = new Picture( imageAsByteArray , "jpg");
            ACTIVITI_IDENTITY_SERVICE_REPOSITORY.setUserPicture(activitiUser.getId() , activitiUserPicture);
        }

//        this will update every relation
        static void ADD_PROFILE_USING_FACEBOOK_PROFILE(org.activiti.engine.identity.User activitiUser, User facebookUserProfile, List<User> friendList) {

            final Profile profile = new Profile(activitiUser.getId() , facebookUserProfile.getName(),
                    facebookUserProfile.getFirstName() , facebookUserProfile.getLastName(), facebookUserProfile.getGender() , facebookUserProfile.getLocale());

            Picture activitiUserPicture = ACTIVITI_IDENTITY_SERVICE_REPOSITORY.getUserPicture(activitiUser.getId());
            if(activitiUserPicture != null )
                profile.setProfilePicture(activitiUserPicture.getBytes());
            PROFILE_REPOSITORY.save(profile);

            friendList.stream().forEach(user -> {
                Profile friendProfile = null;
                Friend friend = new Friend(profile ,
                        friendProfile = new Profile(user.getId(), user.getName(), user.getFirstName(), user.getLastName(), user.getGender(), user.getLocale())
                );

                // add last uploaded picture.
                Picture picture = ACTIVITI_IDENTITY_SERVICE_REPOSITORY.getUserPicture(user.getId());
                if(picture != null)
                    friendProfile.setProfilePicture(picture.getBytes());
                PROFILE_REPOSITORY.save(friendProfile); // just an update

                friend.setStatus(user.getAbout());
                if(!FRIEND_PROFILE_REPOSITORY.findByProfileAndFriendProfile(profile , friendProfile).isPresent()) {
                    FRIEND_PROFILE_REPOSITORY.save(friend);
                }

            });

        }

        public static void setActivitiIdentityServiceRepository(ActivitiIdentityServiceRepository activitiIdentityServiceRepository) {
            ACTIVITI_IDENTITY_SERVICE_REPOSITORY = activitiIdentityServiceRepository;
        }

        public static void setAuthorityRepository(AuthorityRepository authorityRepository) {
            AUTHORITY_REPOSITORY = authorityRepository;
        }

        public static void setProfileRepository(ProfileRepository profileRepository) {
            PROFILE_REPOSITORY = profileRepository;
        }

        public static void setFriendProfileRepository(FriendProfileRepository friendProfileRepository) {
            FRIEND_PROFILE_REPOSITORY = friendProfileRepository;
        }
    }


}
