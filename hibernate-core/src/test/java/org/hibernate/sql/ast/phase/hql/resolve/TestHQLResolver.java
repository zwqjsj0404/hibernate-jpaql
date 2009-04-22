package org.hibernate.sql.ast.phase.hql.resolve;

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
import org.hibernate.sql.ast.phase.hql.parse.HQLLexer;
import org.hibernate.sql.ast.phase.hql.parse.HQLParser;

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

	public void testBasicStructure() throws Throwable {
		Tree queryTree = normalize( "from Animal" );
	}

	public void testBasicSelectStructure() throws Throwable {
		Tree queryTree = normalize( "from Animal as a inner join a.mother" );
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

		HQLResolution resololution = new HQLResolution( nodes, sessionFactory );

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
		HQLParser parser = new HQLParser( tokens );
		return parser;
	}
}
