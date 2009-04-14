/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
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
package org.hibernate.sql.ast.phase.hql.normalize.path.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.sql.ast.phase.hql.normalize.PropertyReference;
import org.hibernate.sql.ast.phase.hql.normalize.Ident;
import org.hibernate.sql.ast.phase.hql.normalize.NormalizationContext;
import org.hibernate.sql.ast.alias.ImplicitAliasGenerator;
import org.hibernate.sql.ast.phase.hql.normalize.PersisterReferenceBuilder;
import org.hibernate.sql.ast.phase.hql.normalize.PersisterReference;
import org.hibernate.sql.ast.phase.hql.normalize.HqlNormalizeTokenTypes;
import org.hibernate.sql.ast.phase.hql.normalize.Join;
import org.hibernate.sql.ast.phase.hql.normalize.PropertyPathTerminus;
import org.hibernate.sql.ast.phase.hql.normalize.path.PathNormalizationStrategy;
import org.hibernate.sql.ast.phase.hql.normalize.path.PathedPropertyReferenceSource;
import org.hibernate.sql.ast.util.ASTPrinter;
import org.hibernate.sql.ast.util.DisplayableNode;
import org.hibernate.sql.ast.common.Node;
import org.hibernate.type.ComponentType;
import org.hibernate.QueryException;
import org.hibernate.engine.SessionFactoryImplementor;

import antlr.collections.AST;
import antlr.CommonAST;
import antlr.Token;

/**
 * Abstract implementation of {@link org.hibernate.sql.ast.phase.hql.normalize.path.PathNormalizationStrategy} providing convenience methods to actual
 * {@link org.hibernate.sql.ast.phase.hql.normalize.path.PathNormalizationStrategy} implementors.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractPathNormalizationStrategy implements PathNormalizationStrategy, HqlNormalizeTokenTypes {
	private static final Logger log = LoggerFactory.getLogger( AbstractPathNormalizationStrategy.class );

	private final NormalizationContext normalizationContext;
	private PersisterReference root;  // todo whether we need this depends on how we deciode to structure the prop-joins...
	private String pathThusFar = null;

	protected AbstractPathNormalizationStrategy(NormalizationContext normalizationContext) {
		this.normalizationContext = normalizationContext;
	}

	/**
	 * Getter for property 'root'.
	 *
	 * @return Value for property 'root'.
	 */
	protected PersisterReference getRoot() {
		return root;
	}


	// normalization context ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Getter for property 'normalizationContext'.
	 *
	 * @return Value for property 'normalizationContext'.
	 */
	protected NormalizationContext normalizationContext() {
		return normalizationContext;
	}

	protected final Node createNode(int type, String text) {
		return ( Node ) normalizationContext().getASTFactory().create( type, text );
	}

	/**
	 * Getter for property 'aliasBuilder'.
	 *
	 * @return Value for property 'aliasBuilder'.
	 */
	protected final ImplicitAliasGenerator getAliasBuilder() {
		return normalizationContext().getAliasBuilder();
	}

	/**
	 * Getter for property 'persisterReferenceBuilder'.
	 *
	 * @return Value for property 'persisterReferenceBuilder'.
	 */
	protected final PersisterReferenceBuilder getPersisterReferenceBuilder() {
		return normalizationContext().getPersisterReferenceBuilder();
	}

	/**
	 * Getter for property 'sessionFactoryImplementor'.
	 *
	 * @return Value for property 'sessionFactoryImplementor'.
	 */
	protected final SessionFactoryImplementor getSessionFactoryImplementor() {
		return normalizationContext().getSessionFactoryImplementor();
	}

	/**
	 * Getter for property 'ASTPrinter'.
	 *
	 * @return Value for property 'ASTPrinter'.
	 */
	protected final ASTPrinter getASTPrinter() {
		return normalizationContext().getPrinter();
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
	public final PathedPropertyReferenceSource handleRoot(PersisterReference persisterReference) {
		initializePathSoFar( persisterReference.getAlias() );
		root = persisterReference;
		log.trace( "handling root path source [" + pathThusFar + "]" );
		return internalHandleRoot( persisterReference );
	}

	/**
	 * Hook for subclasses to process the path root.
	 *
	 * @param persisterReference The persister defining the source root.
	 * @return The appropriate property path source implementation.
	 */
	protected abstract PathedPropertyReferenceSource internalHandleRoot(PersisterReference persisterReference);

	/**
	 * {@inheritDoc}
	 */
	public final PathedPropertyReferenceSource handleIntermediatePathPart(PathedPropertyReferenceSource source, Ident pathPart) {
		pathThusFar = ( pathThusFar == null ) ? pathPart.getText() : pathThusFar + "." + pathPart.getText();
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
	protected PathedPropertyReferenceSource internalResolveIntermediatePathPart(PathedPropertyReferenceSource source, Ident pathPart) {
		return source.handleIntermediatePathPart( pathPart );
	}

	/**
	 * {@inheritDoc}
	 */
	public final PropertyPathTerminus handleTerminalPathPart(PathedPropertyReferenceSource source, Ident pathPart) {
		pathThusFar = ( pathThusFar == null ) ? pathPart.getText() : pathThusFar + "." + pathPart.getText();
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
	protected PropertyPathTerminus internalResolveTerminalPathPart(PathedPropertyReferenceSource source, Ident pathPart) {
		return source.handleTerminalPathPart( pathPart );
	}

	/**
	 * {@inheritDoc}
	 */
	public final PathedPropertyReferenceSource handleIntermediateIndexAccess(PathedPropertyReferenceSource source, Ident pathPart, Node selector) {
		pathThusFar = ( ( pathThusFar == null ) ? pathPart.getText() : pathThusFar + "." + pathPart.getText() ) + "[]";
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
	protected PathedPropertyReferenceSource internalHandleIntermediateIndexAccess(PathedPropertyReferenceSource source, Ident pathPart, Node selector) {
		return source.handleIntermediateIndexAccess( pathPart, selector );
	}

	/**
	 * {@inheritDoc}
	 */
	public final PropertyPathTerminus handleTerminalIndexAccess(PathedPropertyReferenceSource source, Ident pathPart, Node selector) {
		pathThusFar = ( ( pathThusFar == null ) ? pathPart.getText() : pathThusFar + "." + pathPart.getText() ) + "[]";
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
	protected PropertyPathTerminus internalHandleTerminalIndexAccess(PathedPropertyReferenceSource source, Ident pathPart, Node selector) {
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
	protected final int locateComponentPropertyIndex(ComponentType componentType, Ident subPropertyName) {
		return locateComponentPropertyIndex( componentType, subPropertyName.getText() );
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
	 * Generate a {@link #PROPERTY_REF} node.
	 *
	 * @param origin The persister from which the property originates.
	 * @param propertyName The name of the property being referenced.
	 * @return The {@link #PROPERTY_REF} node.
	 */
	protected final PropertyReference generatePropertyReference(PersisterReference origin, String propertyName) {
		AST propertyReference = createNode( PROPERTY_REF, "property-reference" );
		propertyReference.addChild( createNode( ALIAS_REF, origin.getAlias() ) );
		propertyReference.addChild( createNode( IDENT, propertyName ) );
		return ( PropertyReference ) propertyReference;
	}

	/**
	 * Locate (if property joins are reusable) or build an appropriate join.
	 *
	 * @param lhs The join lhs, which is the origin of the property.
	 * @param propertyName The name of the property
	 * @param alias The alias, if any, to apply to the generated RHS persister reference.
	 * @param propertyFetching Should property fetching be applied to the generated RHS?
	 * @param associationFetching Did this property join specify association fetching (join fetch)?
	 * @return The appropriate join.
	 */
	protected final Join locateOrBuildPropertyJoin(
			PersisterReference lhs,
			String propertyName,
			String alias,
			boolean propertyFetching,
			boolean associationFetching) {
		Join join = null;

		if ( areJoinsReusable() ) {
			join = lhs.locateReusablePropertyJoin( propertyName );
		}

		if ( join == null ) {
			join = buildPropertyJoin( lhs, propertyName, alias, propertyFetching, associationFetching );
			if ( areJoinsReusable() ) {
				lhs.registerReusablePropertyJoin( propertyName, join );
			}
		}

		return join;
	}

	/**
	 * Build a property join node.
	 *
	 * @param lhs The join's left-hand-side persister-reference
	 * @param propertyName The property name.
	 * @param alias The alias to apply to the rhs of the join
	 * @param propertyFetching should property fetching be applied to the joined persister?
	 * @param associationFetching Should the association making up the property join also be fetched?
	 *
	 * @return The right-hand-side persister-reference.
	 */
	protected Join buildPropertyJoin(
			PersisterReference lhs,
			String propertyName,
			String alias,
			boolean propertyFetching,
			boolean associationFetching) {
		validateJoinCreation( lhs, propertyName );
		return normalizationContext().getPropertyJoinBuilder().buildPropertyJoin(
				lhs,
				propertyName,
				alias,
				buildJoinTypeNode(),
				propertyFetching,
				associationFetching
		);
	}

	/**
	 * Hook to allow subclasses to disallow implicit join.
	 *
	 * @param origin The persister-reference which is the origin of the property
	 * @param property The property resulting in a join.
	 */
	protected void validateJoinCreation(PersisterReference origin, String property) {
		log.debug( "creating path expression implied join [" + origin.getAlias() + "].[" + property + "]" );
	}

	/**
	 * Hook to allow subclasses to define the type of join to use for an implciit join.
	 * <p/>
	 * The default is to use an {@link #INNER} join.
	 *
	 * @return The join type node.
	 */
	protected Node buildJoinTypeNode() {
		return createNode( INNER, "inner" );
	}

	/**
	 * Does this strategy allows property joins to be reused?
	 *
	 * @return True/false.
	 */
	protected boolean areJoinsReusable() {
		return true;
	}


	// source impl support ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public abstract class AbstractPathedPropertyReferenceSource extends CommonAST
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
			this.originationPath = originationPath;
		}

		protected AbstractPathedPropertyReferenceSource(Token token, String originationPath) {
			super( token );
			this.originationPath = originationPath;
		}

		public PropertyPathTerminus handleTerminalIndexAccess(PersisterReference lhs, Ident name, Node selector) {
			Join join = normalizationContext().getPropertyJoinBuilder().buildIndexOperationJoin(
					lhs,
					name.getText(),
					null,
					buildJoinTypeNode(),
					selector
			);
			return ( PropertyPathTerminus ) createNode( INDEXED_COLLECTION_ELEMENT_REF, join.locateRhs().getAlias() );
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
}
