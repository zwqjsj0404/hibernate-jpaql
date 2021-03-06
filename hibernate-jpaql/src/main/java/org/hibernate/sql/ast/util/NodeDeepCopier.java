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

import antlr.collections.AST;
import antlr.ASTFactory;

/**
 * Strategy for creating copies of AST trees.
 *
 * @author Steve Ebersole
 */
public class NodeDeepCopier {
	private final ASTFactory astFactory;

	public NodeDeepCopier(ASTFactory astFactory) {
		this.astFactory = astFactory;
	}

	public AST copy(AST node) {
		// copy the root (incoming) node
		AST newNode = createShallowCopy( node );
		// track a running reference to the child node we are currently processing.
		AST child = node.getFirstChild();
		while ( child != null ) {
			// create a deep copy of the current child and add it as a child to the copied root
			newNode.addChild( copy( child ) );
			// spin forward to the next child
			child = child.getNextSibling();
		}
		return newNode;
	}

	/**
	 * Creates a shallow (non-linked) copy of an AST node.
	 *
	 * @param node The AST node to shallow copy.
	 *
	 * @return The shallow copy.
	 */
	public AST createShallowCopy(AST node) {
		if ( node instanceof CopyableNode ) {
			return ( ( CopyableNode ) node ).createCopy();
		}
		else {
			AST newNode = astFactory.create( node.getType(), node.getText() );
			newNode.initialize( node );
			return newNode;
		}
	}
}
