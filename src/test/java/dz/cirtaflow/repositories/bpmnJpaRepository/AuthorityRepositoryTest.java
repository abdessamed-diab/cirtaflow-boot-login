package dz.cirtaflow.repositories.bpmnJpaRepository;

import dz.cirtaflow.context.CirtaflowBootApplicationEntryPoint;
import dz.cirtaflow.models.act.CfActIdUserAuthority;
import dz.cirtaflow.repositories.bpmnJPARepository.AuthorityRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.Serializable;
import java.util.Optional;

import static org.junit.jupiter.api.Assumptions.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CirtaflowBootApplicationEntryPoint.class)
@TestPropertySource(locations = "classpath:/config/application-dev.properties")
public class AuthorityRepositoryTest implements Serializable{
    private static final Logger LOG= LogManager.getLogger(AuthorityRepositoryTest.class);

    private String email;
    private CfActIdUserAuthority auth;

    @Autowired
    private AuthorityRepository authorityRepository;

    public AuthorityRepositoryTest() {
    }

    @BeforeEach
    public void setup() {
        email = "abdou_gl@live.fr";
        if(!authorityRepository.existsByEmail(email)) {
            auth = new CfActIdUserAuthority("USER", "abdou_gl@live.fr");
            this.authorityRepository.save(auth);
        }else
            auth = authorityRepository.findByEmail(email).get();

    }

    @AfterEach
    public void tearDown() {
        this.authorityRepository.deleteAll();
    }

    @Test
    public void testFind() {
        Optional<CfActIdUserAuthority> authority= this.authorityRepository.findById(auth.getId());
        assumeTrue(authority.isPresent(), "assumption failed.");
        assertEquals(email, authority.get().getEmail(), "email mismatch");
    }
}
