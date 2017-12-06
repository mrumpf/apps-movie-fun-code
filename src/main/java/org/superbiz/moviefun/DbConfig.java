package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DbConfig {
    @Bean
    public DataSource albumsDataSource(
            @Value("${moviefun.datasources.albums.url}") String url,
            @Value("${moviefun.datasources.albums.username}") String username,
            @Value("${moviefun.datasources.albums.password}") String password
    ) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        config.setPoolName("ALBUMS_POOL");
        config.setConnectionTimeout(10000);
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    @Bean
    public DataSource moviesDataSource(
            @Value("${moviefun.datasources.movies.url}") String url,
            @Value("${moviefun.datasources.movies.username}") String username,
            @Value("${moviefun.datasources.movies.password}") String password
    ) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        config.setPoolName("MOVIES_POOL");
        config.setConnectionTimeout(10000);
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    @Bean
    public Properties hibernateProperties(){
        final Properties properties = new Properties();

        properties.put( "hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect" );
        properties.put( "hibernate.connection.driver_class", "com.mysql.jdbc.Driver" );
        properties.put( "hibernate.hbm2ddl.auto", "create-drop" );

        return properties;
    }
    @Bean
    public EntityManagerFactory albumsEntityManagerFactory(@Autowired DataSource moviesDataSource, @Autowired Properties hibernateProperties ){
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource( moviesDataSource );
        em.setPackagesToScan( "org.superbiz.moviefun.albums" );
        em.setJpaVendorAdapter( new HibernateJpaVendorAdapter() );
        em.setJpaProperties( hibernateProperties );
        em.setPersistenceUnitName( "albums" );
        em.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        em.afterPropertiesSet();

        return em.getObject();
    }

    @Bean
    public EntityManagerFactory moviesEntityManagerFactory(@Autowired DataSource albumsDataSource, @Autowired Properties hibernateProperties ){
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource( albumsDataSource );
        em.setPackagesToScan( "org.superbiz.moviefun.movies" );
        em.setJpaVendorAdapter( new HibernateJpaVendorAdapter() );
        em.setJpaProperties( hibernateProperties );
        em.setPersistenceUnitName( "movies" );
        em.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        em.afterPropertiesSet();

        return em.getObject();
    }

    @Bean
    public PlatformTransactionManager moviesPlatformTransactionManager(@Autowired EntityManagerFactory moviesEntityManagerFactory) {
        JpaTransactionManager ptm = new JpaTransactionManager();
        ptm.setEntityManagerFactory(moviesEntityManagerFactory);
        return ptm;
    }

    @Bean
    public PlatformTransactionManager albumsPlatformTransactionManager(@Autowired EntityManagerFactory albumsEntityManagerFactory) {
        JpaTransactionManager ptm = new JpaTransactionManager();
        ptm.setEntityManagerFactory(albumsEntityManagerFactory);
        return ptm;
    }
}
