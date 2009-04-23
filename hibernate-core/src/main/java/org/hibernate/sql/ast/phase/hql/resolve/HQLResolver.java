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
import org.hibernate.sql.ast.common.HibernateTree;
import org.hibernate.sql.ast.common.JoinType;
import org.hibernate.sql.ast.phase.hql.parse.HQLParser;
import org.hibernate.sql.ast.phase.hql.resolve.path.PathResolutionStrategy;
import org.hibernate.sql.ast.phase.hql.resolve.path.PathResolutionStrategyStack;
import org.hibernate.sql.ast.phase.hql.resolve.path.PathedPropertyReferenceSource;
import org.hibernate.sql.ast.phase.hql.resolve.path.impl.BasicPathResolutionStrategySupport;
import org.hibernate.sql.ast.phase.hql.resolve.path.impl.FromClausePathResolutionStrategy;
import org.hibernate.sql.ast.phase.hql.resolve.path.impl.SelectClausePathResolutionStrategy;
import org.hibernate.sql.ast.tree.Table;
import org.hibernate.sql.ast.tree.Table.EntityTableSpace;
import org.hibernate.sql.ast.util.TreePrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HQLResolver extends GeneratedHQLResolver implements
		ResolutionContext {
	private static final Logger log = LoggerFactory
			.getLogger( HQLResolver.class );

	private final SessionFactoryImplementor sessionFactory;
	private final PersisterSpaceContext persisterSpaceContext;
	private final DefaultTableAliasGenerator defaultTableAliasGenerator;
	private final PathResolutionStrategyStack pathResolutionStrategyStack;
	private final TreePrinter printer;

	private boolean isProcessingFunction = false;

	public HQLResolver( TreeNodeStream input,
			SessionFactoryImplementor sessionFactory ) {
		this( input, new RecognizerSharedState(), sessionFactory );
	}

	public HQLResolver( TreeNodeStream input, RecognizerSharedState state,
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

	protected void registerPersisterSpace( Tree entityName, Tree alias ) {
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
		throw new UnsupportedOperationException( "must be implemented!" );
	}

	public void registerPropertyFetch( PersisterSpace persisterSpace ) {
		throw new UnsupportedOperationException( "must be implemented!" );
	}

	protected void pushFromStrategy( JoinType joinType,
			Tree assosiationFetchTree, Tree propertyFetchTree, Tree alias ) {
		boolean assosiationFetch = assosiationFetchTree != null ? true : false;
		boolean propertyFetch = propertyFetchTree != null ? true : false;
		pathResolutionStrategyStack.push( new FromClausePathResolutionStrategy(
				this, joinType, assosiationFetch, propertyFetch, alias
						.getText() ) );
	}

	protected void pushSelectStrategy() {
		pathResolutionStrategyStack
				.push( new SelectClausePathResolutionStrategy( this ) );
	}

	protected void popStrategy() {
		pathResolutionStrategyStack.pop();
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

	protected PathedPropertyReferenceSource normalizeUnqualifiedPropertyReference(
			Tree property ) {
		return getCurrentPathResolutionStrategy().handleRoot(
				getCurrentPersisterSpaceContext()
						.locatePersisterSpaceExposingProperty(
								property.getText() ) );
	}

	protected PathedPropertyReferenceSource normalizeUnqualifiedRoot(
			Tree propertyName ) {
		log.debug( "normalizing path expression root as unqualified property ["
				+ textOrNull( propertyName ) + "]" );
		PathedPropertyReferenceSource root = getCurrentPathResolutionStrategy()
				.handleRoot(
						getCurrentPersisterSpaceContext()
								.locatePersisterSpaceExposingProperty(
										propertyName.getText() ) );
		return root.handleIntermediatePathPart( propertyName.getText() );
	}

	protected PathedPropertyReferenceSource normalizeQualifiedRoot( Tree alias ) {
		log.debug( "normalizing path expression root as alias ["
				+ alias.getText() + "]" );
		return getCurrentPathResolutionStrategy().handleRoot(
				getCurrentPersisterSpaceContext().locatePersisterSpaceByAlias(
						alias.getText() ) );
	}

	protected Tree normalizePropertyPathTerminus(
			PathedPropertyReferenceSource source, Tree propertyNameNode ) {
		log.trace( "normalizing terminal path expression ["
				+ textOrNull( propertyNameNode ) + "]" );
		return getCurrentPathResolutionStrategy().handleTerminalPathPart(
				source, propertyNameNode.getText() );
	}

	protected PathedPropertyReferenceSource normalizePropertyPathIntermediary(
			PathedPropertyReferenceSource source, Tree propertyName ) {
		log.trace( "normalizing intermediate path expression ["
				+ textOrNull( propertyName ) + "]" );
		return getCurrentPathResolutionStrategy().handleIntermediatePathPart(
				( PathedPropertyReferenceSource ) source,
				propertyName.getText() );
	}

	protected PathedPropertyReferenceSource normalizeUnqualifiedPropertyReferenceSource(
			Tree propertyName ) {
		return getCurrentPathResolutionStrategy().handleRoot(
				getCurrentPersisterSpaceContext()
						.locatePersisterSpaceExposingProperty(
								propertyName.getText() ) );
	}

	protected void normalizeIntermediateIndexOperation(
			PathedPropertyReferenceSource propertyReferenceSource,
			Tree collectionProperty, Tree selector ) {
		throw new UnsupportedOperationException(
				"must be implemented!: normalizeIntermediateIndexOperation" );
	}

	protected void normalizeTerminalIndexOperation(
			PathedPropertyReferenceSource propertyReferenceSource,
			Tree collectionProperty, Tree selector ) {
		log.trace( "normalizing terminal index access ["
				+ textOrNull( collectionProperty ) + "]" );
		PathedPropertyReferenceSource collectionSource = propertyReferenceSource;
		getCurrentPathResolutionStrategy().handleTerminalIndexAccess(
				collectionSource, collectionProperty.getText(),
				new HibernateTree( ( CommonTree ) selector ) );
	}

	private String textOrNull( Tree tree ) {
		return tree == null ? null : tree.getText();
	}

}