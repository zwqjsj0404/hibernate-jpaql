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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.HibernateException;
import org.hibernate.sql.ast.util.TreePrinter;
import org.hibernate.sql.ast.origin.hql.parse.HQLLexer;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.CommonGroupLoader;
import org.antlr.stringtemplate.StringTemplateErrorListener;

/**
 * A translator which coordinates translation of an <tt>order-by</tt> mapping.
 *
 * @author Steve Ebersole
 */
public class OrderByFragmentTranslator {
	private static final Logger log = LoggerFactory.getLogger( OrderByFragmentTranslator.class );

	public final TranslationContext context;

	public OrderByFragmentTranslator(TranslationContext context) {
		this.context = context;
	}

	/**
	 * The main contract, performing the translation.
	 *
	 * @param fragment The <tt>order-by</tt> mapping fragment to be translated.
	 *
	 * @return The translated fragment.
	 */
	public String render(String fragment) {
		HQLLexer lexer = new HQLLexer( new ANTLRStringStream( fragment ) );
		ContextualOrderByParser parser = new ContextualOrderByParser( new CommonTokenStream( lexer ), context );
		Tree orderByTree;
		try {
            orderByTree = ( Tree ) parser.orderByFragment().getTree();

            if ( log.isTraceEnabled() ) {
                TreePrinter printer = new TreePrinter( OrderByParser.class );
                log.trace( printer.renderAsString( orderByTree, "--- {order-by fragment} ---" ) );
            }
		}
		catch ( HibernateException e ) {
			throw e;
		}
		catch ( Throwable t ) {
			throw new HibernateException( "Unable to parse order-by fragment", t );
		}

		// Load the string templates...
		CommonGroupLoader groupLoader = new CommonGroupLoader(
				"org/hibernate/sql/ast/template/",
				new StringTemplateErrorListener() {
					public void error(String s, Throwable throwable) {
						throw new RuntimeException( s, throwable );
					}

					public void warning(String s) {
						System.out.println( "GROUP-LOADER : " + s );
					}
				}
		);
		StringTemplateGroup.registerGroupLoader( groupLoader );
		StringTemplateGroup stringTemplateGroup = groupLoader.loadGroup( "OrderBy" );
		ContextualOrderByRenderer renderer = new ContextualOrderByRenderer( new CommonTreeNodeStream( orderByTree ), context, stringTemplateGroup );

		try {
			return renderer.orderByFragment().st.toString();
		}
		catch ( RecognitionException e ) {
			throw new HibernateException( "Unable to render order-by fragment", e );
		}

	}
}
