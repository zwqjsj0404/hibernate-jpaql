/*
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
package org.hibernate.sql.ast.origin.hql.resolve;

import junit.framework.TestCase;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.Tree;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.hql.classic.ClassicQueryTranslatorFactory;
import org.hibernate.sql.ast.origin.hql.parse.HQLLexer;
import org.hibernate.sql.ast.origin.hql.parse.HQLParser;

public class TestHQLResolver extends TestCase {

	private SessionFactoryImplementor sessionFactory;
	private CommonTokenStream tokens;

	protected void setUp() throws Exception {
		super.setUp();
		sessionFactory = ( SessionFactoryImplementor ) new Configuration()
				.setProperty( Environment.HBM2DDL_AUTO, "none" )
				.setProperty( Environment.DIALECT, HSQLDialect.class.getName() )
				.setProperty( Environment.QUERY_TRANSLATOR,
						ClassicQueryTranslatorFactory.class.getName() )
				.addResource(
						"org/hibernate/sql/ast/phase/hql/domain/Mappings.hbm.xml" )
				.buildSessionFactory();
	}

	protected void tearDown() throws Exception {
		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
		super.tearDown();
	}

	public void testBasicStructure() throws RecognitionException {
		normalize( "from Animal" );
	}

	public void testBasicSelectStructure() throws Throwable {
		normalize( "from Zoo z where z.mammals['dog'].id = ?" );
	}

	public Tree normalize( String hql ) throws RecognitionException {
		return normalize( hql, getSessionFactoryImplementor() );
	}

	protected SessionFactoryImplementor getSessionFactoryImplementor() {
		return sessionFactory;
	}

	public Tree normalize( String hql, SessionFactoryImplementor sessionFactory )
			throws RecognitionException {
		Tree result = parse( hql );

		CommonTreeNodeStream nodes = new CommonTreeNodeStream( result );
		nodes.setTokenStream( tokens );

		HQLResolver resololution = new HQLResolver( nodes, sessionFactory );

		System.out.println( resololution.getTreePrinter().renderAsString(
				result, "Parser Result" ) );

		Tree resolvedTree = ( Tree ) resololution.statement().getTree();

		System.out.println( resololution.getTreePrinter().renderAsString(
				resolvedTree, "Resolution Result" ) );
		return resolvedTree;
	}

	public Tree parse( String input ) throws RecognitionException {
		HQLParser parser = buildHQLParser( input );
		return ( Tree ) parser.statement().getTree();
	}

	private HQLParser buildHQLParser( String input ) {
		ANTLRStringStream charStream = new ANTLRStringStream( input );
		HQLLexer hqlLexer = new HQLLexer( charStream );
		tokens = new CommonTokenStream( hqlLexer );
		return new HQLParser( tokens );
	}
}
