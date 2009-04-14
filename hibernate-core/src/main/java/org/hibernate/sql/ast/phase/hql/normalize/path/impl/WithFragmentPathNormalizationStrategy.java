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
package org.hibernate.sql.ast.phase.hql.normalize.path.impl;

import org.hibernate.sql.ast.phase.hql.normalize.PersisterReference;
import org.hibernate.sql.ast.phase.hql.normalize.NormalizationContext;
import org.hibernate.sql.ast.phase.hql.normalize.Join;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;

import antlr.collections.AST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link org.hibernate.sql.ast.phase.hql.normalize.path.PathNormalizationStrategy} for dealing with <tt>WITH</tt>
 * fragments used to supply addition join conditions to property joins.
 *
 * @author Steve Ebersole
 */
public class WithFragmentPathNormalizationStrategy extends BasicPathNormalizationStrategySupport {
	private static final Logger log = LoggerFactory.getLogger( WithFragmentPathNormalizationStrategy.class );

	private final PersisterReference lhs;

	public WithFragmentPathNormalizationStrategy(NormalizationContext normalizationContext, PersisterReference lhs) {
		super( normalizationContext );
		this.lhs = lhs;
	}

	public void applyWithFragment(AST withFragment) {
		PersisterReference rhs = getRoot();
		// first, locate the actual Join node which links the lhs and rhs...
		Join join = null;
		AST nextPossible = lhs.getFirstChild();
		while ( nextPossible != null ) {
			if ( nextPossible instanceof Join ) {
				if ( ( ( Join ) nextPossible ).locateRhs() == rhs ) {
					join = ( Join ) nextPossible;
					break;
				}
			}
			nextPossible = nextPossible.getNextSibling();
		}
		if ( join == null ) {
			throw new QueryException( "could not locate specific join node to which to apply with fragment [" + withFragment + "]" );
		}
		join.addChild( withFragment );
	}

	protected void validateJoinCreation(PersisterReference origin, String property) {
		// todo : why not???
		throw new HibernateException( "Path expression [" + origin.getText() + "].[" + property + "] within 'with clause' cannot result in physical join" );
	}
}
