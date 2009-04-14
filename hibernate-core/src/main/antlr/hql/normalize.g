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
package org.hibernate.sql.ast.phase.hql.normalize;

import antlr.collections.AST;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.sql.ast.DetailedSemanticException;
}

/**
 * An Antlr tree parser for "normalizing" an HQL syntax AST.  This parser provides the vast majority of the semantic
 * analysis of the HQL AST.
 * <p/>
 * The notion of "normalizing" seeks to build an AST that is:<ul>
 * <li>dis-ambiguated</li>
 * <li>a unified representation for different ways to express the same "idea"</li>
 * </ul>
 * <p/>
 * The main thrust of this normalization is normalizing "property path structures" into joins and subqueries as
 * needed for implicit joins and index operations.
 *
 * @author Joshua Davis
 * @author Steve Ebersole
 */
class GeneratedHqlNormalizer extends TreeParser;

options {
	importVocab = HqlParse;
	exportVocab = HqlNormalize;
	buildAST = true;
}

tokens {
	PROPERTY_REF;
	INDEXED_COLLECTION_ACCESS_PERSISTER_REF;
	INDEXED_COLLECTION_ELEMENT_REF;
	ASSOCIATION_NAME;
	INDEX_VALUE_CORRELATION;
}


// -- Declarations --
{
    private static Logger log = LoggerFactory.getLogger( GeneratedHqlNormalizer.class );

    private AST currentFromClause;

	private int ordinalParamCount = 0;
	private int statementDepth = 0;

	protected final int getStatementDepth() {
	    return statementDepth;
    }

    protected final boolean isSubquery() {
        return statementDepth > 1;
    }


    // persister reference related actions ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    protected AST normalizeEntityName(AST node) throws SemanticException {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected final AST normalizeAlias(AST node) {
        if ( node != null ) {
            return node;
        }
        String syntheticAlias = buildUniqueImplicitAlias();
        return #([ALIAS,syntheticAlias]);
    }

    protected String buildUniqueImplicitAlias() {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected void registerPersisterReference(AST reference) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected boolean isPersisterReferenceAlias(AST alias) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected boolean isCollectionPersisterReferenceAlias(AST alias) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }


    // property reference related actions ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    protected final boolean isUnqualifiedPropertyReference(AST property) {
        return locateOwningPersisterAlias( property ) != null;
    }

    protected final AST normalizeUnqualifiedPropertyReference(AST property) {
        return #( [PROPERTY_REF], owningPersisterAliasReference( property ), property );
    }

    protected final AST owningPersisterAliasReference(AST property) {
        return #( [ALIAS_REF,locateOwningPersisterAlias(property)] );
    }

    protected String locateOwningPersisterAlias(AST property) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }


	// path normalization ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    protected AST normalizeQualifiedRoot(AST alias) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected AST normalizeUnqualifiedRoot(AST alias) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

	protected AST normalizeUnqualifiedPropertyReferenceSource(AST propertyName) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected AST normalizeIndexedRoot(AST alias) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected AST normalizePropertyPathIntermediary(AST source, AST propertyNameNode) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected AST normalizePropertyPathTerminus(AST source, AST propertyNameNode) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected AST normalizeIntermediateIndexOperation(AST collectionPath, AST selector) throws SemanticException {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected AST normalizeTerminalIndexOperation(AST collectionPath, AST selector) throws SemanticException {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected void applyWithFragment(AST withFragment) {
	}


	// Statement node BEGIN/END handling ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	protected void pushStatement(AST statementNode) {
        throw new UnsupportedOperationException( "must be overridden!" );
	}

	protected void popStatement() {
        throw new UnsupportedOperationException( "must be overridden!" );
	}


	// property-path context pushing/popping ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	protected void pushFromClausePropertyPathContext(AST joinType, AST fetch, AST alias, AST propertyFetch) {
        throw new UnsupportedOperationException( "must be overridden!" );
	}

	protected void popFromClausePropertyPathContext() {
        throw new UnsupportedOperationException( "must be overridden!" );
	}

    protected void pushSelectClausePropertyPathContext() {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected void popSelectClausePropertyPathContext() {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

	protected void pushOnFragmentPropertyPathContext(AST rhsPersisterReference) {
        throw new UnsupportedOperationException( "must be overridden!" );
	}

	protected AST popOnFragmentPropertyPathContext() {
        throw new UnsupportedOperationException( "must be overridden!" );
	}

	protected void pushWithFragmentPropertyPathContext(AST rhsPersisterReference) {
        throw new UnsupportedOperationException( "must be overridden!" );
	}

	protected void popWithFragmentPropertyPathContext() {
        throw new UnsupportedOperationException( "must be overridden!" );
	}


    // function processing ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    protected void startingFunction() {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected void endingFunction() {
        throw new UnsupportedOperationException( "must be overridden!" );
    }


    protected void registerSelectItem(AST selectItem) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    private void startQuerySpec(AST querySpecIn) {
        statementDepth++;
        applyCollectionFilter(querySpecIn);
    }

    private void endQuerySpec(AST querySpec) {
        postProcessQuery( querySpec );
        --statementDepth;
    }

    protected void applyCollectionFilter(AST querySpecIn) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected void postProcessQuery(AST query) {
        throw new UnsupportedOperationException( "must be overridden!" );
    }

    protected AST buildRootEntityPersisterReference(AST persisterReferenceNode, AST entityName, AST alias, AST filter, AST propertyFetch) {
        AST rtn = #( persisterReferenceNode, entityName, alias, filter );
		registerPersisterReference( rtn );
		if ( propertyFetch != null ) {
		    registerPropertyFetchNode( rtn );
        }
        return rtn;
    }

    protected void registerPropertyFetchNode(AST persisterReference) {
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
    #(
        UPDATE { pushStatement( #updateStatement ); }
	        (VERSIONED)?
	        ENTITY_NAME (ALIAS)?
	        setClause
	        (whereClause)? { popStatement(); }
    )
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
    #(
	    DELETE { pushStatement( #deleteStatement ); }
	        ENTITY_NAME (ALIAS)?
	        (whereClause)? { popStatement(); }
    )
;


// <tt>INSERT</tt> statement ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Recognize an HQL <tt>INSERT</tt> statement
 */
insertStatement :
    #(
        i:INSERT { pushStatement( #insertStatement ); }
            ic:intoClause
            qe:queryExpression { popStatement(); }
    )
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
    #( QUERY { pushStatement( #selectStatement ); } queryExpression (orderByClause)? ) { popStatement(); }
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

querySpec { startQuerySpec(#querySpec_in); } :
    #( QUERY_SPEC selectFrom ( whereClause )? ( groupByClause ( havingClause )? )? ) { endQuerySpec(#querySpec); }
;


selectFrom :
    #( SELECT_FROM fromClause (sc:selectClause)? )
;


// table/persister related rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

fromClause! :
    #( f:FROM {currentFromClause=#f;} (persisterSpace)+ {currentFromClause=null;} ) {
        #fromClause = #f;
    }
;

persisterSpace! :
    #( PERSISTER_SPACE pr:entityPersisterReference {currentFromClause.addChild( #pr );} ( explicitPersisterJoin | explicitPropertyJoin )* )
;

entityPersisterReference! :
    #( epr:ENTITY_PERSISTER_REF en:ENTITY_NAME a:ALIAS (f:FILTER)? (pf:PROP_FETCH)? ) {
        #entityPersisterReference = buildRootEntityPersisterReference(
                #epr,
                normalizeEntityName( #en ),
                normalizeAlias( #a ),
                #f,
                #pf
        );
	}
;

explicitPersisterJoin!:
    #(
        j:PERSISTER_JOIN (
            CROSS cjpr:entityPersisterReference {
                currentFromClause.addChild( #cjpr );
            }
            | jt:qualifiedJoinType rhs:entityPersisterReference {pushOnFragmentPropertyPathContext(#rhs);} on:onFragment {
                AST lhs = popOnFragmentPropertyPathContext();
                lhs.addChild( #( #j, #jt, #rhs, #on ) );
            }
        )
    )
;

explicitPropertyJoin! :
    #( j:PROPERTY_JOIN jt:qualifiedJoinType (f:FETCH)? (a:ALIAS)? (pf:PROP_FETCH)? {pushFromClausePropertyPathContext(#jt,#f,#a,#pf);} prop:propertyReference (with:withFragment[#prop])? {popFromClausePropertyPathContext();} )
;

qualifiedJoinType :
    ( (LEFT | RIGHT) (OUTER)? )
	| INNER
;

onFragment :
    #( o:ON sc:searchCondition )
;

withFragment[ AST rhsPropertyReference ] :
    #( w:WITH { pushWithFragmentPropertyPathContext( rhsPropertyReference ); }  sc:searchCondition ) {
		#withFragment = #( w, sc );
		applyWithFragment( #withFragment );
		popWithFragmentPropertyPathContext();
	}
;


// select clause related rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

selectClause :
    #( SELECT (d:DISTINCT)? { pushSelectClausePropertyPathContext(); } rootSelectExpression ) {
		popSelectClausePropertyPathContext();
	}
;

rootSelectExpression :
    #( SELECT_LIST explicitSelectList )
    | #( SELECT_ITEM ( rootDynamicInstantiation | jpaSelectObjectSyntax ) )
;

explicitSelectList :
    ( explicitSelectItem )+
;

explicitSelectItem :
    #( SELECT_ITEM selectExpression ) {
        registerSelectItem( #explicitSelectItem );
    }
;

selectExpression :
    valueExpression ( ALIAS )?
;

rootDynamicInstantiation :
    #( DYNAMIC_INSTANTIATION dynamicInstantiationArguments ) {
        registerSelectItem( #rootDynamicInstantiation );
    }
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

jpaSelectObjectSyntax! :
    #( SELECT_ITEM OBJECT a:ALIAS_REF ) {
        if ( isPersisterReferenceAlias( #a ) ) {
            #jpaSelectObjectSyntax = #a;
        }
        else {
            throw new DetailedSemanticException( "token [" + #a.getText() + "] in syntax [object (" + #a.getText() + ")] must refer to persister reference", #a );
        }
    }
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
    (persisterReferenceAliasCheck) => nonCollectionPersisterAliasReference
    | caseExpression
//	| quantifiedExpression
	| function
	| collectionFunction
	| collectionExpression
	| literal
	| parameter
    | propertyReference
;


function : {
        startingFunction();
    }
    ( standardFunction | setFunction ) {
        endingFunction();
    }
;

nonCollectionPersisterAliasReference :
    a:persisterAliasReference {
        if ( isCollectionPersisterReferenceAlias( #a ) ) {
            throw new DetailedSemanticException( "expecting non-collection alias reference [" + #a.getText() + "]", #a );
        }
    }
;

persisterAliasReference :
    ALIAS_REF
    | a:IDENT { isPersisterReferenceAlias( #a ) }? {
        #a.setType( ALIAS_REF );
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

//quantifiedExpression :
//    // todo : this should be a function of the predicates (logicalExpression) not of concatenation/additiveExpression
//    //      they can come only in very certain circumstances.
//	#( ( SOME^ | EXISTS^ | ALL^ | ANY^ ) ( IDENT | collectionExpression | (LEFT_PAREN! ( subQuery ) RIGHT_PAREN!) ) )
//;

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
    | #( COUNT {startingFunction();} ( ASTERISK | ( ( DISTINCT | ALL )? ( propertyReference | literal ) ) ) {endingFunction();} )
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
    p:PARAM { p.setText( Integer.toString( ordinalParamCount++ ) ); }  // todo : move this to parse?
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



// property-reference related rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

nonCollectionPropertyReference :
    pr:propertyReference {
        // todo : validate the property type
    }
;

collectionPropertyReference :
    pr:propertyReference {
        // todo : validate the property type
    }
;

/**
 * The top level property ref recognition rule.
 */
propertyReference :
    (unqualifiedPropertyReferenceCheck) => unqualifiedPropertyReference
    | pathedPropertyReference
    | terminalIndexOperation
;

/**
 * A rule utilizing a validating semantic predicate to contextually
 * ensure that we have unqualified property reference.  Generally
 * used from within syntactic predicates to disambiguate the match path
 *
 * see {@link #implicitJoinSource} for example
 */
unqualifiedPropertyReferenceCheck! :
    prop:IDENT { isUnqualifiedPropertyReference( prop ) }?
;

/**
 * AST construction rule for building AST relating to *known*
 * unqualified property references.  Do not call this rule unless
 * you know for certain (ala, have verified via the unqualifiedPropertyRefCheck
 * rule or similiar) that the next token is an IDENT representing an
 * unqualified property reference.
 */
unqualifiedPropertyReference! :
    prop:IDENT {
        #unqualifiedPropertyReference = normalizeUnqualifiedPropertyReference( prop );
    }
;

/**
 * Perhaps better named as 'complex property ref' or 'pathed property ref' i.e.
 * Anyway, the basic idea is (DOT <source> IDENT)
 */
pathedPropertyReference! :
    #( d:DOT source:pathedPropertyReferenceSource prop:IDENT) {
        #pathedPropertyReference = normalizePropertyPathTerminus( #source, #prop );
    }
;

/**
 * The output of the implicitJoinSource rule is a {@link org.hibernate.sql.ast.phase.resolve.patg.PropertyPathPart}
 * reference which is a tokenized and encoded representation of the current path expression
 * resolution state.
 */
pathedPropertyReferenceSource :
    (persisterReferenceAliasCheck) => a:IDENT { #pathedPropertyReferenceSource = normalizeQualifiedRoot( #a ); }
    | (unqualifiedPropertyReferenceCheck) => pr:IDENT { #pathedPropertyReferenceSource = normalizeUnqualifiedRoot( #pr ); }
    | intermediatePathedPropertyReference
    | intermediateIndexOperation
;

/**
 * A rule utilizing a validating semantic predicate to contextually
 * ensure that we have an alias for a persister reference previously
 * encountered and processed.  Generally used from within syntactic
 * predicates to disambiguate the path.
 */
persisterReferenceAliasCheck! :
    alias:IDENT { isPersisterReferenceAlias( alias ) }?
;

intermediatePathedPropertyReference! :
    #( d:DOT source:pathedPropertyReferenceSource prop:IDENT ) {
        #intermediatePathedPropertyReference = normalizePropertyPathIntermediary( #source, #prop );
    }
;

intermediateIndexOperation! :
    #( INDEX_OP collection:indexOperationSource selector:indexSelector ) {
        #intermediateIndexOperation = normalizeIntermediateIndexOperation( #collection, #selector );
    }
;

terminalIndexOperation! :
    #( INDEX_OP collection:indexOperationSource selector:indexSelector ) {
        #terminalIndexOperation = normalizeTerminalIndexOperation( #collection, #selector );
    }
;

indexOperationSource :
    #( DOT pathedPropertyReferenceSource IDENT )
    | (unqualifiedPropertyReferenceCheck) => pr:IDENT {
        #indexOperationSource = #( [DOT], normalizeUnqualifiedPropertyReferenceSource( #pr ), #pr );
    }
;

indexSelector
	: valueExpression
//	| literal
//	| parameter
//	| collectionFunctionCall : "index(alias)"
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

