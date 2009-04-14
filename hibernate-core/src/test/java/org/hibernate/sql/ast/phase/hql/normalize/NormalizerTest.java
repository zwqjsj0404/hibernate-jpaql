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

import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.sql.ast.util.NodeTraverser;
import org.hibernate.sql.ast.phase.hql.parse.ParserTest;
import org.hibernate.sql.ast.phase.hql.domain.Animal;
import org.hibernate.sql.ast.common.JoinType;
import org.hibernate.hql.QuerySplitter;

import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public class NormalizerTest extends TestCase implements HqlNormalizeTokenTypes {
	private SessionFactoryImplementor sessionFactory;

	protected void setUp() throws Exception {
		super.setUp();
		sessionFactory = ( SessionFactoryImplementor ) new Configuration()
				.setProperty( Environment.HBM2DDL_AUTO, "none" )
				.setProperty( Environment.DIALECT, HSQLDialect.class.getName() )
				.addResource( "org/hibernate/sql/ast/phase/hql/domain/Mappings.hbm.xml" )
				.buildSessionFactory();
	}

	protected void tearDown() throws Exception {
		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
		super.tearDown();
	}

	public void testBasicStructure() throws Throwable {
		AST query = normalize( "select a from Animal a order by a.name" );
		assertEquals( query.getType(), QUERY );

		AST querySpec = query.getFirstChild();
		assertEquals( QUERY_SPEC, querySpec.getType() );

		AST selectFrom = querySpec.getFirstChild();
		assertEquals( SELECT_FROM, selectFrom.getType() );

		AST from = selectFrom.getFirstChild();
		assertEquals( FROM, from.getType() );
		assertSame( from, ( ( QuerySpec ) querySpec ).locateFromClause() );

		AST select = from.getNextSibling();
		assertEquals( SELECT, select.getType() );
		assertSame( select, ( ( QuerySpec ) querySpec ).locateSelectClause() );

		assertEquals( SELECT_LIST, select.getFirstChild().getType() );
		assertEquals( SELECT_ITEM, select.getFirstChild().getFirstChild().getType() );

		AST sorting = querySpec.getNextSibling();
		assertEquals( ORDER_BY, sorting.getType() );

	}

	public void testSimpleHql() throws Exception {
		// First, get an AST by parsing some HQL text.
		AST ast = normalize( "from Animal" );
		// Assert:
		assertEquals( ast.getType(), QUERY );
	}

	public void testSimpleExplicitJoins() throws Throwable {
		AST ast = normalize( "from Animal as a inner join a.mother as m" );
		JoinCounter.assertJoinCount( 1, ast );

		ast = normalize( "from Animal as a inner join a.offspring as c inner join a.mother as m" );
		JoinCounter.assertJoinCount( 2, ast );
	}

	public void testPathedExplicitJoins() throws Throwable {
		AST ast = normalize( "from Animal as a inner join a.mother.father.offspring as auntsanduncles" );
		JoinCounter.assertJoinCount( 3, ast );
	}

	public void testSelectPersisterReference() throws Throwable {
		AST query = normalize( "select a from Animal a" );
		JoinCounter.assertJoinCount( 0, query );
		SelectItemCounter.assertSelectItemCount( 1, query );

		assertEquals( query.getType(), QUERY );
		AST selectClause = query.getFirstChild().getFirstChild().getFirstChild().getNextSibling();
		assertNotNull( selectClause );
		assertEquals( SELECT, selectClause.getType() );
		AST selectItem = selectClause.getFirstChild().getFirstChild();
		assertEquals( SELECT_ITEM, selectItem.getType() );
		AST aliasRef = selectItem.getFirstChild();
		assertEquals( ALIAS_REF, aliasRef.getType() );
		assertEquals( "a", aliasRef.getText() );
	}

	public void testSelectAssociationPropertyReference() throws Throwable {
		// todo : implicit joins...
		AST query = normalize( "select a.mother as m from Animal as a" );
		JoinCounter.assertJoinCount( 1, query );

		assertTrue( query instanceof SelectStatement );
		QuerySpec querySpec = ( ( SelectStatement ) query ).locateQuerySpec();
		AST persisterSpace = querySpec.locateFromClause().getFirstChild();
		assertEquals( PERSISTER_SPACE, persisterSpace.getType() );
		EntityPersisterReference entityPersisterReference = ( EntityPersisterReference ) persisterSpace.getFirstChild();
		Iterator itr = entityPersisterReference.locateJoins();
		assertTrue( itr.hasNext() );
		Join join = ( Join ) itr.next();
		assertFalse( itr.hasNext() );
		assertEquals( JoinType.INNER, join.getEnumeratedJoinType() );
		PersisterReference rhs = join.locateRhs();
		assertEquals( Animal.class.getName(), rhs.getName() );
		// this should work since there is only one non-explicit persister alias...
		assertEquals( "<gen:0>", rhs.getAlias() );

		SelectItemCounter.assertSelectItemCount( 1, query );
		AST selectList = querySpec.locateSelectClause().getFirstChild();
		assertEquals( SELECT_LIST, selectList.getType() );
		assertEquals( 1, selectList.getNumberOfChildren() );
		SelectItem selectItem = ( SelectItem ) selectList.getFirstChild();
		assertEquals( "m", selectItem.getAliasText() );
		PropertyReference propertyReference = ( PropertyReference ) selectItem.getSelectExpression();
		assertEquals( ALIAS_REF, propertyReference.getFirstChild().getType() );
		assertEquals( "a", propertyReference.getFirstChild().getText() );
		assertEquals( "a.mother", propertyReference.getNormalizedPath() );
	}

	public void testSimpleSimplePropertyReference() throws Throwable {
		AST query = normalize( "select a.name as m from Animal as a" );
		JoinCounter.assertJoinCount( 0, query );
		SelectItemCounter.assertSelectItemCount( 1, query );
		assertTrue( query instanceof SelectStatement );
		AST selectClause = ( ( QuerySpec ) query.getFirstChild() ).locateSelectClause();
		assertNotNull( selectClause );
		assertEquals( SELECT, selectClause.getType() );
		assertEquals( 1, selectClause.getNumberOfChildren() );
		AST selectList = selectClause.getFirstChild();
		assertEquals( SELECT_LIST, selectList.getType() );
		assertEquals( 1, selectList.getNumberOfChildren() );
		AST selectItem = selectList.getFirstChild();
		assertEquals( SELECT_ITEM, selectItem.getType() );
		AST selectExpression = selectItem.getFirstChild();
		assertEquals( PROPERTY_REF, selectExpression.getType() );
		PropertyReference propertyReference = ( PropertyReference ) selectExpression;
		assertEquals( "a.name", propertyReference.getNormalizedPath() );
		assertEquals( "m", ( ( SelectItem ) selectItem ).getAliasText() );
	}

	public void testSelectPropertyRefCount() throws Throwable {
		AST query = normalize( "select count(a.mother) from Animal as a" );
		JoinCounter.assertJoinCount( 0, query );
		SelectItemCounter.assertSelectItemCount( 1, query );
		assertTrue( query instanceof SelectStatement );
		AST selectClause = ( ( QuerySpec ) query.getFirstChild() ).locateSelectClause();
		assertNotNull( selectClause );
		assertEquals( SELECT, selectClause.getType() );
		assertEquals( 1, selectClause.getNumberOfChildren() );
		AST selectList = selectClause.getFirstChild();
		assertEquals( SELECT_LIST, selectList.getType() );
		assertEquals( 1, selectList.getNumberOfChildren() );
		AST selectItem = selectList.getFirstChild();
		assertEquals( SELECT_ITEM, selectItem.getType() );
		AST selectExpression = selectItem.getFirstChild();
		assertEquals( COUNT, selectExpression.getType() );
		assertEquals( "count", selectExpression.getText() );
	}

	public void testExplicitImplicitJoin() throws Exception {
		AST ast = normalize( "from Animal a left join fetch a.mother.mother.mother as ggm where ggm.name like '%weeble%'" );
		assertTrue( ast instanceof SelectStatement );
		JoinCounter.assertJoinCount( 3, ast );

		ast = normalize( "from Animal as a inner join a.mother.mother as gm" );
		JoinCounter.assertJoinCount( 2, ast );

		ast = normalize( "from Animal as a inner join a.mother.father as gf" );
		JoinCounter.assertJoinCount( 2, ast );

		ast = normalize( "from Animal as a inner join a.offspring as c inner join a.mother.father as mgf inner join fetch a.father" );
		JoinCounter.assertJoinCount( 4, ast );
	}

	public void testExplicitCollectionJoin() throws Throwable {
		normalize( "from Animal as a inner join a.offspring as o where o.name like '%boots%'" );
	}

	public void testSimpleImplicitJoin() throws Exception {
		AST ast = normalize( "from Animal a where a.mother.name like '%mary%'" );
		assertTrue( ast instanceof SelectStatement );
		JoinCounter.assertJoinCount( 1, ast );

		ast = normalize( "from Animal a where a.mother.father.name like '%weeble%'" );
		assertTrue( ast instanceof SelectStatement );
		JoinCounter.assertJoinCount( 2, ast );

		ast = normalize( "from Animal a where a.mother.mother.name like '%weeble%'" );
		assertTrue( ast instanceof SelectStatement );
		JoinCounter.assertJoinCount( 2, ast );

		ast = normalize( "from Animal a where a.mother.mother = ?" );
		assertTrue( ast instanceof SelectStatement );
		JoinCounter.assertJoinCount( 1, ast );
	}

	public void testWithClause() throws Throwable {
		normalize( "from Zoo z inner join z.mammals as m with m.name = ?" );
	}

	public void testUnqualifiedPropertyReference() throws Exception {
		AST ast = normalize( "from Animal where name like '%mary%'" );
		assertTrue( ast instanceof SelectStatement );
		JoinCounter.assertJoinCount( 0, ast );

		ast = normalize( "from Animal where mother.name like '%mary%'" );
		assertTrue( ast instanceof SelectStatement );
		JoinCounter.assertJoinCount( 1, ast );
	}

	public void testThetaJoins() throws Exception {
		AST ast = normalize( "from Animal a, Animal b where a.mother.id = b.id and b.name like '%mary%'" );
		assertTrue( ast instanceof SelectStatement );
//		JoinCounter.assertJoinCount( 1, ast );

		ast = normalize( "from Animal a, Animal b inner join b.mother as c where a.mother.id = b.id and b.name like '%mary%'" );
		assertTrue( ast instanceof SelectStatement );
//		JoinCounter.assertJoinCount( 2, ast );
	}

	public void testAdHocJoins() throws Exception {
		normalize( "from Animal a inner join Zoo z on a.id = z.id" );
		normalize( "from Animal a inner join Zoo z on a.id = z.id inner join z.mammals as m with m.name = ?" );
	}

	public void testReusingImplcitJoins() throws Throwable {
		AST ast = normalize( "from Animal a where a.mother.father.name = 'abc' and a.mother.father.description = 'xyz'" );
		assertTrue( ast instanceof SelectStatement );
		JoinCounter.assertJoinCount( 2, ast );
	}

// these entities were not copied over...
//	public void testIndexOperations() throws Throwable {
//		normalize( "select o from IndexedCollectionOwner as o where o.simpleMap['test'] = 'xyz'" );
//		normalize( "select o from IndexedCollectionOwner as o inner join o.simpleMap as s where o.simpleMap[ index(s) ] = 'xyz'" );
//	}

	public void testIndexOperations2() throws Throwable {
		normalize( "select zoo from Zoo as zoo where zoo.mammals['dog'] = maxelement(zoo.mammals)" );
		normalize( "select zoo from Zoo as zoo where zoo.mammals['dog'].description = 'abc'" );
		normalize( "from Zoo zoo join zoo.animals an where zoo.mammals[ index(an) ] = an" );
		normalize( "from Zoo where mammals['dog'] = ?" );
		normalize( "from Zoo zoo where zoo.mammals['dog'].father.description like '%black%'" );
	}

	public void testFilters() throws Exception {
		String role = Animal.class.getName() + ".offspring";
		normalizeFilter( "", role );
		normalizeFilter( "order by this.id", role );
		normalizeFilter( "where this.name = ?", role );
	}


	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private AST normalize(String hql) throws RecognitionException, TokenStreamException {
		return normalize( hql, getSessionFactoryImplementor() );
	}

	private AST normalizeFilter(String hql, String role) throws RecognitionException, TokenStreamException {
		return normalizeFilter( hql, role, getSessionFactoryImplementor() );
	}

	public static AST normalize(String hql, SessionFactoryImplementor sessionFactory) throws TokenStreamException, RecognitionException {
		AST hqlAst = ParserTest.parse( splitQuery( hql, sessionFactory ) );

		HqlNormalizer normalizer = new HqlNormalizer( sessionFactory );
		normalizer.statement( hqlAst );
		AST normalizedAST = normalizer.getAST();
		System.out.println( normalizer.getPrinter().showAsString( normalizedAST, "Normalized query AST" ) );
		return normalizedAST;
	}

	public static AST normalizeFilter(String hql, String role, SessionFactoryImplementor sessionFactory) throws TokenStreamException, RecognitionException {
		AST hqlAst = ParserTest.parseFilter( splitQuery( hql, sessionFactory ), role );

		HqlNormalizer normalizer = new HqlNormalizer( sessionFactory );
		normalizer.statement( hqlAst );
		AST normalizedAST = normalizer.getAST();
		System.out.println( normalizer.getPrinter().showAsString( normalizedAST, "Normalized filter AST" ) );
		return normalizedAST;
	}

	private static String splitQuery(String hql, SessionFactoryImplementor sessionFactory) {
		String[] queries = QuerySplitter.concreteQueries( hql, sessionFactory );
		assertEquals( "polymorhpic queries not allowed here", 1, queries.length );
		return queries[0];
	}

	protected SessionFactoryImplementor getSessionFactoryImplementor() {
		return sessionFactory;
	}

	private static class JoinCounter implements NodeTraverser.VisitationStrategy {
		int count = 0;
		public void visit(AST node) {
			if ( node.getType() == JOIN ) {
				count++;
			}
		}
		public static void assertJoinCount(int expected, AST tree) {
			JoinCounter.assertJoinCount( "unexpected join count", expected, tree );
		}
		public static void assertJoinCount(String failMessage, int expected, AST tree) {
			JoinCounter counter = new JoinCounter();
			NodeTraverser walker = new NodeTraverser( counter );
			walker.traverseDepthFirst( tree );
			assertEquals( failMessage, expected, counter.count );
		}
	}

	private static class SelectItemCounter implements NodeTraverser.VisitationStrategy {
		int count;
		public void visit(AST node) {
			if ( node instanceof SelectItem ) {
				count++;
			}
		}
		public static void assertSelectItemCount(int expected, AST tree) {
			SelectItemCounter.assertSelectItemCount( "unexpected select expression count", expected, tree );
		}
		public static void assertSelectItemCount(String failMessage, int expected, AST tree) {
			SelectItemCounter counter = new SelectItemCounter();
			NodeTraverser walker = new NodeTraverser( counter );
			walker.traverseDepthFirst( tree );
			assertEquals( failMessage, expected, counter.count );
		}
	}

	public static Test suite() {
		return new TestSuite( NormalizerTest.class );
	}
}
