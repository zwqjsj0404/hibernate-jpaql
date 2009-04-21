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

package org.hibernate.sql.ast.phase.hql.resolve.path.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.HibernateException;
import org.hibernate.hql.CollectionProperties;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.ast.common.HibernateTree;
import org.hibernate.sql.ast.phase.hql.resolve.PersisterSpace;
import org.hibernate.sql.ast.phase.hql.resolve.PropertyPathTerminus;
import org.hibernate.sql.ast.phase.hql.resolve.ResolutionContext;
import org.hibernate.sql.ast.phase.hql.resolve.path.PathedPropertyReferenceSource;
import org.hibernate.sql.ast.tree.Table;
import org.hibernate.type.ComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

/**
 * todo : javadocs
 *
 * @author Steve Ebersole
 */
public class BasicPathResolutionStrategySupport extends AbstractPathResolutionStrategy {
	private static final Logger log = LoggerFactory.getLogger( BasicPathResolutionStrategySupport.class );


	public BasicPathResolutionStrategySupport(ResolutionContext resolutionContext) {
		super( resolutionContext );
	}

	protected PathedPropertyReferenceSource internalHandleRoot(PersisterSpace persisterSpace) {
		return new RootSourceImpl( persisterSpace );
	}

	private PathedPropertyReferenceSource determineAppropriateIntermediateSourceType(PersisterSpace origin, String propertyName) {
		final Type propertyType = origin.getPropertyType( propertyName );
		if ( propertyType.isComponentType() ) {
			return new ComponentIntermediatePathSource( origin, propertyName, ( ComponentType ) propertyType );
		}
		else if ( propertyType.isEntityType() ) {
			return new EntityIntermediatePathSource( origin, propertyName );
		}
		else if ( propertyType.isCollectionType() ) {
			return new CollectionIntermediatePathSource( origin, propertyName );
		}
		else {
			return new SimpleIntermediatePathSource();
		}
	}

	/**
	 * Is the given property name a reference to the primary key of the associated
	 * entity construed by the given entity type?
	 * <p/>
	 * For example, consider a fragment like order.customer.id
	 * (where order is a from-element alias).  Here, we'd have:
	 * propertyName = "id" AND
	 * owningType = ManyToOneType(Customer)
	 * and are being asked to determine whether "customer.id" is a reference
	 * to customer's PK...
	 *
	 * @param propertyName The name of the property to check.
	 * @param owningType The type represeting the entity "owning" the property
	 * @return True if propertyName references the enti ty's (owningType->associatedEntity)
	 * primary key; false otherwise.
	 */
	private boolean isReferenceToPrimaryKey(EntityType owningType, String propertyName) {
		EntityPersister persister = getSessionFactoryImplementor().getEntityPersister(
				owningType.getAssociatedEntityName( getSessionFactoryImplementor() )
		);
		if ( persister.getEntityMetamodel().hasNonIdentifierPropertyNamedId() ) {
			// only the identifier property field name can be a reference to the associated entity's PK...
			return propertyName.equals( persister.getIdentifierPropertyName() ) && owningType.isReferenceToPrimaryKey();
		}
		else {
			// here, we have two possibilities:
			// 		1) the property-name matches the explicitly identifier property name
			//		2) the property-name matches the implicit 'id' property name
			if ( EntityPersister.ENTITY_ID.equals( propertyName ) ) {
				// the referenced node text is the special 'id'
				return owningType.isReferenceToPrimaryKey();
			}
			else {
				String keyPropertyName = owningType.getIdentifierOrUniqueKeyPropertyName( getSessionFactoryImplementor() );
				return keyPropertyName != null && keyPropertyName.equals( propertyName ) && owningType.isReferenceToPrimaryKey();
			}
		}
	}



	// source impls ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private class RootSourceImpl extends AbstractPathedPropertyReferenceSource {
		private final PersisterSpace lhs;

		public RootSourceImpl(PersisterSpace lhs) {
			this.lhs = lhs;
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(String name) {
			return determineAppropriateIntermediateSourceType( lhs, name );
		}

		public HibernateTree handleTerminalPathPart(String name) {
			if ( lhs.getPropertyType( name ).isEntityType() ) {
				if ( shouldTerminalEntityPropertyForceJoin() ) {
					locateOrBuildPropertyJoinedTable( lhs, name, null, false, false );
				}
			}
			return generatePropertyColumnList( lhs, name );
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(String collectionPropertyName, HibernateTree selector) {
			validateIndexedCollectionReference( lhs, collectionPropertyName );
			QueryableCollection collectionPersister = resolveCollectionPersister( lhs, collectionPropertyName );

			Table joinedCollectionTable = createJoin( lhs, collectionPersister, null, selector );
			return new IndexAccessIntermediatePathSource(joinedCollectionTable.getTableSpace().getPersisterSpace() );
		}

		public HibernateTree handleTerminalIndexAccess(String collectionPropertyName, HibernateTree selector) {
			return handleTerminalIndexAccess( lhs, collectionPropertyName, selector );
		}

		public String getText() {
			return "root-source {" + lhs.getSourceAlias() + "}";
		}
	}

	protected class SimpleIntermediatePathSource extends AbstractPathedPropertyReferenceSource {
		public PathedPropertyReferenceSource handleIntermediatePathPart(String name) {
			throw new HibernateException( "cannot dereference simple value as part of path expression" );
		}

		public HibernateTree handleTerminalPathPart(String name) {
			throw new HibernateException( "cannot dereference simple value as part of path expression" );
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(String name, HibernateTree selector) {
			throw new HibernateException( "cannot apply index operation to simple value" );
		}

		public HibernateTree handleTerminalIndexAccess(String name, HibernateTree selector) {
			throw new HibernateException( "cannot apply index operation to simple value" );
		}
	}

	private class ComponentIntermediatePathSource extends AbstractPathedPropertyReferenceSource {
		private final PersisterSpace lhs;
		private final String propertyPath;
		private final ComponentType componentType;

		public ComponentIntermediatePathSource(PersisterSpace lhs, String propertyPath) {
			this( lhs, propertyPath, ( ComponentType ) lhs.getPropertyType( propertyPath ) );
		}

		private ComponentIntermediatePathSource(PersisterSpace lhs, String propertyPath, ComponentType componentType) {
			this.lhs = lhs;
			this.propertyPath = propertyPath;
			this.componentType = componentType;
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(String propertyName) {
			final int index = locateComponentPropertyIndex( componentType, propertyName );
			final String path = buildComponentDereferencePath( propertyName );
			final Type propertyType = componentType.getSubtypes()[index];
			if ( propertyType.isComponentType() ) {
				return new ComponentIntermediatePathSource( lhs, path, ( ComponentType ) propertyType );
			}
			else if ( propertyType.isEntityType() ) {
				return new EntityIntermediatePathSource( lhs, path );
			}
			else {
				return new SimpleIntermediatePathSource();
			}
		}

		public HibernateTree handleTerminalPathPart(String name) {
			return generatePropertyColumnList( lhs, buildComponentDereferencePath( name ) );
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(String name, HibernateTree selector) {
			throw new HibernateException( "cannot apply index operation to component value" );
		}

		public PropertyPathTerminus handleTerminalIndexAccess(String name, HibernateTree selector) {
			throw new HibernateException( "cannot apply index operation to component value" );
		}

		private String buildComponentDereferencePath(String subPropertyName) {
			return propertyPath + "." + subPropertyName;
		}
	}

	protected class EntityIntermediatePathSource extends AbstractPathedPropertyReferenceSource {
		private final PersisterSpace lhs;
		private final String lhsPropertyName;

		public EntityIntermediatePathSource(PersisterSpace lhs, String lhsPropertyName) {
			this.lhs = lhs;
			this.lhsPropertyName = lhsPropertyName;
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(String name) {
			Table joinedTable = locateOrBuildPropertyJoinedTable( lhs, lhsPropertyName, null, false, false );
			return determineAppropriateIntermediateSourceType( joinedTable.getTableSpace().getPersisterSpace(), name );
		}

		public HibernateTree handleTerminalPathPart(String name) {
			final EntityType type = ( EntityType ) lhs.getPropertyType( lhsPropertyName );
			if ( isReferenceToPrimaryKey( type, lhsPropertyName ) ) {
				// todo : create a column-list based on the FKs...
				return null;
			}
			else {
				Table joinedTable = locateOrBuildPropertyJoinedTable( lhs, lhsPropertyName, null, false, false );
				PersisterSpace rhs = joinedTable.getTableSpace().getPersisterSpace();
				if ( type.isEntityType() ) {
					if ( shouldTerminalEntityPropertyForceJoin() ) {
						locateOrBuildPropertyJoinedTable( rhs, name, null, false, false );
					}
				}
				return generatePropertyColumnList( rhs, name );
			}

		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(String name, HibernateTree selector) {
			Table lhsJoinedTable = locateOrBuildPropertyJoinedTable( lhs, lhsPropertyName, null, false, false );
			PersisterSpace lhsJoinedPersisterSpace = lhsJoinedTable.getTableSpace().getPersisterSpace();

			validateIndexedCollectionReference( lhs, name );
			QueryableCollection collectionPersister = resolveCollectionPersister( lhsJoinedPersisterSpace, name );
			Table joinedTable = createJoin( lhsJoinedPersisterSpace, collectionPersister, null, selector );

			return new IndexAccessIntermediatePathSource( joinedTable.getTableSpace().getPersisterSpace() );
		}

		public HibernateTree handleTerminalIndexAccess(String collectionPropertyName, HibernateTree selector) {
			return handleTerminalIndexAccess( lhs, collectionPropertyName, selector );
		}
	}

	protected boolean shouldTerminalEntityPropertyForceJoin() {
		return false;
	}

	protected class CollectionIntermediatePathSource extends AbstractPathedPropertyReferenceSource {
		private final PersisterSpace lhs;
		private final String propertyName;

		public CollectionIntermediatePathSource(PersisterSpace lhs, String propertyName) {
			this.lhs = lhs;
			this.propertyName = propertyName;
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(String name) {
			throw new HibernateException( "cannot implicit join across a collection association" );
		}

		public HibernateTree handleTerminalPathPart(String name) {
			// TODO : what are the circusmstances under which we need to *join* to the collection, as opposed to say munge it into a subquery???
			if ( CollectionProperties.isAnyCollectionProperty( name ) ) {
				Table joinedTable = locateOrBuildPropertyJoinedTable( lhs, propertyName, null, false, false );
				return generatePropertyColumnList( joinedTable.getTableSpace().getPersisterSpace(), name );
			}
			throw new HibernateException( "cannot implicit join across a collection association" );
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(String name, HibernateTree selector) {
			throw new HibernateException( "cannot implicit join across a collection association" );
		}

		public HibernateTree handleTerminalIndexAccess(String name, HibernateTree selector) {
			throw new HibernateException( "cannot implicit join across a collection association" );
		}
	}

	protected class IndexAccessIntermediatePathSource extends AbstractPathedPropertyReferenceSource {
		private final PersisterSpace persisterSpace;

		public IndexAccessIntermediatePathSource(PersisterSpace persisterSpace) {
			this.persisterSpace = persisterSpace;
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(String name) {
			return determineAppropriateIntermediateSourceType( persisterSpace, name );
		}

		public HibernateTree handleTerminalPathPart(String name) {
			return generatePropertyColumnList( persisterSpace, name );
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(String name, HibernateTree selector) {
			throw new IllegalStateException( "doubled up index operators" );
		}

		public HibernateTree handleTerminalIndexAccess(String name, HibernateTree selector) {
			throw new IllegalStateException( "doubled up index operators" );
		}
	}
}
