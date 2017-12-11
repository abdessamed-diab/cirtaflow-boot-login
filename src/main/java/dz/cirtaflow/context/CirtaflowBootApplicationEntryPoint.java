package dz.cirtaflow.context;

import dz.cirtaflow.security.CirtaflowSecurityConfigurer;
import lombok.Data;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@EnableAutoConfiguration(exclude = {DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"dz.cirtaflow.web", "dz.cirtaflow.security"})
@Configuration
@ConfigurationProperties(prefix = "cirtaflow.datasource")
@Data
public class CirtaflowBootApplicationEntryPoint implements WebMvcConfigurer, ApplicationContextAware, Serializable{
    private static Logger LOG= LogManager.getLogger(CirtaflowBootApplicationEntryPoint.class);

    protected ApplicationContext applicationContext;

    @NotNull private String driverClassName;
    @NotNull private String jdbcUrl;
    @NotNull private String username;
    @NotNull private String password;

    /**
     * default constructor.
     */
    public CirtaflowBootApplicationEntryPoint() {
        LOG.debug("*******************************************************");
        LOG.debug("\t\t     CIRTAFLOW ENTRY POINT"                     );
        LOG.debug("*******************************************************");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LOG.debug("set app context using application context aware interface.");
        this.applicationContext= applicationContext;
    }

    /**
     * convenient way to map viewControllers path to view names
     * @param registry {@link ViewControllerRegistry}
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/index").setViewName("/user/index");
    }

//    *****************************************************************************************************************

    /**
     * we will delegate the creation and initialization of the DataSource to {@link DataSourceBuilder}
     * @return {@link DataSource} object
     */
    @Bean(name = "hikariDataSource")
    @Profile(value = {"dev"})
    public DataSource dataSource() {
        LOG.info("**************************************************");
        LOG.info("create DataSource using spring data source builder.");
        LOG.info("**************************************************");
        return DataSourceBuilder.create().driverClassName(driverClassName)
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .build();
    }

    @Bean
    @Profile(value = {"dev"})
    public DataSourceTransactionManager jdbcTransactionManager() {
        LOG.info("**************************************************");
        LOG.info("\t\t  create JdbcTransactionManager   .");
        LOG.info("**************************************************");
        DataSourceTransactionManager dataSourceTransactionManager= new DataSourceTransactionManager(dataSource());
        dataSourceTransactionManager.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
        return dataSourceTransactionManager;
    }

    /**
     * initialize process engine configuration object.
     * this object is necessary for creating an instance of process engine @{@link ProcessEngine}
     * @return @{@link ProcessEngineConfiguration}
     */
    @Bean
    @Profile(value = {"dev"})
    public ProcessEngineConfiguration processEngineConfiguration() {
        LOG.debug("init process engine configuration.");
        SpringProcessEngineConfiguration processEngineConfiguration= new SpringProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(dataSource())
                .setAsyncExecutorActivate(true)
                .setDatabaseSchemaUpdate("true");

        processEngineConfiguration.setTransactionManager(jdbcTransactionManager());
        return processEngineConfiguration;
    }

    /**
     * entry point for activiti process engine. @see {@link ProcessEngine}
     * @return bean of type {@link ProcessEngine}
     * @throws Exception thrown if database unreachable.
     */
    @Bean
    @Profile(value = {"dev"})
    public ProcessEngine processEngine() throws Exception {
        ProcessEngineFactoryBean processEngineFactory= new ProcessEngineFactoryBean();
        processEngineFactory.setApplicationContext(this.applicationContext);
        processEngineFactory.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration());

        return processEngineFactory.getObject();
    }

//    *****************************************************************************************************************

    @Configuration
    @Order(Ordered.LOWEST_PRECEDENCE)
    @ConfigurationProperties(prefix = "cirtaflow.flyway")
    @Data
    @Profile(value = {"dev"})
    protected static class FlywayConfigurer implements FlywayMigrationStrategy {
        private String checkTable="";

        public FlywayConfigurer() {
            LOG.debug("flyway configurer, default Constructor.");
            LOG.info("flyway is configured to check against existing of table: "+checkTable);
        }

        @Override
        public void migrate(Flyway flyway) {
            LOG.debug("**************************************************");
            LOG.debug("\t\t\t\t  override migration strategy."      );
            LOG.debug("**************************************************");
            try {

                DatabaseMetaData metadata= flyway.getDataSource().getConnection().getMetaData();
                LOG.info("flyway is using a primary database under the URL: "+metadata.getURL());
//               catalog,  schemaPattern,  tableNamePattern, types String []
                ResultSet resultSet= metadata.getTables(
                        flyway.getDataSource().getConnection().getCatalog(),
                        null,
                        this.checkTable,
                        new String[] {"TABLE"}
                );

                if(!resultSet.next())
                    flyway.migrate();

            } catch (SQLException e) {
                LOG.error(e);
            }
        }
    }

//    *****************************************************************************************************************
    @Profile(value = {"dev"})
    @Configuration
    @Data
    @EnableJpaRepositories(
            basePackages                = {"dz.cirtaflow.repositories"},
            considerNestedRepositories  = false,
            entityManagerFactoryRef     = "entityManagerFactoryBean",
            transactionManagerRef       = "jpaTransactionManager",
            queryLookupStrategy         = QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND)
    protected static class JpaConfigurer implements Serializable{
        private DataSource dataSource;
        private String packageToScan= "dz.cirtaflow.models";

        @Autowired
        public JpaConfigurer(@NotNull DataSource hikariDataSource) {
            this.dataSource= hikariDataSource;
        }


        @Bean
        public AbstractEntityManagerFactoryBean entityManagerFactoryBean() {
            LocalContainerEntityManagerFactoryBean entityManagerFactoryBean= new LocalContainerEntityManagerFactoryBean();
            entityManagerFactoryBean.setDataSource(dataSource);
            entityManagerFactoryBean.setPackagesToScan(this.packageToScan);
            entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            entityManagerFactoryBean.getJpaPropertyMap().put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
            entityManagerFactoryBean.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "none");
            entityManagerFactoryBean.getJpaPropertyMap().put("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
            entityManagerFactoryBean.getJpaPropertyMap().put("hibernate.show_sql", "false");
            entityManagerFactoryBean.getJpaPropertyMap().put("hibernate.format_sql", "true");
            entityManagerFactoryBean.getJpaPropertyMap().put("spring.jpa.hibernate.ddl-auto", "none");

            return entityManagerFactoryBean;
        }

        @Bean
        public JpaTransactionManager jpaTransactionManager () {
            JpaTransactionManager jpaTransactionManager= new JpaTransactionManager();
            jpaTransactionManager.setDataSource(dataSource);
            jpaTransactionManager.setEntityManagerFactory(entityManagerFactoryBean().getObject());

            return jpaTransactionManager;
        }
    }


    /**
     * setup security for the web app.
     * @return {@link org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter}
     */
    @Bean
    public CirtaflowSecurityConfigurer cirtaflowSecurityConfigurer() {
        return new CirtaflowSecurityConfigurer();
    }


}
