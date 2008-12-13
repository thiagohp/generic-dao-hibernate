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
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import br.com.arsmachina.dao.ReadableDAO;
import br.com.arsmachina.dao.SortCriterion;

/**
 * {@link ReadableDAO} implementation using Hibernate. All methods use {@link #getSession()} to get
 * {@link Session}.
 * 
 * @author Thiago H. de Paula Figueiredo
 * @param <T> the entity class related to this DAO.
 * @param <K> the type of the field that represents the entity class' primary key.
 */
public abstract class ReadableDAOImpl<T, K extends Serializable> extends BaseHibernateDAO<T, K>
		implements ReadableDAO<T, K> {

	/**
	 * A {@link SortCriterion} array with no elements.
	 */
	final public static SortCriterion[] EMPTY_SORTING_CRITERIA = new SortCriterion[0];

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

			StringBuilder builder = new StringBuilder(" ORDER BY ");

			for (int i = 0; i < sortCriteria.length - 1; i++) {
				builder.append(sortCriteria.toString());
				builder.append(", ");
			}

			builder.append(sortCriteria[sortCriteria.length - 1]);

			string = builder.toString();

		}

		return string;

	}

	/**
	 * Constructor that takes a {@link Class} and a {@link SessionFactory}.
	 * 
	 * @param clasz a {@link Class}.
	 * @param sessionFactory a {@link SessionFactory}. It cannot be null.
	 */
	@SuppressWarnings("unchecked")
	public ReadableDAOImpl(SessionFactory sessionFactory) {
		super(null, sessionFactory);
	}

	/**
	 * Constructor that takes a {@link Class} and a {@link SessionFactory}.
	 * 
	 * @param clasz a {@link Class}.
	 * @param sessionFactory a {@link SessionFactory}. It cannot be null.
	 */
	@SuppressWarnings("unchecked")
	public ReadableDAOImpl(Class<T> clasz, SessionFactory sessionFactory) {
		super(clasz, sessionFactory);
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
		return (T) getSession().get(getEntityClass(), id);
	}

	/**
	 * @see br.com.arsmachina.dao.ReadableDAO#findByIds(K[])
	 */
	@SuppressWarnings("unchecked")
	public List<T> findByIds(K... ids) {

		Criteria criteria = createCriteria();
		criteria.add(Restrictions.in(getPrimaryKeyPropertyName(), ids));
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
	 * @see br.com.arsmachina.dao.WriteableDAO#refresh(java.lang.Object)
	 */
	public void refresh(T object) {
		getSession().refresh(object);
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
	public List<T> findAll(int firstResult, int maximumResults, SortCriterion... sortingConstraints) {

		Criteria criteria = createCriteria();
		criteria.setFirstResult(firstResult);
		criteria.setMaxResults(maximumResults);

		if (sortingConstraints == null || sortingConstraints.length == 0) {
			sortingConstraints = getDefaultSortCriteria();
		}

		addSortCriteria(criteria, sortingConstraints);

		return criteria.list();

	}

	/**
	 * Reattaches the object to the current {@link org.hibernate.Session} using
	 * <code>Session.lock(object, LockMode.NONE)</code> and then returns the object.
	 * 
	 * @param a <code>T</code>.
	 * @return <code>object</code>.
	 * @see br.com.arsmachina.dao.ReadableDAO#reattach(java.lang.Object)
	 */
	public T reattach(T object) {
		
		getSession().lock(object, LockMode.NONE);
		return object;
		
	}
	
	/**
	 * Adds <code>sortCriteria</code> to a {@link Criteria} instance.
	 * 
	 * @param criteria a {@link Criteria}. It cannot be null.
	 * @param sortCriteria a {@link SortCriterion}<code>...</code>. It cannot be null.
	 * @todo Support for property paths, not just property names.
	 */
	final public void addSortCriteria(Criteria criteria, SortCriterion... sortCriteria) {

		assert criteria != null;
		
		if (sortCriteria == null || sortCriteria.length == 0) {
			sortCriteria = getDefaultSortCriteria();
		}
		
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
	 * Creates a {@link Criteria} for this entity class.
	 * 
	 * @return a {@link Criteria}.
	 */
	public Criteria createCriteria() {
		return getSession().createCriteria(getEntityClass());
	}

	/**
	 * Creates a {@link Criteria} for this entity class with given sort criteria.
	 * 
	 * @return a {@link Criteria}.
	 */
	public Criteria createCriteria(SortCriterion ... sortCriteria) {
		
		Criteria criteria = createCriteria();
		addSortCriteria(criteria, sortCriteria);
		return criteria;
		
	}

	/**
	 * Creates a {@link Criteria} for this entity class with given sort criteria,
	 * first result index and maximum number of results. 
	 * 
	 * @return a {@link Criteria}.
	 */
	public Criteria createCriteria(int firstIndex, int maximumResults, SortCriterion ... sortCriteria) {
		
		Criteria criteria = createCriteria(sortCriteria);
		criteria.setFirstResult(firstIndex);
		criteria.setMaxResults(maximumResults);
		return criteria;
		
	}

	/**
	 * Used by {@link #findByExample(Object)} to create an {@link Example} instance.
	 * 
	 * @todo add criteria for property types not handled by Example (primary keys, associations,
	 * etc)
	 * @return an {@link Example}.
	 */
	public Example createExample(T entity) {

		Example example = Example.create(entity);
		example.enableLike(MatchMode.ANYWHERE);
		example.excludeZeroes();
		example.ignoreCase();

		return example;

	}

	/**
	 * Returns the value of the <code>defaultHqlOrderBy</code> property.
	 * 
	 * @return a {@link String}.
	 */
	public final String getDefaultHqlOrderBy() {
		return defaultHqlOrderBy;
	}

}
