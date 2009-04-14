package org.hibernate.sql.ast.phase.hql.resolve;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sql.ast.common.Node;
import org.hibernate.sql.ast.alias.ImplicitAliasGenerator;
import org.hibernate.sql.ast.util.DisplayableNode;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.util.StringHelper;
import org.hibernate.type.Type;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public class Table extends Node implements DisplayableNode {
	private TableSpace tableSpace;

	public Table() {
		setType( HqlResolveTokenTypes.TABLE );
		setText( "table" );
	}

	public TableSpace getTableSpace() {
		return tableSpace;
	}

	public Node getTableName() {
		return ( Node ) getFirstChild();
	}

	public String getTableNameText() {
		return getTableName().getText();
	}

	public Node getAlias() {
		return ( Node ) getFirstChild().getNextSibling();
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

	public static interface TableSpace {
		/**
		 * Used as a unique identification since each table space originates from a single source alias (persister reference).
		 *
		 * @return The source alias.
		 */
		public String getSourceAlias();

		/**
		 * Get the table reference that should act as the RHS for this table space whenever we join to into it.
		 *
		 * @return The RHS table for joining into this table space structure.
		 */
		public Table getJoinIntoTable();

		public String[] getJoinIntoColumns();

		/**
		 * Get the table reference that contains the columns to which the given property is mapped.
		 *
		 * @param propertyName The name of the property for which to locate the containing table.
		 *
		 * @return The containing table.
		 */
		public Table getContainingTable(String propertyName);

		public Type getPropertyType(String propertyName);

		public String[] getPropertyColumnNames(String propertyName);

		public List buildIdentifierColumnReferences();

		public List buildCompleteColumnReferences();
	}

	public static abstract class AbstractTableSpace implements Table.TableSpace {
		private final String sourceAlias;
		private final String sqlAliasBaseRoot;

		private AbstractTableSpace(String sourceAlias, String persisterName) {
			this.sourceAlias = sourceAlias;
			this.sqlAliasBaseRoot = ImplicitAliasGenerator.isImplicitAlias( sourceAlias ) ? persisterName : sourceAlias;
		}

		public String getSourceAlias() {
			return sourceAlias;
		}

		public String getSqlAliasBaseRoot() {
			return sqlAliasBaseRoot;
		}
	}

	public static class EntityTableSpace extends AbstractTableSpace {
		private final Queryable entityPersister;
		private final ArrayList tables;

		public EntityTableSpace(Queryable entityPersister, String sourecAlias) {
			super( sourecAlias, StringHelper.unqualifyEntityName( entityPersister.getEntityName() ) );
			this.entityPersister = entityPersister;
			int numberOfTables = entityPersister.getMappedTableMetadata().getJoinedTables().length + 1;
			int listSize = numberOfTables + (int) ( numberOfTables * .75 ) + 1;
			this.tables = new ArrayList( listSize );
		}

		public Queryable getEntityPersister() {
			return entityPersister;
		}

		public void registerTable(Table table) {
			table.tableSpace = this;
			tables.add( table );
		}

		public Table getDrivingTable() {
			return ( Table ) tables.get( 0 );
		}

		public Table getJoinIntoTable() {
			return getDrivingTable();
		}

		public String[] getJoinIntoColumns() {
			return entityPersister.getIdentifierColumnNames();
		}

		public Table getContainingTable(String propertyName) {
			return ( Table ) tables.get( entityPersister.getSubclassPropertyTableNumber( propertyName ) );
		}

		public Type getPropertyType(String propertyName) {
			return entityPersister.getPropertyType( propertyName );
		}

		public String[] getPropertyColumnNames(String propertyName) {
			int index = entityPersister.getEntityMetamodel().getPropertyIndex( propertyName );
			return entityPersister.getPropertyColumnNames( index );
		}

		public List buildIdentifierColumnReferences() {
			String[] identifierColumnsNames = entityPersister.getIdentifierColumnNames();
			ArrayList columnsReferences = new ArrayList( collectionSizeWithoutRehashing( identifierColumnsNames.length ) );
			for ( int i = 0; i < identifierColumnsNames.length; i++ ) {
				columnsReferences.add( new Column( getDrivingTable().getAliasText(), identifierColumnsNames[i] ) );
			}
			return columnsReferences;
		}

		public List buildCompleteColumnReferences() {
			// todo : implement
			return null;
		}
	}

	private static int collectionSizeWithoutRehashing(int elements) {
		// usually collection load factors are .75
		return collectionSizeWithoutRehashing( elements, .75 );
	}

	private static int collectionSizeWithoutRehashing(int elements, double factor) {
		return elements + ( (int) ( elements * factor ) + 1 );
	}

	public static class CollectionTableSpace extends AbstractTableSpace {
		private final QueryableCollection persister;
		private final boolean areElementsEntities;

		private Table collectionTable;
		private EntityTableSpace entityElementTableSpace;

		public CollectionTableSpace(QueryableCollection persister, String sourceAlias) {
			super( sourceAlias, StringHelper.unqualify( persister.getRole() ) );
			this.persister = persister;
			this.areElementsEntities = persister.getElementType().isEntityType();
			if ( areElementsEntities ) {
				entityElementTableSpace = new EntityTableSpace( ( Queryable ) persister.getElementPersister(), sourceAlias );
			}
		}

		public QueryableCollection getPersister() {
			return persister;
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
			return persister.getKeyColumnNames();
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

		public List buildIdentifierColumnReferences() {
			// todo : implement
			return null;
		}

		public List buildCompleteColumnReferences() {
			// todo : implement
			return null;
		}
	}
}
