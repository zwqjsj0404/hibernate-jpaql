tree grammar HQLTreeWalker;

options{
	tokenVocab=HQL;
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
package org.hibernate.sql.ast.phase.hql.parse;

import java.util.LinkedList;
}

filterStatement[String collectionRole]
	:	^(QUERY ^(QUERY_SPEC FILTER 
				selectClause? whereClause? ( groupByClause havingClause?)? orderByClause?))
	;

statement
	:	updateStatement
	|	deleteStatement
	|	insertStatement
	|	queryStatement
	;

updateStatement
	:	^(UPDATE VERSIONED? ENTITY_NAME ALIAS_NAME? ^(SET assignment+) whereClause?)
	;

assignment
	:	^(EQUALS ^(ASSIGNMENT_FIELD dotIdentifierPath) valueExpression)
	;

deleteStatement
	:	^(DELETE ENTITY_NAME ALIAS_NAME? whereClause?)
	;

insertStatement
	:	^(INSERT intoClause queryStatement)
	;

intoClause
	:	^(INTO ENTITY_NAME ^(INSERTABILITY_SPEC insertablePropertySpecification+ ) )
	;

insertablePropertySpecification
	:	^(INSERTABLE_PROPERTY dotIdentifierPath)
	;

queryStatement
	:	^(QUERY queryExpression orderByClause?)
	;

queryExpression
	:	^(UNION queryExpression queryExpression)
	|	^(INTERSECT ALL? queryExpression queryExpression)
	|	^(EXCEPT ALL? queryExpression queryExpression)
	|	querySpec	
	;

querySpec
	:	^(QUERY_SPEC selectFrom whereClause? groupByClause? havingClause?)
	;

whereClause
	:	^(WHERE searchCondition)
	;

groupByClause
	:	^(GROUP_BY groupingValue+)
	;

groupingValue
	:	^(GROUPING_VALUE valueExpression COLLATE?)
	;

havingClause
	:	^(HAVING searchCondition)
	;

selectFrom
	:	^(SELECT_FROM fromClause selectClause?)
	;

fromClause
	:	^(FROM persisterSpaces+)
	;

persisterSpaces
	:	^(PERSISTER_SPACE persisterSpace)
	;

persisterSpace
	:	persisterSpaceRoot joins*
	;

joins
	:	^(PROPERTY_JOIN joinType FETCH? ALIAS_NAME? PROP_FETCH? (propertyReference|collectionExpression) withClause?)
	|	^(PERSISTER_JOIN joinType persisterSpaceRoot onClause?)
	;

withClause
	:	^(WITH searchCondition)
	;

onClause
	:	^(ON searchCondition)
	;

joinType
	:	CROSS
	|	INNER
	|	(LEFT |	RIGHT | FULL) OUTER?
	;

persisterSpaceRoot
	:	^(ENTITY_PERSISTER_REF ENTITY_NAME ALIAS_NAME? PROP_FETCH?)
	|	joins
	;

selectClause
	:	^(SELECT DISTINCT? rootSelectExpression) 
	;

rootSelectExpression
	:	^(SELECT_ITEM ^(DYNAMIC_INSTANTIATION dynamicInstantiationArg+))
	|	^(SELECT_ITEM ^(OBJECT ALIAS_REF))
	|	^(SELECT_LIST rootSelectExpression+)
	|	valueExpression ALIAS_NAME?
	;

dynamicInstantiationArg
	:	^(DYNAMIC_INSTANTIATION_ARG rootSelectExpression)
	;

orderByClause
	:	^(ORDER_BY sortSpecification+)
	;

sortSpecification
	:	^(SORT_SPEC valueExpression COLLATE? (ASC|DESC)?)
	;

searchCondition
	:	^( OR searchCondition searchCondition )
	|	^( AND searchCondition searchCondition )
	|	^( NOT searchCondition )
	|	predicate
	;

predicate
	:	^( EQUALS rowValueConstructor comparativePredicateValue )
	|	^( NOT_EQUAL rowValueConstructor comparativePredicateValue )
	|	^( LESS rowValueConstructor comparativePredicateValue )
	|	^( LESS_EQUAL rowValueConstructor comparativePredicateValue )
	|	^( GREATER rowValueConstructor comparativePredicateValue )
	|	^( GREATER_EQUAL rowValueConstructor comparativePredicateValue )
	|	^( IS_NULL rowValueConstructor )
	|	^( IS_NOT_NULL rowValueConstructor )
	|	^( LIKE valueExpression valueExpression escapeSpecification? )
	|	^( NOT_LIKE valueExpression valueExpression escapeSpecification? )
	|	^( BETWEEN rowValueConstructor betweenList )
	|	^( NOT_BETWEEN rowValueConstructor betweenList )
	|	^( IN rowValueConstructor inPredicateValue )
	|	^( NOT_IN rowValueConstructor inPredicateValue )
	|	^( EXISTS (rowValueConstructor|ALIAS_NAME))
	|	rowValueConstructor
	;

betweenList
	:	^( BETWEEN_LIST rowValueConstructor rowValueConstructor )
	;	

comparativePredicateValue
	:	rowValueConstructor
	;

rowValueConstructor
	:	valueExpression
	;

escapeSpecification
	:	^(ESCAPE characterValueExpression)
	;

inPredicateValue
	:	^(IN_LIST valueExpression+)
	;

numericValueExpression
	:	valueExpression
	;

characterValueExpression
	:	valueExpression
	;

datetimeValueExpression
	:	valueExpression
	;

valueExpression
	:	^( DOUBLE_PIPE characterValueExpression+ )
	|	^( UNARY_MINUS numericValueExpression )
	|	^( UNARY_PLUS numericValueExpression )
	|	^( PLUS valueExpression valueExpression )
	|	^( MINUS valueExpression valueExpression )
	|	^( ASTERISK numericValueExpression numericValueExpression )
	|	^( SOLIDUS numericValueExpression numericValueExpression )
	|	^( VECTOR_EXPR valueExpression+)
    |	^( SOME (valueExpression|ALIAS_NAME) )
    |	^( ALL (valueExpression|ALIAS_NAME) )
    |	^( ANY (valueExpression|ALIAS_NAME) )
	|	valueExpressionPrimary
	;

valueExpressionPrimary
	:	ALIAS_REF
	|	caseExpression
	|	function
	|	collectionFunction
	|	collectionExpression
	|	constant
	|	parameter
	|	propertyReference
	|	^(GENERIC_ELEMENT identPrimary)
	|	queryStatement
	;

caseExpression
	:	caseAbbreviation
	|	caseSpecification
	;


caseAbbreviation
	:	^(NULLIF valueExpression valueExpression)
	|	^(COALESCE valueExpression valueExpression*)
	;

caseSpecification
	:	simpleCase
	|	searchedCase
	;

simpleCase
	:	^(SIMPLE_CASE valueExpression simpleCaseWhenClause+ elseClause?)
	;

simpleCaseWhenClause
	:	^(WHEN valueExpression valueExpression)
	;

elseClause
	:	^(ELSE valueExpression)
	;

searchedCase
	:	^(SEARCHED_CASE searchedWhenClause+ elseClause?)
	;

searchedWhenClause
	:	^(WHEN searchCondition valueExpression)
	;

function
	:	( standardFunction | setFunction )
	;

standardFunction
	:	castFunction
	|	concatFunction
	|	substringFunction
	|	trimFunction
	|	upperFunction
	|	lowerFunction
	|	lengthFunction
	|	locateFunction
	|	absFunction
	|	sqrtFunction
	|	modFunction
	|	sizeFunction
	|	indexFunction
	|	currentDateFunction
	|	currentTimeFunction
	|	currentTimestampFunction
	|	extractFunction
	|	positionFunction
	|	charLengthFunction
	|	octetLengthFunction
	|	bitLengthFunction
	;

castFunction
	:	^(CAST valueExpression IDENTIFIER)
	;

concatFunction
	:	^(CONCAT valueExpression+)
	;

substringFunction
	:	^(SUBSTRING characterValueExpression numericValueExpression numericValueExpression?)
	;

trimFunction
	:	^(TRIM trimOperands)
	;

trimOperands
	:	^((LEADING|TRAILING|BOTH) characterValueExpression characterValueExpression)
	;

upperFunction
	:	^(UPPER characterValueExpression)
	;

lowerFunction
	:	^(LOWER characterValueExpression)
	;

lengthFunction
	:	^(LENGTH characterValueExpression)
	;

locateFunction
	:	^(LOCATE characterValueExpression characterValueExpression numericValueExpression?)
	;

absFunction
	:	^(ABS numericValueExpression)
	;

sqrtFunction
	:	^(SQRT numericValueExpression)
	;

modFunction
	:	^(MOD numericValueExpression numericValueExpression)
	;

sizeFunction
	:	^(SIZE propertyReference)
	;

indexFunction
	:	^(INDEX ALIAS_REF)
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
	:	^(EXTRACT extractField datetimeValueExpression)
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
	:	^(POSITION characterValueExpression characterValueExpression)
	;

charLengthFunction
	:	^(CHARACTER_LENGTH characterValueExpression)
	;

octetLengthFunction
	:	^(OCTET_LENGTH characterValueExpression)	
	;

bitLengthFunction
	:	^(BIT_LENGTH characterValueExpression)
	;

setFunction
	:	^(SUM numericValueExpression)
	|	^(AVG numericValueExpression)
	|	^(MAX numericValueExpression)
	|	^(MIN numericValueExpression)
	|	^(COUNT (ASTERISK | (DISTINCT|ALL)? countFunctionArguments))
	;

countFunctionArguments
	:	collectionExpression
	|	propertyReference
	|	numeric_literal
	;

collectionFunction
	:	^((MAXELEMENT|MAXINDEX|MINELEMENT|MININDEX) collectionPropertyReference)
	;

collectionPropertyReference
	:	propertyReference
	;

collectionExpression
	:	^(ELEMENTS propertyReference)
	|	^(INDICES propertyReference)
	;

parameter
	:	NAMED_PARAM
	|	JPA_PARAM
	|	PARAM	
	;

constant
	:	literal
	|	NULL
	|	TRUE
	|	FALSE
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

propertyReference
	:	^(PROPERTY_REFERENCE path)
	;

identPrimary
	: 	IDENTIFIER
	|	^(DOT identPrimary identPrimary )
	|	^(LEFT_SQUARE identPrimary valueExpression* )
	|	^(LEFT_PAREN identPrimary valueExpression* )
	;

dotIdentifierPath
	:	IDENTIFIER
	|	^(DOT dotIdentifierPath dotIdentifierPath) 
	;

path
	:	IDENTIFIER
	|	^( DOT path path )
	|	^(LEFT_SQUARE path valueExpression* )
	;
