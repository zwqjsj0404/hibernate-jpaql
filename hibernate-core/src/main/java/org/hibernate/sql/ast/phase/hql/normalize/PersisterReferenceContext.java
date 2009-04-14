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

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public interface PersisterReferenceContext {
	/**
	 * Does the given text represent an alias for a persister within this context?
	 *
	 * @param text The potential persister alias.
	 * @return True if given text is a persister alias; false otherwise.
	 */
	public boolean isContainedAlias(String text);

	/**
	 * Does the given text represent a property exposed from a persister in this context?
	 *
	 * @param propertyName The potential property name.
	 * @return True if a persister in this context exposes such a property; false otherwise.
	 */
	public boolean isContainedExposedProperty(String propertyName);

	/**
	 * ???
	 * todo : needed?
	 *
	 * @param aliasOrPropertyName
	 * @return
	 */
	public PersisterReference locatePersisterReference(String aliasOrPropertyName);

	/**
	 * Locate a {@link PersisterReference} by alias.
	 *
	 * @param alias The alias by which to locate the persister reference.
	 * @return The persister reference, or null.
	 */
	public PersisterReference locatePersisterReferenceByAlias(String alias);

	/**
	 * Locate an persister reference in this context which exposes the
	 * specified property.
	 *
	 * @param propertyName The name of the property.
	 * @return The persister reference, or null.
	 */
	public PersisterReference locatePersisterReferenceExposingProperty(String propertyName);

	/**
	 * Registers a persister reference in this context.
	 *
	 * @param persisterReference The persister reference to register.
	 */
	public void registerPersisterReference(PersisterReference persisterReference);

	public void registerPersisterReferenceImplicitInDerivedSelectClause(PersisterReference persisterReference);

	public List getPersisterReferencesImplicitInDerivedSelectClause();

}
