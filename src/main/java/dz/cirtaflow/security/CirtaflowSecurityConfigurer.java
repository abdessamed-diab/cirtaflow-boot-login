package dz.cirtaflow.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.access.channel.ChannelDecisionManager;
import org.springframework.security.web.access.channel.ChannelDecisionManagerImpl;

import javax.sql.DataSource;
import java.util.Arrays;

//prePostEnabled caused me much more problems
//lets as use secured against prePostEnabled
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = false)
@Configuration
public class CirtaflowSecurityConfigurer extends WebSecurityConfigurerAdapter{
    private static final Logger LOG = LogManager.getLogger(CirtaflowSecurityConfigurer.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment env;

    public CirtaflowSecurityConfigurer() {
        LOG.debug("**************************************************");
        LOG.debug("\t\t ENTRY POINT CIRTAFLOW SECURITY LAYER."  )      ;
        LOG.debug("**************************************************");
    }


    /**
     * configurer authorized resources using http security.
     * @param http {@link HttpSecurity} define the parameters for httpSecurity object to be used for setting the basic security authorized resources.
     * @throws Exception url malformed exception can be thrown.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        LOG.info("custom security.");

        http.authorizeRequests()
                    .antMatchers("/assets/**").permitAll()
                    .antMatchers("/facebook/**").permitAll()
                    .antMatchers("/user/**").hasRole("USER")
                    .antMatchers("/admin/**").hasRole("ADMIN")
                    .antMatchers("/login").permitAll()
                    .antMatchers("/**").permitAll()
                .and()
                    .formLogin()
                    .loginPage("/login")
                    .loginProcessingUrl("/index")
                    .defaultSuccessUrl("/index", true)
                    .usernameParameter("EMAIL_")
                    .passwordParameter("PWD_")
                    .failureUrl("/login?error=2")
                    .failureForwardUrl("/login?error=1")
                .and()
                    .exceptionHandling()
                    .accessDeniedPage("/access-denied")
                .and()
                    .csrf()
                .and()
                    .requiresChannel().anyRequest().requires(ChannelDecisionManagerImpl.ANY_CHANNEL);
    }

    /**
     * configure the authentication manager for this security layer.
     * when a user submit a form filled with his credentials, the security container {@link WebSecurityConfigurerAdapter}
     * will delegate the request to the configure method.
     * this is the default behavior for a simple authentication. like using a submit form.
     * @param auth {@link org.springframework.security.authentication.AuthenticationManager} to used when the user try to authenticate.
     * @throws Exception @see {@link java.sql.SQLException}, sql query can throw an exception.
     */
    @Override
    @Profile(value = {"dev", "cloud"})
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.jdbcAuthentication().dataSource(dataSource).
                usersByUsernameQuery("select EMAIL_, pwd_, TRUE from ACT_ID_USER where EMAIL_ =?").
                authoritiesByUsernameQuery("select EMAIL, AUTHORITY from CF_ACT_ID_USER_AUTHORITY where EMAIL=?").
                passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
    }

    /**
     * every new request {@link SecurityContextHolder} get cleared by the security context filter.
     * this bean cams handy
     * @param dataSource
     * @return JdbcUserDetailsManager
     */
    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
        jdbcUserDetailsManager.setDataSource(dataSource);
        return jdbcUserDetailsManager;
    }



}
