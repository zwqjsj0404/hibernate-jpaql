header
{
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
 * Portions of SQL grammar parsing copyright (C) 2003 by Lubos Vnuk.  All rights
 * reserved.  These portions are distributed under license by Red Hat Middleware
 * LLC and are covered by the above LGPL notice.  If you redistribute this material,
 * with or without modification, you must preserve this copyright notice in its
 * entirety.
 */
package org.hibernate.sql.ast.common;

import antlr.collections.AST;
import antlr.Token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.util.StringHelper;
}

/**
 * A convenience base class for Hibernate parsers.
 * <p/>
 * Our parsers are generally dealing with some aspect of SQL, and so this parser provides commonly needed
 * actions as well as exporting the basic common vocaulary of needed tokens (including all ISO/ANSI SQL-92
 * key/reserved words).
 *
 * @author Lubos Vnuk
 * @author Joshua Davis
 * @author Steve Ebersole
 */
class CommonHibernateParserSupport extends Parser;

options
{
	exportVocab=Sql92;
	buildAST=true;
	k=2;
//    codeGenMakeSwitchThreshold=4; // Code optimization
//    codeGenBitsetTestThreshold=8; // Code optimization
}

tokens
{
    // SQL key/reserved words
	ALL = "all";
	AND = "and";
	ANY = "any";
	AS = "as";
	ASCENDING = "asc";
    AT = "at";
	AVG = "avg";
	BETWEEN = "between";
    BIT = "bit";
    BIT_LENGTH = "bit_lenght";
    BOTH = "both";
//    BY = "by";
    CASE = "case";
    CAST = "cast";
    CHAR = "char";
    CHARACTER = "character";
    CHAR_LENGTH = "char_length";
    CHARACTER_LENGTH = "character_length";
    COALESCE = "coalesce";
    COLLATE = "collate";
    CONVERT = "convert";
    CORRESPONDING = "corresponding";
    COUNT = "count";
    CROSS = "cross";
    CURRENT = "current";
    CURRENT_DATE = "current_date";
    CURRENT_TIME = "current_time";
    CURRENT_TIMESTAMP = "current_timestamp";
    DATE = "date";
    DAY = "day";
    DEC = "dec";
    DECIMAL = "decimal";
    DEFAULT = "default";
    DELETE = "delete";
    DESCENDING = "desc";
	DISTINCT = "distinct";
	DOUBLE = "double";
	ELSE = "else";
	END = "end";
	ESCAPE = "escape";
    EXCEPT = "except";
	EXISTS = "exists";
	EXTRACT = "extract";
	FALSE = "false";
	FLOAT = "float";
	FOR = "for";
    FROM = "from";
    FULL = "full";
    GLOBAL = "global";
//    GROUP = "group";
    HAVING = "having";
    HOUR = "hour";
    IN = "in";
    INDICATOR = "indicator";
    INNER = "inner";
    INSERT = "insert";
    INT = "int";
    INTEGER = "integer";
    INTERSECT = "intersect";
    INTERVAL = "interval";
    INTO = "into";
	IS = "is";
    JOIN = "join";
    LEADING = "leading";
    LEFT = "left";
    LIKE = "like";
    LOCAL = "local";
    LOWER = "lower";
    MATCH = "match";
    MAX = "max";
    MIN = "min";
    MINUS = "minus";
    MINUTE = "minute";
    MODULE = "module";
    MONTH = "month";
    NATIONAL = "national";
    NATURAL = "natural";
    NCHAR = "nchar";
    NOT = "not";
    NULL = "null";
    NULLIF = "nullif";
    NUMERIC = "numeric";
    OCTET_LENGTH = "octet_length";
    OF = "of";
    ON = "on";
    ONLY = "only";
    OR = "or";
//    ORDER = "order";
	OUTER = "outer";
	OVERLAPS = "overlaps";
	PARTIAL = "partial";
	POSITION = "position";
	PRECISION = "precision";
    READ = "read";
    REAL = "real";
    RIGHT = "right";
    SECOND = "second";
    SELECT = "select";
    SET = "set";
    SMALLINT = "smallint";
    SOME = "some";
    SUBSTRING = "substring";
    SUM = "sum";
    TABLE = "table";
    THEN = "then";
    TIME = "time";
    TIMESTAMP = "timestamp";
    TIMEZONE_HOUR = "timezone_hour";
    TIMEZONE_MINUTE = "timezone_minute";
    TO = "to";
    TRAILING = "trailing";
    TRANSLATE = "translate";
    TRIM = "trim";
    TRUE = "true";
    UNION = "union";
    UNIQUE = "unique";
    UNKNOWN = "unknown";
    UPDATE = "update";
    UPPER = "upper";
    USING = "using";
    VALUE = "value";
    VALUES = "values";
    VARCHAR = "varchar";
    VARYING = "varying";
    WHEN = "when";
    WHERE = "where";
    WITH = "with";
    YEAR = "year";
    ZONE = "zone";

    DOT;
    ORDER_BY;
    GROUP_BY;

    ROW_VALUE_CONSTRUCTOR_LIST;

    // synthetic numeric literal types
    NUM_INT_LITERAL;
    NUM_LONG_LITERAL;
    NUM_DOUBLE_LITERAL;
    NUM_FLOAT_LITERAL;

    NATIONAL_CHAR_STRING_LIT;
    BIT_STRING_LIT;
    HEX_STRING_LIT;

    // HQL-specific keywords
    ABS = "abs";
	CLASS = "class";
	CONCAT = "concat";
	ELEMENTS = "elements";
	EMPTY = "empty";
	FETCH = "fetch";
	INDEX = "index";
	INDICES = "indices";
	LENGTH = "length";
	LOCATE = "locate";
	MAXELEMENT = "maxelement";
	MAXINDEX = "maxindex";
	MEMBER = "member";
	MINELEMENT = "minelement";
	MININDEX = "minindex";
	MOD = "mod";
	NEW = "new";
	OBJECT = "object";
	PROPERTIES = "properties";
	SIZE = "size";
	SQRT = "sqrt";
	VERSIONED = "versioned";
}

{
	private static final Logger log = LoggerFactory.getLogger( CommonHibernateParserSupport.class );

	private int traceDepth = 0;

	/**
	 * {@inheritDoc}
	 */
	public void traceIn(String s) throws TokenStreamException {
		if ( inputState.guessing > 0 ) {
			return;
		}
		String prefix = StringHelper.repeat( "-", (traceDepth++ * 2) ) + "->";
		traceExecution( prefix + s );
	}

	/**
	 * {@inheritDoc}
	 */
	public void traceOut(String s) throws TokenStreamException {
		if ( inputState.guessing > 0 ) {
			return;
		}
		String prefix = "<-" + StringHelper.repeat( "-", (--traceDepth * 2) );
		traceExecution( prefix + s );
	}

	/**
	 * Perform trace logging.  Called from both {@link #traceIn} and {@link #traceOut}.
	 *
	 * @param msg The trace string
	 */
	protected void traceExecution(String msg) {
		log.trace( msg );
	}

	public void showAST(AST ast) {
	    showAST( ast, "AST" );
	}

	public void showAST(AST ast, String title) {
	    if ( log.isDebugEnabled() ) {
	        log.debug( title + " [tree string] : " + ast.toStringTree() );
    	}
	}
}

protected
literal :
    numericLiteral
    | characterLiteral
    | dateLiteral
    | timeLiteral
    | timestampLiteral
    | intervalLiteral
;

protected
numericLiteral :
    UNSIGNED_INTEGER
    | NUM_INT_LITERAL
    | NUM_LONG_LITERAL
    | NUM_DOUBLE_LITERAL
    | NUM_FLOAT_LITERAL
;

protected
characterLiteral :
    CHAR_STRING
    | NATIONAL_CHAR_STRING_LIT
	| BIT_STRING_LIT
	| HEX_STRING_LIT
;

protected
dateLiteral :
    DATE^ CHAR_STRING
;

protected
timeLiteral :
    TIME^ CHAR_STRING
;

protected
timestampLiteral :
    TIMESTAMP^ CHAR_STRING
;

protected
intervalLiteral :
    INTERVAL ( PLUS_SIGN | MINUS_SIGN )? CHAR_STRING intervalQualifier
;

protected
intervalQualifier :
    intervalStartField ( TO intervalEndField | )
    | SECOND ( LEFT_PAREN UNSIGNED_INTEGER ( COMMA UNSIGNED_INTEGER )? RIGHT_PAREN )?
;

protected
intervalStartField :
    nonSecondDatetimeField ( LEFT_PAREN UNSIGNED_INTEGER RIGHT_PAREN )?
;

protected
intervalEndField :
    nonSecondDatetimeField
	| SECOND ( LEFT_PAREN UNSIGNED_INTEGER RIGHT_PAREN )?
;

protected
nonSecondDatetimeField :
    YEAR
    | MONTH
    | DAY
    | HOUR
    | MINUTE
;



// Lexer ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * A Lexer for recognizing ISO/ANSI SQL vocabnulary tokens.
 *
 * @author Lubos Vnuk
 * @author Joshua Davis
 * @author Steve Ebersole
 */
class CommonHibernateLexerSupport extends Lexer;

options {
	exportVocab = Sql92;
	testLiterals = false;
	k = 2;
	charVocabulary = '\u0000'..'\uFFFE';
	caseSensitive = false;
	caseSensitiveLiterals = false;
}


{
    /**
     * Lexer action used to hook callbacks from lexer rules whenever we encounter a token
     * which could conceivable be used as an identifer.  This is used to provide "keyword-as-identifier"
     * handling (where a keyword is used as an identifier).
     *
     * @param possibleIdentifier Whether the token could be an idientifier.
     */
	protected void setPossibleIdentifier(boolean possibleIdentifier) {
	}

    protected void markAsApproximate() {
    }
}

EQUALS_OP : '=' ;
NOT_EQUALS_OP : "!=" | "^=";
SQL_NOT_EQUALS_OP : "<>" { $setType(NOT_EQUALS_OP); };

LESS_THAN_OP : '<';
LESS_THAN_OR_EQUALS_OP : "<=";

GREATER_THAN_OP : '>';
GREATER_THAN_OR_EQUALS_OP : ">=";

CONCATENATION_OP : "||";
VERTICAL_BAR : '|';

PARAM : '?' ;

COLON : ':';
SEMICOLON : ';' ;

LEFT_BRACKET : '[' ;
RIGHT_BRACKET : ']' ;

LEFT_PAREN : '(' ;
RIGHT_PAREN : ')' ;

PLUS_SIGN : '+'	;
MINUS_SIGN : '-';

ASTERISK : '*' ;
SOLIDUS : '/' ;

COMMA : ',' ;

PERCENT : '%' ;
AMPERSAND : '&' ;


CHAR_STRING :
	  ('\'' (options{greedy=true;}: ~('\'' | '\r' | '\n') | '\'' '\'' | NEWLINE)* '\'' )+
//	| '\'' {$setType(QUOTE);}
;

//QUOTED_STRING :
//    '\'' ( (ESCqs)=> ESCqs | ~'\'' )* '\''
//;
//
//protected
//ESCqs :
//    '\'' '\''
//;

/**
 * Recognize either double-quote (") or back-tick (`) as delimiting a quoted identifier
 */
QUOTED_IDENT :
	    '"' (~('"' | '\r' | '\n') | '"' '"')+ '"'
	|   '`' (~('`' | '\r' | '\n') | '`' '`')+ '`'
;

IDENT options {testLiterals=true;} :
	( NATIONAL_CHAR_STRING_LIT {$setType(NATIONAL_CHAR_STRING_LIT);}
	    | BIT_STRING_LIT {$setType(BIT_STRING_LIT);}
	    | HEX_STRING_LIT {$setType(HEX_STRING_LIT);}
	)
	| id:ID { setPossibleIdentifier( true ); } (
	    { id.getText().equalsIgnoreCase("order") }? (WHITESPACE)+ "by"! { setPossibleIdentifier( false ); $setType(ORDER_BY); $setText("order by"); }
	    | { id.getText().equalsIgnoreCase("group") }? (WHITESPACE)+ "by"! { setPossibleIdentifier( false ); $setType(GROUP_BY); $setText("group by"); }
	)?
;

protected
ID options { testLiterals=true; } :
	(SIMPLE_LETTER | '_' | '$') (SIMPLE_LETTER | '_' | '$' | '0'..'9')* {
        setPossibleIdentifier( true );
	}
;

protected
NATIONAL_CHAR_STRING_LIT :
	'n' ('\'' (options{greedy=true;}: ~('\'' | '\r' | '\n' ) | '\'' '\'' | NEWLINE)* '\'' )+
;

protected
BIT_STRING_LIT :
	'b' ('\'' ('0' | '1')* '\'' )+
;

protected
HEX_STRING_LIT :
	'x' ("\'" ('a'..'f' | '0'..'9')* "\'" )+
;

protected
SIMPLE_LETTER :
	'a'..'z'
    | '\u0080'..'\ufffe'
;

/**
 * This rule actually recognizes all numeric literals despite the name...
 */
NUM_INT_LITERAL :
    // IMPL NOTE : 2 basic alt-branches:
    //      1) starting with unsigned-int
    //      2) starting with a decimal ('.')
    UNSIGNED_INTEGER (
        '.' UNSIGNED_INTEGER {$setType(NUM_DOUBLE_LITERAL);} ( 'e' SIGNED_INTEGER {markAsApproximate();} )? ( 'f' {$setType(NUM_FLOAT_LITERAL);} )?
        | ( 'e' SIGNED_INTEGER {markAsApproximate();} )? ( ls:'l' {$setType(NUM_LONG_LITERAL);} )?
    )
    | '.' UNSIGNED_INTEGER {$setType(NUM_DOUBLE_LITERAL);} ( 'e' SIGNED_INTEGER {markAsApproximate();} )? ( 'f' {$setType(NUM_FLOAT_LITERAL);} )?
    | '.' {$setType(DOT);}
;

protected
SIGNED_INTEGER :
    ( '+' | '-' )? UNSIGNED_INTEGER
;

protected
UNSIGNED_INTEGER :
	('0'..'9')+
;

WHITESPACE :
    ( SPACE | NEWLINE ) {
        //ignore this token
        $setType( Token.SKIP );
    }
;

protected
NEWLINE :
	( '\r' (options{greedy=true;}: '\n')? | '\n' ) {newline();}
;

protected
SPACE :
	  ' ' | '\t'
;

protected
ANY_CHAR :
	.
;
