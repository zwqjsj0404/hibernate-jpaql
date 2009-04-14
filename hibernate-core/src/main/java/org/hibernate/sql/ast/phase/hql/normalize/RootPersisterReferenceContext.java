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
package org.hibernate.sql.ast.phase.hql.normalize;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.QueryException;

/**
 * Defines the contract for implementors of a "context" or a "scope" for references to persisters.  Generally speaking,
 * this maps to the notion of a FROM clause in a SELECT statement.  However, DML operations also have a notion of a
 * persister reference.  This, then, acts as the abstraction of these grouped references to persisters.
 *
 * @see PersisterReference
 *
 * @author Steve Ebersole
 */
public class RootPersisterReferenceContext implements PersisterReferenceContext {
	private static final Logger log = LoggerFactory.getLogger( RootPersisterReferenceContext.class );

	private List persisterReferences = new ArrayList();
	private List persisterReferencesImplicitInDerivedSelectClause;
	private Map aliasXref = new HashMap();

	/**
	 * {@inheritDoc}
	 */
	public void registerPersisterReference(PersisterReference persisterReference) {
		if ( persisterReference.getAlias() == null ) {
			throw new IllegalArgumentException( "unexpected null persister-reference alias" );
		}
		persisterReferences.add( persisterReference );
		aliasXref.put( persisterReference.getAlias(), persisterReference );
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerPersisterReferenceImplicitInDerivedSelectClause(PersisterReference persisterReference) {
		if ( persisterReferencesImplicitInDerivedSelectClause == null ) {
			persisterReferencesImplicitInDerivedSelectClause = new ArrayList();
		}
		persisterReferencesImplicitInDerivedSelectClause.add( persisterReference );
	}

	/**
	 * {@inheritDoc}
	 */
	public List getPersisterReferencesImplicitInDerivedSelectClause() {
		return persisterReferencesImplicitInDerivedSelectClause;
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
		return ( ! isContainedAlias( propertyName ) ) && locatePersisterReferenceExposingProperty( propertyName ) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public PersisterReference locatePersisterReference(String aliasOrPropertyName) {
		PersisterReference persisterReference = locatePersisterReferenceByAlias( aliasOrPropertyName );
		if ( persisterReference == null ) {
			persisterReference = locatePersisterReferenceExposingProperty( aliasOrPropertyName );
		}
		return persisterReference;
	}

	/**
	 * {@inheritDoc}
	 */
	public PersisterReference locatePersisterReferenceByAlias(String alias) {
		log.trace( "attempting to resolve [" + alias + "] as persister reference alias" );
		return ( PersisterReference ) aliasXref.get( alias );
	}

	/**
	 * {@inheritDoc}
	 */
	public PersisterReference locatePersisterReferenceExposingProperty(String propertyName) {
		log.trace( "attempting to resolve [" + propertyName + "] as unqualified property" );
		PersisterReference persisterReference = null;
		Iterator itr = persisterReferences.iterator();
		while( itr.hasNext() ) {
			final PersisterReference test = ( PersisterReference ) itr.next();
			if ( test.containsProperty( propertyName ) ) {
				if ( persisterReference != null ) {
					// todo : better exception type
					throw new QueryException( "multiple persisters contained property [" + propertyName + "]" );
				}
				persisterReference = test;
			}
		}
		return persisterReference;
	}
}
