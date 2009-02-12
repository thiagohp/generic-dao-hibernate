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

package br.com.arsmachina.dao.hibernate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.metadata.ClassMetadata;

/**
 * Superclass of both {@link ReadableDAOImpl} and {@link WriteableDAOImpl}.
 * 
 * @author Thiago H. de Paula Figueiredo
 * @param <T> the entity class related to this DAO.
 * @param <K> the type of the field that represents the entity class' primary key.
 */
public class BaseHibernateDAO<T, K extends Serializable> {

	private final SessionFactory sessionFactory;

	private final Class<T> entityClass;

	private final ClassMetadata classMetadata;

	private final String primaryKeyPropertyName;

	/**
	 * Constructor that takes a {@link Class} and a {@link SessionFactory}.
	 * 
	 * @param clasz a {@link Class}.
	 * @param sessionFactory a {@link SessionFactory}. It cannot be null.
	 */
	public BaseHibernateDAO(SessionFactory sessionFactory) {
		this(null, sessionFactory);
	}

	/**
	 * Constructor that takes a {@link Class} and a {@link SessionFactory}.
	 * 
	 * @param clasz a {@link Class}.
	 * @param sessionFactory a {@link SessionFactory}. It cannot be null.
	 */
	public BaseHibernateDAO(Class<T> clasz, SessionFactory sessionFactory) {

		if (sessionFactory == null) {
			throw new IllegalArgumentException("Parameter sessionFactory cannot be null");
		}

		this.sessionFactory = sessionFactory;

		entityClass = clasz != null ? clasz : extractEntityClassFromHierarchy();
		classMetadata = sessionFactory.getClassMetadata(getEntityClass());

		if (getClassMetadata() == null) {
			throw new RuntimeException("Class " + getEntityClass().getName() + " is not mapped");
		}

		primaryKeyPropertyName = getClassMetadata().getIdentifierPropertyName();

		assert getEntityClass() != null;
		assert getClassMetadata() != null;
		assert getPrimaryKeyPropertyName() != null;

	}

	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<T> extractEntityClassFromHierarchy() {

		final Type genericSuperclass = getClass().getGenericSuperclass();
		final ParameterizedType parameterizedType = ((ParameterizedType) genericSuperclass);
		return (Class<T>) parameterizedType.getActualTypeArguments()[0];

	}

	/**
	 * Returns the entity class handled by this DAO.
	 * 
	 * @return a {@link Class<T>}.
	 */
	protected final Class<T> getEntityClass() {
		return entityClass;
	}

	/**
	 * Returns a {@link Session}. This implementation returns
	 * {@link SessionFactory#getCurrentSession()} and can be overriden if needed.
	 * 
	 * @return a {@link Session}.
	 */
	protected Session getSession() {
		return getSessionFactory().getCurrentSession();
	}

	/**
	 * Returns this DAO's {@link SessionFactory}.
	 * 
	 * @return a {@link SessionFactory}.
	 */
	protected final SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Returns the {@link ClassMetadata} for the corresponding entity class.
	 * 
	 * @return a {@link ClassMetadata}.
	 */
	protected final ClassMetadata getClassMetadata() {
		return classMetadata;
	}

	/**
	 * Returns the name of the id property.
	 * 
	 * @return a {@link String}.
	 */
	protected String getPrimaryKeyPropertyName() {
		return primaryKeyPropertyName;
	}

}
