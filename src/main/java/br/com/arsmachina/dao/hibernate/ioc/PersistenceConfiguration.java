// Copyright 2008 Thiago H. de Paula Figueiredo
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package br.com.arsmachina.dao.hibernate.ioc;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Class that configures the persistence layer for Spring. Database info is read from a
 * <code>datasource.properties</code> file in the root of the classpath. Hibernate configuration
 * properties is read from a <code>hibernate.cfg.xml</code> file, also in the root of the
 * classpath.
 * 
 * @author Thiago H. de Paula Figueiredo
 */
@Configuration(defaultLazy = Lazy.TRUE)
@ResourceBundles( { "classpath:/datasource" })
public class PersistenceConfiguration {

	/**
	 * Location, in the classpath, of the Hibernate configuration file.
	 */
	public static final String HIBERNATE_CONFIGURATION_FILE = "/hibernate.cfg.xml";

	/**
	 * Property used to define the database url.
	 */
	final public static String DATABASE_URL = "database.url";

	/**
	 * Property used to define the database JDBC driver class name.
	 */
	final public static String JDBC_DRIVER = "jdbc.driver";

	/**
	 * Property used to define the database user name.
	 */
	final public static String DATABASE_USERNAME = "database.username";

	/**
	 * Property used to define the database user password.
	 */
	final public static String DATABASE_PASSWORD = "database.password";

	/**
	 * Single constructor of this class.
	 */
	public PersistenceConfiguration() {
	}

	/**
	 * Creates a {@link DataSource}.
	 * 
	 * @return a {@link DataSource}.
	 */
	@Bean
	public DataSource dataSource() {

		ComboPooledDataSource dataSource = new ComboPooledDataSource();

		dataSource.setJdbcUrl(getDatabaseURL());
		dataSource.setUser(getDatabaseUsername());
		dataSource.setPassword(getDatabasePassword());

		try {
			dataSource.setDriverClass(getJDBCDriver());
		}
		catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}

		return dataSource;

	}

	/**
	 * Creates a {@link SessionFactory} that uses annotations and/or XML for mapping classes.
	 * 
	 * @return a {@link SessionFactory}.
	 */
	@Bean
	public SessionFactory sessionFactory() {

		LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
		factoryBean.setConfigurationClass(AnnotationConfiguration.class);
		factoryBean.setConfigLocation(new ClassPathResource(HIBERNATE_CONFIGURATION_FILE));

		try {
			factoryBean.afterPropertiesSet();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return (SessionFactory) factoryBean.getObject();

	}

	/**
	 * Creates the {@link PlatformTransactionManager} to be used by Spring.
	 * 
	 * @return a {@link HibernateTransactionManager}.
	 */
	@Bean
	public PlatformTransactionManager transactionManager() {
		return new HibernateTransactionManager(sessionFactory());
	}

//	@Bean(lazy = Lazy.FALSE)
//	public OpenSessionInViewInterceptor openSessionInViewInterceptor() {
//		
//		final OpenSessionInViewInterceptor interceptor = new OpenSessionInViewInterceptor();
//		interceptor.setSessionFactory(sessionFactory());
//		interceptor.afterPropertiesSet();
//		
//		return interceptor;
//		
//	}

	/**
	 * Returns the JDBC driver class name.
	 * 
	 * @return a {@link String}.
	 */
	@ExternalValue(DATABASE_URL)
	public String getDatabaseURL() {
		return "value not set";
	}

	/**
	 * Returns the JDBC driver class name.
	 * 
	 * @return a {@link String}.
	 */
	@ExternalValue(JDBC_DRIVER)
	public String getJDBCDriver() {
		return "value not set";
	}

	/**
	 * Returns the database user name.
	 * 
	 * @return a {@link String}.
	 */
	@ExternalValue(DATABASE_USERNAME)
	public String getDatabaseUsername() {
		return "value not set";
	}

	/**
	 * Returns the database user password.
	 * 
	 * @return a {@link String}.
	 */
	@ExternalValue(DATABASE_URL)
	public String getDatabasePassword() {
		return "value not set";
	}

}