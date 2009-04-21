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

package org.hibernate.sql.ast.phase.hql.resolve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.QueryException;

/**
 * Defines the contract for implementors of a "context" or a "scope" for references to persisters.  Generally speaking,
 * this maps to the notion of a FROM clause in a SELECT statement.  However, DML operations also have a notion of a
 * persister reference.  This, then, acts as the abstraction of these grouped references to persisters.
 *
 * @author Steve Ebersole
 */
public class RootPersisterSpaceContext implements PersisterSpaceContext {
	private static final Logger log = LoggerFactory.getLogger( RootPersisterSpaceContext.class );

	private List<PersisterSpace> persisterSpaces = new ArrayList<PersisterSpace>();
	private Map<String,PersisterSpace> aliasXref = new HashMap<String,PersisterSpace>();

	/**
	 * {@inheritDoc}
	 */
	public void registerPersisterSpace(PersisterSpace persisterSpace) {
		if ( persisterSpace.getSourceAlias() == null ) {
			throw new IllegalArgumentException( "unexpected null persister-reference alias" );
		}
		persisterSpaces.add( persisterSpace );
		aliasXref.put( persisterSpace.getSourceAlias(), persisterSpace );
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isContainedAlias(String alias) {
		return aliasXref.containsKey( alias );
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isContainedExposedProperty(String propertyName) {
		// a matching alias always takes precedence...
		return ( ! isContainedAlias( propertyName ) ) && locatePersisterSpaceExposingProperty( propertyName ) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public PersisterSpace locatePersisterSpaceByAlias(String alias) {
		log.trace( "attempting to resolve [" + alias + "] as persister space alias" );
		return aliasXref.get( alias );
	}

	/**
	 * {@inheritDoc}
	 */
	public PersisterSpace locatePersisterSpaceExposingProperty(String propertyName) {
		log.trace( "attempting to resolve [" + propertyName + "] as unqualified property" );
		PersisterSpace match = null;
		for ( PersisterSpace persisterSpace : persisterSpaces ) {
			if ( persisterSpace.contansProperty( propertyName ) ) {
				if ( match != null ) {
					// todo : better exception type
					throw new QueryException( "multiple persisters contained property [" + propertyName + "]" );
				}
				match = persisterSpace;
			}
		}
		return match;
	}

}
