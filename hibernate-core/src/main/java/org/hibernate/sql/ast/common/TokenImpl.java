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
 * A custom token class for various Hibernate lexers; specifically we are adding the
 * {@link #isPossibleIdentifier() keyword-as-identifier} capability and tracking of any changes in  token types
 * via {@link #getPreviousTokenType()}.
 *
 * @author Steve Ebersole
 */
public class TokenImpl extends AbstractToken {
	private boolean possibleIdentifier;

	/**
	 * Getter for property 'possibleIdentifier'.
	 *
	 * @return Value for property 'possibleIdentifier'.
	 */
	public boolean isPossibleIdentifier() {
		return possibleIdentifier;
	}

	/**
	 * Setter for property 'possibleIdentifier'.
	 *
	 * @param possibleIdentifier Value to set for property 'possibleIdentifier'.
	 */
	public void setPossibleIdentifier(boolean possibleIdentifier) {
		this.possibleIdentifier = possibleIdentifier;
	}

	protected void appendTextualInfo(StringBuffer text) {
		text.append( ",possibleIdentifier?=" ).append( possibleIdentifier );
	}

}
