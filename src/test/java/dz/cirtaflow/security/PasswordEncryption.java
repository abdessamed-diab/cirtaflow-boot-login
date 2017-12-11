package dz.cirtaflow.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.authentication.PasswordEncoderParser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordEncryption implements Serializable {
    private static final Logger LOG= LogManager.getLogger(PasswordEncryption.class);

    private UserDetails userDetails;
    private String password;
    private String encryptedPassword;

    public PasswordEncryption() {

    }

    @BeforeEach
    public void setup() {
        this.password= "mechel178";
         userDetails= User.withDefaultPasswordEncoder().username("abdessamed")
                .password(this.password)
                .roles("USER", "DAF")
                .accountLocked(false)
                .disabled(false)
                .credentialsExpired(false)
                .build();

         this.encryptedPassword = userDetails.getPassword();
    }

    @Test
    public void testEncryptPassword() {
        LOG.debug("start encrypting password: "+this.password);
        LOG.info("encrypted password:  "+this.encryptedPassword);
        assertTrue(userDetails.getPassword().startsWith("{bcrypt}"),
                "see encrypted method. result mismatch");
    }

    @Test
    public void testDecryptPassword() {
        LOG.debug("start decrypting password: {bcrypt}..."+this.encryptedPassword);
        LOG.debug("real password before encryption: "+this.password);

        assertTrue(PasswordEncoderFactories.createDelegatingPasswordEncoder()
                .matches(this.password, this.encryptedPassword), "result mismatch.");
    }
}
