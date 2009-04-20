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

package org.hibernate.sql.ast.phase.hql.resolve;

import org.antlr.runtime.tree.TreeAdaptor;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.sql.ast.alias.ImplicitAliasGenerator;
import org.hibernate.sql.ast.util.TreePrinter;
import org.hibernate.sql.ast.phase.hql.resolve.path.PathResolutionStrategy;

/**
 * todo : javadocs
 *
 * @author Steve Ebersole
 */
public interface ResolutionContext {

	/**
	 * The session factory available for this context.  Providing, for example, mapping information.
	 *
	 * @return The session factory.
	 */
	public SessionFactoryImplementor getSessionFactoryImplementor();

	public TreeAdaptor getTreeAdaptor();

//	/**
//	 * The alias builder available in this context.
//	 *
//	 * @return The alias builder.
//	 */
//	public ImplicitAliasGenerator getAliasBuilder();

	/**
	 * The current {@link PersisterSpaceContext} for this context.  The {@link PersisterSpaceContext}
	 * can change in relation to subqueries and such.  See {@link PersisterSpaceContext} docs for more info.
	 *
	 * @return The current {@link PersisterSpaceContext} for this resolution context.
	 */
	public PersisterSpaceContext getCurrentPersisterSpaceContext();

//	/**
//	 * The builder of {@link PersisterReference} instances for this context.
//	 *
//	 * @return The {@link PersisterReference} builder.
//	 */
//	public PersisterReferenceBuilder getPersisterReferenceBuilder();
//
//	/**
//	 * The builder of {@link Join} instances pertaining to property joins for this context.
//	 *
//	 * @return The property {@link Join} builder.
//	 */
//	public PropertyJoinBuilder getPropertyJoinBuilder();

	/**
	 * The tree printer available for this context.
	 *
	 * @return The tree printer.
	 */
	public TreePrinter getTreePrinter();

	/**
	 * Is this context currently processing a function?
	 *
	 * @return True or false.
	 */
	public boolean isCurrentlyProcessingFunction();

	public PathResolutionStrategy getCurrentPathResolutionStrategy();

//	public void registerAssociationFetch(Join join);
//
//	public void registerPropertyFetch(PersisterReference persisterReference);
}
