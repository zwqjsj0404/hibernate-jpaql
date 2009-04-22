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
import org.hibernate.sql.ast.phase.hql.parse.HQLLexer;
import org.hibernate.sql.ast.tree.Table;
import org.hibernate.sql.ast.util.TreePrinter;

/**
 * Generate table expressions for persisters.
 * <p/>
 * NOTE : temporary, until such time as we are able to have the persisters themselves return these structures.
 *
 * @author Steve Ebersole
 */
public abstract class PersisterTableExpressionGenerator {
	/**
	 * Generate the table expression for the given entity persister.
	 *
	 * @param persister The entity persister.
	 * @param aliasRoot The alias root for SQL alias generation.
	 * @param tableSpace The table space to which any generated table references need to belong.
	 *
	 * @return The generated table expression (could be simply the root table in a joined table structure).
	 */
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

			final HibernateTree join = new HibernateTree( HQLLexer.JOIN, "join" );
			drivingTable.addChild( join );
			if ( joinedTable.useInnerJoin() ) {
				join.addChild( new HibernateTree( HQLLexer.INNER, "inner" ) );
			}
			else {
				join.addChild( new HibernateTree( HQLLexer.LEFT, "left outer" ) );
			}
			join.addChild( table );

			final HibernateTree on = new HibernateTree( HQLLexer.ON, "on" );
			join.addChild( on );
			final HibernateTree joinCondition = generateJoinCorrelation(
					drivingTableAlias,
					drivingTableJoinColumns,
					joinTableAlias,
					joinedTable.getKeyColumns()
			);
			on.addChild( joinCondition );
		}

		// todo : temporary...
		System.out.println(
				new TreePrinter( HQLLexer.class ).renderAsString( drivingTable, "Generated table space" )
		);

		return drivingTable;
	}

	/**
	 * Generate the table expression for the given collection persister.
	 *
	 * @param collectionPersister The collection persister
	 * @param aliasRoot The alias root for SQL alias generation.
	 * @param tableSpace The table space to which any generated table references need to belong.
	 *
	 * @return The generated table expression (could be simply the root table in a joined table structure).
	 */
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

				final HibernateTree join = new HibernateTree( HQLLexer.JOIN );
				associationTable.addChild( join );
				join.addChild( new HibernateTree( HQLLexer.LEFT, "left outer" ) );
				join.addChild( drivingTable );

				String[] entityFkColumnNames = collectionPersister.getElementColumnNames();
				String[] entityPkColumnNames = elementPersister.getKeyColumnNames();

				final HibernateTree on = new HibernateTree( HQLLexer.ON );
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
		return new Table( tableName, tableAlias, tableSpace );
	}

	/**
	 * Creates a join correlation subtree (AST representing all the conditions on which the join occurs).
	 *
	 * @param lhsAlias The alias for the left-hand side (LHS) of the join
	 * @param lhsColumns The LHS columns
	 * @param rhsAlias The alias for the right-hand side (RHS) of the join
	 * @param rhsColumns The RHS columns
	 *
	 * @return The join correlation AST.
	 */
	public static HibernateTree generateJoinCorrelation(
			String lhsAlias,
			String[] lhsColumns,
			String rhsAlias,
			String[] rhsColumns) {
		HibernateTree correlation = generateJoinCorrelation( lhsAlias, lhsColumns[0], rhsAlias, rhsColumns[0] );
		if ( lhsColumns.length > 1 ) {
			for ( int i = 1; i < lhsColumns.length; i++ ) {
				HibernateTree previous = correlation;
				correlation = new HibernateTree( HQLLexer.AND, "and" );
				correlation.addChild( previous );
				correlation.addChild( generateJoinCorrelation( lhsAlias, lhsColumns[i], rhsAlias, rhsColumns[i] ) );
			}
		}
		return correlation;
	}

	/**
	 * Creates a join correlation subtree.  The difference here is that we have just a single column on each side.
	 *
	 * @param lhsAlias The alias for the left-hand side (LHS) of the join
	 * @param lhsColumn The LHS column
	 * @param rhsAlias The alias for the right-hand side (RHS) of the join
	 * @param rhsColumn The RHS column
	 *
	 * @return The join correlation AST.
	 */
	public static HibernateTree generateJoinCorrelation(String lhsAlias, String lhsColumn, String rhsAlias, String rhsColumn) {
		HibernateTree lhs = new HibernateTree( HQLLexer.COLUMN );
		lhs.addChild( new HibernateTree( HQLLexer.ALIAS_REF, lhsAlias ) );
		lhs.addChild( new HibernateTree( HQLLexer.IDENTIFIER, lhsColumn ) );

		HibernateTree rhs = new HibernateTree( HQLLexer.COLUMN );
		rhs.addChild( new HibernateTree( HQLLexer.ALIAS_REF, rhsAlias ) );
		rhs.addChild( new HibernateTree( HQLLexer.IDENTIFIER, rhsColumn ) );

		HibernateTree correlation = new HibernateTree( HQLLexer.EQUALS, "=" );
		correlation.addChild( lhs );
		correlation.addChild( rhs );

		return correlation;
	}
}
