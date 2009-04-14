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

import java.util.Iterator;

import antlr.collections.AST;

import org.hibernate.sql.ast.util.DisplayableNode;
import org.hibernate.sql.ast.util.ASTChildIterator;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.AssertionFailure;
import org.hibernate.util.EmptyIterator;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;
import org.hibernate.persister.entity.Queryable;

/**
 * Represents a reference to an {@link org.hibernate.persister.entity.EntityPersister}
 *
 * @author Steve Ebersole
 */
public class EntityPersisterReference extends AbstractPersisterReference implements DisplayableNode {
	private transient Queryable persister;

	/**
	 * Retrieve the entity name of the underlying {@link org.hibernate.persister.entity.EntityPersister}
	 *
	 * @return The entity name
	 */
	public String getEntityName() {
		return getFirstChild().getText();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAlias() {
		return getFirstChild().getNextSibling().getText();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return getEntityName();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCollection() {
		return false;
	}

	/**
	 * Retrieves a reference to the underlying entity persister.
	 *
	 * @return The underlying entity persister.
	 */
	public Queryable getEntityPersister() {
		if ( persister == null ) {
			persister = ( Queryable ) getSessionFactory().getEntityPersister( getEntityName() );
		}
		return persister;
	}

	/**
	 * {@inheritDoc}
	 */
	public AssociationType getPersisterType() {
		return ( AssociationType ) getEntityPersister().getType();
	}

	public Type getPropertyType(String propertyName) {
		try {
			return getEntityPersister().getPropertyType( propertyName );
		}
		catch( Throwable ignore ) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsProperty(String propertyName) {
		return getPropertyType( propertyName ) != null;
	}

	private SessionFactoryImplementor getSessionFactory() {
		if ( normalizationContext() == null ) {
			throw new AssertionFailure( "normalizatioon context was null on attempt to retrieve session factory reference" );
		}
		return normalizationContext().getSessionFactoryImplementor();
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "EntityPersisterReference [entity-name=" + getEntityName() + ",alias=" + getAlias() + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayText() {
		return getEntityName() + " (" + getAlias() + ")";
	}

	public Iterator locateJoins() {
		AST firstJoin = getFirstChild().getNextSibling().getNextSibling();
		if ( firstJoin == null ) {
			return EmptyIterator.INSTANCE;
		}
		else {
			return new ASTChildIterator( firstJoin );
		}
	}
}
