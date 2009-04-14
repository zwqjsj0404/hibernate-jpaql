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
package org.hibernate.sql.ast.phase.hql.parse;

import org.hibernate.sql.ast.util.ASTUtil;
import org.hibernate.sql.ast.util.ASTPrinter;
import org.hibernate.sql.ast.util.ErrorHandlerDelegateImpl;
import org.hibernate.sql.ast.util.ErrorHandlerDelegate;
import org.hibernate.QueryException;
import org.hibernate.MappingException;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.sql.ast.common.CommonHibernateLexer;
import org.hibernate.sql.ast.common.TokenImpl;
import org.hibernate.sql.ast.common.Node;
import org.hibernate.sql.ast.common.NodeFactory;
import org.hibernate.util.ReflectHelper;
import org.hibernate.util.StringHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.PrintStream;
import java.io.PrintWriter;

import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.Token;
import antlr.ASTPair;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ASTFactory;

/**
 * The parser used by Hibernate to generate an AST given an input HQL string (a "stream parser").  The produced
 * AST is then used (and mutated) by later phases/parsers to apply semantic resolution; this parser, however, is
 * all about syntax resolution.
 *
 * @author Steve Ebersole
 */
public class HqlParser extends GeneratedHqlParser {
	private static final Logger log = LoggerFactory.getLogger( HqlParser.class );

	private final Context context;
	private final ErrorHandlerDelegate parseErrorHandler = new ErrorHandlerDelegateImpl();
	private final ASTPrinter printer = new ASTPrinter( HqlParseTokenTypes.class );
	private int traceDepth = 0;

	public HqlParser(String hql, Context context) {
		super( new CommonHibernateLexer( new StringReader( hql ) ) );
		this.context = context;
		super.setASTFactory( new NodeFactory() );
	}

	public HqlParser(String hql, final SessionFactoryImplementor sessionFactoryImplementor) {
		this( hql, wrapInContext( sessionFactoryImplementor ) );
	}

	// overrides of Antlr infastructure methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void reportError(RecognitionException e) {
		parseErrorHandler.reportError( e );
	}

	public void reportError(String s) {
		parseErrorHandler.reportError( s );
	}

	public void reportWarning(String s) {
		parseErrorHandler.reportWarning( s );
	}

	public ErrorHandlerDelegate getParseErrorHandler() {
		return parseErrorHandler;
	}

	static public void panic() {
		//overriden to avoid System.exit
		throw new QueryException( "Parser: panic" );
	}

	public void setASTFactory(ASTFactory astFactory) {
		throw new UnsupportedOperationException( "not allowed!" );
	}


// various AST output methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void traceIn(String s) throws TokenStreamException {
		if ( inputState.guessing > 0 ) {
			return;
		}
		String prefix = StringHelper.repeat( "-", (traceDepth++ * 2) ) + "->";
		trace( prefix + s );
	}

	public void traceOut(String s) throws TokenStreamException {
		if ( inputState.guessing > 0 ) {
			return;
		}
		String prefix = "<-" + StringHelper.repeat( "-", (--traceDepth * 2) );
		trace( prefix + s );
	}

    private void trace(String msg) {
		System.out.println( msg );
//		log.trace( msg );
	}

	public void dumpAst(AST ast) {
		dumpAst( ast, "DUMP" );
	}

	public void dumpAst(AST ast, String header) {
		log.info( printer.showAsString( ast, header ) );
	}

	public void showAst(AST ast, PrintStream out) {
		showAst( ast, new PrintWriter( out ) );
	}

	private void showAst(AST ast, PrintWriter pw) {
		printer.showAst( ast, pw );
	}


	// overrides of grammar semantic actions ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public String extractPath(AST tree) {
		try {
			return PathCollector.getPath( tree );
		}
		catch ( Throwable t ) {
			return tree.getText();
		}
	}

	public boolean isRegisteredFunction(AST tree) {
		return context.isRegisteredFunctionName( extractPath( tree ) );
	}

	public boolean isJavaConstant(String path) {
		try {
			log.debug( "Testing path [" + path + "] as potential java constant" );
			Object value = ReflectHelper.getConstantValueStrictly( path );
			log.debug( "Resolved path to java constant [" + value + "]" );
			return true;
		}
		catch( Throwable t ) {
			log.debug( "Path did not resolve to java constant : " + t );
			return false;
		}
	}

	public String resolveEntityName(String name) {
		return context.getImportedName( name );
	}

	public String resolveDynamicInstantiationPojoName(AST name) throws SemanticException {
		String path = extractPath( name );
		if ( "list".equals( path ) || "map".equals( path ) ) {
			return path;
		}
		else {
			String importedName = context.getImportedName( path );
			try {
				Class importedClass = ReflectHelper.classForName( importedName );
				return importedClass.getName();
			}
			catch ( ClassNotFoundException e ) {
				throw new SemanticException( "Unable to locate dynamic instantiation class [" + importedName + "]" );
			}
		}
	}

	protected AST processEqualityExpression(AST x) {
		if ( x == null ) {
			log.warn( "processEqualityExpression() : No expression to process!" );
			return null;
		}

		int type = x.getType();
		if ( log.isTraceEnabled() ) {
			log.trace( "processEqualityExpression() -> type : {}, name : {}", Integer.toString( type ), ASTUtil.getTokenTypeName( HqlParseTokenTypes.class, type ) );
		}
		if ( type == EQUALS_OP || type == NOT_EQUALS_OP || type == SQL_NOT_EQUALS_OP ) {
			boolean negated = ( type != EQUALS_OP );
			if ( x.getNumberOfChildren() == 2 ) {
				AST a = x.getFirstChild();
				AST b = a.getNextSibling();
				// (EQ NULL b) => (IS_NULL b)
				if ( a.getType() == NULL && b.getType() != NULL ) {
					return createIsNullParent( b, negated );
				}
				// (EQ a NULL) => (IS_NULL a)
				else if ( b.getType() == NULL && a.getType() != NULL ) {
					return createIsNullParent( a, negated );
				}
				else if ( b.getType() == EMPTY ) {
					return processIsEmpty( a, negated );
				}
				else {
					return x;
				}
			}
			else {
				return x;
			}
		}
		else {
			return x;
		}
	}

	private AST createIsNullParent(AST node, boolean negated) {
		node.setNextSibling( null );
		int type = negated ? IS_NOT_NULL : IS_NULL;
		String text = negated ? "is not null" : "is null";
		return ASTUtil.createParent( astFactory, type, text, node );
	}

	protected AST processMemberOf(AST path, AST notNode) {
		AST inNode = notNode == null ? astFactory.create( IN, "in" ) : astFactory.create( NOT_IN, "not in" );
		AST subqueryNode = createSubquery( path );
		AST inListNode = ASTUtil.createParent( astFactory, IN_LIST, "in-list", subqueryNode );
		inNode.addChild( inListNode );
		return inNode;
	}

	private AST createSubquery(AST subquerySource) {
//		AST ast = ASTUtil.createParent( astFactory, RANGE, "RANGE", node );
// todo : what is the type of 'node'?
		log.debug(
				"Generating subquery; incoming node type = {}; incoming node = [{}]",
				printer.getTokenTypeName( subquerySource.getType() ),
				subquerySource
		);
		AST fromNode = ASTUtil.createParent( astFactory, FROM, "from", subquerySource );
		AST selectFromNode = ASTUtil.createParent( astFactory, SELECT_FROM, "SELECT_FROM", fromNode );
		return ASTUtil.createParent( astFactory, QUERY, "QUERY", selectFromNode );
	}

	protected AST processIsEmpty(AST collection, AST notToken) {
		return processIsEmpty( collection, notToken != null );
	}

	private AST processIsEmpty(AST node, boolean negated) {
		node.setNextSibling( null );
		AST ast = createSubquery( node );
		ast = ASTUtil.createParent( astFactory, EXISTS, "exists", ast );
		// Add NOT if it's negated.
		if ( negated ) {
			ast = ASTUtil.createParent( astFactory, NOT, "not", ast );
		}
		return ast;
	}
	public AST negateNode(AST x) {
		//TODO: switch statements are always evil! We already had bugs because
		//      of forgotten token types. Use polymorphism for this!
		switch ( x.getType() ) {
			case OR:
				x.setType(AND);
				x.setText("{and}");
				negateNode( x.getFirstChild() );
				negateNode( x.getFirstChild().getNextSibling() );
				return x;
			case AND:
				x.setType(OR);
				x.setText("{or}");
				negateNode( x.getFirstChild() );
				negateNode( x.getFirstChild().getNextSibling() );
				return x;
			case EQUALS_OP:
				x.setType( NOT_EQUALS_OP );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (EQ a b) ) => (NE a b)
			case NOT_EQUALS_OP:
				x.setType( EQUALS_OP );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (NE a b) ) => (EQ a b)
			case GREATER_THAN_OP:
				x.setType( LESS_THAN_OR_EQUALS_OP );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (GT a b) ) => (LE a b)
			case LESS_THAN_OP:
				x.setType( GREATER_THAN_OR_EQUALS_OP );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (LT a b) ) => (GE a b)
			case GREATER_THAN_OR_EQUALS_OP:
				x.setType( LESS_THAN_OP );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (GE a b) ) => (LT a b)
			case LESS_THAN_OR_EQUALS_OP:
				x.setType( GREATER_THAN_OP );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (LE a b) ) => (GT a b)
			case LIKE:
				x.setType( NOT_LIKE );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (LIKE a b) ) => (NOT_LIKE a b)
			case NOT_LIKE:
				x.setType( LIKE );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (NOT_LIKE a b) ) => (LIKE a b)
			case IN:
				x.setType( NOT_IN );
				x.setText( "{not}" + x.getText() );
				return x;
			case NOT_IN:
				x.setType( IN );
				x.setText( "{not}" + x.getText() );
				return x;
			case IS_NULL:
				x.setType( IS_NOT_NULL );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (IS_NULL a b) ) => (IS_NOT_NULL a b)
			case IS_NOT_NULL:
				x.setType( IS_NULL );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (IS_NOT_NULL a b) ) => (IS_NULL a b)
			case BETWEEN:
				x.setType( NOT_BETWEEN );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (BETWEEN a b) ) => (NOT_BETWEEN a b)
			case NOT_BETWEEN:
				x.setType( BETWEEN );
				x.setText( "{not}" + x.getText() );
				return x;	// (NOT (NOT_BETWEEN a b) ) => (BETWEEN a b)
			case NOT:
				return x.getFirstChild();			// (NOT (NOT x) ) => (x)
			default:
				return super.negateNode( x );		// Just add a 'not' parent.
		}
	}

	protected void transferTrackingInfo(AST source, AST target) {
		if ( target instanceof Node ) {
			( ( Node ) target ).transferTrackingInfo( source );
		}
		else {
			super.transferTrackingInfo( source, target );
		}
	}

	/**
	 * Overrides the base behavior to retry keywords as identifiers.
	 *
	 * @param token The token.
	 * @param ex    The recognition exception.
	 * @return AST - The new AST.
	 * @throws antlr.RecognitionException if the substitution was not possible.
	 * @throws antlr.TokenStreamException if the substitution was not possible.
	 */
	public AST handleIdentifierError(Token token, RecognitionException ex) throws RecognitionException, TokenStreamException {
		// If the token can tell us if it could be an identifier...
		if ( token instanceof TokenImpl ) {
			TokenImpl hqlToken = ( TokenImpl ) token;
			// ... and the token could be an identifer and the error is
			// a mismatched token error ...
			if ( hqlToken.isPossibleIdentifier() && ex instanceof MismatchedTokenException ) {
				MismatchedTokenException mte = ( MismatchedTokenException ) ex;
				// ... and the expected token type was an identifier, then:
				if ( mte.expecting == IDENT ) {
					// Use the token as an identifier.
					reportWarning(
							"Keyword  '"+ token.getText()
							+ "' is being interpreted as an identifier due to: "
							+ mte.getMessage()
					);
					// Add the token to the AST.
					ASTPair currentAST = new ASTPair();
//					token.setType( WEIRD_IDENT );
					token.setType( IDENT );
					astFactory.addASTChild( currentAST, astFactory.create( token ) );
					consume();
					return currentAST.root;
				}
			}
		}
		return super.handleIdentifierError( token, ex );
	}

    public void handleDotIdent() throws TokenStreamException {
        // This handles HHH-354, where there is a strange property name in a where clause.
        // If the lookahead contains a DOT then something that isn't an IDENT...
		handleDotIdent( 1 );
    }

	protected int handleDotIdent(int offset) throws TokenStreamException {
		while ( LA( offset ) == DOT ) {
			switch ( LA( offset+1 ) ) {
				case IDENT:
				case CLASS:
					break; // break the case statement, not the loop...
				default:
					convertPossibleIdentifier( ( TokenImpl ) LT( offset+1 ) );
			}
			offset += 2;
		}
		return offset;
	}

	protected void weakKeywords() throws TokenStreamException {
		switch ( LA( 1 ) ) {
			case IDENT:
				break;
			default :
				if ( LA(0) == FROM && LA(2) == DOT ) {
					convertPossibleIdentifier( ( TokenImpl ) LT(1) );
				}
		}
	}

	protected void unequivocalKeywordAsIdentifier() throws TokenStreamException {
		if ( LA(1) == IDENT ) {
			return;
		}
		convertPossibleIdentifier( ( TokenImpl ) LT(1) );
	}

    protected void potentialUpdatePersisterAlias() throws TokenStreamException {
		switch( LA(1) ) {
			case AS:
				// alias rule will handle this...
			case SET:
				// SET marks the beginning of the UPDATE's SET-clause
				break;
			default:
				convertPossibleIdentifier( ( TokenImpl ) LT(1) );
		}
	}

    protected void potentialDeletePersisterAlias() throws TokenStreamException {
		switch( LA(1) ) {
			case AS:
				// alias rule will handle this...
			case WHERE:
				break;
			default:
				convertPossibleIdentifier( ( TokenImpl ) LT(1) );
		}
    }

    protected void prepareForPersisterReferenceRoot() throws TokenStreamException {
		if ( LA(1) == IN ) {
			return;
		}

		unequivocalKeywordAsIdentifier();

		if ( LA(1) == IDENT && LA(2) == IN ) {
			return;
		}

		prepareForBasicEntityPersisterReference();
	}

    protected void prepareForCrossJoinElements() throws TokenStreamException {
		unequivocalKeywordAsIdentifier();
		prepareForBasicEntityPersisterReference();
    }

	protected void prepareForBasicEntityPersisterReference() throws TokenStreamException {
		int offset = handleDotIdent( 2 );
		int next = LA(offset);
		switch ( next ) {
			case AS: {
				// the next token would need to be the persister alias
				convertPossibleIdentifier( (TokenImpl) LT(offset+1) );
				break;
			}
			case IDENT : {
				// nothing to do
				break;
			}
			case WHERE:
			case COMMA:
			case ON:
			case WITH:
			case JOIN:
			case RIGHT:
			case LEFT:
			case CROSS: {
				// single token structural elements indicating that the next thing could not be an alias
				break;
			}
			case UNION:
			case INTERSECT:
			case EXCEPT: {
				int nextNext = LA(offset+1);
				if ( nextNext == SELECT || nextNext == FROM ) {
					break;
				}
				else if ( nextNext == ALL ) {
					int nextNextNext = LA(offset+2);
					if ( nextNextNext == SELECT || nextNextNext == FROM ) {
						break;
					}
				}
				convertPossibleIdentifier( ( TokenImpl ) LT(offset) );
				break;
			}
			case FETCH:
				if ( ! ( LA(offset+1) == ALL && LA(offset+2) == PROPERTIES ) ) {
					// not sure sure I like allowing 'fetch' as an indentifier
					log.warn( "interpretting [fetch] keyword as alias; consider using differen alias" );
					convertPossibleIdentifier( ( TokenImpl ) LT(offset) );
				}
				break;
			default:
				convertPossibleIdentifier( ( TokenImpl ) LT(offset) );
				break;
		}
	}

	protected void prepareForQualifiedJoinElements() throws TokenStreamException {
		unequivocalKeywordAsIdentifier();
		prepareForBasicEntityPersisterReference();
    }

	protected void convertPossibleIdentifier(TokenImpl token) {
		if ( token.isPossibleIdentifier() && token.getType() != IDENT ) {
			log.debug( "Converting keyword [{}] to identifier", printer.getTokenTypeName( token.getType() ) );
			token.setType( IDENT );
		}
	}

	private static Context wrapInContext(final SessionFactoryImplementor sessionFactoryImplementor) {
		return new Context() {
			public boolean isEntityName(String name) {
				return findEntityPersisterByName( name ) != null;
			}

			public String getImportedName(String name) {
				return sessionFactoryImplementor.getImportedClassName( name );
			}

			public boolean isRegisteredFunctionName(String name) {
				return sessionFactoryImplementor.getDialect().getFunctions().get( name ) != null;
			}

			private EntityPersister findEntityPersisterByName(String name) throws MappingException {
				try {
					return sessionFactoryImplementor.getEntityPersister( name );
				}
				catch ( MappingException ignore ) {
					// unable to locate it using this name
				}

				// If that didn't work, try using the 'import' name.
				String importedClassName = sessionFactoryImplementor.getImportedClassName( name );
				if ( importedClassName == null ) {
					return null;
				}
				return sessionFactoryImplementor.getEntityPersister( importedClassName );
			}
		};
	}

	public static interface Context {
		/**
		 * Does this name represent an entity name?
		 *
		 * @param name The name to check
		 * @return True if the name represents an entioty name; false otherwise.
		 */
		public boolean isEntityName(String name);

		/**
		 * Get the import "replacement" name for the given name (e.g., 'User' -> 'com.acme.User').  <tt>null</tt>
		 * indicates an unrecognized name.
		 *
		 * @param name The name for which to locate the imported name.
		 * @return The corresponding imported name, or null.
		 */
		public String getImportedName(String name);

		/**
		 * Does the given name represent a registered function name?
		 *
		 * @param name The name to check
		 * @return True if the name matches a registered function name; false otherwise.
		 */
		public boolean isRegisteredFunctionName(String name);
	}
}
