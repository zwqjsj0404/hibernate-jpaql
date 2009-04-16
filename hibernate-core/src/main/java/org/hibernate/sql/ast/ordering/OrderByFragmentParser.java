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
package org.hibernate.sql.ast.ordering;

import java.util.ArrayList;

import org.hibernate.sql.Template;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.util.StringHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

/**
 * Extension of the Antlr-generated parser for the purpose of adding our custom parsing behavior.
 *
 * @author Steve Ebersole
 */
public class OrderByFragmentParser extends OrderByParserParser {
	private static final Logger log = LoggerFactory.getLogger( OrderByFragmentParser.class );

	private final TranslationContext context;
	private int traceDepth = 0;

	public OrderByFragmentParser(TokenStream lexer, TranslationContext context) {
		super( lexer );
		this.context = context;
	}
//
//
//	// handle trace logging ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//	public void traceIn(String ruleName) throws TokenStreamException {
//		if ( inputState.guessing > 0 ) {
//			return;
//		}
//		String prefix = StringHelper.repeat( "-", (traceDepth++ * 2) ) + "->";
//		trace( prefix + ruleName );
//	}
//
//	public void traceOut(String ruleName) throws TokenStreamException {
//		if ( inputState.guessing > 0 ) {
//			return;
//		}
//		String prefix = "<-" + StringHelper.repeat( "-", (--traceDepth * 2) );
//		trace( prefix + ruleName );
//	}
//
//    private void trace(String msg) {
//		log.trace( msg );
//	}

protected CommonTree quotedIdentifier(CommonTree ident) {
    return createTreeNode( IDENTIFIER, Template.TEMPLATE + "." + context.getDialect().quote( '`' + ident.getText() + '`' ) );
}

    protected CommonTree quotedString(CommonTree ident) {
		return createTreeNode( IDENTIFIER, context.getDialect().quote( ident.getText() ) );
	}

    protected boolean isFunctionName(CommonToken token) {
        return context.getSqlFunctionRegistry().hasFunction( token.getText() );
    }

    protected CommonTree resolveFunction(CommonTree tree) {
		Tree argumentList = tree.getChild( 0 );
		assert "{param list}".equals( argumentList.getText() );

		final String functionName = tree.getText();
		final SQLFunction function = context.getSqlFunctionRegistry().findSQLFunction( functionName );

		if ( function == null ) {
            // If the function is not registered with the session factory we just need to render it as-is
            // including its arguments...
			String text = functionName;
            int count = argumentList.getChildCount();
            if ( count > 0 ) {
                text += '(';
                for ( int i = 0; i < count; i++ ) {
                    Tree argument = argumentList.getChild( i );
                    text += argument.getText();
                    if ( i < count ) {
                        text += ", ";
                    }
                }
                text += ')';
            }
            return createTreeNode( IDENTIFIER, text );
		}
		else {
			ArrayList expressions = new ArrayList();
            for ( int i = 0; i < argumentList.getChildCount(); i++ ) {
                expressions.add( argumentList.getChild( i ).getText() );
            }
			final String text = function.render( expressions, context.getSessionFactory() );
			return createTreeNode( IDENTIFIER, text );
		}
	}

    protected CommonTree resolveIdent(CommonTree ident) {
		String text = ident.getText();
		String[] replacements;
		try {
			replacements = context.getColumnMapper().map( text );
		}
		catch( Throwable t ) {
			replacements = null;
		}

		if ( replacements == null || replacements.length == 0 ) {
			return createTreeNode( IDENTIFIER, Template.TEMPLATE + "." + text );
		}
		else if ( replacements.length == 1 ) {
			return createTreeNode( IDENTIFIER, Template.TEMPLATE + "." + replacements[0] );
		}
		else {
            final CommonTree root = createTreeNode( IDENT_LIST, "{ident list}" );
			for ( int i = 0; i < replacements.length; i++ ) {
				final String identText = Template.TEMPLATE + '.' + replacements[i];
				root.addChild( createTreeNode( IDENTIFIER, identText ) );
			}
			return root;
		}
	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	protected AST postProcessSortSpecification(AST sortSpec) {
//		assert SORT_SPEC == sortSpec.getType();
//		SortSpecification sortSpecification = ( SortSpecification ) sortSpec;
//		AST sortKey = sortSpecification.getSortKey();
//		if ( IDENT_LIST == sortKey.getFirstChild().getType() ) {
//			AST identList = sortKey.getFirstChild();
//			AST ident = identList.getFirstChild();
//			AST holder = new CommonAST();
//			do {
//				holder.addChild(
//						createSortSpecification(
//								ident,
//								sortSpecification.getCollation(),
//								sortSpecification.getOrdering()
//						)
//				);
//				ident = ident.getNextSibling();
//			} while ( ident != null );
//			sortSpec = holder.getFirstChild();
//		}
//		return sortSpec;
//	}
//
//	private SortSpecification createSortSpecification(
//			AST ident,
//			CollationSpecification collationSpecification,
//			OrderingSpecification orderingSpecification) {
//		AST sortSpecification = getASTFactory().create( SORT_SPEC, "{{sort specification}}" );
//		AST sortKey = getASTFactory().create( SORT_KEY, "{{sort key}}" );
//		AST newIdent = getASTFactory().create( ident.getType(), ident.getText() );
//		sortKey.setFirstChild( newIdent );
//		sortSpecification.setFirstChild( sortKey );
//		if ( collationSpecification != null ) {
//			sortSpecification.addChild( collationSpecification );
//		}
//		if ( orderingSpecification != null ) {
//			sortSpecification.addChild( orderingSpecification );
//		}
//		return ( SortSpecification ) sortSpecification;
//	}
}
