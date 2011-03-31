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
package org.hibernate.sql.ast;

import antlr.SemanticException;
import antlr.Token;
import antlr.collections.AST;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Thrown when a call to the underlying Hibernate engine fails, indicating some form of semantic exception (e.g. an
 * entity name was not found in the mappings, etc.).
 *
 * @author Joshua Davis
 * @author Steve Ebersole
 */
public class DetailedSemanticException extends SemanticException {
	private Throwable cause;

	public DetailedSemanticException(String message) {
		super( message );
	}

	public DetailedSemanticException(String message, Token token) {
		super( message, token.getFilename(), token.getLine(), token.getColumn() );
	}

	public DetailedSemanticException(String message, AST ast) {
		super( message, "", ast.getLine(), ast.getColumn() );
	}

	public DetailedSemanticException(String message, Throwable e) {
		this( message );
		cause = e;
	}

	public DetailedSemanticException(String message, Token token, Throwable e) {
		this( message, token );
		cause = e;
	}

	public DetailedSemanticException(String message, AST ast, Throwable e) {
		this( message, ast );
		cause = e;
	}

	/**
	 * Converts everything to a string.
	 *
	 * @return a string.
	 */
	public String toString() {
		if ( cause == null ) {
			return super.toString();
		}
		else {
			return super.toString() + "\n[cause=" + cause.toString() + "]";
		}
	}

	/**
	 * Prints a stack trace.
	 */
	public void printStackTrace() {
		super.printStackTrace();
		if ( cause != null ) {
			cause.printStackTrace();
		}
	}

	/**
	 * Prints a stack trace to the specified print stream.
	 *
	 * @param s the print stream.
	 */
	public void printStackTrace(PrintStream s) {
		super.printStackTrace( s );
		if ( cause != null ) {
			s.println( "Cause:" );
			cause.printStackTrace( s );
		}
	}

	/**
	 * Prints this throwable and its backtrace to the specified print writer.
	 *
	 * @param w the print writer.s
	 */
	public void printStackTrace(PrintWriter w) {
		super.printStackTrace( w );
		if ( cause != null ) {
			w.println( "Cause:" );
			cause.printStackTrace( w );
		}
	}

}
