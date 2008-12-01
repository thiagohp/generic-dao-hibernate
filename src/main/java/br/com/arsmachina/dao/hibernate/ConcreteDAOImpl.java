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

import org.hibernate.SessionFactory;

import br.com.arsmachina.dao.DAO;

/**
 * A concrete {@link GenericDAOImpl} subclass. It is meant to be used to instantiate a DAO
 * for entities that need only the methods that {@link DAO} has. Never subclass
 * <code>ConcreteDAOImpl</code>: subclass {@link GenericDAOImpl} instead.
 * 
 * @author Thiago H. de Paula Figueiredo
 * @param <T> the entity class related to this DAO.
 * @param <K> the type of the field that represents the entity class' primary key.
 */
public class ConcreteDAOImpl<T, K extends Serializable> extends GenericDAOImpl<T, K> {

	/**
	 * Single constructor.
	 * 
	 * @param clasz the entity class. It cannot be null.
	 * @param sessionFactory a {@link SessionFactory}. It cannot be null.
	 */
	public ConcreteDAOImpl(Class<T> clasz, SessionFactory sessionFactory) {
		super(clasz, sessionFactory);
	}

}
