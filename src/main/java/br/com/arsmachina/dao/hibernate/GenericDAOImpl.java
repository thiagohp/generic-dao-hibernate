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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;

import br.com.arsmachina.dao.DAO;
import br.com.arsmachina.dao.SortCriterion;

/**
 * {@link AbstractDAO} implementation using Hibernate. All methods use {@link #getSession()} to get
 * a {@link Session}.
 * 
 * 
 * @author Thiago H. de Paula Figueiredo
 * @param <T> the entity class related to this DAO.
 * @param <K> the type of the field that represents the entity class' primary key.
 */
public abstract class GenericDAOImpl<T, K extends Serializable> implements DAO<T, K> {

	/**
	 * A {@link SortCriterion} array with no elements.
	 */
	final public static SortCriterion[] EMPTY_SORTING_CRITERIA = new SortCriterion[0];

	final private SessionFactory sessionFactory;

	final private Class<T> entityClass;

	final private ClassMetadata classMetadata;

	final private String primaryKeyPropertyName;

	final private String defaultHqlOrderBy = toHqlOrderBy(getDefaultSortCriteria());

	/**
	 * Returns a HQL <code>order by</code> clause given some {@link SortCriterion}s.
	 * 
	 * @param sortCriteria {@link SortCriterion} instances.
	 * @return a {@link String}.
	 */
	final public static String toHqlOrderBy(SortCriterion... sortCriteria) {

		String string = "";

		if (sortCriteria.length > 0) {

			StringBuilder builder = new StringBuilder();

			for (int i = 0; i < sortCriteria.length - 1; i++) {
				builder.append(sortCriteria.toString());
				builder.append(", ");
			}

			builder.append(sortCriteria[sortCriteria.length - 1]);

		}

		return string;

	}

	/**
	 * Single constructor.
	 * 
	 * @param sessionFactory a {@link SessionFactory}. It cannot be null.
	 */
	@SuppressWarnings("unchecked")
	public GenericDAOImpl(SessionFactory sessionFactory) {

		if (sessionFactory == null) {
			throw new IllegalArgumentException("Parameter sessionFactory cannot be null");
		}

		this.sessionFactory = sessionFactory;
		final Type genericSuperclass = getClass().getGenericSuperclass();
		final ParameterizedType parameterizedType = ((ParameterizedType) genericSuperclass);
		entityClass = (Class<T>) parameterizedType.getActualTypeArguments()[0];
		classMetadata = sessionFactory.getClassMetadata(entityClass);

		if (classMetadata == null) {
			throw new RuntimeException("Class " + entityClass.getName() + " is not mapped");
		}
		
		primaryKeyPropertyName = classMetadata.getIdentifierPropertyName();

		assert entityClass != null;
		assert classMetadata != null;
		assert primaryKeyPropertyName != null;

 	}

	/**
	 * @see br.com.arsmachina.dao.ReadableDAO#countAll()
	 */
	public int countAll() {
		
		final Criteria criteria = createCriteria();
		
		criteria.setProjection(Projections.rowCount());
		
		return (Integer) criteria.uniqueResult();
		
	}
	
	/**
	 * Returns all the entity class' objects. They are sorted according to
	 * {@link #getDefaultSortCriterions()}.
	 * 
	 * @see br.com.arsmachina.dao.ReadableDAO#findAll()
	 * @see #getDefaultSortCriterions()
	 */
	@SuppressWarnings("unchecked")
	public List<T> findAll() {

		Criteria criteria = createCriteria();
		addSortCriteria(criteria, getDefaultSortCriteria());
		return criteria.list();

	}

	/**
	 * @see br.com.arsmachina.dao.ReadableDAO#findById(java.io.Serializable)
	 */
	@SuppressWarnings("unchecked")
	public T findById(K id) {
		return (T) getSession().get(entityClass, id);
	}

	/**
	 * @see br.com.arsmachina.dao.ReadableDAO#findByIds(K[])
	 */
	@SuppressWarnings("unchecked")
	public List<T> findByIds(K... ids) {

		Criteria criteria = createCriteria();
		criteria.add(Restrictions.in(primaryKeyPropertyName, ids));
		return criteria.list();

	}

	/** 
	 * @see br.com.arsmachina.dao.ReadableDAO#findByExample(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public List<T> findByExample(T example) {
		
		Criteria criteria = createCriteria();
		
		if (example != null) {
			criteria.add(createExample(example));
		}
		
		return criteria.list();
		
	}

	/**
	 * @see br.com.arsmachina.dao.WriteableDAO#delete(java.io.Serializable)
	 */
	public void delete(K id) {
		delete(findById(id));
	}

	/**
	 * @see br.com.arsmachina.dao.WriteableDAO#delete(java.lang.Object)
	 */
	public void delete(T object) {
		getSession().delete(object);
	}

	/**
	 * @see br.com.arsmachina.dao.WriteableDAO#evict(java.lang.Object)
	 */
	public void evict(T object) {
		getSession().evict(object);
	}

	/**
	 * @see br.com.arsmachina.dao.WriteableDAO#merge(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public T merge(T object) {
		return (T) getSession().merge(object);
	}

	/**
	 * @see br.com.arsmachina.dao.WriteableDAO#save(java.lang.Object)
	 */
	public void save(T object) {
		getSession().save(object);
	}

	/**
	 * @see br.com.arsmachina.dao.WriteableDAO#update(java.lang.Object)
	 */
	public void update(T object) {
		getSession().update(object);
	}

	/**
	 * @see br.com.arsmachina.dao.WriteableDAO#refresh(java.lang.Object)
	 */
	public void refresh(T object) {
		getSession().refresh(object);
	}

	/**
	 * Returns the entity class handled by this DAO.
	 * 
	 * @return a {@link Class<T>}.
	 */
	final public Class<T> getEntityClass() {
		return entityClass;
	}

	/**
	 * Returns <code>true</code> if the primary key field (identifier) of the given object is not
	 * null. Its value is obtained via {@link ClassMetadata#getIdentifier(Object, EntityMode)}.
	 * 
	 * @see br.com.arsmachina.dao.WriteableDAO#isPersistent(java.lang.Object)
	 */
	public boolean isPersistent(T object) {
		return classMetadata.getIdentifier(object, EntityMode.POJO) != null;
	}

	/**
	 * If <code>sortingConstraints</code> is <code>null</code> or empty, this implementation
	 * sort the results by the {@link SortCriterion}s returned by
	 * {@link #getDefaultSortCriterions()}.
	 * 
	 * @see br.com.arsmachina.dao.ReadableDAO#findAll(int, int,
	 * br.com.arsmachina.dao.SortCriterion[])
	 */
	@SuppressWarnings("unchecked")
	public List<T> findAll(int firstResult, int maxResults, SortCriterion... sortingConstraints) {

		Criteria criteria = createCriteria();
		criteria.setFirstResult(firstResult);
		criteria.setMaxResults(maxResults);

		if (sortingConstraints == null || sortingConstraints.length == 0) {
			sortingConstraints = getDefaultSortCriteria();
		}

		addSortCriteria(criteria, sortingConstraints);

		return criteria.list();

	}

	/**
	 * Adds <code>sortCriteria</code> to a {@link Criteria} instance.
	 * 
	 * @param criteria a {@link Criteria}. It cannot be null.
	 * @param sortCriteria a {@link SortCriterion}<code>...</code>. It cannot be null.
	 * @todo Support for property paths, not just property names.
	 */
	final public static void addSortCriteria(Criteria criteria, SortCriterion... sortCriteria) {

		assert sortCriteria != null;
		assert criteria != null;

		for (SortCriterion sortingConstraint : sortCriteria) {

			final String property = sortingConstraint.getProperty();
			final boolean ascending = sortingConstraint.isAscending();
			final Order order = ascending ? Order.asc(property) : Order.desc(property);
			criteria.addOrder(order);

		}

	}

	/**
	 * Adds the default sort criteria to a {@link Criteria} instance. This method just does
	 * <code>addSortCriteria(criteria, getDefaultSortCriteria());</code>.
	 * 
	 * @param criteria a {@link Criteria}. It cannot be null.
	 */
	protected void addSortCriteria(Criteria criteria) {
		addSortCriteria(criteria, getDefaultSortCriteria());
	}

	/**
	 * Returns the default {@link SortCriterion}s to be used to sort the objects lists returned by
	 * methods like {@link #findAll()} and {@link #findAll(int, int, SortCriterion...)} when no
	 * sorting constraints are given. This implementation returns {@link #EMPTY_SORTING_CRITERIA}.
	 * 
	 * @return a {@link SortCriterion} array. It cannot be <code>null</code>.
	 */
	public SortCriterion[] getDefaultSortCriteria() {
		return EMPTY_SORTING_CRITERIA;
	}

	/**
	 * Returns the {@link ClassMetadata} for the corresponding entity class.
	 * 
	 * @return a {@link ClassMetadata}.
	 */
	final protected ClassMetadata getClassMetadata() {
		return classMetadata;
	}

	/**
	 * Returns the primary key field (identifier) name for the corresponding entity class.
	 * 
	 * @return a {@link String}.
	 */
	final protected String getPrimaryKeyPropertyName() {
		return primaryKeyPropertyName;
	}

	/**
	 * Creates a {@link Criteria} for this entity class.
	 * 
	 * @return a {@link Criteria}.
	 */
	protected Criteria createCriteria() {
		return getSession().createCriteria(entityClass);
	}

	/**
	 * Used by {@link #findByExample(Object)} to create an {@link Example} instance.
	 * 
	 * @return an {@link Example}.
	 */
	protected Example createExample(T entity) {
		
		Example example = Example.create(entity);
		example.enableLike(MatchMode.ANYWHERE);
		example.excludeZeroes();
		example.ignoreCase();
		
		return example;
		
	}

	/**
	 * Returns a {@link Session}. This implementation returns
	 * {@link SessionFactory#getCurrentSession()} and can be overriden if needed.
	 * 
	 * @return a {@link Session}.
	 */
	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	/**
	 * Returns this DAO's {@link SessionFactory}.
	 * 
	 * @return a {@link SessionFactory}.
	 */
	final protected SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Returns the value of the <code>defaultHqlOrderBy</code> property.
	 * 
	 * @return a {@link String}.
	 */
	protected final String getDefaultHqlOrderBy() {
		return defaultHqlOrderBy;
	}

}
