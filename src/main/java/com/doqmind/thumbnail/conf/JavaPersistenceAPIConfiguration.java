package com.doqmind.thumbnail.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseConfigurer;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuration for the database({@link DataSource}) and the JPA provider.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 09-11-2023
 */
@Configuration
@EnableTransactionManagement
@EntityScan("com.doqmind.thumbnail")
@EnableJpaRepositories("com.doqmind.thumbnail")
public class JavaPersistenceAPIConfiguration {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String hibernateAuto;
    @Value("${spring.datasource.dialect}")
    private String dialect;
    @Value("${spring.jpa.hibernate.default_schema}")
    protected String defaultSchema;

    /**
     * Creates the database factory that will initialise the database resources like the datasource transaction manager etc.
     */
    @Bean
    public EmbeddedDatabaseFactory databaseFactory() {
        EmbeddedDatabaseFactory embeddedDatabaseFactory = new EmbeddedDatabaseFactory();
        EmbeddedDatabaseConfigurer embeddedDatabaseConfigurer = new EmbeddedDatabaseConfigurer() {
            @Override
            public void configureConnectionProperties(final ConnectionProperties properties, final String databaseName) {
                properties.setUrl(url);
                properties.setUsername(username);
                properties.setPassword(password);
                try {
                    //noinspection unchecked
                    properties.setDriverClass((Class<? extends java.sql.Driver>) Class.forName(driverClassName));
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void shutdown(final DataSource dataSource, final String databaseName) {
                // DO nothing
            }
        };
        embeddedDatabaseFactory.setDatabaseConfigurer(embeddedDatabaseConfigurer);
        return embeddedDatabaseFactory;
    }

    @Bean
    public DataSource dataSource() {
        return databaseFactory().getDatabase();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(Boolean.TRUE);
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.doqmind.thumbnail");
        factory.setDataSource(dataSource());
        factory.setJpaProperties(getJpaProperties());
        return factory;
    }

    /**
     * Creates a transaction manager for the platform, in this case we use(abuse?) the JPA transaction manager.
     */
    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }

    private Properties getJpaProperties() {

        Properties p = new Properties();
        p.put("hibernate.hbm2ddl.auto", hibernateAuto);
        p.put("hibernate.dialect", dialect);
        p.put("hibernate.show_sql", "true");
        p.put("hibernate.format_sql", "false");
        p.setProperty("hibernate.default_schema", defaultSchema);
        return p;
    }
}