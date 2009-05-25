// Copyright 2008-2009 Thiago H. de Paula Figueiredo
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
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.metadata.ClassMetadata;

import br.com.arsmachina.dao.DAO;
import br.com.arsmachina.dao.SortCriterion;

/**
 * {@link AbstractDAO} implementation using Hibernate. All methods use
 * {@link #getSession()} to get a {@link Session}. All methods delegate its
 * calls to an internal {@link ReadableDAOImpl} or {@link WriteableDAOImpl}
 * instance.
 * 
 * @author Thiago H. de Paula Figueiredo
 * @param <T> the entity class related to this DAO.
 * @param <K> the type of the field that represents the entity class' primary
 *            key.
 */
public class GenericDAOImpl<T, K extends Serializable> implements DAO<T, K> {

	final private InternalReadableDAOImpl readableDAO;

	final private InternalWriteableDAOImpl writeableDAO;

	/**
	 * Single public constructor.
	 * 
	 * @param sessionFactory a {@link SessionFactory}. It cannot be null.
	 */
	@SuppressWarnings("unchecked")
	public GenericDAOImpl(SessionFactory sessionFactory) {

		if (sessionFactory == null) {
			throw new IllegalArgumentException(
					"Parameter sessionFactory cannot be null");
		}

		final Type genericSuperclass = getClass().getGenericSuperclass();
		final ParameterizedType parameterizedType =
			((ParameterizedType) genericSuperclass);
		Class clasz = (Class<T>) parameterizedType.getActualTypeArguments()[0];

		readableDAO = new InternalReadableDAOImpl(clasz, sessionFactory);
		writeableDAO = new InternalWriteableDAOImpl(clasz, sessionFactory);
		
		readableDAO.setDefaultSortCriteria(getDefaultSortCriteria());

	}

	/**
	 * Constructor made specifically for {@link ConcreteDAOImpl}. It shouldn't
	 * be used in any other class.
	 * 
	 * @param clasz the entity class. It cannot be null.
	 * @param sessionFactory a {@link SessionFactory}. It cannot be null.
	 */
	GenericDAOImpl(Class<T> clasz, SessionFactory sessionFactory) {

		if (clasz == null) {
			throw new IllegalArgumentException("Parameter clasz cannot be null");
		}

		if (sessionFactory == null) {
			throw new IllegalArgumentException(
					"Parameter sessionFactory cannot be null");
		}

		readableDAO = new InternalReadableDAOImpl(clasz, sessionFactory);
		writeableDAO = new InternalWriteableDAOImpl(clasz, sessionFactory);

		readableDAO.setDefaultSortCriteria(getDefaultSortCriteria());

	}

	public int countAll() {
		return readableDAO.countAll();
	}

	public List<T> findAll() {
		return readableDAO.findAll();
	}

	public List<T> findAll(int firstResult, int maximumResults,
			SortCriterion... sortingConstraints) {
		
		return readableDAO.findAll(firstResult, maximumResults,
				sortingConstraints);
		
	}

	public List<T> findByExample(T example) {
		return readableDAO.findByExample(example);
	}

	public T findById(K id) {
		return readableDAO.findById(id);
	}

	public List<T> findByIds(K... ids) {
		return readableDAO.findByIds(ids);
	}

	public void refresh(T object) {
		readableDAO.refresh(object);
	}

	public void delete(T object) {
		writeableDAO.delete(object);
	}

	public void delete(K id) {
		writeableDAO.delete(id);
	}

	public void evict(T object) {
		writeableDAO.evict(object);
	}

	public boolean isPersistent(T object) {
		return writeableDAO.isPersistent(object);
	}

	public void save(T object) {
		writeableDAO.save(object);
	}

	public T update(T object) {
		return writeableDAO.update(object);
	}

	public T reattach(T object) {
		return readableDAO.reattach(object);
	}

	public SortCriterion[] getDefaultSortCriteria() {
		return readableDAO.getDefaultSortCriteria();
	}

	/**
	 * Invokes <code>readableDAO.addSortCriteria()<code>.
	 * 
	 * @param criteria
	 * @see br.com.arsmachina.dao.hibernate.ReadableDAOImpl#addSortCriteria(org.hibernate.Criteria)
	 */
	protected void addSortCriteria(Criteria criteria) {
		readableDAO.addSortCriteria(criteria);
	}

	/**
	 * Invokes <code>delegate.addSortCriteria()<code>.
	 * 
	 * @param criteria
	 * @param sortCriteria
	 * @see br.com.arsmachina.dao.hibernate.ReadableDAOImpl#addSortCriteria(org.hibernate.Criteria,
	 *      br.com.arsmachina.dao.SortCriterion[])
	 */
	public final void addSortCriteria(Criteria criteria,
			SortCriterion... sortCriteria) {
		readableDAO.addSortCriteria(criteria, sortCriteria);
	}

	/**
	 * Invokes <code>readableDAO.createCriteria()<code>.
	 * 
	 * @return
	 * @see br.com.arsmachina.dao.hibernate.ReadableDAOImpl#createCriteria()
	 */
	protected Criteria createCriteria() {
		return readableDAO.createCriteria();
	}

	/**
	 * Invokes <code>delegate.createCriteria()<code>.
	 * 
	 * @param sortCriteria
	 * @return
	 * @see br.com.arsmachina.dao.hibernate.ReadableDAOImpl#createCriteria(br.com.arsmachina.dao.SortCriterion[])
	 */
	protected Criteria createCriteria(SortCriterion... sortCriteria) {
		return readableDAO.createCriteria(sortCriteria);
	}

	/**
	 * Invokes <code>delegate.createCriteria()<code>.
	 * 
	 * @param firstIndex
	 * @param maximumResults
	 * @param sortCriteria
	 * @return
	 * @see br.com.arsmachina.dao.hibernate.ReadableDAOImpl#createCriteria(int,
	 *      int, br.com.arsmachina.dao.SortCriterion[])
	 */
	public Criteria createCriteria(int firstIndex, int maximumResults,
			SortCriterion... sortCriteria) {
		
		return readableDAO.createCriteria(firstIndex, maximumResults,
				sortCriteria);
		
	}

	/**
	 * Invokes <code>readableDAO.createExample()<code>.
	 * 
	 * @param entity
	 * @return
	 * @see br.com.arsmachina.dao.hibernate.ReadableDAOImpl#createExample(java.lang.Object)
	 */
	protected Example createExample(T entity) {
		return readableDAO.createExample(entity);
	}

	/**
	 * Returns the entity class handled by this DAO.
	 * 
	 * @return a {@link Class<T>}.
	 */
	protected final Class<T> getEntityClass() {
		return readableDAO.getEntityClass();
	}

	/**
	 * Returns a {@link Session}. This implementation returns
	 * {@link SessionFactory#getCurrentSession()} and can be overriden if
	 * needed.
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
		return readableDAO.getSessionFactory();
	}

	/**
	 * Returns the {@link ClassMetadata} for the corresponding entity class.
	 * 
	 * @return a {@link ClassMetadata}.
	 */
	final protected ClassMetadata getClassMetadata() {
		return readableDAO.getClassMetadata();
	}

	/**
	 * Returns the name of the property.
	 * 
	 * @return a {@link String}.
	 */
	public String getPrimaryKeyPropertyName() {
		return readableDAO.getPrimaryKeyPropertyName();
	}

	/**
	 * Concrete {@link ReadableDAOImpl} subclass.
	 * 
	 * @author Thiago H. de Paula Figueiredo
	 * @param <T>
	 * @param <K>
	 */
	private final class InternalReadableDAOImpl extends ReadableDAOImpl<T, K> {

		public InternalReadableDAOImpl(Class<T> clasz,
				SessionFactory sessionFactory) {
			super(clasz, sessionFactory);
		}

	}

	/**
	 * Concrete {@link WriteableDAOImpl} subclass.
	 * 
	 * @author Thiago H. de Paula Figueiredo
	 * @param <T>
	 * @param <K>
	 */
	private final class InternalWriteableDAOImpl extends WriteableDAOImpl<T, K> {

		public InternalWriteableDAOImpl(Class<T> clasz,
				SessionFactory sessionFactory) {
			super(clasz, sessionFactory);
		}

	}

}
