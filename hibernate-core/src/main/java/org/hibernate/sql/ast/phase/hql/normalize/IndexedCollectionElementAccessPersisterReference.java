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
package org.hibernate.sql.ast.phase.hql.normalize;

import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.AssertionFailure;

/**
 * Models the concept of a reference to the element persister of an indexed collection (map, list) which has been
 * accessed or restricted by an index operation.
 *
 * @author Steve Ebersole
 */
public class IndexedCollectionElementAccessPersisterReference extends AbstractPersisterReference {
	private transient QueryableCollection collectionPersister;
	private transient Queryable elementPersister;

	/**
	 * Retrieve the collection "role" identifying the underlying {@link org.hibernate.persister.collection.CollectionPersister}
	 *
	 * @return The collection role
	 */
	public String getCollectionRole() {
		return getFirstChild().getText();
	}

	public String getName() {
		return getCollectionRole() + "[]";
	}

	public boolean isCollection() {
		return false;
	}

	public AssociationType getPersisterType() {
		return elementPersister() == null
				? null
				: ( AssociationType ) elementPersister().getType();
	}

	public QueryableCollection collectionPersister() {
		if ( collectionPersister == null ) {
			collectionPersister = ( QueryableCollection ) normalizationContext().getSessionFactoryImplementor()
					.getCollectionPersister( getCollectionRole() );
		}
		return collectionPersister;
	}

	public Queryable elementPersister() {
		if ( elementPersister == null ) {
			try {
				elementPersister = ( Queryable ) collectionPersister().getElementPersister();
			}
			catch ( AssertionFailure ignore ) {
				// leave it null
			}
		}
		return elementPersister;
	}

	public Type getPropertyType(String propertyName) {
		try {
			return elementPersister().getPropertyType( propertyName );
		}
		catch( Throwable ignore ) {
			return null;
		}
	}

	public boolean containsProperty(String propertyName) {
		return getPropertyType( propertyName ) != null;
	}

	public Type getCollectionIndexType() {
		return collectionPersister().getIndexType();
	}
}
