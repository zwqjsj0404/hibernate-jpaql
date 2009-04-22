/*
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

import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.TreeNodeStream;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.ast.alias.DefaultTableAliasGenerator;
import org.hibernate.sql.ast.alias.TableAliasGenerator;
import org.hibernate.sql.ast.common.JoinType;
import org.hibernate.sql.ast.phase.hql.parse.HQLParser;
import org.hibernate.sql.ast.phase.hql.resolve.path.PathResolutionStrategy;
import org.hibernate.sql.ast.phase.hql.resolve.path.PathResolutionStrategyStack;
import org.hibernate.sql.ast.phase.hql.resolve.path.PathedPropertyReferenceSource;
import org.hibernate.sql.ast.phase.hql.resolve.path.impl.BasicPathResolutionStrategySupport;
import org.hibernate.sql.ast.phase.hql.resolve.path.impl.FromClausePathResolutionStrategy;
import org.hibernate.sql.ast.tree.Table;
import org.hibernate.sql.ast.tree.Table.EntityTableSpace;
import org.hibernate.sql.ast.util.TreePrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HQLResolution extends GeneratedHQLResolution implements
		ResolutionContext {
	private static final Logger log = LoggerFactory
			.getLogger( HQLResolution.class );

	private final SessionFactoryImplementor sessionFactory;
	private final PersisterSpaceContext persisterSpaceContext;
	private final DefaultTableAliasGenerator defaultTableAliasGenerator;
	private final PathResolutionStrategyStack pathResolutionStrategyStack;
	private final TreePrinter printer;

	private boolean isProcessingFunction = false;

	public HQLResolution( TreeNodeStream input,
			SessionFactoryImplementor sessionFactory ) {
		this( input, new RecognizerSharedState(), sessionFactory );
	}

	public HQLResolution( TreeNodeStream input, RecognizerSharedState state,
			SessionFactoryImplementor sessionFactory ) {
		super( input, state );
		this.sessionFactory = sessionFactory;
		this.persisterSpaceContext = new RootPersisterSpaceContext();
		this.defaultTableAliasGenerator = new DefaultTableAliasGenerator(
				sessionFactory.getDialect() );
		this.printer = new TreePrinter( HQLParser.class );
		this.pathResolutionStrategyStack = new PathResolutionStrategyStack();
		this.pathResolutionStrategyStack
				.push( new BasicPathResolutionStrategySupport( this ) );
	}

	protected void registerPersisterSpace( CommonTree entityName,
			CommonTree alias ) {
		String entityPersisterName = sessionFactory
				.getImportedClassName( entityName.getText() );
		Queryable entityPersister = ( Queryable ) sessionFactory
				.getEntityPersister( entityPersisterName );

		TableAliasGenerator.TableAliasRoot tableAliasRoot = getTableAliasGenerator()
				.generateSqlAliasRoot( entityPersister, alias.getText() );
		EntityTableSpace tableSpace = new Table.EntityTableSpace(
				entityPersister, tableAliasRoot );
		registerPersisterSpace( tableSpace.getPersisterSpace() );
	}

	public PersisterSpaceContext getCurrentPersisterSpaceContext() {
		return persisterSpaceContext;
	}

	public SessionFactoryImplementor getSessionFactoryImplementor() {
		return sessionFactory;
	}

	public TableAliasGenerator getTableAliasGenerator() {
		return defaultTableAliasGenerator;
	}

	public TreePrinter getTreePrinter() {
		return printer;
	}

	public boolean isCurrentlyProcessingFunction() {
		return isProcessingFunction;
	}

	public PathResolutionStrategyStack getPathResolutionStrategyStack() {
		return pathResolutionStrategyStack;
	}

	public PathResolutionStrategy getCurrentPathResolutionStrategy() {
		return pathResolutionStrategyStack.getCurrent();
	}

	public void registerAssociationFetch( PersisterSpace persisterSpace ) {
		// TODO Auto-generated method stub
		persisterSpaceContext.registerPersisterSpace( persisterSpace );
	}

	public void registerPropertyFetch( PersisterSpace persisterSpace ) {
		// TODO Auto-generated method stub
	}

	protected void pushFromStrategy( JoinType joinType,
			CommonTree assosiationFetchTree, CommonTree propertyFetchTree,
			CommonTree alias ) {
		boolean assosiationFetch = assosiationFetchTree != null ? true : false;
		boolean propertyFetch = propertyFetchTree != null ? true : false;
		pathResolutionStrategyStack.push( new FromClausePathResolutionStrategy(
				this, joinType, assosiationFetch, propertyFetch, alias
						.getText() ) );
	}

	protected void popStrategy() {
		pathResolutionStrategyStack.pop();
	}

	protected void registerEntityPersisterSpace( CommonTree entityName,
			CommonTree alias ) {
		// String entityPersisterName = sessionFactory
		// .getImportedClassName( entityName.getText() );
		// EntityPersister entityPersister = sessionFactory
		// .getEntityPersister( entityPersisterName );
		//
		// EntityTableSpace tableSpace = new Table.EntityTableSpace(
		// ( Queryable ) entityPersister, getTableAliasGenerator()
		// .generateSqlAliasRoot( ( Queryable ) entityPersister,
		// alias.getText() ) );
		// registerPersisterSpace( tableSpace.getPersisterSpace() );
	}

	private void registerPersisterSpace( PersisterSpace persisterSpace ) {
		persisterSpaceContext.registerPersisterSpace( persisterSpace );
	}

	protected boolean isUnqualifiedPropertyReference() {
		return locateOwningPersisterAlias( ( Tree ) input.LT( 1 ) ) != null;
	}

	protected String locateOwningPersisterAlias( Tree property ) {
		PersisterSpace persisterReference = getCurrentPersisterSpaceContext()
				.locatePersisterSpaceExposingProperty( property.getText() );
		return persisterReference == null ? null : persisterReference
				.getSourceAlias();
	}

	protected boolean isPersisterReferenceAlias() {
		Tree alias = ( Tree ) input.LT( 1 );
		log.trace( "Checking [" + textOrNull( alias )
				+ "] as persister-ref alias" );
		return getCurrentPersisterSpaceContext().isContainedAlias(
				alias.getText() );
	}

	protected Tree normalizeUnqualifiedPropertyReference( CommonTree property ) {
		// TODO Auto-generated method stub
		return null;
	}

	protected PathedPropertyReferenceSource normalizeUnqualifiedRoot(
			CommonTree identifier ) {
		System.out
				.println( "normalizeUnqualifiedRoot: " + identifier.getText() );
		// TODO Auto-generated method stub
		return null;
	}

	protected PathedPropertyReferenceSource normalizeQualifiedRoot(
			CommonTree alias ) {
		log.debug( "normalizing path expression root as alias ["
				+ alias.getText() + "]" );

		return getCurrentPathResolutionStrategy().handleRoot(
				getCurrentPersisterSpaceContext().locatePersisterSpaceByAlias(
						alias.getText() ) );
	}

	protected void normalizePropertyPathTerminus(
			PathedPropertyReferenceSource source, CommonTree propertyNameNode ) {
		log.trace( "normalizing terminal path expression ["
				+ textOrNull( propertyNameNode ) + "]" );
		getCurrentPathResolutionStrategy().handleTerminalPathPart( source,
				propertyNameNode.getText() );
	}

	protected void normalizePropertyPathIntermediary( CommonTree identifier1,
			CommonTree identifier2 ) {
		System.out.println( "normalizePropertyPathIntermediary: "
				+ identifier1.getText() + ":" + identifier2.getText() );
		// TODO Auto-generated method stub
	}

	protected void normalizeIntermediateIndexOperation( CommonTree identifier1,
			CommonTree identifier2 ) {
		System.out.println( "normalizeIntermediateIndexOperation: "
				+ identifier1.getText() + ":" + identifier2.getText() );
		// TODO Auto-generated method stub
	}

	protected void normalizeUnqualifiedPropertyReferenceSource(
			CommonTree identifier ) {
		System.out.println( "normalizeUnqualifiedPropertyReferenceSource: "
				+ identifier.getText() );
		// TODO Auto-generated method stub
	}

	protected void normalizeTerminalIndexOperation( CommonTree collectionPath,
			CommonTree selector ) {
		// TODO Auto-generated method stub
	}

	private String textOrNull( Tree tree ) {
		return tree == null ? null : tree.getText();
	}

}