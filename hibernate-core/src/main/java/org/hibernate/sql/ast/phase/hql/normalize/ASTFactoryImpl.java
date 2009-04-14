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

import antlr.collections.AST;
import antlr.Token;

import org.hibernate.sql.ast.common.NodeFactory;

/**
 * AST factory for the resolve phase of HQL query translation.
 *
 * @author Steve Ebersole
 */
public class ASTFactoryImpl extends NodeFactory implements HqlNormalizeTokenTypes {
	private final NormalizationContext resolutionContext;

	public ASTFactoryImpl(NormalizationContext resolutionContext) {
		this.resolutionContext = resolutionContext;
	}

	public Class getASTNodeType(int tokenType) {
		switch ( tokenType ) {
			case IDENT :
				return Ident.class;
			case DOT :
				return Dot.class;
			case ENTITY_PERSISTER_REF :
				return EntityPersisterReference.class;
			case COLLECTION_PERSISTER_REF :
				return CollectionPersisterReference.class;
			case INDEXED_COLLECTION_ACCESS_PERSISTER_REF :
				return IndexedCollectionElementAccessPersisterReference.class;
			case PROPERTY_JOIN :
				return PropertyJoin.class;
			case PERSISTER_JOIN :
				return PersisterJoin.class;
			case ALIAS_REF :
				return PersisterAliasReference.class;
			case PROPERTY_REF :
				return PropertyReference.class;
			case INDEXED_COLLECTION_ELEMENT_REF :
				return IndexCollectionElementReference.class;
			case INSERT :
				return InsertStatement.class;
			case UPDATE :
				return UpdateStatement.class;
			case DELETE :
				return DeleteStatement.class;
			case QUERY :
				return SelectStatement.class;
			case QUERY_SPEC :
				return QuerySpec.class;
			case SELECT_ITEM :
				return SelectItem.class;
			default:
				return determineDefaultNodeClass();
		}
	}

	protected AST createUsingCtor(Token token, String className) {
		return super.createUsingCtor( token, className );
	}

	/**
	 * Instantiate the AST node of the given type.
	 *
	 * @param c The AST class to instantiate.
	 * @return The instantiated and initialized node.
	 */
	protected AST create(Class c) {
		AST ast = super.create(  c );
		if ( ast != null ) {
			initialize( ast );
		}
		return ast;
	}

	/**
	 * Perform any optional requested initializatio on the node.  In this impl, that means to look at the node's
	 * interfaces.
	 *
	 * @param ast The node
	 */
	protected void initialize(AST ast) {
		if ( ast instanceof NormalizationContextAwareNode ) {
			( ( NormalizationContextAwareNode ) ast ).injectNormalizationContext( resolutionContext );
		}
	}
}
