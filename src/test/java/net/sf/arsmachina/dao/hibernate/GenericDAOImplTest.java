package net.sf.arsmachina.dao.hibernate;


import org.easymock.EasyMock;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.classic.Session;
import org.hibernate.metadata.ClassMetadata;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import br.com.arsmachina.dao.DAO;
import br.com.arsmachina.dao.hibernate.GenericDAOImpl;

/**
 * Test class for {@link GenericDAOImpl}.
 * 
 * @author Thiago H. de Paula Figueiredo (ThiagoHP)
 */
public class GenericDAOImplTest {

	final static String OBJECT = "persistent";

	final static Integer ID = 1;

	private Session session;

	private SessionFactory sessionFactory;

	private GenericDAOImpl<String, Integer> dao;

	private SessionFactory realSessionFactory;
	
	private DummyDAO dummyDAO;

	@SuppressWarnings("unused")
	@BeforeClass
	private void beforeMethod() {

		AnnotationConfiguration configuration = new AnnotationConfiguration();
		configuration.configure();
		realSessionFactory = configuration.buildSessionFactory();
		dummyDAO = new DummyDAO(realSessionFactory);

	}

	@SuppressWarnings( { "unused", "unchecked" })
	@BeforeMethod
	private void setUp() {

		sessionFactory = EasyMock.createMock(org.hibernate.SessionFactory.class);
		session = EasyMock.createMock(Session.class);

		EasyMock.expect(sessionFactory.getCurrentSession()).andReturn(session).anyTimes();

		ClassMetadata classMetadata = EasyMock.createMock(ClassMetadata.class);

		EasyMock.expect(sessionFactory.getClassMetadata(String.class))
				.andReturn(classMetadata)
				.anyTimes();

		EasyMock.expect(classMetadata.getIdentifierPropertyName()).andReturn("id").anyTimes();

		EasyMock.replay(sessionFactory);
		EasyMock.replay(classMetadata);

		dao = new StringDAO(sessionFactory);

	}

	/**
	 * Tests {@link GenericDAOImpl#GenericDAOImpl(DAO)}.
	 */
	@Test
	public void constructor() {

		boolean ok = false;

		try {
			new StringDAO(null);
		}
		catch (IllegalArgumentException e) {
			ok = true;
		}

		assert ok;

		new StringDAO(sessionFactory);

	}

	/**
	 * Tests {@link GenericDAOImpl#save(Object)}.
	 */
	@Test
	public void save() {

		EasyMock.expect(session.save(OBJECT)).andReturn(ID);
		EasyMock.replay(session);

		dao.save(OBJECT);
		EasyMock.verify(session);

	}

	/**
	 * Tests {@link GenericDAOImpl#isPersistent(Object)}.
	 */
	@Test
	public void isPersistent() {

		DummyDAO dummyDAO = new DummyDAO(realSessionFactory);

		DummyClass dummy = new DummyClass();
		dummy.setId(null);

		assert dummyDAO.isPersistent(dummy) == false;

		dummy.setId(Integer.valueOf(1));

		assert dummyDAO.isPersistent(dummy);

	}

	/**
	 * Tests {@link GenericDAOImpl#save(Object)}.
	 */
	@Test
	public void update() {

		session.update(OBJECT);
		EasyMock.replay(session);

		dao.update(OBJECT);
		EasyMock.verify(session);

	}

	/**
	 * Tests {@link GenericDAOImpl#delete(<T>))}.
	 */
	@Test
	public void delete() {

		session.delete((Object) OBJECT);
		EasyMock.replay(session);

		dao.delete(OBJECT);
		EasyMock.verify(session);

	}

	/**
	 * Tests {@link GenericDAOImpl#evict(<T>))}.
	 */
	@Test
	public void evict() {

		session.evict(OBJECT);
		EasyMock.replay(session);

		dao.evict(OBJECT);
		EasyMock.verify(session);

	}

	/**
	 * Tests {@link GenericDAOImpl#refresh(<T>))}.
	 */
	@Test
	public void refresh() {

		session.refresh(OBJECT);
		EasyMock.replay(session);

		dao.refresh(OBJECT);
		EasyMock.verify(session);

	}

	/**
	 * Tests {@link GenericDAOImpl#merge(<T>))}.
	 */
	@Test
	public void merge() {

		final String merged = "xxx";

		EasyMock.expect(session.merge(OBJECT)).andReturn(merged);
		EasyMock.replay(session);

		final String result = dao.merge(OBJECT);
		EasyMock.verify(session);

		assert result == merged;

	}

	/**
	 * Tests {@link GenericDAOImpl#getEntityClass()}.
	 */
	@Test
	public void getEntityClass() {
		assert String.class == dao.getEntityClass();
	}

	/**
	 * Tests {@link GenericDAOImpl#getClassMetadata())}.
	 */
	@Test
	public void getClassMetadata() {
		assert dummyDAO.getClassMetadata() != null;
	}

	/**
	 * Tests {@link GenericDAOImpl#getPrimaryKeyPropertyName())}.
	 */
	@Test
	public void getPrimaryKeyPropertyName() {
		assert dummyDAO.getPrimaryKeyPropertyName().equals("id");
	}

	/**
	 * Tests {@link GenericDAOImpl#getSessionFactory())}.
	 */
	@Test
	public void getSessionFactory() {
		assert sessionFactory == dao.getSessionFactory();
	}

	/**
	 * Tests {@link GenericDAOImpl#getSession())}.
	 */
	@Test
	public void getSession() {
		EasyMock.verify(sessionFactory);
	}

	final private static class StringDAO extends GenericDAOImpl<String, Integer> {

		/**
		 * @param dao
		 */
		@SuppressWarnings("unchecked")
		public StringDAO(SessionFactory sessionFactory) {
			super(sessionFactory);
		}

	}

	final private static class DummyDAO extends GenericDAOImpl<DummyClass, Integer> {

		/**
		 * @param dao
		 */
		@SuppressWarnings("unchecked")
		public DummyDAO(SessionFactory sessionFactory) {
			super(sessionFactory);
		}

	}

}
