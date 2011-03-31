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
package org.hibernate.sql.ast.origin.hql.resolve.path.impl;

import org.hibernate.QueryException;
import org.hibernate.sql.ast.common.HibernateTree;
import org.hibernate.sql.ast.common.JoinType;
import org.hibernate.sql.ast.origin.hql.resolve.PersisterSpace;
import org.hibernate.sql.ast.origin.hql.resolve.ResolutionContext;
import org.hibernate.sql.ast.origin.hql.resolve.path.PathedPropertyReferenceSource;
import org.hibernate.sql.ast.origin.hql.parse.HQLLexer;
import org.hibernate.sql.ast.tree.Table;

/**
 *
 * {@link org.hibernate.sql.ast.origin.hql.resolve.path.PathResolutionStrategy} for dealing with path expressions
 * occuring in the <tt>FROM</tt> clause of a query.
 *
 * @author Steve Ebersole
 */
public class FromClausePathResolutionStrategy extends AbstractPathResolutionStrategy {
	private final JoinType joinType;
	private final boolean associationFetch;
	private final boolean propertyFetch;
	private final String alias;

	/**
	 * Instantiate a normalization strategy for handling path expressions in a <tt>FROM</tt> clause.
	 *
	 * @param resolutionContext The context for resolution.
	 * @param joinType The type of explicit-join specified at the root of this path expression.
	 * @param associationFetch Was association fetching indicated on this path expression?
	 * @param propertyFetch Was property fetching indicated on this path expression?
	 * @param alias The alias (if one) specified on this joined path expression.
	 */
	public FromClausePathResolutionStrategy(
			ResolutionContext resolutionContext,
			JoinType joinType,
			boolean associationFetch,
			boolean propertyFetch,
			String alias) {
		super( resolutionContext );
		this.joinType = joinType;
		this.associationFetch = associationFetch;
		this.propertyFetch = propertyFetch;
		this.alias = alias;
	}

	protected PathedPropertyReferenceSource internalHandleRoot(PersisterSpace persisterSpace) {
		return new SourceImpl( persisterSpace );
	}

	private class SourceImpl extends AbstractPathedPropertyReferenceSource {
		private final PersisterSpace lhs;

		private SourceImpl(PersisterSpace lhs) {
			this.lhs = lhs;
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(String name) {
			// TODO : still need to account for paths including component dereferences...
			Table joinedTable = buildPropertyJoinedTable( lhs, name, null, false, associationFetch );
			return new SourceImpl( joinedTable.getTableSpace().getPersisterSpace() );
		}

		public HibernateTree handleTerminalPathPart(String propertyName) {
			validateJoinable( lhs, propertyName );

			Table joinedTable = buildPropertyJoinedTable( lhs, propertyName, alias, propertyFetch, associationFetch );
			return joinedTable.getTableSpace().buildIdentifierColumnReferences();
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(String name, HibernateTree selector) {
			throw new UnsupportedOperationException( "index operation not supported in from clause" );
		}

		public HibernateTree handleTerminalIndexAccess(String name, HibernateTree selector) {
			throw new UnsupportedOperationException( "index operation not supported in from clause" );
		}
	}

	protected HibernateTree buildJoinTypeNode() {
		if ( joinType == JoinType.INNER ) {
			return new HibernateTree( HQLLexer.INNER );
		}
		else if ( joinType == JoinType.LEFT ) {
			return new HibernateTree( HQLLexer.LEFT );
		}
		else if ( joinType == JoinType.RIGHT ) {
			return new HibernateTree( HQLLexer.RIGHT );
		}
		// if no match found, throw exception
		throw new QueryException( "Unrecognized join type [" + joinType.toString() + "]" );
	}

	protected boolean areJoinsReusable() {
		return false;
	}
}
