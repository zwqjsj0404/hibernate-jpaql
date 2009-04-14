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
package org.hibernate.persister;

/**
 * Provides access to information about the tables used to persist a collection or entity.
 * <p/>
 * Note that this includes all inheritence tables and secondary tables.  The "main" table is called the
 * {@link #getDrivingTableName() driving} table.  This is the table from which all {@link #getJoinedTables joins}
 * originate.
 *
 * @author Steve Ebersole
 */
public interface MappedTableMetadata {
	/**
	 * Provides information about a secondary or inheritence table to which we must join to define the entire
	 * table space for this thing being persisted.
	 */
	public static class JoinedTable {
		private final String name;
		private final String[] keyColumns;
		private final boolean useInnerJoin;

		/**
		 * Create a JoinedTable definition.  By default we are saying to not use inner joins.
		 *
		 * @param name The table name.
		 * @param keyColumns The columns which make up the key to the {@link MappedTableMetadata#getDrivingTableName driving}
		 * table.
		 */
		public JoinedTable(String name, String[] keyColumns) {
			this( name, keyColumns, false );
		}

		/**
		 * Create a JoinedTable definition.
		 *
		 * @param name The table name.
		 * @param keyColumns The columns which make up the key to the {@link MappedTableMetadata#getDrivingTableName driving}
		 * table.
		 * @param useInnerJoin Should we use an inner join (faster, but restrictive) as the join type?
		 */
		public JoinedTable(String name, String[] keyColumns, boolean useInnerJoin) {
			this.name = name;
			this.keyColumns = keyColumns;
			this.useInnerJoin = useInnerJoin;
		}

		public String getName() {
			return name;
		}

		public String[] getKeyColumns() {
			return keyColumns;
		}

		public boolean useInnerJoin() {
			return useInnerJoin;
		}
	}


	public String getSqlAliasRootBase();

	/**
	 * Get the name of the driving table.
	 *
	 * @return
	 */
	public String getDrivingTableName();

	/**
	 * Get the names of the identifier columns from the driving table.  The entity persisters
	 * assume that all <i>key</i> mappings refer to the driving table's PK (this is true for both <i>join</i> and
	 * <i>joined-subclass</i> mappings).
	 *
	 * @return The driving table columns used in all {@link #getJoinedTables joins}
	 */
	public String[] getIdentifierColumnNames();

	/**
	 * Tables to which we must join to provide the complete "table space" for the given persister.
	 *
	 * @return The joined table definitions.
	 */
	public JoinedTable[] getJoinedTables();
}
