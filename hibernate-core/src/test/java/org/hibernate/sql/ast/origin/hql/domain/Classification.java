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

import java.io.Serializable;
import java.util.HashMap;

/**
 * Mimic a JDK 5 enum.
 *
 * @author Steve Ebersole
 */
public class Classification implements Serializable, Comparable {

	public static final Classification COOL = new Classification( "COOL", 0 );
	public static final Classification LAME = new Classification( "LAME", 1 );

	private static final HashMap INSTANCES = new HashMap();
	static {
		INSTANCES.put( COOL.name, COOL );
		INSTANCES.put( LAME.name, LAME );
	}

	private final String name;
	private final int ordinal;
	private final int hashCode;

	private Classification(String name, int ordinal) {
		this.name = name;
		this.ordinal = ordinal;

		int hashCode = name.hashCode();
		hashCode = 29 * hashCode + ordinal;
		this.hashCode = hashCode;
	}

	public String name() {
		return name;
	}

	public int ordinal() {
		return ordinal;
	}

	public boolean equals(Object obj) {
		return compareTo( obj ) == 0;
	}

	public int compareTo(Object o) {
		int otherOrdinal = ( ( Classification ) o ).ordinal;
		if ( ordinal == otherOrdinal ) {
			return 0;
		}
		else if ( ordinal > otherOrdinal ) {
			return 1;
		}
		else {
			return -1;
		}
	}

	public int hashCode() {
		return hashCode;
	}

	public static Classification valueOf(String name) {
		return ( Classification ) INSTANCES.get( name );
	}

	public static Classification valueOf(Integer ordinal) {
		if ( ordinal == null ) {
			return null;
		}
		switch ( ordinal.intValue() ) {
			case 0: return COOL;
			case 1: return LAME;
			default: throw new IllegalArgumentException( "unknown classification ordinal [" + ordinal + "]" );
		}
	}
}
