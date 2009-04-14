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
package org.hibernate.sql.ast.util;

import antlr.RecognitionException;

/**
 * Contract for handling error originating from the Antlr {@link antlr.TreeParser base} parser.  In fact,
 * these methods are all redefinitions of the error handling methods defined there.
 *
 * @author Joshua Davis
 * @author Steve Ebersole
 */
public interface ErrorHandler {
	/**
	 * Handle delegation of {@link antlr.TreeParser#reportError(antlr.RecognitionException)}
	 *
	 * @param recognitionException The Antlr recognition exception
	 */
	public void reportError(RecognitionException recognitionException);

	/**
	 * Handle delegation of {@link antlr.TreeParser#reportError(java.lang.String)}
	 *
	 * @param message The error message
	 */
	public void reportError(String message);

	/**
	 * Handle delegation of {@link antlr.TreeParser#reportWarning(String)}
	 *
	 * @param message The warning message
	 */
	public void reportWarning(String message);
}