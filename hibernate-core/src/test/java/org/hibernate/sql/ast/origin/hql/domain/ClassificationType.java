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
package org.hibernate.sql.ast.origin.hql.domain;

import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.HibernateException;
import org.hibernate.Hibernate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.io.Serializable;

/**
 * A custom type for mapping {@link Classification} instances
 * to the respective db column.
 * </p>
 * THis is largely intended to mimic JDK5 enum support in JPA.  Here we are
 * using the approach of storing the ordinal values, rather than the names.
 *
 * @author Steve Ebersole
 */
public class ClassificationType implements EnhancedUserType {

	public int[] sqlTypes() {
		return new int[] { Types.TINYINT };
	}

	public Class returnedClass() {
		return Classification.class;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if ( x == null && y == null ) {
			return false;
		}
		else if ( x != null ) {
			return x.equals( y );
		}
		else {
			return y.equals( x );
		}
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		Integer ordinal = ( Integer ) Hibernate.INTEGER.nullSafeGet( rs, names[0] );
		return Classification.valueOf( ordinal );
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		Integer ordinal = value == null ? null : new Integer( ( ( Classification ) value ).ordinal() );
		Hibernate.INTEGER.nullSafeSet( st, ordinal, index );
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public boolean isMutable() {
		return false;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return ( Classification ) value;
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	public String objectToSQLString(Object value) {
		return extractOrdinalString( value );
	}

	public String toXMLString(Object value) {
		return extractName( value );
	}

	public Object fromXMLString(String xmlValue) {
		return Classification.valueOf( xmlValue );
	}

	private String extractName(Object obj) {
		return ( ( Classification ) obj ).name();
	}

	private int extractOrdinal(Object value) {
		return ( ( Classification ) value ).ordinal();
	}

	private String extractOrdinalString(Object value) {
		return Integer.toString( extractOrdinal( value ) );
	}
}
