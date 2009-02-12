// Copyright 2007-2008 Thiago H. de Paula Figueiredo
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

import org.hibernate.EntityMode;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.metadata.ClassMetadata;

import br.com.arsmachina.dao.WriteableDAO;

/**
 * {@link WriteableDAO} implementation using Hibernate. All methods use {@link #getSession()} to get
 * a {@link Session}.
 * 
 * @author Thiago H. de Paula Figueiredo
 * @param <T> the entity class related to this DAO.
 * @param <K> the type of the field that represents the entity class' primary key.
 */
public abstract class WriteableDAOImpl<T, K extends Serializable> extends BaseHibernateDAO<T, K>
		implements WriteableDAO<T, K> {

	final private String deleteHQL;

	/**
	 * Constructor that takes a {@link Class} and a {@link SessionFactory}.
	 * 
	 * @param clasz a {@link Class}.
	 * @param sessionFactory a {@link SessionFactory}. It cannot be null.
	 */
	public WriteableDAOImpl(SessionFactory sessionFactory) {
		this(null, sessionFactory);
	}

	/**
	 * Constructor that takes a {@link Class} and a {@link SessionFactory}.
	 * 
	 * @param clasz a {@link Class}.
	 * @param sessionFactory a {@link SessionFactory}. It cannot be null.
	 */
	public WriteableDAOImpl(Class<T> clasz, SessionFactory sessionFactory) {
		
		super(clasz, sessionFactory);
		deleteHQL = createDeleteHQL();
		
	}

	/**
	 * Creates an HQL query used to delete an object given its primary key value.
	 * 
	 * @return a {@link String}.
	 */
	String createDeleteHQL() {

		return "delete from " + getEntityClass().getName() + " where "
				+ getPrimaryKeyPropertyName() + " = :id";

	}

	public void delete(K id) {

		Query query = getSession().createQuery(deleteHQL);
		query.setParameter("id", id);
		query.executeUpdate();

	}

	public void delete(T object) {
		getSession().delete(object);
	}

	public void evict(T object) {
		getSession().evict(object);
	}

	public void save(T object) {
		getSession().save(object);
	}
	
	public T update(T object) {
		
		if (isPersistent(object) == false) {
			throw new IllegalArgumentException("Object not persistent");
		}
		
		getSession().update(object);
		return object;
		
	}

	/**
	 * Returns <code>true</code> if the primary key field (identifier) of the given object is not
	 * null. Its value is obtained via {@link ClassMetadata#getIdentifier(Object, EntityMode)}.
	 * 
	 * @see br.com.arsmachina.dao.WriteableDAO#isPersistent(java.lang.Object)
	 * @throws IllegalArgumentException if <code>object</code> is null.
	 */
	public boolean isPersistent(T object) {
		
		if (object == null) {
			throw new IllegalArgumentException("Parameter object cannot be null");
		}
		
		return getClassMetadata().getIdentifier(object, EntityMode.POJO) != null;
		
	}

}
