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

import org.hibernate.HibernateException;
import org.hibernate.sql.ast.origin.hql.resolve.PersisterSpace;
import org.hibernate.sql.ast.origin.hql.resolve.ResolutionContext;
import org.hibernate.sql.ast.origin.hql.resolve.path.PathedPropertyReferenceSource;

/**
 * {@link org.hibernate.sql.ast.origin.hql.resolve.path.PathResolutionStrategy} for dealing with path expressions
 * occuring in the <tt>ON</tt> clause of a persister join.
 *
 * @author Steve Ebersole
 */
public class OnFragmentPathResolutionStrategy extends BasicPathResolutionStrategySupport {
	private final PersisterSpace joinRhs;
	private PersisterSpace joinLhs;

	public OnFragmentPathResolutionStrategy(ResolutionContext resolutionContext, PersisterSpace joinRhs) {
		super( resolutionContext );
		this.joinRhs = joinRhs;
	}

	public PersisterSpace getDiscoveredLHS() {
		return joinLhs;
	}

	protected PathedPropertyReferenceSource internalHandleRoot(PersisterSpace persisterSpace) {
		// persisterSpace must either refer to our LHS or RHS...
		if ( persisterSpace == joinRhs ) {
			// nothing to do...
		}
		else if ( joinLhs != null ) {
			if ( persisterSpace != joinLhs ) {
				throw new HibernateException(
						"path root not resolveable against either left-hand-side [" +
						joinLhs.getSourceAlias() + "] nor right-hand-side [" +
						joinRhs.getSourceAlias() + "] of the join"
				);
			}
		}
		else {
			joinLhs = persisterSpace;
		}
		return super.internalHandleRoot( persisterSpace );
	}
}
