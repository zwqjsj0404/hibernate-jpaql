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
package org.hibernate.sql.ast.origin.hql.resolve;

/**
 * Defines a hierarchical representation of a persister reference context.
 * <p/>
 * Generally speaking this notion should really only hold for SELECT statements.  Does not make sense for
 * INSERT or UPDATE or DELETE statements to have a parent, as that would mean they are being used as in a subqquery
 * (certainly, however, it makes sense for these to *be the parent* context...).
 *
 * @author Steve Ebersole
 */
public class HierarchicalPersisterSpaceContext extends RootPersisterSpaceContext {
	private final PersisterSpaceContext parent;

	public HierarchicalPersisterSpaceContext(PersisterSpaceContext parent) {
		super();
		if ( parent == null ) {
			throw new IllegalArgumentException( "Parent PersisterSpaceContext cannot be null!" );
		}
		this.parent = parent;
	}

	/**
	 * Get the parent context of this context.
	 *
	 * @return Our parent context.
	 */
	public PersisterSpaceContext getParent() {
		return parent;
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Overriden to project the lookup to our parent if not found locally.
	 */
	public boolean isContainedAlias(String alias) {
		return super.isContainedAlias( alias ) || getParent().isContainedAlias( alias );
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Overriden to project the lookup to our parent if not found locally.
	 */
	public boolean isContainedExposedProperty(String propertyName) {
		return super.isContainedExposedProperty( propertyName ) || getParent().isContainedExposedProperty( propertyName );
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Overriden to project the lookup to our parent if not found locally.
	 */
	public PersisterSpace locatePersisterSpaceByAlias(String alias) {
		PersisterSpace persisterSpace = super.locatePersisterSpaceByAlias( alias );
		if ( persisterSpace == null ) {
			persisterSpace = getParent().locatePersisterSpaceByAlias( alias );
		}
		return persisterSpace;
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Overriden to project the lookup to our parent if not found locally.
	 */
	public PersisterSpace locatePersisterSpaceExposingProperty(String propertyName) {
		PersisterSpace persisterSpace = super.locatePersisterSpaceExposingProperty( propertyName );
		if ( persisterSpace == null ) {
			persisterSpace = getParent().locatePersisterSpaceExposingProperty( propertyName );
		}
		return persisterSpace;
	}
}
