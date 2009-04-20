/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2009, Red Hat Middleware LLC or third-party
 * contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */

package org.hibernate.sql.ast.phase.hql.resolve.path;

import org.antlr.runtime.tree.Tree;

import org.hibernate.sql.ast.common.HibernateTree;
import org.hibernate.sql.ast.phase.hql.resolve.PropertyPathTerminus;

/**
 * The contract for representing the non-terminal parts of a property path expression
 * <p/>
 * NOTE : extends AST so the grammar can more easily handle it, not because it will actually end up in the syntax
 * tree (it will not).
 *
 * @author Steve Ebersole
 */
public interface PathedPropertyReferenceSource extends Tree {
	/**
	 * Return the path which led to this source.
	 *
	 * @return The origination path.
	 */
	public String getOriginationPath();

	/**
	 * Handle an intermediate path part reference.
	 *
	 * @param name The name for the path part to handle.
	 *
	 * @return An appropriate source representation of said intermeidate path part.
	 */
	public PathedPropertyReferenceSource handleIntermediatePathPart(String name);

	/**
	 * Handle the terminal path reference.
	 *
	 * @param name The name of the terminal path part.
	 *
	 * @return The property reference terminus.
	 */
	public PropertyPathTerminus handleTerminalPathPart(String name);

	/**
	 * Handle an index access operation (a.b[selector] for example).  In this particular case the index access
	 * is further dereferenced (it is intermediate).
	 *
	 * @param collectionPropertyName The name of the collection property to which the index operator applies
	 * @param selector The index selection expression
	 *
	 * @return An appropriate source representation of said intermeidate path part.
	 */
	public PathedPropertyReferenceSource handleIntermediateIndexAccess(String collectionPropertyName, HibernateTree selector);

	/**
	 * Handle an index access operation (a.b[selector] for example).  In this particular case the index access
	 * is the terminus of the path expression.
	 * 
	 * @param collectionPropertyName The name of the collection property to which the index operator applies
	 * @param selector The index selection expression
	 *
	 * @return The property reference terminus.
	 */
	public PropertyPathTerminus handleTerminalIndexAccess(String collectionPropertyName, HibernateTree selector);
}
