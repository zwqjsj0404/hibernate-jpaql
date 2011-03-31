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

package org.hibernate.sql.ast.origin.hql.resolve.path;

import org.hibernate.sql.ast.origin.hql.resolve.PersisterSpace;
import org.hibernate.sql.ast.common.HibernateTree;

/**
 * Applies a strategy pattern to the manner in which path expressions are normalized, allowing contextual pluggability
 * of the implicit-join and index-access handling rules...
 *
 * @author Steve Ebersole
 */
public interface PathResolutionStrategy {
	/**
	 * Handle the root of the pathed property reference.
	 *
	 * @param persisterReference The root of the path.
	 *
	 * @return The source representation of the path root.
	 */
	public PathedPropertyReferenceSource handleRoot(PersisterSpace persisterReference);

	/**
	 * Handle an intermeidary path part.
	 *
	 * @param source The source of the property reference.
	 * @param pathPart The current property path part.
	 *
	 * @return The new source for further property part handling.
	 */
	public PathedPropertyReferenceSource handleIntermediatePathPart(PathedPropertyReferenceSource source, String pathPart);

	/**
	 * Handle the terminal path part.
	 *
	 * @param source The source of the property reference.
	 * @param pathPart The current (and terminal/last) path part.
	 *
	 * @return The terminal property reference indicated by the overall path.
	 */
	public HibernateTree handleTerminalPathPart(PathedPropertyReferenceSource source, String pathPart);

	/**
	 * Handle an index access operation (a.b[selector] for example).  In this particular case the index access
	 * is further dereferenced (it is intermediate).
	 *
	 * @param source The source of the property reference.
	 * @param pathPart The current property path part, here specifically naming the collection property
	 * @param selector The index selection expression
	 *
	 * @return The new source for further property part handling.
	 */
	public PathedPropertyReferenceSource handleIntermediateIndexAccess(PathedPropertyReferenceSource source, String pathPart, HibernateTree selector);

	/**
	 * Handle an index access operation (a.b[selector] for example).  In this particular case the index access
	 * is the terminus of the path expression.
	 *
	 * @param source The source of the property reference.
	 * @param pathPart The current property path part, here specifically naming the collection property
	 * @param selector The index selection expression
	 *
	 * @return The terminal property reference indicated by the overall path.
	 */
	public HibernateTree handleTerminalIndexAccess(PathedPropertyReferenceSource source, String pathPart, HibernateTree selector);
}
