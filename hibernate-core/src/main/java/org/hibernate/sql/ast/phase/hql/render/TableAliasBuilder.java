/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2009, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.sql.ast.phase.hql.render;

import org.hibernate.persister.entity.Queryable;
import org.hibernate.persister.collection.QueryableCollection;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public interface TableAliasBuilder {

	public static class AliasRoot {
		private final String base;

		public AliasRoot(String base) {
			this.base = base;
		}

		public String generate(int suffix) {
			return base + Integer.toString( suffix ) + '_';
		}

		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			AliasRoot aliasRoot = ( AliasRoot ) o;
			return base.equals( aliasRoot.base );
		}

		public int hashCode() {
			return base.hashCode();
		}
	}

	public AliasRoot getSqlAliasRoot(Queryable persister, String alias);

	public AliasRoot getSqlAliasRoot(QueryableCollection persister, String alias);

	public AliasRoot getSimpleSqlAliasRoot(QueryableCollection persister);
}
