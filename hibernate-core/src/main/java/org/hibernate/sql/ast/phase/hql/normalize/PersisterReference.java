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

import antlr.collections.AST;

import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;

/**
 * Contract for persister references.
 *
 * @author Steve Ebersole
 */
public interface PersisterReference extends AST, PropertyReferenceSource {
	/**
	 * Get the alias assigned to this persister reference.  May be explcitly defined by the query author or
	 * supplied by {@link org.hibernate.sql.ast.alias.ImplicitAliasGenerator#buildUniqueImplicitAlias()}, but all persister references will have an alias.
	 *
	 * @return This persister reference's alias.
	 */
	public String getAlias();

	/**
	 * Get the name of this persister reference.  This is just some descriptive text.
	 * 
	 * @return The name.
	 */
	public String getName();

	/**
	 * Is this persister reference representing a collection persister?
	 *
	 * @return True/false.
	 */
	public boolean isCollection();

	/**
	 * Get the type mapping of the underlying persister,
	 *
 	 * @return The unerlygin persister's type mapping.
	 */
	public AssociationType getPersisterType();

	/**
	 * Get the type mapping of a property originating from this persister reference.
	 *
	 * @param propertyName The property name.
	 * @return The type mapping.
	 */
	public Type getPropertyType(String propertyName);

	/**
	 * Does this persister reference represent a persister which contains a property of the given name?
	 *
	 * @param propertyName The property name.
	 * @return True/false.
	 */
	public boolean containsProperty(String propertyName);

	/**
	 * Locate a reusable property join originating from this persister reference.
	 *
	 * @param path The property path.
	 * @return The reusable join, or null if none yet {@link #registerReusablePropertyJoin registered}.
	 */
	public Join locateReusablePropertyJoin(String path);

	/**
	 * Register a reusable property join.
	 *
	 * @param path The property path
	 * @param join The join to be registered.
	 */
	public void registerReusablePropertyJoin(String path, Join join);
}
