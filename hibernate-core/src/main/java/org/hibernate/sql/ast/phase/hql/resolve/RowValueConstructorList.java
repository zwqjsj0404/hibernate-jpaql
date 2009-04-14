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
package org.hibernate.sql.ast.phase.hql.resolve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.sql.ast.common.Node;
import org.hibernate.type.Type;
import org.hibernate.type.ComponentType;

import antlr.collections.AST;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public class RowValueConstructorList extends Node implements ExpectedTypeAware {
	private static final Logger log = LoggerFactory.getLogger( RowValueConstructorList.class );

	private Type hibernateType;

	public RowValueConstructorList() {
		setType( HqlResolveTokenTypes.ROW_VALUE_CONSTRUCTOR_LIST );
		setText( "row-value-ctor-list" );
	}

	public void setExpectedType(Type expectedType) {
		if ( hibernateType != null ) {
			log.info( "Over-setting expected type [{} -> {}]", hibernateType.getName(), expectedType.getName() );
		}
		hibernateType = expectedType;
		if ( expectedType.isComponentType() ) {
			final ComponentType componentType = ( ComponentType ) expectedType;
			final Type[] subtypes = componentType.getSubtypes();
			final int length = subtypes.length;
			if ( length == getNumberOfChildren() ) {
				AST child = getFirstChild();
				for ( int i = 0, max = subtypes.length; i < max; i++ ) {
					if ( child instanceof ExpectedTypeAware ) {
						log.debug( "propogating expected type info for component sub-property [{}]", componentType.getPropertyNames()[i] );
						( ( ExpectedTypeAware ) child ).setExpectedType( subtypes[i] );
					}
					child = child.getNextSibling();
				}
			}
		}
	}

	public Type getHibernateType() {
		return hibernateType;
	}
}
