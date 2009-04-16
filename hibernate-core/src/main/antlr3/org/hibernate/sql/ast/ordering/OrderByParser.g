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
    /**
     * Process the given node as a quote identifier.  These need to be quoted in the dialect-specific way.
     *
     * @param ident The quoted-identifier node.
     *
     * @return The processed node.
     */
    protected CommonTree quotedIdentifier(CommonTree ident) {
        // here we assume single-quote as the identifier quote character...
        return createTreeNode( IDENTIFIER, Template.TEMPLATE + ".'" + ident.getText() + "'" );
    }


    /**
     * Process the given node as a quote string.
     *
     * @param token The quoted string.  This is used from within function param recognition, and represents a
     * SQL-quoted string.
     *
     * @return The processed node.
     */
    protected CommonTree quotedString(Token token) {
    	return createTreeNode( STRING_LITERAL, "'" + token.getText() + "'" );
    }

    /**
     * A check to see if the text of the given node represents a known function name.
     *
     * @param token The node whose text we want to check.
     *
     * @return True if the node's text is a known function name, false otherwise.
     *
     * @see org.hibernate.dialect.function.SQLFunctionRegistry
     */
    protected boolean isFunctionName(Token token) {
    	return false;
    }

    /**
     * Process the given node as a function name.  Differs from {@link #resolveFunction(org.antlr.runtime.tree.CommonTree)
     * specifically in that here we are expecting just a function name without parens or arguments.
     *
     * @param token The token representing the function name.
     *
     * @return The processed node.
     */
    protected CommonTree resolveFunction(Token token) {
        return resolveFunction( new CommonTree( token ) );
    }

    /**
     * Process the given node as a function.
     *
     * @param tree The node representing the function invocation (including parameters as subtree components).
     *
     * @return The processed node.
     */
    protected CommonTree resolveFunction(CommonTree tree) {
		Tree argumentList = tree.getChild( 0 );
		assert argumentList == null || "{param list}".equals( argumentList.getText() );

        String text = tree.getText();
        int count = argumentList == null ? 0 : argumentList.getChildCount();
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

    protected CommonTree resolveIdent(Token token) {
        return resolveIdent( new CommonTree( token ) );
    }

    /**
     * Process the given node as an IDENTIFIER.  May represent either a column reference or a property reference.
     *
     * @param ident The node whose text represents either a column or property reference.
     *
     * @return The processed node.
     */
    protected CommonTree resolveIdent(CommonTree ident) {
        return createTreeNode( IDENTIFIER, Template.TEMPLATE + "." + ident.getText() );
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

	protected CommonTree createTreeNode(int type, String text) {
		return new CommonTree( new CommonToken( type, text ) );
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
	| ( IDENTIFIER ('.' IDENTIFIER)* OPEN_PAREN ) => functionCall
    | simplePropertyPath
    | IDENTIFIER 	-> {isFunctionName($IDENTIFIER)}? 	{ resolveFunction( $IDENTIFIER ) }
    			-> 							{ resolveIdent( $IDENTIFIER ) }
	;

hardQuoteExpression
@after { $tree = quotedIdentifier( $tree ); }
	: HARD_QUOTE IDENTIFIER HARD_QUOTE -> IDENTIFIER
	;

/**
 * Recognition rule for a function call
 */
functionCall
@after { $tree = resolveFunction( $tree ); }
	: functionName OPEN_PAREN functionParameterList CLOSE_PAREN -> ^( functionName functionParameterList )
	;

/**
 * A function-name is an IDENTIFIER followed by zero or more (DOT IDENTIFIER) sequences
 */
functionName returns [String nameText]
	: i=IDENTIFIER { $nameText = $i.text; } ( '.' i=IDENTIFIER { $nameText += ( '.' + $i.text ); } )+
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
    | qs=STRING_LITERAL -> { quotedString( $qs ) }
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
	: {(validateIdentifierAsKeyword("collate"))}?=>  id=IDENTIFIER
		->	COLLATE[$id]

	;

/**
 * The collation name wrt {@link #collationSpecification}.  Namely, the character-set.
 */
collationName
	: IDENTIFIER
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
 * A simple-property-path is an IDENTIFIER followed by one or more (DOT IDENTIFIER) sequences
 */
simplePropertyPath
@after { $tree = resolveIdent($tree); }
	: p=simplePropertyPathText -> { createTreeNode(IDENTIFIER, $p.pathText) }
	;

simplePropertyPathText returns [String pathText]
	: i=IDENTIFIER { $pathText = $i.text; } ( '.' i=IDENTIFIER { $pathText += ( '.' + $i.text ); } )+
	;



// Lexer rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

WS
    : (SPACE | EOL | '\u000C') { $channel=HIDDEN; }
    ;

fragment
EOL
    : ( '\r' (options{greedy=true;}: '\n')? | '\n' )
    ;

fragment
SPACE
    : ' '
    | '\t'
    ;

OPEN_PAREN
    : '('
    ;
CLOSE_PAREN
    : ')'
    ;

COMMA
    : ','
    ;

HARD_QUOTE
    : '`'
    ;

INTEGER_LITERAL
    : (
        '0'
        | '1'..'9' ('0'..'9')*
    )
    ;

DECIMAL_LITERAL : ('0' | '1'..'9' '0'..'9'*) INTEGER_TYPE_SUFFIX ;

HEX_LITERAL
    : '0' ('x'|'X') HEX_DIGIT+ INTEGER_TYPE_SUFFIX?
    ;

OCTAL_LITERAL : '0' ('0'..'7')+ INTEGER_TYPE_SUFFIX? ;


fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
INTEGER_TYPE_SUFFIX : ('l'|'L') ;

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


STRING_LITERAL
    :   '\'' ( ESCAPE_SEQUENCE | ~('\''|'\\') ) '\''
    ;

fragment
ESCAPE_SEQUENCE
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESCAPE
    |   OCTAL_ESCAPE
    ;

fragment
OCTAL_ESCAPE
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESCAPE
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

IDENTIFIER
	: IDENTIFIER_START_FRAGMENT (IDENTIFER_FRAGMENT)*
	;

fragment
IDENTIFIER_START_FRAGMENT
    : ('a'..'z'|'A'..'Z'|'_'|'$'|'\u0080'..'\ufffe')
    ;

fragment
IDENTIFER_FRAGMENT
    : IDENTIFIER_START_FRAGMENT
    | '0'..'9'
    ;

