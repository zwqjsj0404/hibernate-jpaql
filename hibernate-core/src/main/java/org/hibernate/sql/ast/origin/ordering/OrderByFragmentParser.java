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
 *
 */
package org.hibernate.sql.ast.origin.ordering;

import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.sql.ast.common.HibernateTree;
import org.hibernate.sql.Template;

/**
 * Extension of the Antlr-generated parser for the purpose of adding our custom parsing behavior.
 *
 * @author Steve Ebersole
 */
public class OrderByFragmentParser extends OrderByParser {
	private static final Logger log = LoggerFactory.getLogger( OrderByFragmentParser.class );
	private final TranslationContext context;

	private final String openQuoteChar;
	private final String closeQuoteChar;

	public OrderByFragmentParser(TokenStream lexer, TranslationContext context) {
		super( lexer );
		this.context = context;

		this.openQuoteChar = Character.toString( context.getDialect().openQuote() );
		this.closeQuoteChar = Character.toString( context.getDialect().closeQuote() );
	}

	@Override
	protected boolean isFunctionName(String text) {
		log.trace( "Checking function name [" + text + "]" );
		return context.getSqlFunctionRegistry().hasFunction( text );
	}

	@Override
	protected boolean isPropertyName(String text) {
		log.trace( "Checking property name [" + text + "]" );
		try {
			return context.getColumnMapper().map( text ) != null;
		}
		catch ( Throwable t ) {
			return false;
		}
	}

	@Override
	protected CommonTree buildPropertyColumns(CommonTree propertyTree) {
		final String text = extractPropertyName( propertyTree );
		String[] columns = context.getColumnMapper().map( text );
		if ( columns.length == 1 ) {
			return buildColumn( columns[0] );
		}
		else {
			HibernateTree vector = new HibernateTree( VECTOR_EXPR );
			for ( String column : columns ) {
				vector.addChild( buildColumn( column ) );
			}
			return vector;
		}
	}

	private CommonTree buildColumn(String columnName) {
		// ugh
		HibernateTree columnNameNode;
		if ( columnName.startsWith( openQuoteChar ) && columnName.endsWith( closeQuoteChar ) ) {
			columnName = columnName.substring( 1, columnName.length() - 1 );
			columnNameNode = new HibernateTree( QUOTED_IDENTIFIER, columnName );
		}
		else {
			columnNameNode = new HibernateTree( IDENTIFIER, columnName );
		}

		HibernateTree tree = new HibernateTree( COLUMN );
		tree.addChild( new HibernateTree( ALIAS_REF, Template.TEMPLATE ) );
		tree.addChild( columnNameNode );

		return tree;
	}

	private String extractPropertyName(CommonTree propertyTree) {
		return propertyTree.getText();
	}
}
