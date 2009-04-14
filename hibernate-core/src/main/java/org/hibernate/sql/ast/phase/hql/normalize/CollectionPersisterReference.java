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

import org.hibernate.sql.ast.util.DisplayableNode;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.AssertionFailure;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;
import org.hibernate.type.EntityType;
import org.hibernate.type.ComponentType;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Queryable;

/**
 * Represents a reference to an {@link org.hibernate.persister.collection.CollectionPersister}
 *
 * @author Steve Ebersole
 */
public class CollectionPersisterReference extends AbstractPersisterReference implements DisplayableNode {
	private transient QueryableCollection persister;

	/**
	 * Retrieve the collection "role" identifying the underlying {@link org.hibernate.persister.collection.CollectionPersister}
	 *
	 * @return The collection role
	 */
	public String getRole() {
		return getFirstChild().getText();
	}

	public String getAlias() {
		return getFirstChild().getNextSibling().getText();
	}

	public String getName() {
		return getRole();
	}

	public boolean isCollection() {
		return true;
	}

	/**
	 * Retrieve a reference to the underlying collection persister.
	 *
	 * @return The collection persister.
	 */
	public QueryableCollection getCollectionPersister() {
		if ( persister == null ) {
			persister = ( QueryableCollection ) getSessionFactory().getCollectionPersister( getRole() );
		}
		return persister;
	}

	/**
	 * {@inheritDoc}
	 */
	public AssociationType getPersisterType() {
		return ( AssociationType ) getCollectionPersister().getElementType();
	}

	public Type getPropertyType(String propertyName) {
		// this should not be called for "collection properties" (i.e., SIZE, MAXELEMENT, etc)...
		Type elementType = getCollectionPersister().getElementType();
		if ( elementType.isAssociationType() ) {
			// a collection of entities
			EntityType elementEntityType = ( EntityType ) elementType;
			try {
				Queryable elementEntityPersister = ( Queryable ) elementEntityType
						.getAssociatedJoinable( getSessionFactory() );
				return elementEntityPersister.getPropertyType( propertyName );
			}
			catch( Throwable ignore ) {
				// ignore
			}
		}
		else if ( elementType.isComponentType() ) {
			ComponentType elementComponentType = ( ComponentType ) elementType;
			String[] subPropertyNames = elementComponentType.getPropertyNames();
			for ( int i = 0; i < subPropertyNames.length; i++ ) {
				if ( subPropertyNames[i].equals( propertyName ) ) {
					return elementComponentType.getSubtypes()[i];
				}
			}
		}
		return null;
	}

	public boolean containsProperty(String propertyName) {
		return getPropertyType( propertyName ) != null;
	}

	private SessionFactoryImplementor getSessionFactory() {
		if ( normalizationContext() == null ) {
			throw new AssertionFailure( "resolution context was null on attempt to retrieve session factory reference" );
		}
		return normalizationContext().getSessionFactoryImplementor();
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "CollectionPersisterReference [role=" + getRole()
				+ ",alias=" + getAlias()
				+ ",element-type=" + getCollectionPersister().getElementType().getName()
				+ "]";
	}

	public String getDisplayText() {
		return getRole() + " (" + getAlias() + ")";
	}
}
