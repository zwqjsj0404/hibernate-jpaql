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
package org.hibernate.sql.ast.phase.hql.normalize.path;

import org.hibernate.sql.ast.phase.hql.normalize.PersisterReference;
import org.hibernate.sql.ast.phase.hql.normalize.Ident;
import org.hibernate.sql.ast.phase.hql.normalize.PropertyPathTerminus;
import org.hibernate.sql.ast.common.Node;

/**
 * Applies a strategy pattern to the manner in which path expressions are normalized, allowing contextual pluggability
 * of the implicit-join and index-access handling rules...
 *
 * @author Steve Ebersole
 */
public interface PathNormalizationStrategy {
	/**
	 * Handle the root of the pathed property reference path.
	 *
	 * @param persisterReference The root of the path.
	 *
	 * @return The source representation of the path root.
	 */
	public PathedPropertyReferenceSource handleRoot(PersisterReference persisterReference);

	/**
	 * Handle an intermeidary path part.
	 *
	 * @param source The source of the property reference.
	 * @param pathPart The current property path part.
	 *
	 * @return The new source for further property part handling.
	 */
	public PathedPropertyReferenceSource handleIntermediatePathPart(PathedPropertyReferenceSource source, Ident pathPart);

	/**
	 * Handle the terminal path part.
	 *
	 * @param source The source of the property reference.
	 * @param pathPart The current (and terminal/last) path part.
	 *
	 * @return The terminal property reference indicated by the overall path.
	 */
	public PropertyPathTerminus handleTerminalPathPart(PathedPropertyReferenceSource source, Ident pathPart);
	
	public PathedPropertyReferenceSource handleIntermediateIndexAccess(PathedPropertyReferenceSource source, Ident name, Node selector);

	public PropertyPathTerminus handleTerminalIndexAccess(PathedPropertyReferenceSource source, Ident name, Node selector);
}
