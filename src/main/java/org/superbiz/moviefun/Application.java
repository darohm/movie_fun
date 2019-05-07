package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.xml.crypto.Data;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials() {
        return new DatabaseServiceCredentials(System.getenv("VCAP_SERVICES"));
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource datasource = new MysqlDataSource();
        datasource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));
        return datasource;
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource datasource = new MysqlDataSource();
        datasource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));
        return datasource;
    }

    @Bean
    public PlatformTransactionManager moviesPlatformTransactionManager(EntityManagerFactory moviesContainerEntityManagerFactoryBean){
        return new JpaTransactionManager(moviesContainerEntityManagerFactoryBean);
    }

    @Bean
    public PlatformTransactionManager albumsPlatformTransactionManager(EntityManagerFactory albumsContainerEntityManagerFactoryBean){
        return new JpaTransactionManager(albumsContainerEntityManagerFactoryBean);
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter(){
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.MYSQL);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        jpaVendorAdapter.setGenerateDdl(true);
        return jpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesContainerEntityManagerFactoryBean(DataSource moviesDataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter){
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(moviesDataSource);
        entityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        entityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.movies");
        entityManagerFactoryBean.setPersistenceUnitName("movies");
        return entityManagerFactoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsContainerEntityManagerFactoryBean(DataSource albumsDataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter){
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(albumsDataSource);
        entityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        entityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.albums");
        entityManagerFactoryBean.setPersistenceUnitName("albums");
        return entityManagerFactoryBean;
    }
}
