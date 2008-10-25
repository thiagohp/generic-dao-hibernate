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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.CollectionOfElements;

/**
 * 
 * @author Thiago H. de Paula Figueiredo
 */
@Entity
public class DummyClass {

	@Id
	@GeneratedValue
	private Integer id;

	private String string;

	@CollectionOfElements
	private List<Integer> elements = new ArrayList<Integer>();

	/**
	 * @return a {@link Integer}.
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Returns the value of the <code>elements</code> property.
	 * 
	 * @return a {@link List<Integer>}.
	 */
	public List<Integer> getElements() {
		return elements;
	}

	/**
	 * Changes the value of the <code>elements</code> property.
	 * 
	 * @param elements a {@link List<Integer>}.
	 */
	public void setElements(List<Integer> elements) {
		this.elements = elements;
	}

	/**
	 * Returns the value of the <code>string</code> property.
	 * 
	 * @return a {@link String}.
	 */
	public String getString() {
		return string;
	}

	/**
	 * Changes the value of the <code>string</code> property.
	 * 
	 * @param string a {@link String}.
	 */
	public void setString(String string) {
		this.string = string;
	}

}
