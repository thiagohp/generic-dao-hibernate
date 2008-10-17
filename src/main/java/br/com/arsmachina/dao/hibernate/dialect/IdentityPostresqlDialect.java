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

package br.com.arsmachina.dao.hibernate.dialect;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.SequenceGenerator;

/**
 * {@link PostgreSQLDialect} that defines {@link IdentityGenerator} as the default id value
 * generator instead of {@link SequenceGenerator}. In addition, it fixes the "wrong sequence name
 * in backticks-scaped table name bug".
 * 
 * @author Thiago H. de Paula Figueiredo
 */
public class IdentityPostresqlDialect extends PostgreSQLDialect {

	/**
	 * Returns {@link IdentityGenerator} instead of {@link SequenceGenerator}.
	 * 
	 * @see org.hibernate.dialect.PostgreSQLDialect#getNativeIdentifierGeneratorClass()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class getNativeIdentifierGeneratorClass() {
		return IdentityGenerator.class;
	}

	/**
	 * Removes the backtickes from the <code>sequenceName</code> and then returns
	 * <code>super.getSequenceNextValString(sequenceName)</code>.
	 * @see org.hibernate.dialect.PostgreSQLDialect#getSequenceNextValString(java.lang.String)
	 */
	@Override
	public String getIdentitySelectString(String table, String column, int type) {
		table = table.replace("\"", "");
		return new StringBuffer().append("select currval('")
			.append(table)
			.append('_')
			.append(column)
			.append("_seq')")
			.toString();
	}

}
