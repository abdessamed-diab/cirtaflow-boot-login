package dz.cirtaflow.web;

import dz.cirtaflow.context.CirtaflowBootApplicationEntryPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.Serializable;
import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CirtaflowBootApplicationEntryPoint.class)
@TestPropertySource(locations = "classpath:/config/application-dev.properties")
public class LoginControllerTest implements Serializable{
    private static final Logger LOG= LogManager.getLogger(LoginControllerTest.class);

    private MockMvc mockMvc;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private LoginController loginController;

    @Value("${cirtaflow.welcome-page}")
    private String welcomePage;

    public LoginControllerTest() {
        LOG.debug("default constructor.");
    }

    @BeforeEach
    public void setup() {
        this.mockMvc= MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test
    @WithAnonymousUser
    public void testEntryPoint() throws Exception {
        LOG.debug("test entry point.");
        this.mockMvc.perform(   get(new URI("/")  ).accept(MediaType.TEXT_HTML_VALUE)   )
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void testGoToIndexAnonymously() throws Exception {
        LOG.debug("test go to index page.");
        this.mockMvc.perform(get("index").accept(MediaType.TEXT_HTML_VALUE))
                .andExpect(status().isNotFound()); // 404 resource not found
    }

    @Test
    @WithMockUser(username = "abdessamed", password = "063639118", roles = {"USER", "DAF"})
    public void testGoToIndexFullyAuthenticated() throws Exception {
        LOG.debug("go to index page with fully authenticated user.");
        this.mockMvc.perform(get("/index").accept(MediaType.TEXT_HTML_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "abdessamed", password = "063639118", roles = {"USER", "DAF"})
    public void testGoToIndexFullyAuthenticatedRedirectPage() throws Exception {
        LOG.debug("go to index page fully authenticated with next redirect page must equal to : "+this.welcomePage);
        this.mockMvc.perform(get("/index").accept(MediaType.TEXT_HTML_VALUE))
                .andExpect(view().name(this.welcomePage));
    }

    @Test
    @WithAnonymousUser
    public void testGoToFacebookLoginPage() throws Exception {
        LOG.debug("test go to facebook login page.");
        this.mockMvc.perform(get("/facebook?scope=email,user_about-me").accept(MediaType.TEXT_HTML_VALUE))
                .andExpect(status().isOk());
    }

}
