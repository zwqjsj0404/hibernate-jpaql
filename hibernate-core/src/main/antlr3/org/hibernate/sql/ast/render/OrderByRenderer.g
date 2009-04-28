tree grammar OrderByRenderer;

options{
	output=template;
	tokenVocab=HQLLexer;
	ASTLabelType=CommonTree;
	TokenLabelType=CommonToken;
}

@header {
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
package org.hibernate.sql.ast.render;
}

// todo : merge with 'full sql rendering' grammar
//    this is currently just a temporary subset grammar limited to the needs of mapping-defined order-by fragments

orderByClause
    : ^( ORDER_BY sortSpecs+=sortSpecification+ )
        ->  orderByClause( sortSpecifications={$sortSpecs} )
    ;

sortSpecification
    : ^( SORT_SPEC sortKey COLLATE? ORDER_SPEC? )
        ->  sortSpecification(
                    sortKey={$sortKey.st},
                    collationSpecification={$COLLATE.text},
                    orderingSpecification={$ORDER_SPEC.text}
            )
    ;

sortKey
    : expression
    ;

expression
    : column
    | function
    ;

column
    : ^( COLUMN ALIAS_REF identifier )
        -> column( qualifier={$ALIAS_REF.text}, name={$identifier.text} )
    ;

identifier
    : IDENTIFIER
    | QUOTED_IDENTIFIER
    ;

function
    : functionFunction
//	| castFunction
//	| concatFunction
//	| substringFunction
//	| trimFunction
//	| upperFunction
//	| lowerFunction
//	| lengthFunction
//	| locateFunction
	| absFunction
	| sqrtFunction
	| modFunction
	| currentDateFunction
	| currentTimeFunction
	| currentTimestampFunction
	| extractFunction
	| positionFunction
	| charLengthFunction
	| octetLengthFunction
	| bitLengthFunction
    ;

functionFunction
    : ^( FUNCTION args+=functionArgument* )
        -> basicFunction( name={$FUNCTION.text}, args={$args} )
    ;

functionArgument
    : expression
    | literal
;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// rewritten in parser as ^( FUNCTION ... ) structures
//castFunction
//	: ^( CAST valueExpression IDENTIFIER )
//	;
//
//concatFunction
//	: ^( CONCAT valueExpression+ )
//	;
//
//substringFunction
//	:	^(SUBSTRING characterValueExpression numericValueExpression numericValueExpression?)
//	;
//
//trimFunction
//	:	^(TRIM trimOperands)
//	;
//
//trimOperands
//	:	^((LEADING|TRAILING|BOTH) characterValueExpression characterValueExpression)
//	;
//
//upperFunction
//	:	^(UPPER characterValueExpression)
//	;
//
//lowerFunction
//	:	^(LOWER characterValueExpression)
//	;
//
//lengthFunction
//	:	^(LENGTH characterValueExpression)
//	;
//
//locateFunction
//	:	^(LOCATE characterValueExpression characterValueExpression numericValueExpression?)
//	;
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

absFunction
	:	^( ABS expression )
	;

sqrtFunction
	:	^(SQRT expression)
	;

modFunction
	:	^(MOD expression expression)
	;

currentDateFunction
	:	CURRENT_DATE
	;

currentTimeFunction
	:	CURRENT_TIME
	;

currentTimestampFunction
	:	CURRENT_TIMESTAMP
	;

extractFunction
	:	^(EXTRACT extractField expression)
	;

extractField
	:	datetimeField
	|	timeZoneField
	;

datetimeField
	:	YEAR
	|	MONTH
	|	DAY
	|	HOUR
	|	MINUTE
	|	SECOND
	;

timeZoneField
	:	TIMEZONE_HOUR
	|	TIMEZONE_MINUTE
	;

positionFunction
	:	^(POSITION expression expression)
	;

charLengthFunction
	:	^(CHARACTER_LENGTH expression)
	;

octetLengthFunction
	:	^(OCTET_LENGTH expression)
	;

bitLengthFunction
	:	^(BIT_LENGTH expression)
	;

literal
	:	numeric_literal
	|	HEX_LITERAL
	|	OCTAL_LITERAL
	|	CHARACTER_LITERAL
	|	STRING_LITERAL
	;

numeric_literal
	:	INTEGER_LITERAL
	|	DECIMAL_LITERAL
	|	FLOATING_POINT_LITERAL
	;

