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
package org.hibernate.sql.ast.phase.hql.resolve;

import junit.framework.TestCase;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.sql.ast.util.ASTPrinter;
import org.hibernate.sql.ast.util.NodeDeepCopier;
import org.hibernate.sql.ast.phase.hql.domain.Animal;
import org.hibernate.sql.ast.phase.hql.domain.Human;
import org.hibernate.sql.ast.phase.hql.normalize.NormalizerTest;
import org.hibernate.persister.entity.AbstractEntityPersister;

import antlr.ASTFactory;
import antlr.TokenStreamException;
import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public class ResolverTest extends TestCase {
	private static ASTPrinter printer = new ASTPrinter( HqlResolveTokenTypes.class );
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

	public void testResolve() throws Throwable {
//		resolve( "from Animal a" );
//		resolve( "select a.description from Animal a" );
		resolve( "select a from Animal a" );
	}

	public void testFilters() throws Exception {
		String role = Animal.class.getName() + ".offspring";
		AST ast = resolveFilter( "", role );
		resolveFilter( "order by this.id", role );
		resolveFilter( "where this.name = ?", role );
	}

	public void testPropertyJoins() throws Throwable {
		resolve( "from Animal a inner join a.father as f" );
	}

	public void testPersisterJoins() throws Throwable {
//		resolve( "from Animal a, Animal b" );
		resolve( "from Animal a cross join Animal b" );
//		resolve( "from Animal a inner join Animal b on a.mother = b.father" );
	}

	private AST resolve(String hql) throws TokenStreamException, RecognitionException {
		return resolve( hql, sessionFactory );
	}

	private AST resolveFilter(String hql, String role) throws TokenStreamException, RecognitionException {
		return resolveFilter( hql, role, sessionFactory );
	}

	public static AST resolve(String hql, SessionFactoryImplementor sessionFactory) throws TokenStreamException, RecognitionException {
		AST normalizedAST = NormalizerTest.normalize( hql, sessionFactory );
		return resolve( normalizedAST, sessionFactory );
	}

	public static AST resolveFilter(String hql, String role, SessionFactoryImplementor sessionFactory) throws TokenStreamException, RecognitionException {
		AST normalizedAST = NormalizerTest.normalizeFilter( hql, role, sessionFactory );
		return resolve( normalizedAST, sessionFactory );
	}

	private static AST resolve(AST normalizedAST, SessionFactoryImplementor sessionFactory) throws RecognitionException {
		HqlResolver resolver = new HqlResolver( sessionFactory );
		resolver.statement( normalizedAST );
		AST resolvedAST = resolver.getAST();
		System.out.println( printer.showAsString( resolvedAST, "Resolved AST" ) );
		return resolvedAST;
	}
}
