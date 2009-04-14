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
package org.hibernate.sql.ast.common;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public abstract class AbstractToken extends antlr.CommonToken {
	private int previousTokenType;

	/**
	 * Getter for property 'previousTokenType'.
	 *
	 * @return Value for property 'previousTokenType'.
	 */
	public int getPreviousTokenType() {
		return previousTokenType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setType(int type) {
		this.previousTokenType = getType();
		super.setType( type );
	}

	/**
	 * {@inheritDoc}
	 */
	public String toDisplayString() {
		StringBuffer text = new StringBuffer( super.toString() );
		text.append( "['" )
				.append( getText() )
				.append( "', <" )
				.append( getType() )
				.append( "> previously: <" )
				.append( getPreviousTokenType() )
				.append( ">, line=" )
				.append( line )
				.append( ", col=" )
				.append( col );
		appendTextualInfo( text );
		text.append( "]" );
		return text.toString();
	}

	protected void appendTextualInfo(StringBuffer text) {
	}

	public String getLocation() {
		return getLine() + ":" + getColumn();
	}
}
