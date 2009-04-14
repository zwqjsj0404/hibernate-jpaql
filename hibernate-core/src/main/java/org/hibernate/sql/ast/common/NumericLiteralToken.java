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
 * A custom token class for representing numeric literals.  The reasoning is that there are 2 classifications
 * by which we need to identify numeric literals:<ol>
 * <li>The SQL notions of <tt>exact</tt> and <tt>approximate</tt></li>
 * <li>The java language types (i.e. <tt>int</tt>, <tt>long</tt>, etc)</li>
 * </ol>
 * <p/>
 * Explicitly handling the intersection of these two classification sets is unwieldy (EXACT_INTEGER_LITERAL, etc) so
 * we instead track one classification by the token-types, and the other is tracked by state on the token.  Since we
 * generally need to treat with the java types, we use this custom token to track whether the literal is  <tt>exact</tt>
 * or <tt>approximate</tt>.
 *
 * @author Steve Ebersole
 */
public class NumericLiteralToken extends AbstractToken {
	private boolean isApproximate = false; // assume exact

	public boolean isApproximate() {
		return isApproximate;
	}

	public void setApproximate(boolean approximate) {
		isApproximate = approximate;
	}
}
