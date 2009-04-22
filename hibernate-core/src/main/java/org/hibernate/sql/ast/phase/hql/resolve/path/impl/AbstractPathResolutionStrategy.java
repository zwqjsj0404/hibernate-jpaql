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

package org.hibernate.sql.ast.phase.hql.resolve.path.impl;

import org.antlr.runtime.Token;
import org.hibernate.QueryException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.ast.alias.TableAliasGenerator;
import org.hibernate.sql.ast.common.HibernateToken;
import org.hibernate.sql.ast.common.HibernateTree;
import org.hibernate.sql.ast.phase.hql.parse.HQLParser;
import org.hibernate.sql.ast.phase.hql.parse.HQLLexer;
import org.hibernate.sql.ast.phase.hql.resolve.PersisterSpace;
import org.hibernate.sql.ast.phase.hql.resolve.PersisterTableExpressionGenerator;
import org.hibernate.sql.ast.phase.hql.resolve.ResolutionContext;
import org.hibernate.sql.ast.phase.hql.resolve.path.PathResolutionStrategy;
import org.hibernate.sql.ast.phase.hql.resolve.path.PathedPropertyReferenceSource;
import org.hibernate.sql.ast.tree.Table;
import org.hibernate.sql.ast.util.DisplayableNode;
import org.hibernate.sql.ast.util.TreePrinter;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of {@link PathResolutionStrategy} providing convenience methods to actual
 * {@link PathResolutionStrategy} implementors.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractPathResolutionStrategy implements PathResolutionStrategy {
	private static final Logger log = LoggerFactory.getLogger( AbstractPathResolutionStrategy.class );

	private final ResolutionContext resolutionContext;
	private String pathThusFar = null;

	protected AbstractPathResolutionStrategy(ResolutionContext resolutionContext) {
		this.resolutionContext = resolutionContext;
	}

	// reolution context ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Getter for property 'resolutionContext'.
	 *
	 * @return Value for property 'resolutionContext'.
	 */
	protected ResolutionContext resolutionContext() {
		return resolutionContext;
	}

	protected final HibernateTree createNode(int type, String text) {
		return new HibernateTree(type, text);
	}

	/**
	 * Getter for property 'sessionFactoryImplementor'.
	 *
	 * @return Value for property 'sessionFactoryImplementor'.
	 */
	protected final SessionFactoryImplementor getSessionFactoryImplementor() {
		return resolutionContext().getSessionFactoryImplementor();
	}

	/**
	 * Getter for property 'ASTPrinter'.
	 *
	 * @return Value for property 'ASTPrinter'.
	 */
	protected final TreePrinter getTreePrinter() {
		return resolutionContext().getTreePrinter();
	}


	// path ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	protected void initializePathSoFar(String root) {
		pathThusFar = root;
	}

	/**
	 * Getter for property 'pathThusFar'.
	 *
	 * @return Value for property 'pathThusFar'.
	 */
	public String getPathThusFar() {
		return pathThusFar;
	}

	/**
	 * {@inheritDoc}
	 */
	public final PathedPropertyReferenceSource handleRoot(PersisterSpace persisterSpace) {
		initializePathSoFar( persisterSpace.getSourceAlias() );
		log.trace( "handling root path source [" + pathThusFar + "]" );
		return internalHandleRoot( persisterSpace );
	}

	/**
	 * Hook for subclasses to process the path root.
	 *
	 * @param persisterSpace The persister defining the source root.
	 * @return The appropriate property path source implementation.
	 */
	protected abstract PathedPropertyReferenceSource internalHandleRoot(PersisterSpace persisterSpace);

	/**
	 * {@inheritDoc}
	 */
	public final PathedPropertyReferenceSource handleIntermediatePathPart(PathedPropertyReferenceSource source, String pathPart) {
		pathThusFar = ( pathThusFar == null ) ? pathPart : pathThusFar + "." + pathPart;
		log.trace( "handling intermediate path source [" + pathThusFar + "]" );
		return internalResolveIntermediatePathPart( source, pathPart );
	}

	/**
	 * Hook for subclasses to process an intermediate part of the path.
	 *
	 * @param source The source from which pathPart originates.
	 * @param pathPart The name of the path part to be processed.
	 * @return The appropriate property path source implementation.
	 */
	protected PathedPropertyReferenceSource internalResolveIntermediatePathPart(PathedPropertyReferenceSource source, String pathPart) {
		return source.handleIntermediatePathPart( pathPart );
	}

	/**
	 * {@inheritDoc}
	 */
	public final HibernateTree handleTerminalPathPart(PathedPropertyReferenceSource source, String pathPart) {
		pathThusFar = ( pathThusFar == null ) ? pathPart : pathThusFar + "." + pathPart;
		log.trace( "handling terminal path part [" + pathThusFar + "]" );
		try {
			return internalResolveTerminalPathPart( source, pathPart );
		}
		finally {
			pathThusFar = null;
		}
	}

	/**
	 * Hook for subclasses to process the terminal (or ending) part of a path.
	 *
	 * @param source The source from which pathPart originates.
	 * @param pathPart The name of the path part to be processed.
	 * @return a node representing the normalized property path.
	 */
	protected HibernateTree internalResolveTerminalPathPart(PathedPropertyReferenceSource source, String pathPart) {
		return source.handleTerminalPathPart( pathPart );
	}

	/**
	 * {@inheritDoc}
	 */
	public final PathedPropertyReferenceSource handleIntermediateIndexAccess(PathedPropertyReferenceSource source, String pathPart, HibernateTree selector) {
		pathThusFar = ( ( pathThusFar == null ) ? pathPart : pathThusFar + "." + pathPart ) + "[]";
		log.trace( "handling intermediate index access [" + pathThusFar + "]" );
		try {
			return internalHandleIntermediateIndexAccess( source, pathPart, selector );
		}
		finally {
			pathThusFar = null;
		}
	}

	/**
	 * Hook for subclasses to process an index access as an intermediate property path.
	 *
	 * @param source The source from which pathPart originates.
	 * @param pathPart The name of the path part to be processed.
	 * @param selector The index selector to be appliedto the indexed collection
	 *
	 * @return The appropriate property path source implementation.
	 */
	protected PathedPropertyReferenceSource internalHandleIntermediateIndexAccess(PathedPropertyReferenceSource source, String pathPart, HibernateTree selector) {
		return source.handleIntermediateIndexAccess( pathPart, selector );
	}

	/**
	 * {@inheritDoc}
	 */
	public final HibernateTree handleTerminalIndexAccess(PathedPropertyReferenceSource source, String pathPart, HibernateTree selector) {
		pathThusFar = ( ( pathThusFar == null ) ? pathPart : pathThusFar + "." + pathPart ) + "[]";
		log.trace( "handling terminal index access [" + pathThusFar + "]" );
		try {
			return internalHandleTerminalIndexAccess( source, pathPart, selector );
		}
		finally {
			pathThusFar = null;
		}
	}

	/**
	 * Hook for subclasses to process an index access as the terminus of a property path.
	 *
	 * @param source The source from which pathPart originates.
	 * @param pathPart The name of the path part to be processed.
	 * @param selector The index selector to be appliedto the indexed collection
	 *
	 * @return a node representing the normalized property path.
	 */
	protected HibernateTree internalHandleTerminalIndexAccess(PathedPropertyReferenceSource source, String pathPart, HibernateTree selector) {
		return source.handleTerminalIndexAccess( pathPart, selector );
	}

	/**
	 * Convenience method to locate the index of a component sub-property.  The returned index is relative to
	 * {@link ComponentType#getPropertyNames}.
	 *
	 * @param componentType The component type mapping.
	 * @param subPropertyName The sub-property name.
	 * @return The index.
	 */
	protected static int locateComponentPropertyIndex(ComponentType componentType, String subPropertyName) {
		String[] componentPropertyNames = componentType.getPropertyNames();
		for ( int i = 0; i < componentPropertyNames.length; i++ ) {
			if ( componentPropertyNames[i].equals( subPropertyName ) ) {
				return i;
			}
		}
		throw new QueryException( "could not locate component property [" + subPropertyName + "]" );
	}

	/**
	 * Hook to allow subclasses to disallow implicit join.
	 *
	 * @param origin The persister-reference which is the origin of the property
	 * @param property The property resulting in a join.
	 */
	protected void validateJoinCreation(PersisterSpace origin, String property) {
		log.debug( "creating path expression implied join [" + origin.getSourceAlias() + "].[" + property + "]" );
	}

	/**
	 * Hook to allow subclasses to define the type of join to use for an implciit join.
	 * <p/>
	 * The default is to use an {@link HQLParser#INNER} join.
	 *
	 * @return The join type node.
	 */
	protected HibernateTree buildJoinTypeNode() {
		return createNode( HQLLexer.INNER, "inner" );
	}

	/**
	 * Does this strategy allows property joins to be reused?
	 *
	 * @return True/false.
	 */
	protected boolean areJoinsReusable() {
		return true;
	}

	/**
	 * Locate (if property joins are reusable) or build an appropriate joined table.
	 *
	 * @param lhs The join lhs, which is the origin of the property.
	 * @param propertyName The name of the property
	 * @param alias The alias, if any, to apply to the generated RHS persister reference.
	 * @param propertyFetching Should property fetching be applied to the generated RHS?
	 * @param associationFetching Did this property join specify association fetching (join fetch)?
	 * @return The appropriate join.
	 */
	protected final Table locateOrBuildPropertyJoinedTable(
			PersisterSpace lhs,
			String propertyName,
			String alias,
			boolean propertyFetching,
			boolean associationFetching) {
		Table joinedTable = null;
		if ( areJoinsReusable() ) {
			joinedTable = lhs.locateReusablePropertyJoinedTable( propertyName );
		}

		if ( joinedTable == null ) {
			joinedTable = buildPropertyJoinedTable( lhs, propertyName, alias, propertyFetching, associationFetching );
			if ( areJoinsReusable() ) {
				lhs.registerReusablePropertyJoinedTable( propertyName, joinedTable );
			}
		}

		return joinedTable;
	}

	/**
	 * Build a property joined table
	 *
	 * @param lhs The join's left-hand-side persister-reference
	 * @param propertyName The property name.
	 * @param alias The alias to apply to the rhs of the join
	 * @param propertyFetching should property fetching be applied to the joined persister?
	 * @param associationFetching Should the association making up the property join also be fetched?
	 *
	 * @return The right-hand-side persister-reference.
	 */
	protected Table buildPropertyJoinedTable(
			PersisterSpace lhs,
			String propertyName,
			String alias,
			boolean propertyFetching,
			boolean associationFetching) {
		validateJoinCreation( lhs, propertyName );
		Table joinedTable;
		Type propertyType = lhs.getPropertyType( propertyName );
		if ( propertyType.isEntityType() ) {
			EntityType entityType = ( EntityType ) propertyType;
			Queryable entityPersister = ( Queryable ) getSessionFactoryImplementor()
					.getEntityPersister( entityType.getAssociatedEntityName( getSessionFactoryImplementor() ) );
			joinedTable = createJoin( lhs, entityPersister, alias );
		}
		else if ( propertyType.isCollectionType() ) {
			CollectionType collectionType = ( CollectionType ) propertyType;
			QueryableCollection collectionPersister = ( QueryableCollection ) getSessionFactoryImplementor()
					.getCollectionPersister( collectionType.getRole() );
			joinedTable = createJoin( lhs, collectionPersister, alias, null );
		}
		else {
			throw new InvalidPropertyJoinException( getPathThusFar(), lhs.getName(), propertyName );
		}

		if ( propertyFetching ) {
			resolutionContext().registerPropertyFetch( joinedTable.getTableSpace().getPersisterSpace() );
		}
		if ( associationFetching ) {
			resolutionContext.registerAssociationFetch( joinedTable.getTableSpace().getPersisterSpace() );
		}

		return joinedTable;
	}

	/**
	 * Generate a column list (tree w/ token type {@link HQLParser#COLUMN_LIST} for the columns making up the given
	 * property.
	 *
	 * @param origin The persister-space from which the property originates.
	 * @param propertyName The name of the property being referenced.
	 *
	 * @return The column list.
	 */
	protected final HibernateTree generatePropertyColumnList(PersisterSpace origin, String propertyName) {
		HibernateTree columnList = new HibernateTree( HQLLexer.COLUMN_LIST );
		Table containingTable = origin.getTableSpace().getContainingTable( propertyName );
		for ( String columnName : origin.getTableSpace().getPropertyColumnNames( propertyName ) ) {
			final HibernateTree column = new HibernateTree( HQLLexer.COLUMN );
			columnList.addChild( column );
			column.addChild( new HibernateTree( HQLLexer.ALIAS_REF, containingTable.getAliasText() ) );
			column.addChild( new HibernateTree( HQLLexer.IDENTIFIER, columnName ) );
		}
		return columnList;
	}


	// source impl support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public abstract class AbstractPathedPropertyReferenceSource
			extends HibernateTree
			implements PathedPropertyReferenceSource, DisplayableNode {
		private final String originationPath;

		/**
		 * Constructs a new AbstractPathedPropertyReferenceSource.
		 */
		protected AbstractPathedPropertyReferenceSource() {
			this( getPathThusFar() );
		}

		protected AbstractPathedPropertyReferenceSource(Token token) {
			this( token, getPathThusFar() );
		}

		protected AbstractPathedPropertyReferenceSource(String originationPath) {
			super( new HibernateToken( HQLLexer.IDENTIFIER, originationPath ) );
			this.originationPath = originationPath;
		}

		protected AbstractPathedPropertyReferenceSource(Token token, String originationPath) {
			super( token );
			this.originationPath = originationPath;
		}

		public HibernateTree handleTerminalIndexAccess(PersisterSpace lhs, String collectionPropertyName, HibernateTree selector) {
			Table joinedCollectionTable = createIndexAccessJoin( lhs, collectionPropertyName, selector );

			// in general we need the collection element column list
			QueryableCollection collectionPersister = resolveCollectionPersister( lhs, collectionPropertyName );
			HibernateTree columnList = new HibernateTree( HQLLexer.COLUMN_LIST );
			for ( String columnName : collectionPersister.getElementColumnNames() ) {
				final HibernateTree column = new HibernateTree( HQLLexer.COLUMN );
				column.addChild( new HibernateTree( HQLLexer.ALIAS_REF, joinedCollectionTable.getAliasText() ) );
				column.addChild( new HibernateTree( HQLLexer.IDENTIFIER, columnName ) );
			}
			return columnList;
		}

		protected Table createIndexAccessJoin(PersisterSpace lhs, String collectionPropertyName, HibernateTree selector) {
			validateIndexedCollectionReference( lhs, collectionPropertyName );

			QueryableCollection collectionPersister = resolveCollectionPersister( lhs, collectionPropertyName );
			HibernateTree join = createJoin( lhs, collectionPersister, null, selector );

			if ( log.isTraceEnabled() ) {
				log.trace(
						resolutionContext().getTreePrinter().renderAsString(
								join,
								"implicit join : " + lhs.getSourceAlias() + "." + collectionPropertyName + "[]"
						)
				);
			}

			return ( Table ) join.getChild( 1 );
		}

		/**
		 * {@inheritDoc}
		 */
		public String getOriginationPath() {
			return originationPath;
		}

		/**
		 * {@inheritDoc}
		 */
		public final String getDisplayText() {
				return " ADPATER : SHOULD NEVER END UP IN TREE!";
		}
	}

	protected Table createJoin(PersisterSpace lhs, String propertyName, String alias) {
		validateJoinable( lhs, propertyName );
		Type propertyType = lhs.getPropertyType( propertyName );
		if ( propertyType.isEntityType() ) {
			return createJoin( lhs, resolveEntityPersister( lhs, propertyName ), alias );
		}
		else {
			// assume collection because of validation of being joinable...
			return createJoin( lhs, resolveCollectionPersister( lhs, propertyName ), alias, null );
		}
	}

	protected Table createJoin(PersisterSpace lhs, Queryable entityPersister, String alias) {
		EntityType entityType = entityPersister.getEntityMetamodel().getEntityType();

		TableAliasGenerator.TableAliasRoot tableAliasRoot = resolutionContext().getTableAliasGenerator()
				.generateSqlAliasRoot( entityPersister, alias );
		Table.EntityTableSpace tableSpace = new Table.EntityTableSpace( entityPersister, tableAliasRoot );
		Table joinedTableExpression = tableSpace.getDrivingTable();

		HibernateTree join = new HibernateTree( HQLLexer.JOIN );
		join.addChild( buildJoinTypeNode() );
		join.addChild( joinedTableExpression );

		HibernateTree joinCondition;
		final String lhsJoinProperty = entityType.getLHSPropertyName();
		if ( lhsJoinProperty == null ) {
			// join using the lhs PK
			joinCondition = PersisterTableExpressionGenerator.generateJoinCorrelation(
					lhs.getTableSpace().getJoinIntoTable().getAliasText(),
					lhs.getTableSpace().getJoinIntoColumns(),
					joinedTableExpression.getAliasText(),
					entityPersister.getKeyColumnNames()
			);
		}
		else {
			// join using the columns to which the given lhs property is mapped
			joinCondition = PersisterTableExpressionGenerator.generateJoinCorrelation(
					lhs.getTableSpace().getContainingTable( lhsJoinProperty ).getAliasText(),
					lhs.getTableSpace().getPropertyColumnNames( lhsJoinProperty ),
					joinedTableExpression.getAliasText(),
					entityPersister.getKeyColumnNames()
			);
		}

		HibernateTree on = new HibernateTree( HQLLexer.ON );
		join.addChild( on );
		on.addChild( joinCondition );

		return joinedTableExpression;
	}

	protected Table createJoin(PersisterSpace lhs, QueryableCollection collectionPersister, String sourceAlias, HibernateTree extraJoinConditions) {
		CollectionType collectionType = collectionPersister.getCollectionType();

		TableAliasGenerator.TableAliasRoot tableAliasRoot = resolutionContext().getTableAliasGenerator()
				.generateSqlAliasRoot( collectionPersister, sourceAlias );
		Table.CollectionTableSpace tableSpace = new Table.CollectionTableSpace( collectionPersister, tableAliasRoot );

		Table collectionTableExpression = PersisterTableExpressionGenerator.generateTableExpression(
				collectionPersister,
				tableAliasRoot,
				tableSpace
		);

		HibernateTree joinNode = new HibernateTree( HQLLexer.JOIN );
		joinNode.addChild( buildJoinTypeNode() );
		joinNode.addChild( collectionTableExpression );

		HibernateTree joinCondition;
		final String lhsJoinProperty = collectionType.getLHSPropertyName();
		if ( lhsJoinProperty == null ) {
			// join using the lhs PK
			joinCondition = PersisterTableExpressionGenerator.generateJoinCorrelation(
					lhs.getTableSpace().getJoinIntoTable().getAliasText(),
					lhs.getTableSpace().getJoinIntoColumns(),
					collectionTableExpression.getAliasText(),
					collectionPersister.getKeyColumnNames()
			);
		}
		else {
			// join using the columns to which the given lhs property is mapped
			joinCondition = PersisterTableExpressionGenerator.generateJoinCorrelation(
					lhs.getTableSpace().getContainingTable( lhsJoinProperty ).getAliasText(),
					lhs.getTableSpace().getPropertyColumnNames( lhsJoinProperty ),
					collectionTableExpression.getAliasText(),
					collectionPersister.getKeyColumnNames()
			);
		}

		if ( extraJoinConditions != null ) {
			HibernateTree mappedJoinCondition = joinCondition;
			joinCondition = new HibernateTree( HQLLexer.AND );
			joinCondition.addChild( mappedJoinCondition );
			joinCondition.addChild( extraJoinConditions );
		}

		HibernateTree on = new HibernateTree( HQLLexer.ON );
		joinNode.addChild( on );
		on.addChild( joinCondition );

		return collectionTableExpression;
	}

	protected void validateJoinable(PersisterSpace lhs, String propertyName) {
		if ( ! isAssociation( lhs.getPropertyType( propertyName ) ) ) {
			throw new InvalidPropertyJoinException( getPathThusFar(), lhs.getName(), propertyName );
		}
	}

	protected boolean isAssociation(Type propertyType) {
		return propertyType.isAssociationType();
	}

	protected void validateCollectionReference(PersisterSpace lhs, String propertyName) {
		if ( ! isCollectionReference( lhs.getPropertyType( propertyName ) ) ) {
			throw new CollectionExpectedException( getPathThusFar(), lhs.getName(), propertyName );
		}
	}

	private boolean isCollectionReference(Type propertyType) {
		return propertyType.isCollectionType();
	}

	protected void validateIndexedCollectionReference(PersisterSpace lhs, String propertyName) {
		if ( ! isIndexedCollectionReference( lhs.getPropertyType( propertyName ) ) ) {
			throw new IndexedCollectionExectedException( getPathThusFar(), lhs.getName(), propertyName );
		}
	}

	private boolean isIndexedCollectionReference(Type propertyType) {
		return isCollectionReference( propertyType )
				&& resolveCollectionPersister( ( CollectionType ) propertyType ).hasIndex();
	}

	protected QueryableCollection resolveCollectionPersister(PersisterSpace lhs, String propertyName) {
		return resolveCollectionPersister( ( CollectionType ) lhs.getPropertyType( propertyName ) );
	}

	protected QueryableCollection resolveCollectionPersister(CollectionType collectionType) {
		return ( QueryableCollection ) getSessionFactoryImplementor().getCollectionPersister( collectionType.getRole() );
	}

	protected Queryable resolveEntityPersister(PersisterSpace lhs, String propertyName) {
		return resolveEntityPersister( ( EntityType ) lhs.getPropertyType( propertyName ) );
	}

	protected Queryable resolveEntityPersister(EntityType entityType) {
		return ( Queryable ) getSessionFactoryImplementor().getEntityPersister(
				entityType.getAssociatedEntityName( getSessionFactoryImplementor() )
		);
	}
}
