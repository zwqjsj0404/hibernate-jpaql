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

import antlr.Token;
import antlr.collections.AST;

/**
 * Basic AST node
 *
 * @author Joshua Davis
 * @author Steve Ebersole
 */
public class Node extends antlr.CommonAST {
	private String filename;
	private int line;
	private int column;

	public Node() {
		super();
	}

	public Node(Token tok) {
		super( tok );  // NOTE: This will call initialize(tok)!
	}

	/**
	 * Retrieve the textual representation of a node to be used in the resulting sql.
	 * <p/>
	 * This is intended for subclasses to override to allow certain nodes to provide their own renderable representation
	 * instead of the default {@link antlr.collections.AST#getText()}.
	 *
	 * @return The renderable text.
	 */
	public String getRenderableText() {
		return getText();
	}

	public void initialize(Token token) {
		super.initialize( token );
		// Propagate line/column information from the lexer during
		// stream parsing.
		filename = token.getFilename();
		line = token.getLine();
		column = token.getColumn();
	}

	public void initialize(AST ast) {
		super.initialize( ast );
		if ( ast instanceof Node ) {
			// Propagate line/column information from the source AST during tree walking.
			transferTrackingInfo( ( Node ) ast );
		}
	}

	public void transferTrackingInfo(AST ast) {
		if ( ast instanceof Node ) {
			transferTrackingInfo( ( Node ) ast );
		}
		else {
			line = ast.getLine();
			column = ast.getColumn();
		}
	}

	public void transferTrackingInfo(Node node) {
		filename = node.filename;
		line = node.line;
		column = node.column;
	}

	public String getFilename() {
		return filename;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
}
