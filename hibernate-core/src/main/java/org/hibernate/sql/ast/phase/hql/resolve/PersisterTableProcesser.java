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
package org.hibernate.sql.ast.phase.hql.resolve;

import antlr.ASTFactory;
import antlr.collections.AST;

import org.hibernate.persister.entity.Queryable;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.MappedTableMetadata;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.sql.ast.alias.TableAliasGenerator;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public class PersisterTableProcesser implements HqlResolveTokenTypes {
	private final ASTFactory astFactory;

	public PersisterTableProcesser(ASTFactory astFactory) {
		this.astFactory = astFactory;
	}

	public Table buildTables(Queryable persister, TableAliasGenerator.TableAliasRoot aliasRoot, Table.EntityTableSpace tableSpace) {
		MappedTableMetadata tableMetadata = persister.getMappedTableMetadata();

		final String drivingTableName = tableMetadata.getDrivingTableName();
		final String[] drivingTableJoinColumns = tableMetadata.getIdentifierColumnNames();
		final String drivingTableAlias = aliasRoot.generate( 0 );
		final Table drivingTable = generateTableReference( drivingTableName, drivingTableAlias );
		tableSpace.registerTable( drivingTable );

		int suffix = 0;

		MappedTableMetadata.JoinedTable[] tables = tableMetadata.getJoinedTables();
		for ( int i = 0; i < tables.length; i++ ) {
			final String joinTableAlias = aliasRoot.generate( ++suffix );
			final Table table = generateTableReference( tables[i].getName(), joinTableAlias );
			tableSpace.registerTable( table );

			final AST join = astFactory.create( JOIN, "join" );
			drivingTable.addChild( join );
			join.setFirstChild( tables[i].useInnerJoin() ? astFactory.create( INNER, "inner" ) : astFactory.create(  LEFT, "left outer" ) );
			join.addChild( table );

			final AST on = astFactory.create( ON, "on" );
			join.addChild( on );
			on.setFirstChild( generateJoinCorrelation( drivingTableAlias, drivingTableJoinColumns, joinTableAlias, tables[i].getKeyColumns() ) );
		}

		return drivingTable;
	}

	public Table buildTables(
			QueryableCollection collectionPersister,
			TableAliasGenerator.TableAliasRoot aliasRoot,
			Table.CollectionTableSpace tableSpace) {
		if ( collectionPersister.isOneToMany() ) {
			Table table = buildTables(
					( Queryable ) collectionPersister.getElementPersister(),
					aliasRoot,
					tableSpace.getEntityElementTableSpace()
			);
			tableSpace.setCollectionTable( table );
			return table;
		}
		else {
			Table associationTable = generateTableReference(
					collectionPersister.getTableName(),
					aliasRoot.generateCollectionTableAlias()
			);
			tableSpace.setCollectionTable( associationTable );

			if ( collectionPersister.isManyToMany() ) {
				Queryable elementPersister = ( Queryable ) collectionPersister.getElementPersister();
				Table drivingTable = buildTables(
						elementPersister,
						aliasRoot,
						tableSpace.getEntityElementTableSpace()
				);

				AST join = astFactory.create( JOIN, "join" );
				associationTable.addChild( join );
				join.addChild( astFactory.create( LEFT, "left outer" ) );
				join.addChild( drivingTable );

				String[] entityFkColumnNames = collectionPersister.getElementColumnNames();
				String[] entityPkColumnNames = elementPersister.getKeyColumnNames();

				AST on = astFactory.create( ON, "on" );
				join.addChild( on );
				on.setFirstChild(
						generateJoinCorrelation(
								associationTable.getAliasText(),
								entityFkColumnNames,
								drivingTable.getAliasText(),
								entityPkColumnNames
						)
				);
			}
			return associationTable;
		}
	}

	public AST handleCollectionFilterConditions(
			Table drivingEntityTable,
			TableAliasGenerator.TableAliasRoot aliasRoot,
			QueryableCollection filteredCollectionPersister) {
		Table collectionKeyTable = drivingEntityTable;

		if ( !filteredCollectionPersister.isOneToMany() ) {
			// need to deal with the collection table, which is where the collection-key would be...
			Table associationTable = generateTableReference(
					filteredCollectionPersister.getTableName(),
					aliasRoot.generateCollectionTableAlias()
			);

			String[] entityFkColumnNames = filteredCollectionPersister.getElementColumnNames();
			String[] entityPkColumnNames = ( ( Joinable ) filteredCollectionPersister.getElementPersister() ).getKeyColumnNames();

			AST join = astFactory.create( JOIN, "join" );
			drivingEntityTable.addChild( join );
			join.addChild( astFactory.create( LEFT, "left outer" ) );
			join.addChild( associationTable );

			AST on = astFactory.create( ON, "on" );
			join.addChild( on );
			on.setFirstChild(
					generateJoinCorrelation(
							drivingEntityTable.getAliasText(),
							entityPkColumnNames,
							associationTable.getAliasText(),
							entityFkColumnNames
					)
			);

			collectionKeyTable = associationTable;
		}

		return generateCollectionFilterRestriction(
				collectionKeyTable.getAliasText(),
				filteredCollectionPersister.getKeyColumnNames()
		);
	}

	private Table generateTableReference(String tableName, String alias) {
		Table tableReference = ( Table ) astFactory.create( TABLE, "table" );
		tableReference.addChild( astFactory.create( NAME, tableName ) );
		tableReference.addChild( astFactory.create( ALIAS, alias ) );
		return tableReference;
	}

	public AST generateJoinCorrelation(
			String lhsAlias,
			String[] lhsColumns,
			String rhsAlias,
			String[] rhsColumns) {
		AST correlation = generateJoinCorrelation( lhsAlias, lhsColumns[0], rhsAlias, rhsColumns[0] );
		if ( lhsColumns.length > 1 ) {
			for ( int i = 1; i < lhsColumns.length; i++ ) {
				AST previous = correlation;
				correlation = astFactory.create( AND, "and" );
				correlation.setFirstChild( previous );
				correlation.addChild( generateJoinCorrelation( lhsAlias, lhsColumns[i], rhsAlias, rhsColumns[i] ) );
			}
		}
		return correlation;
	}

	public AST generateJoinCorrelation(String lhsAlias, String lhsColumn, String rhsAlias, String rhsColumn) {
		AST lhs = astFactory.create( COLUMN, "column" );
		lhs.addChild( astFactory.create( ALIAS_REF, lhsAlias ) );
		lhs.addChild( astFactory.create( NAME, lhsColumn ) );

		AST rhs = astFactory.create( COLUMN, "column" );
		rhs.addChild( astFactory.create( ALIAS_REF, rhsAlias ) );
		rhs.addChild( astFactory.create( NAME, rhsColumn ) );

		AST correlation = astFactory.create( EQUALS_OP, "=" );
		correlation.addChild( lhs );
		correlation.addChild( rhs );

		return correlation;
	}

	private AST generateCollectionFilterRestriction(String alias, String[] keyColumns) {
		// todo : for now we restrict this to single-column FKs
		//		Looks like this is true of old code as well...

		AST column = astFactory.create( COLUMN, "column" );
		column.addChild( astFactory.create( ALIAS_REF, alias ) );
		column.addChild( astFactory.create( NAME, keyColumns[0] ) );

		AST param = astFactory.create( NAMED_PARAM, "collection-filter-key" );	// todo : put this in a well known place

		AST correlation = astFactory.create( EQUALS_OP, "=" );
		correlation.addChild( column );
		correlation.addChild( param );

		return correlation;
	}

}
