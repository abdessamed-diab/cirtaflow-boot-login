package dz.cirtaflow.repositories;

import dz.cirtaflow.context.CirtaflowBootApplicationEntryPoint;
import dz.cirtaflow.repositories.facebookRepository.FacebookRepository;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CirtaflowBootApplicationEntryPoint.class)
@TestPropertySource(locations = "classpath:/config/application-dev.properties")
@Data
public class FacebookRepositoryTest implements Serializable{
    private static final Logger LOG = LogManager.getLogger(FacebookRepositoryTest.class);

    @Value("${cirtaflow.facebook.redirect-url}")
    private String redirectUrl;

    @Autowired
    private FacebookRepository facebookRepository;

    public FacebookRepositoryTest() {
    }

    @BeforeEach
    public void setup() {
        LOG.debug("before each.");
    }

    @Test
    public void testAuthenticatedUrl() {
        String url = this.facebookRepository.getAuthenticatedUrl(
                FacebookRepository.CREATE_PARAMS("email", "login_state", this.redirectUrl)
        );

        LOG.info("authenticated url: "+url);
        assertTrue(StringUtils.isNotBlank(url), "can not get facebook authenticated url");
    }

}
