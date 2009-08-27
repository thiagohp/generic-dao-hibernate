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

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.context.ManagedSessionContext;
import org.hibernate.metadata.ClassMetadata;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import br.com.arsmachina.dao.DAO;

/**
 * Test class for {@link GenericDAOImpl}.
 * 
 * @author Thiago H. de Paula Figueiredo
 */
public class ConcreteDAOImplTest {

	final static DummyClass OBJECT = new DummyClass();

	final static Integer ID = 1;

	private org.hibernate.classic.Session session;

	private SessionFactory sessionFactory;

	private ConcreteDAOImpl<DummyClass, Integer> dao;

	private ConcreteDAOImpl<DummyClass, Integer> realDAO;

	private SessionFactory realSessionFactory;

	@SuppressWarnings("unused")
	@BeforeClass
	private void beforeClass() {

		AnnotationConfiguration configuration = new AnnotationConfiguration();
		configuration.configure();
		configuration.setProperty("hibernate.current_session_context_class", "managed");
		realSessionFactory = configuration.buildSessionFactory();
		ManagedSessionContext.bind(realSessionFactory.openSession());
		realDAO = new ConcreteDAOImpl<DummyClass, Integer>(DummyClass.class, realSessionFactory);

	}

	@SuppressWarnings( { "unused" })
	@BeforeMethod
	private void setUp() {

		sessionFactory = EasyMock.createMock(org.hibernate.SessionFactory.class);
		session = EasyMock.createMock(org.hibernate.classic.Session.class);

		EasyMock.expect(sessionFactory.getCurrentSession()).andReturn(session).anyTimes();

		ClassMetadata classMetadata = EasyMock.createMock(ClassMetadata.class);

		EasyMock.expect(sessionFactory.getClassMetadata(DummyClass.class))
				.andReturn(classMetadata)
				.anyTimes();

		EasyMock.expect(classMetadata.getIdentifierPropertyName()).andReturn("id").anyTimes();

		EasyMock.replay(sessionFactory);
		EasyMock.replay(classMetadata);

		dao = new ConcreteDAOImpl<DummyClass, Integer>(DummyClass.class, sessionFactory);

	}

	/**
	 * Tests {@link ConcreteDAOImpl#ConcreteDAOImpl(DAO)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void constructor() {

		boolean ok = false;

		try {
			new ConcreteDAOImpl(null, sessionFactory);
		}
		catch (IllegalArgumentException e) {
			ok = true;
		}

		assert ok;

		try {
			new ConcreteDAOImpl(DummyClass.class, null);
		}
		catch (IllegalArgumentException e) {
			ok = true;
		}

		assert ok;

		new ConcreteDAOImpl(DummyClass.class, sessionFactory);

	}

	/**
	 * Tests {@link ConcreteDAOImpl#save(Object)}.
	 */
	@Test
	public void save() {

		EasyMock.expect(session.save(OBJECT)).andReturn(ID);
		EasyMock.replay(session);

		dao.save(OBJECT);
		EasyMock.verify(session);

	}

	/**
	 * Tests {@link ConcreteDAOImpl#isPersistent(Object)}.
	 */
	@Test
	public void isPersistent() {

		DummyClass dummy = new DummyClass();
		dummy.setId(null);

		assert realDAO.isPersistent(dummy) == false;

		dummy.setId(Integer.valueOf(1));

		assert realDAO.isPersistent(dummy);

	}

	@Test
	public void reattach() {

		boolean ok = false;
		DummyClass dummy = createAndInsertDummyObject();

		// force loading of lazy collection
		realDAO.evict(dummy);
		dummy = realDAO.findById(dummy.getId());
		realDAO.evict(dummy);

		try {

			final List<Integer> elements = dummy.getElements();
			for (Integer integer : elements) {
				integer.toString();
			}

		}
		catch (LazyInitializationException e) {
			ok = true;
		}

		assert ok;

		final DummyClass reattached = realDAO.reattach(dummy);

		// force loading of lazy collection
		final List<Integer> elements = reattached.getElements();
		for (Integer integer : elements) {
			integer.toString();
		}

	}

	/**
	 * Tests {@link ConcreteDAOImpl#update(Object)} with a real {@link SessionFactory}.
	 */
	@Test
	public void update() {

		final String SECOND_STRING = "aaaa";

		DummyClass dummy = createAndInsertDummyObject();

		dummy.setString(SECOND_STRING);

		session.beginTransaction();
		DummyClass returned = realDAO.update(dummy);
		session.getTransaction().commit();

		assert dummy == returned;

		realDAO.evict(dummy);

		DummyClass secondDummy = realDAO.findById(dummy.getId());

		assert dummy != secondDummy;

		assert SECOND_STRING.equals(secondDummy.getString());

		boolean ok = false;

		DummyClass notPersistent = new DummyClass();

		try {
			realDAO.update(notPersistent);
		}
		catch (IllegalArgumentException e) {
			ok = true;
		}

		assert ok;

		try {
			realDAO.update(null);
		}
		catch (IllegalArgumentException e) {
			ok = true;
		}

		assert ok;

	}

	/**
	 * @param FIRST_STRING
	 * @return
	 */
	private DummyClass createAndInsertDummyObject() {

		final String FIRST_STRING = "bbbb";

		List<Integer> elements = new ArrayList<Integer>();
		elements.add(1);
		elements.add(2);

		DummyClass dummy = new DummyClass();
		dummy.setElements(elements);
		dummy.setString(FIRST_STRING);

		session = (org.hibernate.classic.Session) realDAO.getSession();

		session.beginTransaction();
		realDAO.save(dummy);
		session.getTransaction().commit();

		return dummy;
	}

	/**
	 * Tests {@link ConcreteDAOImpl#delete(<T>))}.
	 */
	@Test
	public void delete() {

		session.delete((Object) OBJECT);
		EasyMock.replay(session);

		dao.delete(OBJECT);
		EasyMock.verify(session);

	}

	/**
	 * Tests {@link ConcreteDAOImpl#evict(<T>))}.
	 */
	@Test
	public void evict() {

		session.evict(OBJECT);
		EasyMock.replay(session);

		dao.evict(OBJECT);
		EasyMock.verify(session);

	}

	/**
	 * Tests {@link ConcreteDAOImpl#refresh(<T>))}.
	 */
	@Test
	public void refresh() {

		session.refresh(OBJECT);
		EasyMock.replay(session);

		dao.refresh(OBJECT);
		EasyMock.verify(session);

	}

	/**
	 * Tests {@link ConcreteDAOImpl#getEntityClass()}.
	 */
	@Test
	public void getEntityClass() {
		assert DummyClass.class == realDAO.getEntityClass();
	}

	/**
	 * Tests {@link ConcreteDAOImpl#getClassMetadata())}.
	 */
	@Test
	public void getClassMetadata() {
		assert realDAO.getClassMetadata() != null;
	}

	/**
	 * Tests {@link ConcreteDAOImpl#getPrimaryKeyPropertyName())}.
	 */
	@Test
	public void getPrimaryKeyPropertyName() {
		assert realDAO.getPrimaryKeyPropertyName().equals("id");
	}

	/**
	 * Tests {@link ConcreteDAOImpl#getSessionFactory())}.
	 */
	@Test
	public void getSessionFactory() {
		assert sessionFactory == dao.getSessionFactory();
	}

	/**
	 * Tests {@link ConcreteDAOImpl#getSession())}.
	 */
	@Test
	public void getSession() {
		EasyMock.verify(sessionFactory);
	}

}
