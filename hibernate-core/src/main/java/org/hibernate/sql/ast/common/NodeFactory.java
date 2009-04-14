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

import antlr.ASTFactory;

/**
 * An {@link ASTFactory} which includes line/column tracking information into its generated
 * {@link antlr.collections.AST nodes}.
 *
 * @author Joshua Davis
 * @author Steve Ebersole
 */
public class NodeFactory extends ASTFactory {
	/**
	 * Determine the default {@link antlr.collections.AST node} class to use.  Used from within
	 * {@link #getASTNodeType(int)} when no specific  override is defined/needed for a particular token type.
	 *
	 * @return The default {@link antlr.collections.AST node} class to use
	 */
	protected Class determineDefaultNodeClass() {
		return Node.class;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class getASTNodeType(int tokenType) {
		return determineDefaultNodeClass();
	}
}
