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
package org.hibernate.sql.ast.alias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Queryable;

/**
 * Contract for generating table alias roots.  An alias root is the base used to create aliases for a whole series
 * of related tables (e.g., for all the tables in a joined-subclass hierarchy).
 *
 * @author Steve Ebersole
 */
public interface TableAliasGenerator {
	/**
	 * Encapsulation of the alias root, which is a template for creating a series of "related" SQL aliases (for
	 * a given persister e.g.).
	 */
	public static class TableAliasRoot {
		private static final Logger log = LoggerFactory.getLogger( TableAliasRoot.class );

		private final String source;
		private final String base;

		/**
		 * Create the alias root.
		 *
		 * @param source The original alias from the source query.
		 * @param base The base used to generate SQL aliases.
		 */
		public TableAliasRoot(String source, String base) {
			log.trace( "Creating alias root [" + source + " -> " + base + "]" );
			this.source = source;
			this.base = base;
		}

		/**
		 * Retrieve the original alias from the source query.
		 *
		 * @return The source alias.
		 */
		public String getSource() {
			return source;
		}

		/**
		 * Retrieve the base used to generate SQL aliases.
		 *
		 * @return The SQL alias base.
		 */
		public String getBase() {
			return base;
		}

		/**
		 * Generate the sql alias based on the given suffix which is the <i>subclass table number</i>.
		 *
		 * @param suffix The alias suffix.
		 *
		 * @return The generated alias.
		 */
		public String generate(int suffix) {
			return base + Integer.toString( suffix ) + '_';
		}

		/**
		 * Generate an alias for the <i>collection table</i>.
		 * <p/>
		 * For basic collections and many-to-many mappings the <i>collection table</i> is a distinct ERD entity, and we
		 * must generate a reference to that table (in contrast, the <i>collection table</i> for a one-to-many
		 * association is actually the entity table of the many side).
		 *
		 * @return The generated alias.
		 */
		public String generateCollectionTableAlias() {
			return base + "_";
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			TableAliasRoot aliasRoot = ( TableAliasRoot ) o;
			return base.equals( aliasRoot.base );
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode() {
			return base.hashCode();
		}
	}

	/**
	 * Generate the alias root for the given persister reference.
	 *
	 * @param persister The entity persister.
	 * @param sourceAlias The alias attached to the persister in the source query.
	 *
	 * @return The alias root.
	 */
	public TableAliasRoot generateSqlAliasRoot(Queryable persister, String sourceAlias);

	/**
	 * Generate the alias root for the given persister reference.
	 *
	 * @param persister The collection persister
	 * @param sourceAlias The alias attached to the persister in the source query.
	 *
	 * @return The alias root.
	 */
	public TableAliasRoot generateSqlAliasRoot(QueryableCollection persister, String sourceAlias);
}
