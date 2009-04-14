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

import java.util.HashMap;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.collections.AST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.ast.alias.DefaultTableAliasGenerator;
import org.hibernate.sql.ast.util.ASTPrinter;
import org.hibernate.sql.ast.util.ErrorHandlerDelegateImpl;
import org.hibernate.sql.ast.util.ErrorHandlerDelegate;
import org.hibernate.sql.ast.alias.TableAliasGenerator;
import org.hibernate.sql.ast.phase.hql.resolve.expression.ExpressionResolutionStrategyStack;
import org.hibernate.sql.ast.phase.hql.resolve.expression.DefaultExpressionResolutionStrategy;
import org.hibernate.sql.ast.phase.hql.resolve.expression.SelectClauseExpressionResolutionStrategy;
import org.hibernate.sql.ast.phase.hql.resolve.expression.FunctionArgumentExpressionResolutionStrategy;
import org.hibernate.type.AssociationType;
import org.hibernate.util.StringHelper;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public class HqlResolver extends GeneratedHqlResolver {
	private static final Logger log = LoggerFactory.getLogger( HqlResolver.class );

	private final SessionFactoryImplementor sessionFactory;
	private final ErrorHandlerDelegate parseErrorHandler = new ErrorHandlerDelegateImpl();
	private final ASTPrinter printer = new ASTPrinter( HqlResolveTokenTypes.class );

	private final TableAliasGenerator tableAliasGenerator;
	private final PersisterTableProcesser persisterTableProcesser;

	private ExpressionResolutionStrategyStack expressionResolverStack = new ExpressionResolutionStrategyStack();

	private AST collectionFilterCondition;

	public HqlResolver(SessionFactoryImplementor sessionFactory) {
		super();
		super.setASTFactory( new ASTFactoryImpl() );
		this.sessionFactory = sessionFactory;
		// todo : make this configurable
		this.tableAliasGenerator = new DefaultTableAliasGenerator( sessionFactory.getDialect() );
		this.persisterTableProcesser = new PersisterTableProcesser( getASTFactory() );
		expressionResolverStack.push( new DefaultExpressionResolutionStrategy( this ) );
	}

	public ASTPrinter getPrinter() {
		return printer;
	}


	// overrides of Antlr infastructure methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void reportError(RecognitionException e) {
		getParseErrorHandler().reportError( e );
	}

	public void reportError(String s) {
		getParseErrorHandler().reportError( s );
	}

	public void reportWarning(String s) {
		getParseErrorHandler().reportWarning( s );
	}

	public ErrorHandlerDelegate getParseErrorHandler() {
		return parseErrorHandler;
	}

	static public void panic() {
		//overriden to avoid System.exit
		throw new QueryException( "Parser: panic" );
	}

	public void setASTFactory(ASTFactory astFactory) {
		throw new UnsupportedOperationException( "not allowed!" );
	}


	// handle trace logging ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void traceIn(String ruleName, AST tree) {
		if ( inputState.guessing > 0 ) {
			return;
		}
		String prefix = StringHelper.repeat( '-', (traceDepth++ * 2) ) + "-> ";
		String traceText = ruleName + " (" + buildTraceNodeName(tree) + ")";
		trace( prefix + traceText );
	}

	private String buildTraceNodeName(AST tree) {
		return tree == null
				? "???"
				: tree.getText() + " [" + printer.getTokenTypeName( tree.getType() ) + "]";
	}

	public void traceOut(String ruleName, AST tree) {
		if ( inputState.guessing > 0 ) {
			return;
		}
		String prefix = "<-" + StringHelper.repeat( '-', (--traceDepth * 2) ) + " ";
		trace( prefix + ruleName );
	}

	private void trace(String msg) {
		System.out.println( msg );
//		log.trace( msg );
	}


	// semantic action overrides ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private HashMap aliasToTableSpaceMap = new HashMap();

	public Table.TableSpace resolveTableSpaceByAlias(String alias) {
		return ( Table.TableSpace ) aliasToTableSpaceMap.get( alias );
	}

	protected AST resolveEntityPersister(AST entityName, AST alias, AST filter) {
		final Queryable persister = ( Queryable ) sessionFactory.getEntityPersister( entityName.getText() );
		final String aliasText = alias.getText();

		Table.EntityTableSpace tableSpace = new Table.EntityTableSpace( persister, aliasText );
		aliasToTableSpaceMap.put( aliasText, tableSpace );

		TableAliasGenerator.TableAliasRoot aliasRoot = tableAliasGenerator.generateSqlAliasRoot( persister, aliasText );

		Table table = persisterTableProcesser.buildTables(
				persister,
				aliasRoot,
				tableSpace
		);

		if ( filter != null ) {
			String filteredCollectionRole = filter.getText();
			QueryableCollection filteredCollectionPersister =
					( QueryableCollection ) sessionFactory.getCollectionPersister( filteredCollectionRole );
			collectionFilterCondition = persisterTableProcesser.handleCollectionFilterConditions(
					table,
					aliasRoot,
					filteredCollectionPersister
			);
		}

		return table;
	}

    protected void postProcessQuery(AST query) {
		if ( collectionFilterCondition != null ) {
			AST selectClause = query.getFirstChild();
			AST fromClause = selectClause.getNextSibling();
			AST whereClause = fromClause.getNextSibling();
			if ( whereClause == null ) {
				// there was no where-clause, group-by-clause or having-clause...
				whereClause = astFactory.create( WHERE, "where" );
				fromClause.setNextSibling( whereClause );
			}
			else if ( whereClause.getType() == GROUP_BY ) {
				// there was no where-clause, but was a group-by-clause
				AST groupByClause = whereClause;
				whereClause = astFactory.create( WHERE, "where" );
				whereClause.setNextSibling( groupByClause );
				fromClause.setNextSibling( whereClause );
			}
			assert whereClause.getType() == WHERE : "mis-structured AST";
			log.trace( printer.showAsString( collectionFilterCondition, "collection-filter key condition" ) );
			appendSearchCondition( collectionFilterCondition, whereClause );
			collectionFilterCondition = null;
		}
	}

	protected AST resolveCollectionPersister(AST collectionRole, AST alias) {
		return super.resolveCollectionPersister( collectionRole, alias );
	}

    protected void applyPropertyJoin(AST lhs, AST rhs, AST propertyName, AST joinType, AST with) {
		final AST correlation = generateAssociationJoinCondition( lhs, rhs, propertyName );
		AST on = astFactory.create( ON, "on" );
		if ( with == null ) {
			on.addChild( correlation );
		}
		else {
			AST and = astFactory.create( AND, "and" );
			and.addChild( correlation );
			and.addChild( with );
			on.addChild( and );
		}

		AST join = astFactory.create( JOIN, "join" );
		join.addChild( joinType );
		join.addChild( rhs );
		join.addChild( on );

		lhs.addChild( join );
	}

	private AST generateAssociationJoinCondition(AST lhs, AST rhs, AST propertyName) {
		final String propertyNameText = propertyName.getText();
		Table lhsTable = ( Table ) lhs;
		Table.TableSpace lhsTableSpace = lhsTable.getTableSpace();
		String lhsTableAlias = lhsTableSpace.getContainingTable( propertyNameText ).getAliasText();
		String[] lhsJoinColumns = lhsTableSpace.getPropertyColumnNames( propertyNameText );

		AssociationType propertyType = ( AssociationType ) lhsTableSpace.getPropertyType( propertyNameText );
		String rhsPropertyName = propertyType.getRHSUniqueKeyPropertyName();
		Table.TableSpace rhsTableSpace = ( ( Table ) rhs ).getTableSpace();
		String rhsTableAlias;
		String[] rhsJoinColumns;
		if ( rhsPropertyName == null ) {
			// reference to RHS PK
			rhsTableAlias = rhsTableSpace.getJoinIntoTable().getAliasText();
			rhsJoinColumns = rhsTableSpace.getJoinIntoColumns();
		}
		else {
			// reference to RHS property-ref
			Table rhsTable = rhsTableSpace.getContainingTable( rhsPropertyName );
			rhsTableAlias = rhsTable.getAliasText();
			rhsJoinColumns = rhsTableSpace.getPropertyColumnNames( rhsPropertyName );
		}

		if ( lhsJoinColumns.length != rhsJoinColumns.length ) {
			throw new MappingException( "Association had unequal number of columns : " + propertyType.getName() );
		}

		return persisterTableProcesser.generateJoinCorrelation( lhsTableAlias, lhsJoinColumns, rhsTableAlias, rhsJoinColumns );
	}

	protected AST generateIndexValueCondition(AST lhs, AST rhs, AST propertyName, AST selector) {
		return super.generateIndexValueCondition( lhs, rhs, propertyName, selector );
	}

	protected void startSelectClause() {
		expressionResolverStack.push( new SelectClauseExpressionResolutionStrategy( this ) );
	}

	protected void finishSelectClause() {
		expressionResolverStack.pop();
	}

	protected void startFunction() {
		expressionResolverStack.push( new FunctionArgumentExpressionResolutionStrategy( this ) );
	}

	protected void finishFunction() {
		expressionResolverStack.pop();
	}

	protected void appendSearchCondition(AST condition, AST container) {
		if ( container.getFirstChild() == null ) {
			container.setFirstChild( condition );
		}
		else {
			AST and = getASTFactory().create( AND, "and" );
			and.setFirstChild( container.getFirstChild() );
			and.addChild( condition );
			container.setFirstChild( and );
		}
	}

    protected AST resolvePropertyReference(AST persisterAlias, AST propertyName) {
		final String aliasText = persisterAlias.getText();
		final String propertyNameText = propertyName.getText();

		return expressionResolverStack.getCurrent().resolvePropertyReference( aliasText, propertyNameText );
	}

	protected AST resolveAliasReference(AST aliasReference) {
		return expressionResolverStack.getCurrent().resolveAliasReference( aliasReference.getText() );
	}
}
