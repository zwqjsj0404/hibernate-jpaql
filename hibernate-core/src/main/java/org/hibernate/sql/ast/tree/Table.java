/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2009, Red Hat Middleware LLC or third-party
 * contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.sql.ast.tree;

import java.util.ArrayList;
import java.util.HashMap;

import org.hibernate.type.Type;
import org.hibernate.sql.ast.alias.TableAliasGenerator;
import org.hibernate.sql.ast.common.HibernateTree;
import org.hibernate.sql.ast.common.HibernateToken;
import org.hibernate.sql.ast.util.DisplayableNode;
import org.hibernate.sql.ast.phase.hql.parse.HQLLexer;
import org.hibernate.sql.ast.phase.hql.resolve.PersisterSpace;
import org.hibernate.sql.ast.phase.hql.resolve.PersisterTableExpressionGenerator;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.util.StringHelper;
import org.hibernate.QueryException;

/**
 * todo : javadocs
 *
 * @author Steve Ebersole
 */
public class Table extends HibernateTree implements DisplayableNode {
	private final TableSpace tableSpace;

	public Table(String tableName, String tableAlias, TableSpace tableSpace) {
		super( new HibernateToken( HQLLexer.TABLE ) );
		addChild( new HibernateTree( HQLLexer.IDENTIFIER, tableName ) );
		addChild( new HibernateTree( HQLLexer.ALIAS_NAME, tableAlias ) );
		this.tableSpace = tableSpace;
		tableSpace.addTable( this );
	}

	public TableSpace getTableSpace() {
		return tableSpace;
	}

	public HibernateTree getTableName() {
		return ( HibernateTree ) getChild( 0 );
	}

	public String getTableNameText() {
		return getTableName().getText();
	}

	public HibernateTree getAlias() {
		return ( HibernateTree ) getChild( 1 );
	}

	public String getAliasText() {
		return getAlias().getText();
	}

	public String getText() {
		return getTableNameText() + " (" + getAliasText() + ")";
	}

	public String getDisplayText() {
		return "[source-alias=" + tableSpace.getSourceAlias() + "]";
	}

	/**
	 * Represents a grouping of related tables (i.e. all tables for a given persister).
	 */
	public static interface TableSpace {

		public void addTable(Table table);

		/**
		 * Used as a unique identification since each table space originates from a single source alias (persister reference).
		 *
		 * @return The source alias.
		 */
		public String getSourceAlias();

		/**
		 * PersisterSpace and TableSpace are related one-to-one...
		 *
		 * @return The persister space corresponding to this table space.
		 */
		public PersisterSpace getPersisterSpace();

		/**
		 * Get the table reference that should act as the RHS for this table space whenever we join to into it.
		 *
		 * @return The RHS table for joining into this table space structure.
		 */
		public Table getJoinIntoTable();

		public String[] getJoinIntoColumns();

		public Type getPropertyType(String propertyName);

		/**
		 * Get the table reference that contains the columns to which the given property is mapped.
		 *
		 * @param propertyName The name of the property for which to locate the containing table.
		 *
		 * @return The containing table.
		 */
		public Table getContainingTable(String propertyName);

		public String[] getPropertyColumnNames(String propertyName);

		public HibernateTree buildIdentifierColumnReferences();

		public HibernateTree buildCompleteColumnReferences();
	}

	public static abstract class AbstractTableSpace implements Table.TableSpace {
		private final TableAliasGenerator.TableAliasRoot aliasRoot;
		protected final ArrayList<Table> tables = new ArrayList<Table>();
		protected final HashMap<String,Table> aliasToTableMap = new HashMap<String,Table>();
		protected final HashMap<String,Table> nameToTableMap = new HashMap<String,Table>();

		private AbstractTableSpace(TableAliasGenerator.TableAliasRoot aliasRoot) {
			this.aliasRoot = aliasRoot;
		}

		public String getSourceAlias() {
			return aliasRoot.getSource();
		}

		public void addTable(Table table) {
			tables.add( table );
			aliasToTableMap.put( table.getAliasText(), table );
			nameToTableMap.put( table.getTableNameText(), table );
		}
	}

	public static abstract class AbstractPersisterSpace implements PersisterSpace {
		private final HashMap<String,Table> propertyToJoinedTableMap = new HashMap<String,Table>();

		public Table locateReusablePropertyJoinedTable(String propertyName) {
			return propertyToJoinedTableMap.get( propertyName );
		}

		public void registerReusablePropertyJoinedTable(String propertyName, Table table) {
			propertyToJoinedTableMap.put( propertyName, table );
		}

		public boolean containsProperty(String propertyName) {
			try {
				return getPropertyType( propertyName ) != null;
			}
			catch ( QueryException qe ) {
				return false;
			}
		}
	}

	public static class EntityTableSpace extends AbstractTableSpace {
		private final EntityPersisterSpace persisterSpace;

		public EntityTableSpace(Queryable entityPersister, TableAliasGenerator.TableAliasRoot aliasRoot) {
			super( aliasRoot );
			this.persisterSpace = new EntityPersisterSpace( this, entityPersister );
//			int numberOfTables = entityPersister.getMappedTableMetadata().getJoinedTables().length + 1;
//			int listSize = numberOfTables + (int) ( numberOfTables * .75 ) + 1;
//			this.tables = new ArrayList( listSize );

			PersisterTableExpressionGenerator.generateTableExpression(
					entityPersister,
					aliasRoot,
					this
			);
		}

		public PersisterSpace getPersisterSpace() {
			return persisterSpace;
		}

		public Queryable getEntityPersister() {
			return persisterSpace.getEntityPersister();
		}

		public Table getDrivingTable() {
			return tables.get( 0 );
		}

		public Table getJoinIntoTable() {
			return getDrivingTable();
		}

		public String[] getJoinIntoColumns() {
			return getEntityPersister().getIdentifierColumnNames();
		}

		public Table getContainingTable(String propertyName) {
			// todo : probably a better solution here is to iterate the internal collection of tables...
			return tables.get( getEntityPersister().getSubclassPropertyTableNumber( propertyName ) );
		}

		public Type getPropertyType(String propertyName) {
			return getEntityPersister().getPropertyType( propertyName );
		}

		public String[] getPropertyColumnNames(String propertyName) {
			return getEntityPersister().toColumns( propertyName );
		}

		public HibernateTree buildIdentifierColumnReferences() {
			HibernateTree columnList = new HibernateTree( HQLLexer.COLUMN_LIST );
			for ( String columnName : getEntityPersister().getIdentifierColumnNames() ) {
				HibernateTree columnNode = new HibernateTree( HQLLexer.COLUMN );
				columnNode.addChild( new HibernateTree( HQLLexer.ALIAS_REF, getDrivingTable().getAliasText() ) );
				columnNode.addChild( new HibernateTree( HQLLexer.IDENTIFIER, columnName ) );
				columnList.addChild( columnNode );
			}
			return columnList;
		}

		public HibernateTree buildCompleteColumnReferences() {
			// todo : implement
			return null;
		}
	}

	private static class EntityPersisterSpace extends AbstractPersisterSpace {
		private final EntityTableSpace correspondingTableSpace;
		private final Queryable entityPersister;
		private final String shortName;

		private EntityPersisterSpace(EntityTableSpace correspondingTableSpace, Queryable entityPersister) {
			this.correspondingTableSpace = correspondingTableSpace;
			this.entityPersister = entityPersister;
			this.shortName = StringHelper.unqualifyEntityName( entityPersister.getEntityName() );
		}

		public Queryable getEntityPersister() {
			return entityPersister;
		}

		public String getSourceAlias() {
			return correspondingTableSpace.getSourceAlias();
		}

		public String getName() {
			return entityPersister.getName();
		}

		public String getShortName() {
			return shortName;
		}

		public TableSpace getTableSpace() {
			return correspondingTableSpace;
		}

		public Type getPropertyType(String propertyName) {
			return entityPersister.getPropertyType( propertyName );
		}
	}

	public static class CollectionTableSpace extends AbstractTableSpace {
		private final CollectionPersisterSpace persisterSpace;

		private Table collectionTable;
		private EntityTableSpace entityElementTableSpace;

		public CollectionTableSpace(QueryableCollection persister, TableAliasGenerator.TableAliasRoot aliasRoot) {
			super( aliasRoot );
			this.persisterSpace = new CollectionPersisterSpace( this, persister );
			if ( persisterSpace.areElementsEntities ) {
				entityElementTableSpace = new EntityTableSpace( ( Queryable ) persister.getElementPersister(), aliasRoot );
			}
		}

		public QueryableCollection getCollectionPersister() {
			return persisterSpace.getCollectionPersister();
		}

		public PersisterSpace getPersisterSpace() {
			return persisterSpace;
		}

		public void setCollectionTable(Table collectionTable) {
			this.collectionTable = collectionTable;
		}

		public EntityTableSpace getEntityElementTableSpace() {
			return entityElementTableSpace;
		}

		public Table getJoinIntoTable() {
			return collectionTable;
		}

		public String[] getJoinIntoColumns() {
			return getCollectionPersister().getKeyColumnNames();
		}

		public Table getContainingTable(String propertyName) {
			// todo : are we needing to handle "collection properties" (SIZE, etc) here still?
			return getEntityElementTableSpace().getContainingTable( propertyName );
		}

		public Type getPropertyType(String propertyName) {
			return getEntityElementTableSpace().getPropertyType( propertyName );
		}

		public String[] getPropertyColumnNames(String propertyName) {
			return getEntityElementTableSpace().getPropertyColumnNames( propertyName );
		}

		public HibernateTree buildIdentifierColumnReferences() {
			// todo : implement
			return null;
		}

		public HibernateTree buildCompleteColumnReferences() {
			// todo : implement
			return null;
		}
	}

	public static class CollectionPersisterSpace extends AbstractPersisterSpace {
		private final CollectionTableSpace correspondingTableSpace;
		private final QueryableCollection collectionPersister;

		private final String shortName;
		private final boolean areElementsEntities;

		public CollectionPersisterSpace(CollectionTableSpace correspondingTableSpace, QueryableCollection collectionPersister) {
			this.correspondingTableSpace = correspondingTableSpace;
			this.collectionPersister = collectionPersister;
			this.shortName = StringHelper.unqualify( collectionPersister.getRole() );
			this.areElementsEntities = collectionPersister.getElementType().isEntityType();
		}

		public String getSourceAlias() {
			return correspondingTableSpace.getSourceAlias();
		}

		public QueryableCollection getCollectionPersister() {
			return collectionPersister;
		}

		public String getName() {
			return collectionPersister.getRole();
		}

		public String getShortName() {
			return shortName;
		}

		public TableSpace getTableSpace() {
			return correspondingTableSpace;
		}

		public Type getPropertyType(String propertyName) {
			return areElementsEntities
					? correspondingTableSpace.entityElementTableSpace.getPersisterSpace().getPropertyType( propertyName )
					: null;
		}
	}
}
