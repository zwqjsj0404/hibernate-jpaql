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
package org.hibernate.sql.ast.phase.hql.normalize.path.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.sql.ast.phase.hql.normalize.PersisterReference;
import org.hibernate.sql.ast.phase.hql.normalize.NormalizationContext;
import org.hibernate.sql.ast.phase.hql.normalize.Ident;
import org.hibernate.sql.ast.phase.hql.normalize.Join;
import org.hibernate.sql.ast.phase.hql.normalize.IndexedCollectionElementAccessPersisterReference;
import org.hibernate.sql.ast.phase.hql.normalize.PropertyPathTerminus;
import org.hibernate.sql.ast.phase.hql.normalize.path.impl.AbstractPathNormalizationStrategy;
import org.hibernate.sql.ast.phase.hql.normalize.path.PathedPropertyReferenceSource;
import org.hibernate.sql.ast.common.Node;
import org.hibernate.type.Type;
import org.hibernate.type.ComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.HibernateException;
import org.hibernate.hql.CollectionProperties;

/**
 * Basic {@link org.hibernate.sql.ast.phase.hql.normalize.path.PathNormalizationStrategy}.  Mainly used as the
 * basis for other implementations.
 *
 * @author Steve Ebersole
 */
public class BasicPathNormalizationStrategySupport extends AbstractPathNormalizationStrategy {
	private static final Logger log = LoggerFactory.getLogger( BasicPathNormalizationStrategySupport.class );

	public BasicPathNormalizationStrategySupport(NormalizationContext normalizationContext) {
		super( normalizationContext );
	}

	protected PathedPropertyReferenceSource internalHandleRoot(PersisterReference persisterReference) {
		return new RootSourceImpl( persisterReference );
	}

	private PathedPropertyReferenceSource determineAppropriateIntermediateSourceType(PersisterReference origin, Ident propertyName) {
		final String propertyNameText = propertyName.getText();
		final Type propertyType = origin.getPropertyType( propertyNameText );
		if ( propertyType.isComponentType() ) {
			return new ComponentIntermediatePathSource( origin, propertyNameText, ( ComponentType ) propertyType );
		}
		else if ( propertyType.isEntityType() ) {
			return new EntityIntermediatePathSource( origin, propertyNameText );
		}
		else if ( propertyType.isCollectionType() ) {
			return new CollectionIntermediatePathSource( origin, propertyNameText );
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
		private final PersisterReference lhs;

		public RootSourceImpl(PersisterReference lhs) {
			this.lhs = lhs;
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(Ident name) {
			return determineAppropriateIntermediateSourceType( lhs, name );
		}

		public PropertyPathTerminus handleTerminalPathPart(Ident name) {
			final String nameText = name.getText();
			if ( lhs.getPropertyType( nameText ).isEntityType() ) {
				if ( shouldTerminalEntityPropertyForceJoin() ) {
					locateOrBuildPropertyJoin( lhs, nameText, null, false, false );
				}
			}
			return generatePropertyReference( lhs, name.getText() );
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(Ident name, Node selector) {
			Join join = normalizationContext().getPropertyJoinBuilder().buildIndexOperationJoin(
					lhs,
					name.getText(),
					null,
					buildJoinTypeNode(),
					selector
			);
			return new IndexAccessIntermediatePathSource( join.locateRhs() );
		}

		public PropertyPathTerminus handleTerminalIndexAccess(Ident name, Node selector) {
			return super.handleTerminalIndexAccess( lhs, name, selector );
		}

		public String getText() {
			return "root-source {" + lhs.getAlias() + "}";
		}
	}

	protected class SimpleIntermediatePathSource extends AbstractPathedPropertyReferenceSource {
		public PathedPropertyReferenceSource handleIntermediatePathPart(Ident name) {
			throw new HibernateException( "cannot dereference simple value as part of path expression" );
		}

		public PropertyPathTerminus handleTerminalPathPart(Ident name) {
			throw new HibernateException( "cannot dereference simple value as part of path expression" );
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(Ident name, Node selector) {
			throw new HibernateException( "cannot apply index operation to simple value" );
		}

		public PropertyPathTerminus handleTerminalIndexAccess(Ident name, Node selector) {
			throw new HibernateException( "cannot apply index operation to simple value" );
		}
	}

	private class ComponentIntermediatePathSource extends AbstractPathedPropertyReferenceSource {
		private final PersisterReference lhs;
		private final String propertyPath;
		private final ComponentType componentType;

		public ComponentIntermediatePathSource(PersisterReference lhs, String propertyPath) {
			this( lhs, propertyPath, ( ComponentType ) lhs.getPropertyType( propertyPath ) );
		}

		private ComponentIntermediatePathSource(PersisterReference lhs, String propertyPath, ComponentType componentType) {
			this.lhs = lhs;
			this.propertyPath = propertyPath;
			this.componentType = componentType;
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(Ident name) {
			final String propertyName = name.getText();
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

		public PropertyPathTerminus handleTerminalPathPart(Ident name) {
			return generatePropertyReference( lhs, buildComponentDereferencePath( name.getText() ) );
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(Ident name, Node selector) {
			throw new HibernateException( "cannot apply index operation to component value" );
		}

		public PropertyPathTerminus handleTerminalIndexAccess(Ident name, Node selector) {
			throw new HibernateException( "cannot apply index operation to component value" );
		}

		private String buildComponentDereferencePath(String subPropertyName) {
			return propertyPath + "." + subPropertyName;
		}
	}

	protected class EntityIntermediatePathSource extends AbstractPathedPropertyReferenceSource {
		private final PersisterReference lhs;
		private final String lhsPropertyName;

		public EntityIntermediatePathSource(PersisterReference lhs, String lhsPropertyName) {
			this.lhs = lhs;
			this.lhsPropertyName = lhsPropertyName;
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(Ident name) {
			Join join = locateOrBuildPropertyJoin( lhs, lhsPropertyName, null, false, false );
			return determineAppropriateIntermediateSourceType( join.locateRhs(), name );
		}

		public PropertyPathTerminus handleTerminalPathPart(Ident name) {
			final EntityType type = ( EntityType ) lhs.getPropertyType( lhsPropertyName );
			if ( isReferenceToPrimaryKey( type, lhsPropertyName ) ) {
				return generatePropertyReference( lhs, lhsPropertyName + "." + name.getText() );
			}
			else {
				Join join = locateOrBuildPropertyJoin( lhs, lhsPropertyName, null, false, false );
				final String propertyName = name.getText();
				if ( type.isEntityType() ) {
					if ( shouldTerminalEntityPropertyForceJoin() ) {
						locateOrBuildPropertyJoin( join.locateRhs(), propertyName, null, false, false );
					}
				}
				return generatePropertyReference( join.locateRhs(), propertyName );
			}
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(Ident name, Node selector) {
			Join join = normalizationContext().getPropertyJoinBuilder().buildIndexOperationJoin(
					lhs,
					name.getText(),
					null,
					buildJoinTypeNode(),
					selector
			);
			return new IndexAccessIntermediatePathSource( join.locateRhs() );
		}

		public PropertyPathTerminus handleTerminalIndexAccess(Ident name, Node selector) {
			return super.handleTerminalIndexAccess( lhs, name, selector );
		}
	}

	protected boolean shouldTerminalEntityPropertyForceJoin() {
		return false;
	}

	protected class CollectionIntermediatePathSource extends AbstractPathedPropertyReferenceSource {
		private final PersisterReference lhs;
		private final String propertyName;

		public CollectionIntermediatePathSource(PersisterReference lhs, String propertyName) {
			this.lhs = lhs;
			this.propertyName = propertyName;
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(Ident name) {
			throw new HibernateException( "cannot implicit join across a collection association" );
		}

		public PropertyPathTerminus handleTerminalPathPart(Ident name) {
			// TODO : what are the circusmstances under which we need to *join* to the collection, as opposed to say munge it into a subquery???
			final String nameText = name.getText();
			if ( CollectionProperties.isAnyCollectionProperty( nameText ) ) {
				Join join = locateOrBuildPropertyJoin( lhs, propertyName, null, false, false );
				return generatePropertyReference( join.locateRhs(), nameText );
			}
			throw new HibernateException( "cannot implicit join across a collection association" );
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(Ident name, Node selector) {
			throw new HibernateException( "cannot implicit join across a collection association" );
		}

		public PropertyPathTerminus handleTerminalIndexAccess(Ident name, Node selector) {
			throw new HibernateException( "cannot implicit join across a collection association" );
		}
	}

	protected class IndexAccessIntermediatePathSource extends AbstractPathedPropertyReferenceSource {
		private final IndexedCollectionElementAccessPersisterReference persisterReference;

		public IndexAccessIntermediatePathSource(PersisterReference persisterReference) {
			if ( persisterReference instanceof IndexedCollectionElementAccessPersisterReference ) {
				this.persisterReference = ( IndexedCollectionElementAccessPersisterReference ) persisterReference;
			}
			else {
				throw new HibernateException( "Expecting IndexedCollectionElementAccessPersisterReference, found " + persisterReference.getClass().getName() );
			}
		}

		public PathedPropertyReferenceSource handleIntermediatePathPart(Ident name) {
			return determineAppropriateIntermediateSourceType( persisterReference, name );
		}

		public PropertyPathTerminus handleTerminalPathPart(Ident name) {
			return generatePropertyReference( persisterReference, name.getText() );
		}

		public PathedPropertyReferenceSource handleIntermediateIndexAccess(Ident name, Node selector) {
			throw new IllegalStateException( "doubled up index operators" );
		}

		public PropertyPathTerminus handleTerminalIndexAccess(Ident name, Node selector) {
			throw new IllegalStateException( "doubled up index operators" );
		}
	}
}