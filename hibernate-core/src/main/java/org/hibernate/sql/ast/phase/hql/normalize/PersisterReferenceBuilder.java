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
package org.hibernate.sql.ast.phase.hql.normalize;

import antlr.ASTFactory;
import antlr.collections.AST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.MappingException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.ast.alias.ImplicitAliasGenerator;

/**
 * Centralization of code needed to build a {@link PersisterReference}
 *
 * @author Steve Ebersole
 */
public class PersisterReferenceBuilder implements HqlNormalizeTokenTypes {
	private static final Logger log = LoggerFactory.getLogger( PersisterReferenceBuilder.class );

	private final NormalizationContext normalizationContext;

	public PersisterReferenceBuilder(NormalizationContext normalizationContext) {
		this.normalizationContext = normalizationContext;
	}

	private ASTFactory astFactory() {
		return normalizationContext.getASTFactory();
	}

	private PersisterReferenceContext persisterReferenceContext() {
		return normalizationContext.getCurrentPersisterReferenceContext();
	}

	private ImplicitAliasGenerator aliasBuilder() {
		return normalizationContext.getAliasBuilder();
	}

	private SessionFactoryImplementor sessionFactory() {
		return normalizationContext.getSessionFactoryImplementor();
	}

	// exposed services ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Builds an entity persister reference
	 *
	 * @param entityName The entity name to which to build a PersisterReference.
	 * @param alias The alias (or null) to apply to the built PersisterReference.
	 * @return The built PersisterReference
	 */
	public EntityPersisterReference buildEntityPersisterReference(
			String entityName,
			String alias) {
		EntityPersister persister = lookupEntityPersister( entityName );
		String aliasText = determineAlias( alias );

		AST entityNameNode = astFactory().create( ENTITY_NAME, persister.getEntityName() );
		AST aliasNode = astFactory().create( ALIAS, aliasText );
		AST persisterReference = astFactory().create( ENTITY_PERSISTER_REF );
		persisterReference.setFirstChild( entityNameNode );
		persisterReference.addChild( aliasNode );

		EntityPersisterReference entityPersisterReference = ( EntityPersisterReference ) persisterReference;
		persisterReferenceContext().registerPersisterReference( entityPersisterReference );
		return entityPersisterReference;
	}

	/**
	 * Builds a collection persister reference
	 *
	 * @param collectionRole The {@link org.hibernate.persister.collection.CollectionPersister#getRole() collection role}
	 * for which to build a PersisterReference.
	 * @param alias The alias (or null) to apply to the built PersisterReference.
	 * @return The built PersisterReference
	 */
	public CollectionPersisterReference buildCollectionPersisterReference(
			String collectionRole, 
			String alias) {
		// todo : is this the structure we want here?
		String aliasText = determineAlias( alias );

		AST collectionRoleNode = astFactory().create( COLLECTION_ROLE, collectionRole );
		AST aliasNode = astFactory().create( ALIAS, aliasText );
		AST persisterReference = astFactory().create( COLLECTION_PERSISTER_REF );
		persisterReference.setFirstChild( collectionRoleNode );
		persisterReference.addChild( aliasNode );

		CollectionPersisterReference collectionPersisterReference = ( CollectionPersisterReference ) persisterReference;
		persisterReferenceContext().registerPersisterReference( collectionPersisterReference );
		return collectionPersisterReference;
	}

	public IndexedCollectionElementAccessPersisterReference buildIndexedCollectionElementAccessPersisterReference(
			String collectionRole,
			String alias,
			AST selector) {
		String aliasText = determineAlias( alias );

		AST collectionRoleNode = astFactory().create( COLLECTION_ROLE, collectionRole );
		AST aliasNode = astFactory().create( ALIAS, aliasText );

		AST indexValueCondition = astFactory().create( INDEX_VALUE_CORRELATION, "index-correlation" );
		indexValueCondition.addChild( selector );

		AST persisterReference = astFactory().create( INDEXED_COLLECTION_ACCESS_PERSISTER_REF );
		persisterReference.setFirstChild( collectionRoleNode );
		persisterReference.addChild( aliasNode );
		persisterReference.addChild( indexValueCondition );

		IndexedCollectionElementAccessPersisterReference pr = ( IndexedCollectionElementAccessPersisterReference ) persisterReference;
		persisterReferenceContext().registerPersisterReference( pr );

		return pr;
	}

	private EntityPersister lookupEntityPersister(String name) {
		// NOTE : the parser should have already normalized the entity name...
		try {
			// First, try to get the persister using the class name directly.
			return sessionFactory().getEntityPersister( name );
		}
		catch ( MappingException ignore ) {
			// unable to locate it using this name
		}

		// If that didn't work, try using the 'import' name.
		String importedClassName = sessionFactory().getImportedClassName( name );
		if ( importedClassName == null ) {
			return null;
		}
		return sessionFactory().getEntityPersister( importedClassName );
	}

	private String determineAlias(String alias) {
		if ( alias == null ) {
			return aliasBuilder().buildUniqueImplicitAlias();
		}
		else {
			if ( persisterReferenceContext().isContainedAlias( alias ) ) {
				throw new DuplicateAliasException( alias );
			}
			return alias;
		}
	}

	public static boolean isImplicitAlias(String alias) {
		return ImplicitAliasGenerator.isImplicitAlias( alias );
	}

}
