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
grammar OrderByParser;

options {
	language = Java;
	output = AST;
	ASTLabelType = CommonTree;
}

tokens {
    COLLATE;
	ASCENDING;
	DESCENDING;

    ORDER_BY;
    SORT_SPEC;
    ORDER_SPEC;
    SORT_KEY;
    EXPR_LIST;
    IDENT_LIST;
    COLUMN_REF;

	NUM_INTEGER_LITERAL;
    NUM_LONG_LITERAL;
    NUM_DOUBLE_LITERAL;
    NUM_FLOAT_LITERAL;
}

@lexer::header {
	package org.hibernate.sql.ast.ordering;
}

@lexer::members {
}

@parser::header {
	package org.hibernate.sql.ast.ordering;

	import org.hibernate.sql.Template;
	import org.hibernate.dialect.function.SQLFunction;
}

@parser::members {
	private final TranslationContext context;

//	public OrderByParserParser(TokenStream input, TranslationContext context) {
//		super( input );
//		this.context = context;
//	}

    /**
     * Process the given node as a quote identifier.  These need to be quoted in the dialect-specific way.
     *
     * @param ident The quoted-identifier node.
     *
     * @return The processed node.
     *
     * @see org.hibernate.dialect.Dialect#quote
     */
    protected CommonTree quotedIdentifier(CommonTree ident) {
    	String quotedText = Template.TEMPLATE + "." + context.getDialect().quote( '`' + ident.getText() + '`' );
    	return createTreeNode( ident.getToken().getType(), quotedText );
    }

    /**
     * Process the given node as a quote string.
     *
     * @param ident The quoted string.  This is used from within function param recognition, and represents a
     * SQL-quoted string.
     *
     * @return The processed node.
     */
    protected CommonTree quotedString(CommonTree ident) {
    	String quotedText = context.getDialect().quote( ident.getText() );
    	return createTreeNode( ident.getToken().getType(), quotedText );
    }

    /**
     * A check to see if the text of the given node represents a known function name.
     *
     * @param ast The node whose text we want to check.
     *
     * @return True if the node's text is a known function name, false otherwise.
     *
     * @see org.hibernate.dialect.function.SQLFunctionRegistry
     */
    protected boolean isFunctionName(CommonToken token) {
    	return context.getSqlFunctionRegistry().hasFunction( token.getText() );
    }

    /**
     * Process the given node as a function.
     *
     * @param The node representing the function invocation (including parameters as subtree components).
     *
     * @return The processed node.
     */
    protected CommonTree resolveFunction(CommonTree tree) {
		// todo : handle sub functions?
   		Tree parameters = tree.getChild(0);
		assert EXPR_LIST == parameters.getType();

		final String functionName = tree.getText();
		final SQLFunction function = context.getSqlFunctionRegistry().findSQLFunction( functionName );
		if ( function == null ) {
			String text = functionName;
			if ( parameters.getChildCount() > 0 ) {
				text+= '(';
				for ( int i = 0, x = parameters.getChildCount(); i < x; i++ ) {
					text+= parameters.getChild(i).getText();
					if ( i < x ) {
						text+= ", ";
					}
				}
				text+= ')';
			}
			return createTreeNode( IDENT, text );
		}
		else {
			ArrayList expressions = new ArrayList();
			for ( int i = 0, x = parameters.getChildCount(); i < x; i++ ) {
				expressions.add( parameters.getChild(i).getText() );
			}
			final String text = function.render( expressions, context.getSessionFactory() );
			return createTreeNode( IDENT, text );
		}
    }

    /**
     * Process the given node as an IDENT.  May represent either a column reference or a property reference.
     *
     * @param ident The node whose text represents either a column or property reference.
     *
     * @return The processed node.
     */
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
			return createTreeNode( IDENT, Template.TEMPLATE + "." + text );
		}
		else if ( replacements.length == 1 ) {
			return createTreeNode( IDENT, Template.TEMPLATE + "." + replacements[0] );
		}
		else {
			final CommonTree root = createTreeNode( IDENT_LIST, "{ident list}" );
			for ( int i = 0; i < replacements.length; i++ ) {
				final String identText = Template.TEMPLATE + '.' + replacements[i];
				root.addChild( createTreeNode( IDENT, identText ) );
			}
			return root;
		}
    }

	private boolean validateIdentifierAsKeyword(String text) {
		return validateLT( 1, text );
	}

	private boolean validateLT(int offset, String text) {
		String text2Validate = retrieveLT( offset );
		return text2Validate == null ? false : text2Validate.equalsIgnoreCase(text);
	}

	private String retrieveLT(int offset) {
      	if (null == input) {
      		return null;
      	}
		Token token = input.LT(offset);
		return token == null ? null : token.getText();
	}

	private CommonTree createTreeNode(int type, String text) {
		return new CommonTree( CommonToken( type, text ) );
	}
}


// Parser rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


/**
 * Main recognition rule for this grammar
 */
orderByFragment
	: sortSpecification ( ',' sortSpecification )* -> ^( ORDER_BY sortSpecification+ )
	;


/**
 * Reconition rule for what ANSI SQL terms the <tt>sort specification</tt>, which is essentially each thing upon which
 * the results should be sorted.
 */
sortSpecification
	: sortKey collationSpecification? orderingSpecification? -> ^( SORT_SPEC sortKey collationSpecification? orderingSpecification? )
;


/**
 * Reconition rule for what ANSI SQL terms the <tt>sort key</tt> which is the expression (column, function, etc) upon
 * which to base the sorting.
 */
sortKey
	: expression -> ^( SORT_KEY expression )
;

/**
 * Reconition rule what this grammar recognizes as valid <tt>sort key</tt>.
 */
expression
	: hardQuoteExpression
	| ( IDENT ('.' IDENT)* OPEN_PAREN ) => functionCall
    | simplePropertyPath
    | i=IDENT 	-> {isFunctionName($i)}? 	{ resolveFunction( i ) }
    			-> 							{ resolveIdent( i ) }
	;

hardQuoteExpression
@after { $tree = quotedIdentifier( $tree ); }
	: HARD_QUOTE IDENT HARD_QUOTE -> IDENT
	;

/**
 * Recognition rule for a function call
 */
functionCall
@after { $tree = resolveFunction( $tree ); }
	: functionName OPEN_PAREN functionParameterList CLOSE_PAREN -> ^( functionName functionParameterList )
	;

/**
 * A function-name is an IDENT followed by zero or more (DOT IDENT) sequences
 */
functionName returns [String nameText]
	: i=IDENT { $nameText = $i.text; } ( '.' i=IDENT { $nameText += ( '.' + $i.text ); } )+
	;

/**
 * Recognition rule used to "wrap" all function parameters into an EXPR_LIST node
 */
functionParameterList
	: functionParameter ( COMMA functionParameter )* -> ^( EXPR_LIST functionParameter+ )
	;


/**
 * Recognized function parameters.
 */
functionParameter :
    expression
    | numericLiteral
    | qs=QUOTED_STRING -> { quotedString( $qs ) }
;

numericLiteral
	: HEX_LITERAL
	| OCTAL_LITERAL
	| DECIMAL_LITERAL
	| FLOATING_POINT_LITERAL
	;


/**
 * Reconition rule for what ANSI SQL terms the <tt>collation specification</tt> used to allow specifying that sorting for
 * the given {@link #sortSpecification} be treated within a specific character-set.
 */
collationSpecification! :
    collateKeyword collationName -> { createTreeNode(COLLATE, $collationName.text) }
;

collateKeyword
	: {(validateIdentifierAsKeyword("collate"))}?=>  id=IDENT
		->	COLLATE[$id]

	;

/**
 * The collation name wrt {@link #collationSpecification}.  Namely, the character-set.
 */
collationName
	: IDENT
	;

/**
 * Reconition rule for what ANSI SQL terms the <tt>ordering specification</tt>; <tt>ASCENDING</tt> or
 * <tt>DESCENDING</tt>.
 */
orderingSpecification!
	: ( 'asc' | 'ascending' ) -> { createTreeNode(ORDER_SPEC,"asc") }
    | ( 'desc' | 'descending') -> { createTreeNode(ORDER_SPEC,"desc" ) }
	;

/**
 * A simple-property-path is an IDENT followed by one or more (DOT IDENT) sequences
 */
simplePropertyPath
@after { $tree = resolveIdent($tree); }
	: p=simplePropertyPathText -> { createTreeNode(IDENT, $p.pathText) }
	;

simplePropertyPathText returns [String pathText]
	: i=IDENT { $pathText = $i.text; } ( '.' i=IDENT { $pathText += ( '.' + $i.text ); } )+
	;



// Lexer rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

WS :  (NEWLINE | SPACE | '\u000C') { $channel=HIDDEN; } ;

fragment
NEWLINE :
	( '\r' (options{greedy=true;}: '\n')? | '\n' )
;

fragment
SPACE :
	  ' ' | '\t'
;

OPEN_PAREN : '(';
CLOSE_PAREN : ')';

COMMA : ',';

HARD_QUOTE : '`';

IDENT : ID ;

fragment
ID : ID_START_FRAGMENT ( ID_FRAGMENT )* ;

fragment
ID_START_FRAGMENT
	: '_'
    |    '$'
    |    'a'..'z'
    |    '\u0080'..'\ufffe'
	;

fragment
ID_FRAGMENT
	: ID_START_FRAGMENT
    |    '0'..'9'
	;

HEX_LITERAL : '0' ('x'|'X') ('0'..'9'|'a'..'f'|'A'..'F')+ INTEGRAL_TYPE_SUFFIX? ;

OCTAL_LITERAL : '0' ('0'..'7')+ INTEGRAL_TYPE_SUFFIX? ;

DECIMAL_LITERAL : ('0' | '1'..'9' '0'..'9'*) INTEGRAL_TYPE_SUFFIX? ;

fragment
INTEGRAL_TYPE_SUFFIX
	: ('l'|'L')
	;

FLOATING_POINT_LITERAL
    :   ('0'..'9')+ '.' ('0'..'9')* EXPONENT? FLOAT_TYPE_SUFFIX?
    |   '.' ('0'..'9')+ EXPONENT? FLOAT_TYPE_SUFFIX?
    |   ('0'..'9')+ EXPONENT FLOAT_TYPE_SUFFIX?
    |   ('0'..'9')+ FLOAT_TYPE_SUFFIX
    ;

fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
FLOAT_TYPE_SUFFIX : ('f'|'F'|'d'|'D') ;

QUOTED_STRING :
	  ('\'' (options{greedy=true;}: ~('\'' | '\r' | '\n') | '\'' '\'' | NEWLINE)* '\'' )+
;

/**
 * Recognize either double-quote (") or back-tick (`) as delimiting a quoted identifier
 */
QUOTED_IDENT :
	    '"' (~('"' | '\r' | '\n') | '"' '"')+ '"'
	|   '`' (~('`' | '\r' | '\n') | '`' '`')+ '`'
;
