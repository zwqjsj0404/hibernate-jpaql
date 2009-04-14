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

import org.hibernate.type.Type;
import org.hibernate.type.CollectionType;
import org.hibernate.type.EntityType;
import org.hibernate.HibernateException;
import org.hibernate.sql.ast.util.ASTPrinter;
import org.hibernate.sql.ast.common.Node;
import org.hibernate.sql.ast.alias.ImplicitAliasGenerator;
import org.hibernate.engine.SessionFactoryImplementor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public class PropertyJoinBuilder implements HqlNormalizeTokenTypes {
	private static final Logger log = LoggerFactory.getLogger( PropertyJoinBuilder.class );

	private final NormalizationContext normalizationContext;

	public PropertyJoinBuilder(NormalizationContext normalizationContext) {
		this.normalizationContext = normalizationContext;
	}

	/**
	 * Build a property join node.
	 *
	 * @param lhs The join's left-hand-side persister-reference
	 * @param propertyName The property name.
	 * @param alias The alias to apply to the rhs of the join
	 * @param joinType The type of join (INNER, etc) to produce.
	 * @param propertyFetching should property fetching be applied to the joined persister?
	 * @param associationFetching Should the association making up the property join also be fetched?
	 *
	 * @return The right-hand-side persister-reference.
	 */
	public Join buildPropertyJoin(
			PersisterReference lhs,
			String propertyName,
			String alias,
			Node joinType,
			boolean propertyFetching,
			boolean associationFetching) {
		PersisterReference rhs;
		final Type associationType = lhs.getPropertyType( propertyName );
		if ( associationType.isCollectionType() ) {
			CollectionType collectionType = ( CollectionType ) associationType;
			rhs = persisterReferenceBuilder().buildCollectionPersisterReference(
					collectionType.getRole(),
					alias
			);
		}
		else if ( associationType.isEntityType() ) {
			EntityType entityType = ( EntityType ) associationType;
			String entityName = entityType.getAssociatedEntityName( sessionFactoryImplementor() );
			rhs = persisterReferenceBuilder().buildEntityPersisterReference( entityName, alias );
		}
		else {
			throw new HibernateException(
					"cannot create join on non-association [root=" + lhs.getAlias()
							+ ", name=" + propertyName
							+ ", type=" + associationType.getName() + "]"
			);
		}

		if ( propertyFetching ) {
			normalizationContext.registerPropertyFetch( rhs );
		}

		Join joinNode = buildJoinNode( lhs, rhs, propertyName, joinType, associationFetching );


		if ( log.isTraceEnabled() ) {
			log.trace(
					printer().showAsString(
							joinNode,
							"implicit join : " + lhs.getAlias() + "." + propertyName
					)
			);
		}

		return joinNode;
	}

	public Join buildIndexOperationJoin(
			PersisterReference lhs,
			String collectionPropertyName,
			String alias,
			Node joinType,
			Node selector) {
		final Type associationType = lhs.getPropertyType( collectionPropertyName );
		if ( !associationType.isCollectionType() ) {
			throw new HibernateException(
					"Cannot process index operation against a non-collection property [root=" + lhs.getAlias() +
							", name=" + collectionPropertyName +
							", type=" + associationType.getName()
			);
		}

		CollectionType collectionType = ( CollectionType ) associationType;
		IndexedCollectionElementAccessPersisterReference rhs = persisterReferenceBuilder().buildIndexedCollectionElementAccessPersisterReference(
				collectionType.getRole(),
				alias,
				selector
		);
		if ( rhs.getCollectionIndexType() == null ) {
			throw new HibernateException(
					"Cannot process index operation against a non-indexed collection property [root=" + lhs.getAlias() +
							", name=" + collectionPropertyName +
							", type=" + associationType.getName()
			);
		}

		Join joinNode = buildJoinNode( lhs, rhs, collectionPropertyName, joinType, false );

		if ( log.isTraceEnabled() ) {
			log.trace(
					printer().showAsString(
							joinNode,
							"implicit join : " + lhs.getAlias() + "." + collectionPropertyName + "[]"
					)
			);
		}

		return joinNode;
	}


	private Join buildJoinNode(
			PersisterReference lhs,
			PersisterReference rhs,
			String propertyName,
			Node joinType,
			boolean associationFetching) {
		Join joinNode = ( Join ) createNode( PROPERTY_JOIN, "join" );
		joinNode.setFirstChild( joinType );
		joinNode.addChild( createNode( ASSOCIATION_NAME, propertyName ) );
		joinNode.addChild( rhs );

		if ( associationFetching ) {
			normalizationContext.registerAssociationFetch( joinNode );
		}

		lhs.addChild( joinNode );

		return joinNode;
	}

	// normalization context ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	protected final Node createNode(int type, String text) {
		return ( Node ) normalizationContext.getASTFactory().create( type, text );
	}

	/**
	 * Getter for property 'aliasBuilder'.
	 *
	 * @return Value for property 'aliasBuilder'.
	 */
	protected final ImplicitAliasGenerator aliasBuilder() {
		return normalizationContext.getAliasBuilder();
	}

	/**
	 * Getter for property 'persisterReferenceBuilder'.
	 *
	 * @return Value for property 'persisterReferenceBuilder'.
	 */
	protected final PersisterReferenceBuilder persisterReferenceBuilder() {
		return normalizationContext.getPersisterReferenceBuilder();
	}

	/**
	 * Getter for property 'sessionFactoryImplementor'.
	 *
	 * @return Value for property 'sessionFactoryImplementor'.
	 */
	protected final SessionFactoryImplementor sessionFactoryImplementor() {
		return normalizationContext.getSessionFactoryImplementor();
	}

	/**
	 * Getter for property 'ASTPrinter'.
	 *
	 * @return Value for property 'ASTPrinter'.
	 */
	protected final ASTPrinter printer() {
		return normalizationContext.getPrinter();
	}
}
