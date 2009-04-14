header {
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
package org.hibernate.sql.ast.phase.hql.resolve;

import antlr.collections.AST;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.sql.ast.DetailedSemanticException;
}

/**
 * An Antlr tree parser for "resolving" an HQL AST.  Essentially here we are concerned with creating a generic
 * SQL AST.
 *
 * @author Joshua Davis
 * @author Steve Ebersole
 */
class GeneratedHqlResolver extends TreeParser;

options {
	importVocab = HqlNormalize;
	exportVocab = HqlResolve;
	buildAST = true;
}

tokens {
    COLUMN;
    NAME;
}


{
    private static Logger log = LoggerFactory.getLogger( GeneratedHqlResolver.class );

    protected AST resolveEntityPersister(AST entityName, AST alias, AST filter) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected AST resolveCollectionPersister(AST collectionRole, AST alias) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected void applyPropertyJoin(AST lhs, AST rhs, AST propertyName, AST joinType, AST with) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected AST generateIndexValueCondition(AST lhs, AST rhs, AST propertyName, AST selector ) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected void appendSearchCondition(AST condition, AST container) {
        if ( container.getFirstChild() == null ) {
            container.setFirstChild( condition );
        }
        else {
            AST and = #( [AND,"and"], container.getFirstChild() );
            and.addChild( condition );
            container.setFirstChild( and );
        }
    }

    protected void applyVersionedUpdate(AST updateStatement) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }
    
    protected void postProcessQuery(AST whereClause) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected void startSelectClause() {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected void finishSelectClause() {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected void startFunction() {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected void finishFunction() {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected AST resolvePropertyReference(AST persisterAlias, AST propertyName) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected AST resolveAliasReference(AST aliasReference) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }
}


// Statement rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * The main grammar rule
 */
statement :
    updateStatement
    | deleteStatement
    | insertStatement
    | selectStatement
;


// <tt>UPDATE</tt> statement ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Recognize an <tt>UPDATE</tt> statement
 */
updateStatement :
    #( u:UPDATE (v:VERSIONED)? en:ENTITY_NAME a:ALIAS setClause (whereClause)? ) {
        if ( #v != null ) {
            applyVersionedUpdate( #updateStatement );
        }
    }
;

setClause :
    #( SET (assignment)+ )
;

assignment :
    #( ASSIGNMENT_OP assignmentField newValue )
;

assignmentField :
    propertyReference
;

newValue :
    valueExpression
;



// <tt>DELETE</tt> statement ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Recognize an HQL <tt>DELETE</tt> statement
 */
deleteStatement :
    #( d:DELETE en:ENTITY_NAME a:ALIAS (whereClause)? )
;


// <tt>INSERT</tt> statement ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Recognize an HQL <tt>INSERT</tt> statement
 */
insertStatement :
    #( i:INSERT ic:intoClause qe:queryExpression )
;

intoClause :
    #( INTO ENTITY_NAME insertabilitySpecification )
;

insertabilitySpecification :
    #( INSERTABILITY_SPEC (insertablePropertySpecification)+ )
;

/**
 * The property being inserted into.
 */
insertablePropertySpecification :
    IDENT
;


// <tt>SELECT</tt> statement rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Recognize an HQL <tt>SELECT</tt> statement.
 * <p/>
 * This corresponds most closely to the <cursor specification> rule in ISO/ANSI SQL...
 */
selectStatement :
    #( QUERY queryExpression (orderByClause)? )
;

orderByClause :
    #( ORDER_BY (sortSpecification)+ )
;

sortSpecification :
    #( SORT_SPEC sortKey (collationSpecification)? (orderingSpecification)? )
;

sortKey :
    // todo : do we want to explicitly limit these?
    valueExpression
;

collationSpecification :
    COLLATE
;

orderingSpecification :
    ORDER_SPEC
;

queryExpression :
    querySpec ( ( UNION | INTERSECT | EXCEPT ) (ALL)? querySpec )*
;

subquery :
    queryExpression
;

querySpec! :
    #( qs:QUERY_SPEC #(SELECT_FROM f:fromClause s:selectClause) (w:whereClause)? ( g:groupByClause ( h:havingClause )? )? ) {
        #querySpec = #( #qs, #s, #f, #w, #g, #h );
        postProcessQuery( #querySpec );
    }
;


selectFrom :
    #( SELECT_FROM fromClause selectClause )
;


// table/persister related rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

fromClause :
    #( FROM (entityPersisterReference)+ )
;

persisterReference :
    entityPersisterReference
    | collectionPersisterReference
    | indexedCollectionElementPersisterReference
;

entityPersisterReference! {
    AST lhs = null;
} :
    #( epr:ENTITY_PERSISTER_REF en:ENTITY_NAME a:ALIAS (f:FILTER)? { lhs = resolveEntityPersister( #en, #a, #f ); } joins[lhs] ) {
        #entityPersisterReference = lhs;
    }
;

collectionPersisterReference! {
    AST lhs = null;
} :
    #( COLLECTION_PERSISTER_REF cr:COLLECTION_ROLE a:ALIAS { lhs = resolveCollectionPersister( #cr, #a ); } joins[lhs] ) {
        #collectionPersisterReference = lhs;
    }
;

indexedCollectionElementPersisterReference! {
    AST lhs = null;
} :
    #( INDEXED_COLLECTION_ACCESS_PERSISTER_REF cr:COLLECTION_ROLE a:ALIAS { lhs = resolveCollectionPersister( #cr, #a ); } indexSelector[lhs] joins[lhs] )
;

indexSelector[AST lhs] :
    #( INDEX_VALUE_CORRELATION iv:selectedIndexValue )
;

selectedIndexValue! :
    valueExpression
;

joins[AST lhs] :
    ( persisterJoin[lhs] | propertyJoin[lhs] )*
;

persisterJoin![AST lhs] :
    // NOTE : persister cross joins were mutated into root table references during normalize.
    #( PERSISTER_JOIN jt:correlatedJoinType rhs:entityPersisterReference on:onFragment[lhs,rhs] ) {
        lhs.addChild( #( [JOIN,"join"], #jt, #rhs, #on ) );
    }
;

correlatedJoinType :
    INNER
    | LEFT
    | RIGHT
;

onFragment[AST rhs, AST lhs] :
    #( ON searchCondition )
;

propertyJoin![AST lhs] :
    #( PROPERTY_JOIN jt:propertyJoinType pn:ASSOCIATION_NAME rhs:persisterReference (w:withFragment[lhs,#rhs])? ) {
        applyPropertyJoin( lhs, #rhs, #pn, #jt, #w );
    }
;

propertyJoinType:
    INNER
    | LEFT
;


withFragment[AST lhs, AST rhs] :
    #( w:WITH searchCondition )
;


// select clause related rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// todo : we need to tighten down exactly what is allowed in the select clause here...
// todo : additionally certain nodes need to be interpretted differently when in select-clause versus other clauses:
//      think of an ALIAS_REF; in select-clause this should be expanded to the full columns list; elsewhere to just the pk/fk

// actually may need something more like a strategy based on context...

selectClause :
    #( SELECT {startSelectClause();} (d:DISTINCT)? rootSelectExpression {finishSelectClause();} )
;

rootSelectExpression :
    #( SELECT_LIST explicitSelectList )
    | #( SELECT_ITEM rootDynamicInstantiation )
;

explicitSelectList :
    ( explicitSelectItem )+
;

explicitSelectItem :
    #( SELECT_ITEM selectExpression )
;

selectExpression :
    valueExpression ( ALIAS )?
;

rootDynamicInstantiation :
    #( DYNAMIC_INSTANTIATION dynamicInstantiationArguments )
;

nestedDynamicInstantiation :
    #( DYNAMIC_INSTANTIATION dynamicInstantiationArguments (ALIAS)? )
;

dynamicInstantiationArguments :
    ( dynamicInstantiationArgument )+
;

dynamicInstantiationArgument :
    #( DYNAMIC_INSTANTIATION_ARG ( nestedDynamicInstantiation | selectExpression ) )
;



// where clause rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

whereClause :
    #( WHERE searchCondition )
;



// group by clause rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

groupByClause :
    #( GROUP_BY groupingSpecification )
;

groupingSpecification :
    ( groupingValue )+
;

groupingValue :
    valueExpression ( collationSpecification )?
;



// having clause related rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

havingClause :
    #( HAVING searchCondition )
;



// value/expression recognition rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

numericValueExpression :
	valueExpression
;

stringValueExpression :
	characterValueExpression
;

characterValueExpression :
    valueExpression
;

intervalValueExpression :
	valueExpression
;

datetimeValueExpression :
	valueExpression
;

valueExpression :
    #( CONCATENATION_OP ARGLIST ( characterValueExpression )+ )
    | #( UNARY_MINUS numericValueExpression )
    | #( UNARY_PLUS numericValueExpression )
    | #( PLUS_SIGN valueExpression valueExpression )
    | #( MINUS_SIGN valueExpression valueExpression )
    | #( ASTERISK numericValueExpression numericValueExpression )
    | #( SOLIDUS numericValueExpression numericValueExpression )
    | valueExpressionPrimary
;

valueExpressionPrimary :
    persisterAliasReference
    | caseExpression
	| function
	| collectionFunction
	| collectionExpression
	| literal
	| parameter
    | propertyReference
;

propertyReference! :
    #( PROPERTY_REF a:ALIAS_REF pn:IDENT ) {
        #propertyReference = resolvePropertyReference( #a, #pn );
    }
;

nonCollectionPropertyReference :
    pr:propertyReference
;

collectionPropertyReference :
    pr:propertyReference
;

function {
    startFunction();
} :
    ( standardFunction | setFunction ) {
        finishFunction();
    }
;

persisterAliasReference! :
    a:ALIAS_REF {
        #persisterAliasReference = resolveAliasReference( #a );
    }
;

caseExpression :
    caseAbbreviation
    | caseSpecification
;

caseAbbreviation :
    #( NULLIF valueExpression valueExpression )
    | #( COALESCE ( valueExpression )+ )
;

caseSpecification :
    simpleCase
     | searchedCase
;

simpleCase :
    #( SIMPLE_CASE valueExpression (simpleCaseWhenClause)+ (elseClause)? END )
;

simpleCaseWhenClause :
    #( WHEN valueExpression THEN result )
;

result :
    valueExpression
;

elseClause :
    #( ELSE result )
;

searchedCase :
    #( SEARCHED_CASE (searchedWhenClause)+ (elseClause)? END )
;

searchedWhenClause :
    #( WHEN searchCondition THEN valueExpression )
;

standardFunction :
    castFunction
    | concatFunction
    | substringFunction
    | trimFunction
    | upperFunction
    | lowerFunction
    | lengthFunction
    | locateFunction
    | absFunction
    | sqrtFunction
    | modFunction
    | sizeFunction
    | indexFunction
    | currentDateFunction
    | currentTimeFunction
    | currentTimestampFunction
    | extractFunction
    | positionFunction
    | charLengthFunction
    | octetLengthFunction
    | bitLengthFunction
;

castFunction :
    #( CAST valueExpression dataType )
;

dataType :
    // todo : temp...
    IDENT
;

concatFunction :
    #( CONCAT (valueExpression)* )
;

substringFunction :
    #( SUBSTRING characterValueExpression numericValueExpression (numericValueExpression)? )
;

trimFunction :
    #( TRIM  ( LEADING | TRAILING | BOTH ) CHAR_STRING characterValueExpression )
;

upperFunction :
    #( UPPER characterValueExpression )
;

lowerFunction :
    #( LOWER characterValueExpression )
;

lengthFunction :
    #( LENGTH characterValueExpression )
;

locateFunction :
    #( LOCATE characterValueExpression characterValueExpression (numericValueExpression)? )
;

absFunction :
    #( ABS numericValueExpression )
;

sqrtFunction :
    #( SQRT numericValueExpression )
;

modFunction :
    #( MOD numericValueExpression numericValueExpression )
;

sizeFunction :
    #( SIZE collectionPropertyReference )
;

indexFunction :
    #( INDEX ALIAS_REF )
;

currentDateFunction :
    CURRENT_DATE
;

currentTimeFunction :
    CURRENT_TIME
;

currentTimestampFunction :
    CURRENT_TIMESTAMP
;

extractFunction :
    #( EXTRACT extractField FROM extractSource )
;

extractField :
    datetimeField
	| timeZoneField
;

datetimeField :
    nonSecondDatetimeField
	| SECOND
;

timeZoneField :
    TIMEZONE_HOUR
    | TIMEZONE_MINUTE
;

extractSource :
    datetimeValueExpression
;

positionFunction :
    #( POSITION characterValueExpression characterValueExpression )
;

charLengthFunction :
    #( CHAR_LENGTH characterValueExpression )
    | #( CHARACTER_LENGTH characterValueExpression )
;

octetLengthFunction :
    #( OCTET_LENGTH characterValueExpression )
;

bitLengthFunction :
    #( BIT_LENGTH characterValueExpression )
;

setFunction :
    #( SUM numericValueExpression )
    | #( AVG numericValueExpression )
    | #( MAX numericValueExpression )
    | #( MIN numericValueExpression )
    | #( COUNT ( ASTERISK | ( ( DISTINCT | ALL )? ( propertyReference | literal ) ) ) )
;

collectionFunction :
    #( MAXELEMENT collectionPropertyReference )
    | #( MAXINDEX collectionPropertyReference )
    | #( MINELEMENT collectionPropertyReference )
    | #( MININDEX collectionPropertyReference )
;

collectionExpression :
    #( ELEMENTS collectionPropertyReference )
    #( INDICES collectionPropertyReference )
;

parameter :
    PARAM
    | JPA_PARAM
    | NAMED_PARAM
;


// literals ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

protected
literal :
    javaConstant
    | numericLiteral
    | characterLiteral
    | dateLiteral
    | timeLiteral
    | timestampLiteral
    | intervalLiteral
;

protected
javaConstant :
    JAVA_CONSTANT
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
//    #( DATE CHAR_STRING )
    #( DATE characterValueExpression )
;

protected
timeLiteral :
//    #( TIME CHAR_STRING )
    #( TIME characterValueExpression )
;

protected
timestampLiteral :
//    #( TIMESTAMP CHAR_STRING )
    #( TIMESTAMP characterValueExpression )
;

protected
intervalLiteral :
    #( INTERVAL ( PLUS_SIGN | MINUS_SIGN )? CHAR_STRING intervalQualifier )
//    #( INTERVAL ( PLUS_SIGN | MINUS_SIGN )? characterValueExpression intervalQualifier )  -- characterValueExpression causes non-determinism with + and -
;

protected
intervalQualifier :
    #( YEAR (precision)? ( TO MONTH )? )
    | #( MONTH (precision)? )
    | #( DAY (precision)? ( TO ( HOUR | MINUTE | ( SECOND (scale)? ) ) )? )
    | #( HOUR (precision)? ( TO ( MINUTE | SECOND (scale)? ) )? )
    | #( MINUTE (precision)? ( TO SECOND (scale)? )? )
    | #( SECOND ( precision (scale)? )? )
;

precision :
    NUM_INT_LITERAL
;

scale :
    NUM_INT_LITERAL
;

//protected
//intervalStartField :
//    nonSecondDatetimeField ( LEFT_PAREN UNSIGNED_INTEGER RIGHT_PAREN )?
//;
//
//protected
//intervalEndField :
//    nonSecondDatetimeField
//	| SECOND ( LEFT_PAREN UNSIGNED_INTEGER RIGHT_PAREN )?
//;

protected
nonSecondDatetimeField :
    YEAR
    | MONTH
    | DAY
    | HOUR
    | MINUTE
;



// Search conditions ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Main logical/conditional rule defining boolean evaluated expressions
 * <p/>
 * This first level handles OR expressions
 */
searchCondition :
    #( OR searchCondition searchCondition )
    | #( AND searchCondition searchCondition )
    | #( NOT searchCondition )
    | predicate
;

predicate :
    #( EQUALS_OP rowValueConstructor comparativePredicateValue )
    | #( NOT_EQUALS_OP rowValueConstructor comparativePredicateValue )
    | #( LESS_THAN_OP rowValueConstructor comparativePredicateValue )
    | #( LESS_THAN_OR_EQUALS_OP rowValueConstructor comparativePredicateValue )
    | #( GREATER_THAN_OP rowValueConstructor comparativePredicateValue )
    | #( GREATER_THAN_OR_EQUALS_OP rowValueConstructor comparativePredicateValue )
    | #( IS_NULL rowValueConstructor )
    | #( IS_NOT_NULL rowValueConstructor )
    | #( LIKE valueExpression valueExpression (escapeSpecification)? )
    | #( NOT_LIKE valueExpression valueExpression (escapeSpecification)? )
	| #( BETWEEN rowValueConstructor rowValueConstructor rowValueConstructor )
	| #( NOT_BETWEEN rowValueConstructor rowValueConstructor rowValueConstructor )
	| #( IN rowValueConstructor inPredicateValue )
	| #( NOT_IN rowValueConstructor inPredicateValue )
	| #( EXISTS (
	        queryExpression
	        | ( ELEMENTS | INDICES ) collectionPropertyReference
	    )
	)
;

comparativePredicateValue :
    rowValueConstructor
    | ( ( SOME | ALL | ANY ) subquery )
;

escapeSpecification :
    #( ESCAPE characterValueExpression )
;

inPredicateValue :
    #(
        IN_LIST (
            queryExpression
            | ( valueExpression )+
        )
    )
;

rowValueConstructor :
    ( LEFT_PAREN ) => ( LEFT_PAREN rowValueConstructorList RIGHT_PAREN )
    | rowValueConstructorElement
;

rowValueConstructorElement :
    valueExpression
    | NULL
    | DEFAULT
;

rowValueConstructorList :
    rowValueConstructorElement ( COMMA! rowValueConstructorElement )*
;
