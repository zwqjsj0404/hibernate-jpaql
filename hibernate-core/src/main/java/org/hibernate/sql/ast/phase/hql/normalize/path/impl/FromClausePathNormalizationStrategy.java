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

import org.hibernate.sql.ast.phase.hql.normalize.PersisterReference;
import org.hibernate.sql.ast.phase.hql.normalize.PropertyReference;
import org.hibernate.sql.ast.phase.hql.normalize.Ident;
import org.hibernate.sql.ast.phase.hql.normalize.NormalizationContext;
import org.hibernate.sql.ast.phase.hql.normalize.HqlNormalizeTokenTypes;
import org.hibernate.sql.ast.phase.hql.normalize.PropertyPathTerminus;
import org.hibernate.sql.ast.phase.hql.normalize.Join;
import org.hibernate.sql.ast.phase.hql.normalize.path.impl.AbstractPathNormalizationStrategy;
import org.hibernate.sql.ast.phase.hql.normalize.path.PathNormalizationStrategy;
import org.hibernate.sql.ast.phase.hql.normalize.path.PathedPropertyReferenceSource;
import org.hibernate.sql.ast.common.JoinType;
import org.hibernate.sql.ast.common.Node;
import org.hibernate.type.Type;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;

/**
 *
 * {@link org.hibernate.sql.ast.phase.hql.normalize.path.PathNormalizationStrategy} for dealing with path expressions
 * occuring in the <tt>FROM</tt> clause of a query.
 *
 * @author Steve Ebersole
 */
public class FromClausePathNormalizationStrategy extends AbstractPathNormalizationStrategy
		implements PathNormalizationStrategy, HqlNormalizeTokenTypes {

	private final JoinType joinType;
	private final boolean associationFetch;
	private final boolean propertyFetch;
	private final String alias;


	/**
	 * Instantiate a normalization strategy for handling path expressions in a <tt>FROM</tt> clause.
	 *
	 * @param normalizationContext The context for normalization.
	 * @param joinType The type of explicit-join specified at the root of this path expression.
	 * @param associationFetch Was association fetching indicated on this path expression?
	 * @param propertyFetch Was property fetching indicated on this path expression?
	 * @param alias The alias (if one) specified on this joined path expression.
	 */
	public FromClausePathNormalizationStrategy(
			NormalizationContext normalizationContext,
			JoinType joinType,
			boolean associationFetch,
			boolean propertyFetch,
			String alias) {
		super( normalizationContext );
		this.joinType = joinType;
		this.associationFetch = associationFetch;
		this.propertyFetch = propertyFetch;
		this.alias = alias;
	}

	protected PathedPropertyReferenceSource internalHandleRoot(PersisterReference persisterReference) {
		return new SourceImpl( persisterReference );
	}

	protected Join buildPropertyJoin(
			PersisterReference lhs,
			String propertyName,
			String alias,
			boolean propertyFetching,
			boolean associationFetching) {
		Join join = super.buildPropertyJoin( lhs, propertyName, alias, propertyFetching, associationFetching );
		normalizationContext().getCurrentPersisterReferenceContext()
				.registerPersisterReferenceImplicitInDerivedSelectClause( join.locateRhs() );
		return join;
	}

	private class SourceImpl extends AbstractPathedPropertyReferenceSource {
		private final PersisterReference lhs;

		private SourceImpl(PersisterReference lhs) {
			this.lhs = lhs;
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(Ident name) {
			// TODO : still need to account for paths including component dereferences...
			return new SourceImpl( locateOrBuildPropertyJoin( lhs, name.getText(), null, false, associationFetch ).locateRhs() );
		}

		public PropertyPathTerminus handleTerminalPathPart(Ident name) {
			final String propertyName = name.getText();
			Type terminalPropertyType = lhs.getPropertyType( propertyName );
			if ( !terminalPropertyType.isAssociationType() ) {
				throw new HibernateException( "non-association type cannot be used as termination of path expression in from clause" );
			}

			locateOrBuildPropertyJoin( lhs, name.getText(), alias, propertyFetch, associationFetch );

			PropertyReference propertyReference = generatePropertyReference( lhs, name.getText() );
			propertyReference.injectOriginalPath( getPathThusFar() );

			return propertyReference;
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(Ident name, Node selector) {
			throw new UnsupportedOperationException( "index operation not supported in from clause" );
		}

		public PropertyPathTerminus handleTerminalIndexAccess(Ident name, Node selector) {
			throw new UnsupportedOperationException( "index operation not supported in from clause" );
		}
	}

	protected Node buildJoinTypeNode() {
		if ( joinType == JoinType.INNER ) {
			return createNode( INNER, "inner" );
		}
		else if ( joinType == JoinType.LEFT ) {
			return createNode( LEFT, "left" );
		}
		else if ( joinType == JoinType.RIGHT ) {
			return createNode( RIGHT, "right" );
		}
		// if no match found, throw exception
		throw new QueryException( "Unrecognized join type [" + joinType.toString() + "]" );
	}

	protected boolean areJoinsReusable() {
		return false;
	}
}
