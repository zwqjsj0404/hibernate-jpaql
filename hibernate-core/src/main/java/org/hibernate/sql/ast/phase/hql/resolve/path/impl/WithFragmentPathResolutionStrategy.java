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

import org.hibernate.QueryException;
import org.hibernate.HibernateException;
import org.hibernate.sql.ast.phase.hql.resolve.PersisterSpace;
import org.hibernate.sql.ast.phase.hql.resolve.ResolutionContext;
import org.hibernate.sql.ast.phase.hql.resolve.path.PathedPropertyReferenceSource;
import org.hibernate.sql.ast.common.HibernateTree;
import org.hibernate.sql.ast.tree.Table;

/**
 * {@link org.hibernate.sql.ast.phase.hql.resolve.path.PathResolutionStrategy} for dealing with <tt>WITH</tt>
 * fragments used to supply addition join conditions to property joins.
 *
 * @author Steve Ebersole
 */
public class WithFragmentPathResolutionStrategy extends BasicPathResolutionStrategySupport {
	private final PersisterSpace lhs;

	private String baseRhsPropertyName;
	private PersisterSpace rhs;

	public WithFragmentPathResolutionStrategy(ResolutionContext resolutionContext, PersisterSpace lhs) {
		super( resolutionContext );
		this.lhs = lhs;
	}

	@Override
	protected PathedPropertyReferenceSource internalHandleRoot(PersisterSpace persisterSpace) {
		rhs = persisterSpace;
		return super.internalHandleRoot( persisterSpace );
	}

	@Override
	protected PathedPropertyReferenceSource internalResolveIntermediatePathPart(PathedPropertyReferenceSource source, String pathPart) {
		if ( baseRhsPropertyName == null ) {
			baseRhsPropertyName = pathPart;
		}
		return super.internalResolveIntermediatePathPart( source, pathPart );
	}

	@Override
	protected HibernateTree internalResolveTerminalPathPart(PathedPropertyReferenceSource source, String pathPart) {
		if ( baseRhsPropertyName == null ) {
			baseRhsPropertyName = pathPart;
		}
		return super.internalResolveTerminalPathPart( source, pathPart );
	}

	@Override
	protected PathedPropertyReferenceSource internalHandleIntermediateIndexAccess(PathedPropertyReferenceSource source, String pathPart, HibernateTree selector) {
		if ( baseRhsPropertyName == null ) {
			baseRhsPropertyName = pathPart;
		}
		return super.internalHandleIntermediateIndexAccess( source, pathPart, selector );
	}

	@Override
	protected HibernateTree internalHandleTerminalIndexAccess(PathedPropertyReferenceSource source, String pathPart, HibernateTree selector) {
		if ( baseRhsPropertyName == null ) {
			baseRhsPropertyName = pathPart;
		}
		return super.internalHandleTerminalIndexAccess( source, pathPart, selector );
	}

	public void applyWithFragment(HibernateTree withFragment) {
		// first, locate the actual Join node which links the lhs and rhs...
		Table lhsTable = lhs.getTableSpace().getContainingTable( baseRhsPropertyName );

		// todo : implement...

		// as simple as finding the table under lhsTable which contains a join to a table from the table-space associated with
		// the rhs persister-space???

//		Join join = null;
//		AST nextPossible = lhs.getFirstChild();
//		while ( nextPossible != null ) {
//			if ( nextPossible instanceof Join ) {
//				if ( ( ( Join ) nextPossible ).locateRhs() == rhs ) {
//					join = ( Join ) nextPossible;
//					break;
//				}
//			}
//			nextPossible = nextPossible.getNextSibling();
//		}
//		if ( join == null ) {
//			throw new QueryException( "could not locate specific join node to which to apply with fragment [" + withFragment + "]" );
//		}
//		join.addChild( withFragment );
	}

	protected void validateJoinCreation(PersisterSpace origin, String property) {
		// todo : why not???
		throw new HibernateException( "Path expressions [" + origin.getSourceAlias() + "." + property + "] within 'with clause' cannot result in physical join" );
	}
}
