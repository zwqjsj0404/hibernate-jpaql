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

package org.hibernate.sql.ast.phase.hql.resolve;

import org.hibernate.persister.MappedTableMetadata;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.ast.alias.TableAliasGenerator;
import org.hibernate.sql.ast.common.HibernateTree;
import org.hibernate.sql.ast.phase.hql.parse.HQLParser;
import org.hibernate.sql.ast.tree.Table;

/**
 * Generate table expressions for persisters.
 * <p/>
 * NOTE : temporary, until such time as we are able to have the persisters themselves return these structures.
 *
 * @author Steve Ebersole
 */
public abstract class PersisterTableExpressionGenerator {

	public static Table generateTableExpression(
			Queryable persister,
			TableAliasGenerator.TableAliasRoot aliasRoot,
			Table.EntityTableSpace tableSpace) {
		MappedTableMetadata tableMetadata = persister.getMappedTableMetadata();

		final String drivingTableName = tableMetadata.getDrivingTableName();
		final String[] drivingTableJoinColumns = tableMetadata.getIdentifierColumnNames();
		final String drivingTableAlias = aliasRoot.generate( 0 );
		final Table drivingTable = generateTableReference( drivingTableName, drivingTableAlias, tableSpace );

		int suffix = 0;

		for ( MappedTableMetadata.JoinedTable joinedTable : tableMetadata.getJoinedTables() ) {
			final String joinTableAlias = aliasRoot.generate( ++suffix );
			final Table table = generateTableReference( joinedTable.getName(), joinTableAlias, tableSpace );

			final HibernateTree join = new HibernateTree( HQLParser.JOIN, "join" );
			drivingTable.addChild( join );
			if ( joinedTable.useInnerJoin() ) {
				join.addChild( new HibernateTree( HQLParser.INNER, "inner" ) );
			}
			else {
				join.addChild( new HibernateTree( HQLParser.LEFT, "left outer" ) );
			}
			join.addChild( table );

			final HibernateTree on = new HibernateTree( HQLParser.ON, "on" );
			join.addChild( on );
			final HibernateTree joinCondition = generateJoinCorrelation(
					drivingTableAlias,
					drivingTableJoinColumns,
					joinTableAlias,
					joinedTable.getKeyColumns()
			);
			on.addChild( joinCondition );
		}

		return drivingTable;
	}

	public static Table generateTableExpression(
			QueryableCollection collectionPersister,
			TableAliasGenerator.TableAliasRoot aliasRoot,
			Table.CollectionTableSpace tableSpace) {
		if ( collectionPersister.isOneToMany() ) {
			Table table = generateTableExpression(
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
					aliasRoot.generateCollectionTableAlias(),
					tableSpace
			);
			tableSpace.setCollectionTable( associationTable );

			if ( collectionPersister.isManyToMany() ) {
				Queryable elementPersister = ( Queryable ) collectionPersister.getElementPersister();
				Table drivingTable = generateTableExpression(
						elementPersister,
						aliasRoot,
						tableSpace.getEntityElementTableSpace()
				);

				final HibernateTree join = new HibernateTree( HQLParser.JOIN );
				associationTable.addChild( join );
				join.addChild( new HibernateTree( HQLParser.LEFT, "left outer" ) );
				join.addChild( drivingTable );

				String[] entityFkColumnNames = collectionPersister.getElementColumnNames();
				String[] entityPkColumnNames = elementPersister.getKeyColumnNames();

				final HibernateTree on = new HibernateTree( HQLParser.ON );
				join.addChild( on );
				final HibernateTree joinCondition = generateJoinCorrelation(
						associationTable.getAliasText(),
						entityFkColumnNames,
						drivingTable.getAliasText(),
						entityPkColumnNames
				);
				on.addChild( joinCondition );
			}
			return associationTable;
		}
	}

	private static Table generateTableReference(String tableName, String tableAlias, Table.TableSpace tableSpace) {
		Table table = new Table( HQLParser.TABLE, tableSpace );
		table.addChild( new HibernateTree( HQLParser.IDENTIFIER, tableName ) );
		table.addChild( new HibernateTree( HQLParser.ALIAS_NAME, tableAlias ) );
		return table;
	}

	public static HibernateTree generateJoinCorrelation(
			String lhsAlias,
			String[] lhsColumns,
			String rhsAlias,
			String[] rhsColumns) {
		HibernateTree correlation = generateJoinCorrelation( lhsAlias, lhsColumns[0], rhsAlias, rhsColumns[0] );
		if ( lhsColumns.length > 1 ) {
			for ( int i = 1; i < lhsColumns.length; i++ ) {
				HibernateTree previous = correlation;
				correlation = new HibernateTree( HQLParser.AND, "and" );
				correlation.addChild( previous );
				correlation.addChild( generateJoinCorrelation( lhsAlias, lhsColumns[i], rhsAlias, rhsColumns[i] ) );
			}
		}
		return correlation;
	}

	public static HibernateTree generateJoinCorrelation(String lhsAlias, String lhsColumn, String rhsAlias, String rhsColumn) {
		HibernateTree lhs = new HibernateTree( HQLParser.COLUMN );
		lhs.addChild( new HibernateTree( HQLParser.ALIAS_REF, lhsAlias ) );
		lhs.addChild( new HibernateTree( HQLParser.IDENTIFIER, lhsColumn ) );

		HibernateTree rhs = new HibernateTree( HQLParser.COLUMN );
		rhs.addChild( new HibernateTree( HQLParser.ALIAS_REF, rhsAlias ) );
		rhs.addChild( new HibernateTree( HQLParser.IDENTIFIER, rhsColumn ) );

		HibernateTree correlation = new HibernateTree( HQLParser.EQUALS, "=" );
		correlation.addChild( lhs );
		correlation.addChild( rhs );

		return correlation;
	}
}
