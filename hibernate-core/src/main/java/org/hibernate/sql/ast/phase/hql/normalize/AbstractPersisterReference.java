/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2009, Red Hat Middleware LLC or third-party contributors as
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
package org.hibernate.sql.ast.phase.hql.normalize;

import java.util.Map;
import java.util.HashMap;

import org.hibernate.sql.ast.common.Node;

/**
 * Base class for {@link PersisterReference} implementations.  Mainly it centralizes handling
 * of reusable property joins.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractPersisterReference extends Node implements PersisterReference, NormalizationContextAwareNode {
	private NormalizationContext normalizationContext;
	private Map reusablePropertyJoins = new HashMap();

	/**
	 * {@inheritDoc}
	 */
	public Join locateReusablePropertyJoin(String path) {
		return ( Join ) reusablePropertyJoins.get( path );
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerReusablePropertyJoin(String path, Join join) {
		reusablePropertyJoins.put( path, join );
	}

	/**
	 * {@inheritDoc}
	 */
	public PersisterReference locatePersisterReference() {
		return this;
	}

	/**
	 * Getter for property 'aliasNode'.
	 *
	 * @return Value for property 'aliasNode'.
	 */
	protected Node getAliasNode() {
		return ( Node ) getFirstChild().getNextSibling();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAlias() {
		return getAliasNode().getText();
	}

	/**
	 * {@inheritDoc}
	 */
	public void injectNormalizationContext(NormalizationContext normalizationContext) {
		this.normalizationContext = normalizationContext;
	}

	protected NormalizationContext normalizationContext() {
		return normalizationContext;
	}
}
