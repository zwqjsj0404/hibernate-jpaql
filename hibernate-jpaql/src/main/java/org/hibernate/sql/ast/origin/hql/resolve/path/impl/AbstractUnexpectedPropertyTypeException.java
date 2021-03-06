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

/**
 * Basic exception type definition for indicating that the type of a referenced property did match the type
 * expected given its position and semantic within the query.
 *
 * @author Steve Ebersole
 */
public class AbstractUnexpectedPropertyTypeException extends QueryParseException {
	private final String path;
	private final String persisterName;
	private final String propertyName;

	public AbstractUnexpectedPropertyTypeException(String path, String persisterName, String propertyName) {
		super("path: '" + path + "' property: '" + propertyName + "'");
		this.path = path;
		this.persisterName = persisterName;
		this.propertyName = propertyName; 
	}

	@Override
	protected String internalGetMessage() {
		return "Referenced property [" + buildPropertyReferenceFragment() + "] was not of expected type";
	}

	protected String buildPropertyReferenceFragment() {
		return path + " (" + persisterName + ")." + propertyName;
	}

	public String getPath() {
		return path;
	}

	public String getPersisterName() {
		return persisterName;
	}

	public String getPropertyName() {
		return propertyName;
	}
}
