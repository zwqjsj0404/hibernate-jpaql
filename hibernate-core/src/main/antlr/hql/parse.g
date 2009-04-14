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
package org.hibernate.sql.ast.phase.hql.parse;

import antlr.collections.AST;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.sql.ast.common.CommonHibernateParserSupport;
}

/**
 * An Antlr stream parser for building a syntax AST representing an input Hibernate Query Language (HQL) query.
 *
 * @author Lubos Vnuk
 * @author Joshua Davis
 * @author Steve Ebersole
 */
class GeneratedHqlParser extends CommonHibernateParserSupport;

options {
    importVocab = Sql92;
	exportVocab = HqlParse;
	buildAST = true;
	k = 3;
}

tokens {
    // Various synthetic tokens used to simplify or disambiguate the tree
	ALIAS;
	ALIAS_REF;
	ARG_LIST;
	ASSIGNMENT_OP;
	COLLECTION_PERSISTER_REF;
	COLLECTION_ROLE;
	DYNAMIC_INSTANTIATION;
	DYNAMIC_INSTANTIATION_ARG;
	ENTITY_NAME;
	ENTITY_PERSISTER_REF;
	FILTER;
	GENERIC_FUNCTION;
	INDEX_OP;
	INSERTABILITY_SPEC;
	IN_LIST;
	IS_NOT_NULL;
	IS_NULL;
	JAVA_CONSTANT;
	JPA_PARAM;
	NAMED_PARAM;
	NOT_BETWEEN;
	NOT_IN;
	NOT_LIKE;
    ORDER_SPEC;
	PERSISTER_SPACE;
	PROP_FETCH;
	QUERY;
	QUERY_SPEC;
    REGISTERED_FUNCTION;
    SEARCHED_CASE;
	SELECT_FROM;
    SELECT_ITEM;
    SELECT_LIST;
	SIMPLE_CASE;
    SORT_KEY;
    SORT_SPEC;
    SQL_TOKEN;
	UNARY_MINUS;
	UNARY_PLUS;

	EXPR_LIST;
	VECTOR_EXPR;

	PERSISTER_JOIN;
	PROPERTY_JOIN;
}

{
    private static Logger log = LoggerFactory.getLogger( GeneratedHqlParser.class );

    protected String extractText(AST node) {
        return node.getText();
    }

    protected String extractPath(AST node) {
        return extractText( node );
    }

    protected String normalizeDynamicInstantiationClassName(AST node) {
        return extractPath( node );
    }

	protected boolean isJavaConstant(String test) {
		return false;
	}

	protected AST processEqualityExpression(AST node) throws RecognitionException {
		return node;
	}

	protected AST processMemberOf(AST path, AST notNode) {
	    return path;
	}

	protected AST processIsEmpty(AST collection, AST notNode) {
	    return null;
    }

	/**
	 * This method is overriden in the sub class in order to provide the
	 * 'keyword as identifier' hack.
	 * @param token The token to retry as an identifier.
	 * @param ex The exception to throw if it cannot be retried as an identifier.
	 */
	protected AST handleIdentifierError(Token token,RecognitionException ex) throws RecognitionException, TokenStreamException {
		// Base implementation: Just re-throw the exception.
		throw ex;
	}

    /**
     * This method looks ahead and converts . <token> into . IDENT when
     * appropriate.
     */
    protected void handleDotIdent() throws TokenStreamException {
    }

	/**
	 * Returns the negated equivalent of the expression.
	 * @param node The expression to negate.
	 */
	protected AST negateNode(AST node) {
		// Just create a 'not' parent for the default behavior.
	    return #( [NOT,"not"], node );
	}

	protected void weakKeywords() throws TokenStreamException {
	}

    protected void potentialUpdatePersisterAlias() throws TokenStreamException {
    }

    protected void potentialDeletePersisterAlias() throws TokenStreamException {
    }

    protected void prepareForPersisterReferenceRoot() throws TokenStreamException {
    }

    protected void prepareForCrossJoinElements() throws TokenStreamException {
    }

    protected void prepareForQualifiedJoinElements() throws TokenStreamException {
    }

    protected void unequivocalKeywordAsIdentifier() throws TokenStreamException {
    }

    protected void transferTrackingInfo(AST source, AST target) {
        int type = target.getType();
        String text = target.getText();
        target.initialize( source );
        target.setType( type );
        target.setText( text );
    }
}

// Rules dealing with various IDENT structures ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

identifier :
    IDENT
	exception
	catch [RecognitionException ex] {
		identifier_AST = handleIdentifierError(LT(1),ex);
	}
;

path :
    identifier { handleDotIdent(); } ( DOT^ identifier )*
;

/**
 * Specialied IDENT ( DOT IDENT ) recognition rule for cases where we are fully expecting an entity name.
 * <p/>
 * Use the {@link #path} rule here because it is less complex to resolve (and so i assume faster).
 */
protected
entityName :
    p:path {
        String name = extractPath( #p );
        #entityName = #( [ENTITY_NAME,name] );
        transferTrackingInfo( #p, #entityName );
    }
;


// alias/correlation recognition rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Creation of an alias or correlation-name.
 */
alias :
    // if the AS is present, it signals that whatever follows is unequivocally an identifier
    ( AS! { unequivocalKeywordAsIdentifier(); } )? {weakKeywords();} i:identifier {
        #i.setType( ALIAS );
    }
;

/**
 * Reference to an alias or correlation-name
 */
aliasReference :
    {weakKeywords();} i:identifier {
        #i.setType( ALIAS_REF );
    }
;


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



// filter rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

filterStatement[String collectionRole] :
    filteredSelectFrom (whereClause)? ( groupByClause ( havingClause )? )? (orderByClause)? {
        AST querySpec = #( [QUERY_SPEC, "filter-query-spec"], [FILTER, collectionRole], #filterStatement );
	    #filterStatement = #( [QUERY,"query"], querySpec );
    }
;

filteredSelectFrom :
    ( s:selectClause )? ( f:fromClause )? {
		if ( #f != null ) {
            throw new SemanticException( "collection filters cannot specify explicit FROM clause" );
        }
		// Create an artificial token so the 'FROM' can be placed
		// before the SELECT in the tree to make tree processing
		// simpler.
		#filteredSelectFrom = #( [SELECT_FROM,"select-from"], #f, #s );
	}
;


// <tt>UPDATE</tt> statement ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Recognize an <tt>UPDATE</tt> statement
 */
updateStatement :
    UPDATE^ (VERSIONED)?
	    (FROM!)? {unequivocalKeywordAsIdentifier();} entityName {potentialUpdatePersisterAlias();} (alias)?
	    setClause
	    (whereClause)?
;

setClause
	: (SET^ assignment (COMMA! assignment)*)
	;

assignment :
    assignmentField eo:EQUALS_OP^ newValue {
	    #eo.setType( ASSIGNMENT_OP );
    }
;

assignmentField :
    path
;

newValue :
    concatenation
;


// <tt>DELETE</tt> statement ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Recognize an HQL <tt>DELETE</tt> statement
 */
deleteStatement :
    DELETE^
        ( FROM! )? {unequivocalKeywordAsIdentifier();} entityName {potentialDeletePersisterAlias();} (alias)?
        ( whereClause )?
;


// <tt>INSERT</tt> statement ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Recognize an HQL <tt>INSERT</tt> statement
 */
insertStatement :
	INSERT^ intoClause selectStatement
;

intoClause :
    INTO^ {unequivocalKeywordAsIdentifier();} entityName insertabilitySpecification
;

insertabilitySpecification :
    LEFT_PAREN! insertablePropertySpecification ( COMMA! insertablePropertySpecification )* RIGHT_PAREN! {
		#insertabilitySpecification = #( [INSERTABILITY_SPEC, "insertability-spec"], #insertabilitySpecification );
	}
;

/**
 * The property being inserted into.
 */
insertablePropertySpecification :
    // todo : ok for this to just be IDENT?
    //      do we want to allow users to target specific component sub-properties?
    identifier
;


// <tt>SELECT</tt> statement rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Recognize an HQL <tt>SELECT</tt> statement.
 * <p/>
 * This corresponds most closely to the <cursor specification> rule in ISO/ANSI SQL...
 */
selectStatement :
	queryExpression ( orderByClause )? {
	    #selectStatement = #( [QUERY,"query"], #selectStatement );
    }
;

orderByClause :
    ORDER_BY^ sortSpecification ( COMMA! sortSpecification )*
;

/**
 * Reconition rule for what ANSI SQL terms the <tt>sort specification</tt>, which is essentially each thing upon which
 * the results should be sorted.
 */
sortSpecification :
    sortKey (collationSpecification)? (orderingSpecification)? {
        #sortSpecification = #( [SORT_SPEC, "sort-specification"], #sortSpecification );
    }
;

/**
 * Reconition rule for what ANSI SQL terms the <tt>sort key</tt> which is the expression (column, function, etc) upon
 * which to base the sorting.
 */
sortKey :
    expression
;

/**
 * Reconition rule for what ANSI SQL terms the <tt>collation specification</tt> used to allow specifying that sorting for
 * the given {@link #sortSpecification} be treated within a specific character-set.
 */
collationSpecification!  :
    c:COLLATE cn:collationName {
        #collationSpecification = #( [COLLATE, extractText( #cn )] );
    }
;

/**
 * The collation name wrt {@link #collationSpecification}.  Namely, the character-set.
 */
collationName :
    identifier
;

/**
 * Reconition rule for what ANSI SQL terms the <tt>ordering specification</tt>; <tt>ASCENDING</tt> or
 * <tt>DESCENDING</tt>.
 */
orderingSpecification :
    // todo : what about "ascending" and "descending" literals?
    ( ASCENDING | DESCENDING) {
	    #orderingSpecification = #( [ORDER_SPEC, extractText( #orderingSpecification )] );
    }
;

queryExpression :
  querySpec ( ( UNION | INTERSECT | EXCEPT ) (ALL)? querySpec )*
;

querySpec :
    selectFrom ( whereClause )? ( groupByClause ( havingClause )? )? {
        #querySpec = #( [QUERY_SPEC], #querySpec );
    }
;

selectFrom! :
    ( s:selectClause )? f:fromClause {
		// Create an artificial token so the 'FROM' can be placed
		// before the SELECT in the tree to make tree processing
		// simpler.
		#selectFrom = #( [SELECT_FROM,"select-from"], f, s );
	}
;

subQuery :
    queryExpression {
        #subQuery = #( [QUERY,"query"], #subQuery );
    }
;


// table/persister related rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

fromClause :
    FROM^ {unequivocalKeywordAsIdentifier();} persisterSpaces
;

persisterSpaces :
    // todo : better name?
    persisterSpace ( COMMA! persisterSpace )*
;

persisterSpace :
    {prepareForPersisterReferenceRoot();} persisterSpaceRoot ( qualifiedJoin | crossJoin )* {
        #persisterSpace = #( [PERSISTER_SPACE, "persister-space"], #persisterSpace );
    }
;

/**
 * Recognition rule for root persister references.  Forms include:<ol>
 * <li>[entityName [AS alias]? [FETCH ALL PROPERTIES]?] - which is the standard entity persister reference</li>
 * <li>[IN (collection-reference) [AS alias]?] - which is the JPA variant of a collection join</li>
 * <li>[alias IN CLASS entityName] - which is the legacy HQL syntax</li>
 * <li>[alias IN [ELEMENTS|INDICES]? (collection-reference)] - legacy HQL syntax for a collection join</li>
 *</ol>
 * <p/>
 * NOTE that only options #1 and #3 are valid as the initial root space for a top-level query since
 * all the other forms require an already existing persister reference in order to reference a collection.
 */
persisterSpaceRoot :
    hibernateLegacySyntax
    | jpaCollectionReference
    | mainEntityPersisterReference
;

hibernateLegacySyntax :
    a:identifier! IN! (
        // allow these legacy Hibernate syntaxes, but normalize them and emit a warning...
        CLASS! {unequivocalKeywordAsIdentifier();} en:entityName! {
            log.warn( "encountered deprecated, legacy from-clause syntax : [<alias> in class <entity-name>]" );
            #a.setType( ALIAS );
            #hibernateLegacySyntax = #( [ENTITY_PERSISTER_REF], #en, #a );
        }
        | ( ELEMENTS! | INDICES! )? LEFT_PAREN! {unequivocalKeywordAsIdentifier();} p:path! RIGHT_PAREN! {
            log.warn( "encountered deprecated, legacy from-clause syntax : [<alias> in <elements|indices> (<collection-ref>)]" );
            // todo : properly treat ELEMENTS | INDICES...
            #a.setType( ALIAS );
            #hibernateLegacySyntax = #( [PROPERTY_JOIN,"property-join"], [INNER, "inner"], #a, #p );
        }
    )
;

mainEntityPersisterReference! :
    en:entityName (a:alias)? (pf:propertyFetch)? {
        #mainEntityPersisterReference = #( [ENTITY_PERSISTER_REF], #en, #a, #pf );
    }
;

jpaCollectionReference :
    IN! LEFT_PAREN! {unequivocalKeywordAsIdentifier();} p:path RIGHT_PAREN! ( a:alias )? {
        #jpaCollectionReference = #( [PROPERTY_JOIN,"property-join"], [INNER, "inner"], #a, #p );
    }
;

crossJoin! :
    c:CROSS j:JOIN {prepareForCrossJoinElements();} rhs:mainEntityPersisterReference {
        #crossJoin = #( [PERSISTER_JOIN,"persister-join"], #c, rhs );
        transferTrackingInfo( #j, #crossJoin );
    }
;

qualifiedJoin! :
    (jt:nonCrossJoinType)? j:JOIN {if (#jt==null) #jt=#([INNER]);} (f:FETCH)? {prepareForQualifiedJoinElements();} p:path (a:alias)? (
        // use the ON keyword to disambiguate
        js:joinSpecification {
            if ( #f != null ) {
                throw new org.hibernate.QueryException( "Cannot use fetch keyword in conjunction with an ad hoc join" );
            }
            String entityName = extractPath( #p );
            AST persisterReference = #( [ENTITY_PERSISTER_REF], [ENTITY_NAME,entityName], #a );
            #qualifiedJoin = #( [PERSISTER_JOIN,"persister-join"], #jt, persisterReference, #js );
        }
        | (pf:propertyFetch)? (w:withClause)? {
            #qualifiedJoin = #( [PROPERTY_JOIN,"property-join"], #jt, #f, #a, #pf, #p, #w );
        }
    )
;

nonCrossJoinType :
    INNER
    | outerJoinType ( OUTER )?
;

outerJoinType :
    LEFT
    | RIGHT
    | FULL // ugh!
;

joinSpecification :
    ON^ logicalExpression
;

withClause :
    WITH^ logicalExpression
;

propertyFetch! :
    FETCH! ALL! PROPERTIES! {
		#propertyFetch = #( [PROP_FETCH, "property-fetch"] );
	}
;


// select clause related rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


selectClause :
    SELECT^ (DISTINCT)? { weakKeywords(); } rootSelectExpression
;

rootSelectExpression :
    explicitSelectList
    | rootDynamicInstantiation
    | jpaSelectObjectSyntax
;

explicitSelectList :
    explicitSelectItem ( COMMA! explicitSelectItem )* {
        #explicitSelectList = #( [SELECT_LIST], #explicitSelectList );
    }
;

explicitSelectItem :
    selectExpression {
        #explicitSelectItem = #( [SELECT_ITEM], #explicitSelectItem );
    }
;

selectExpression :
    expression ( AS! i:identifier {#i.setType(ALIAS);} )?
;

rootDynamicInstantiation! :
	n:NEW! p:path op:LEFT_PAREN! args:dynamicInstantiationArgs RIGHT_PAREN! {
        #rootDynamicInstantiation = #( [ DYNAMIC_INSTANTIATION, normalizeDynamicInstantiationClassName( #p ) ], #args );
        transferTrackingInfo( #n, #rootDynamicInstantiation );
        #rootDynamicInstantiation = #( [SELECT_ITEM], #rootDynamicInstantiation );
        transferTrackingInfo( #n, #rootDynamicInstantiation );
    }
;

nestedDynamicInstantiation :
    n:NEW! p:path op:LEFT_PAREN! args:dynamicInstantiationArgs RIGHT_PAREN! ( AS! a:identifier {#a.setType(ALIAS);} )? {
        #nestedDynamicInstantiation = #( [ DYNAMIC_INSTANTIATION, normalizeDynamicInstantiationClassName( #p ) ], #args, #a );
        transferTrackingInfo( #n, #nestedDynamicInstantiation );
    }
;

dynamicInstantiationArgs :
    dynamicInstantiationArg ( COMMA! dynamicInstantiationArg )*
;

dynamicInstantiationArg :
    ( selectExpression | nestedDynamicInstantiation ) {
        #dynamicInstantiationArg = #( [DYNAMIC_INSTANTIATION_ARG], #dynamicInstantiationArg );
    }
;

jpaSelectObjectSyntax :
    OBJECT^ LEFT_PAREN! i:identifier RIGHT_PAREN! {
        #i.setType(ALIAS_REF);
        #jpaSelectObjectSyntax = #( [SELECT_ITEM], #jpaSelectObjectSyntax );
    }
;



// where clause rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

whereClause :
    WHERE^ logicalExpression
;



// group by clause rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

groupByClause :
    GROUP_BY^ groupingSpecification
;

groupingSpecification :
    groupingValue ( COMMA groupingValue )*
;

groupingValue :
    path ( collationSpecification )?
;



// having clause related rules ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

havingClause :
    HAVING^ logicalExpression
;

// expressions
// Note that most of these expressions follow the pattern
//   thisLevelExpression :
//       nextHigherPrecedenceExpression
//           (OPERATOR nextHigherPrecedenceExpression)*
// which is a standard recursive definition for a parsing an expression.
//
// Operator precedence in HQL
// lowest  --> ( 7)  OR
//             ( 6)  AND, NOT
//             ( 5)  equality: ==, <>, !=, is
//             ( 4)  relational: <, <=, >, >=,
//                   LIKE, NOT LIKE, BETWEEN, NOT BETWEEN, IN, NOT IN
//             ( 3)  addition and subtraction: +(binary) -(binary)
//             ( 2)  multiplication: * / %, concatenate: ||
// highest --> ( 1)  +(unary) -(unary)
//                   []   () (method call)  . (dot -- identifier qualification)
//                   aggregate function
//                   ()  (explicit parenthesis)
//
// Note that the above precedence levels map to the rules below...
// Once you have a precedence chart, writing the appropriate rules as below
// is usually very straightfoward

logicalExpression
	: expression
	;

// Main expression rule
expression
	: logicalOrExpression
	;

// level 7 - OR
logicalOrExpression
	: logicalAndExpression ( OR^ logicalAndExpression )*
	;

// level 6 - AND, NOT
logicalAndExpression
	: negatedExpression ( AND^ negatedExpression )*
	;

// NOT nodes aren't generated.  Instead, the operator in the sub-tree will be
// negated, if possible.   Expressions without a NOT parent are passed through.
negatedExpression!
{ weakKeywords(); } // Weak keywords can appear in an expression, so look ahead.
	: NOT^ x:negatedExpression { #negatedExpression = negateNode(#x); }
	| y:equalityExpression { #negatedExpression = #y; }
	;

//## OP: EQUALS_OP | LESS_THAN_OP | GREATER_THAN_OP | LESS_THAN_OR_EQUALS_OP | GREATER_THAN_OR_EQUALS_OP | NOT_EQUALS_OP | LIKE;

// level 5 - EQUALS_OP, NOT_EQUALS_OP
equalityExpression : 
    x:relationalExpression (
	    IS! (not:NOT!)? ( nullness:NULL! | emptiness:EMPTY! ) {
	        if ( #nullness != null ) {
	            log.debug( "handling <is [not] null> : " + #x.toStringTree() );
	            #equalityExpression = #( [IS_NULL, "is null"], #x );
	            if ( #not != null ) {
	                negateNode( #equalityExpression );
                }
            }
	        else {
	            #equalityExpression = processIsEmpty( #x, #not );
            }
        }
        | ( EQUALS_OP^ | NOT_EQUALS_OP^ ) relationalExpression
    )* {
        // Post process the equality expression to clean up 'is null', etc.
		#equalityExpression = processEqualityExpression(#equalityExpression);
    }
;

// level 4 - LESS_THAN_OP, GREATER_THAN_OP, LESS_THAN_OR_EQUALS_OP, GREATER_THAN_OR_EQUALS_OP, LIKE, NOT LIKE, BETWEEN, NOT BETWEEN
// NOTE: The NOT prefix for LIKE and BETWEEN will be represented in the
// token type.  When traversing the AST, use the token type, and not the
// token text to interpret the semantics of these nodes.
relationalExpression
	: concatenation (
		( ( ( LESS_THAN_OP^ | GREATER_THAN_OP^ | LESS_THAN_OR_EQUALS_OP^ | GREATER_THAN_OR_EQUALS_OP^ ) additiveExpression )* )
		// Disable node production for the optional 'not'.
		| (n:NOT!)? (
			// Represent the optional NOT prefix using the token type by
			// testing 'n' and setting the token type accordingly.
			(i:IN^ {
					#i.setType( (n == null) ? IN : NOT_IN);
					#i.setText( (n == null) ? "in" : "not in");
				}
				inList)
			| (b:BETWEEN^ {
					#b.setType( (n == null) ? BETWEEN : NOT_BETWEEN);
					#b.setText( (n == null) ? "between" : "not between");
				}
				betweenList )
			| (l:LIKE^ {
					#l.setType( (n == null) ? LIKE : NOT_LIKE);
					#l.setText( (n == null) ? "like" : "not like");
				}
				concatenation likeEscape)
			| (MEMBER! OF! p:path! {
				processMemberOf( #p, #n );
			  } ) )
		)
	;

likeEscape
	: (ESCAPE^ concatenation)?
	;

inList
	: x:compoundExpr
	{ #inList = #([IN_LIST,"inList"], #inList); }
	;

betweenList
	: concatenation AND! concatenation
	;

//level 4 - string concatenation
concatenation :
    additiveExpression (
        co:CONCATENATION_OP^ { #co.setType(ARG_LIST); #co.setText("concat-series"); } additiveExpression ( CONCATENATION_OP!  additiveExpression )* {
            #concatenation = #( [CONCATENATION_OP, "||"], #co );
        }
    )?
;

    
// level 3 - binary plus and minus
additiveExpression
	: multiplyExpression ( ( PLUS_SIGN^ | MINUS_SIGN^ ) multiplyExpression )*
	;

// level 2 - binary multiply and divide
multiplyExpression
	: unaryExpression ( ( ASTERISK^ | SOLIDUS^ ) unaryExpression )*
	;
	
// level 1 - unary minus, unary plus, not
unaryExpression
	: MINUS_SIGN^ {#MINUS_SIGN.setType(UNARY_MINUS);} unaryExpression
	| PLUS_SIGN^ {#PLUS_SIGN.setType(UNARY_PLUS);} unaryExpression
	| caseExpression
	| quantifiedExpression
	| standardFunction
	| setFunction
	| collectionFunction
	| collectionExpression
	| atom
	;

caseExpression :
    caseAbbreviation
    | caseSpecification
;

caseAbbreviation :
    ( NULLIF^ LEFT_PAREN! unaryExpression COMMA! unaryExpression RIGHT_PAREN! )
    | ( COALESCE^ LEFT_PAREN! unaryExpression (COMMA! unaryExpression)* RIGHT_PAREN! )
;

caseSpecification :
    simpleCase
     | searchedCase
;

simpleCase :
    c:CASE^ unaryExpression (simpleCaseWhenClause)+ (elseClause)? END {
        #c.setType( SIMPLE_CASE );
    }
;

simpleCaseWhenClause :
    WHEN unaryExpression THEN result
;

result :
    unaryExpression
;

elseClause :
    ELSE result
;

searchedCase :
    c:CASE^ (searchedWhenClause)+ (elseClause)? END {
        c.setType( SEARCHED_CASE );
    }
;

searchedWhenClause :
    WHEN logicalExpression THEN unaryExpression
;

quantifiedExpression // todo : this should be a function of the predicates (logicalExpression) not of concatenation/additiveExpression: these can come only in very certain circumstances.
	: ( SOME^ | EXISTS^ | ALL^ | ANY^ ) 
	( identifier | collectionExpression | (LEFT_PAREN! ( subQuery ) RIGHT_PAREN!) )
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
//	| USER
//	| CURRENT_USER
//	| SESSION_USER
//	| SYSTEM_USER
;

castFunction :
    CAST^ LEFT_PAREN! unaryExpression AS! dataType RIGHT_PAREN!
;

dataType :
    // todo : temp...
    identifier
;

concatFunction :
    CONCAT^ LEFT_PAREN! unaryExpression ( COMMA! unaryExpression )+ RIGHT_PAREN!
;

substringFunction :
    SUBSTRING^ LEFT_PAREN! unaryExpression COMMA! unaryExpression ( COMMA! unaryExpression)? RIGHT_PAREN!
;

trimFunction :
    TRIM^ LEFT_PAREN! trimOperands RIGHT_PAREN!
;

trimOperands :
    trimSpecification unaryExpression FROM! unaryExpression
    | tsp:trimSpecification FROM ts:unaryExpression {
        #trimOperands = #( #tsp, [CHAR_STRING," "], #ts );
    }
    | FROM! ts3:unaryExpression {
        #trimOperands = #( [BOTH,"both"], [CHAR_STRING," "], #ts3 );
    }
    | sve1:unaryExpression ( FROM! sve2:unaryExpression ) {
        if ( #sve2 != null ) {
            #trimOperands = #( [BOTH,"both"], #sve1, #sve2 );
        }
        else {
            #trimOperands = #( [BOTH,"both"], [CHAR_STRING," "], #sve1 );
        }
    }
;

trimSpecification :
    LEADING
    | TRAILING
    | BOTH
;

upperFunction :
    UPPER^ LEFT_PAREN! concatenation RIGHT_PAREN!
;

lowerFunction :
    LOWER^ LEFT_PAREN! concatenation RIGHT_PAREN!
;

lengthFunction :
    LENGTH^ LEFT_PAREN! concatenation RIGHT_PAREN!
;

locateFunction :
    LOCATE^ LEFT_PAREN! unaryExpression COMMA! unaryExpression ( COMMA! unaryExpression )? RIGHT_PAREN!
;

absFunction :
    ABS^ LEFT_PAREN! unaryExpression RIGHT_PAREN!
;

sqrtFunction :
    SQRT^ LEFT_PAREN! unaryExpression RIGHT_PAREN!
;

modFunction :
    MOD^ LEFT_PAREN! unaryExpression COMMA! unaryExpression RIGHT_PAREN!
;

sizeFunction :
    SIZE^ LEFT_PAREN! path RIGHT_PAREN!
;

indexFunction :
    INDEX^ LEFT_PAREN! aliasReference RIGHT_PAREN!
;

currentDateFunction :
    CURRENT_DATE^ ( LEFT_PAREN! RIGHT_PAREN! )?
;

currentTimeFunction :
    CURRENT_TIME^ ( LEFT_PAREN! RIGHT_PAREN! )?
;

currentTimestampFunction :
    CURRENT_TIMESTAMP^ ( LEFT_PAREN! RIGHT_PAREN! )?
;

extractFunction :
    EXTRACT^ LEFT_PAREN! extractField FROM! extractSource RIGHT_PAREN!
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
    concatenation
;

positionFunction :
    POSITION^ LEFT_PAREN! unaryExpression IN! unaryExpression RIGHT_PAREN!
;

charLengthFunction :
    ( CHAR_LENGTH^ | CHARACTER_LENGTH^ ) LEFT_PAREN! concatenation RIGHT_PAREN!
;

octetLengthFunction :
	OCTET_LENGTH^ LEFT_PAREN! unaryExpression RIGHT_PAREN!
;

bitLengthFunction :
	BIT_LENGTH^ LEFT_PAREN! unaryExpression RIGHT_PAREN!
;

setFunction :
    ( SUM^ | AVG^ | MAX^ | MIN^ ) LEFT_PAREN! additiveExpression RIGHT_PAREN!
    | COUNT^ LEFT_PAREN! ( ASTERISK | ( ( DISTINCT | ALL )? ( path | collectionExpression ) ) ) RIGHT_PAREN!
;

collectionFunction :
    ( MAXELEMENT^ | MAXINDEX^ | MINELEMENT^ | MININDEX^ ) LEFT_PAREN! identPrimary RIGHT_PAREN!
;

collectionExpression :
    ( ELEMENTS^ | INDICES^ ) LEFT_PAREN! path RIGHT_PAREN!
;


// level 0 - expression atom
// ident qualifier ('.' ident ), array index ( [ expr ] ),
// method call ( '.' ident '(' exprList ') )
atom :
    primaryExpression (
        DOT^ identifier ( options { greedy=true; } :
            ( op:LEFT_PAREN^ {#op.setType(GENERIC_FUNCTION);} exprList RIGHT_PAREN! )
        )?
		| lb:LEFT_BRACKET^ {#lb.setType(INDEX_OP);} expression RIGHT_BRACKET!
    )*
	;

// level 0 - the basic element of an expression
primaryExpression :
    identPrimary ( options {greedy=true;} : DOT^ CLASS )?
	| constant
	| parameterSpecification
	| LEFT_PAREN! (expressionOrVector | subQuery) RIGHT_PAREN!
	;

parameterSpecification! :
    c:COLON! { weakKeywords(); } name:identifier! {
        #c.setType( NAMED_PARAM );
        #c.setText( #name.getText() );
        #parameterSpecification = #c;
    }
    | p:PARAM! ( position:NUM_INT_LITERAL! )? {
        if ( #position == null ) {
            #parameterSpecification = #p;
        }
        else {
            #position.setType( JPA_PARAM );
            #parameterSpecification = #position;
        }
    }
;

// This parses normal expression and a list of expressions separated by commas.  If a comma is encountered
// a parent VECTOR_EXPR node will be created for the list.
expressionOrVector!
	: e:expression ( v:vectorExpr )? {
		// If this is a vector expression, create a parent node for it.
		if (#v != null)
			#expressionOrVector = #([VECTOR_EXPR,"{vector}"], #e, #v);
		else
			#expressionOrVector = #e;
	}
	;

vectorExpr
	: COMMA! expression (COMMA! expression)*
	;

// identifier, followed by member refs (dot ident), or method calls.
// NOTE: handleDotIdent() is called immediately after the first IDENT is recognized because
// the method looks a head to find keywords after DOT and turns them into identifiers.
identPrimary :
    identifier { handleDotIdent(); }
        ( options { greedy=true; } : DOT^ ( identifier | ELEMENTS | o:OBJECT { #o.setType(IDENT); } ) )*
        ( options { greedy=true; } : ( op:LEFT_PAREN^ { #op.setType(GENERIC_FUNCTION);} exprList RIGHT_PAREN! ) )? {
    			if ( #op == null ) {
	    		    String path = extractPath( #identPrimary );
		    	    if ( isJavaConstant( path ) ) {
			            #identPrimary = #( [JAVA_CONSTANT,path] );
                    }
                }
        }
;

//aggregate
//	: ( SUM^ | AVG^ | MAX^ | MIN^ ) LEFT_PAREN! additiveExpression RIGHT_PAREN! { #aggregate.setType(AGGREGATE); }
//	// Special case for count - It's 'parameters' can be keywords.
//	|  COUNT^ LEFT_PAREN! ( ASTERISK { #ASTERISK.setType(ROW_STAR); } | ( ( DISTINCT | ALL )? ( path | collectionExpr ) ) ) RIGHT_PAREN!
//	|  collectionExpr
//	;
//
//collectionExpr
//	: (ELEMENTS^ | INDICES^) LEFT_PAREN! path RIGHT_PAREN!
//	;
                                           
// NOTE: compoundExpr can be a 'path' where the last token in the path is '.elements' or '.indicies'
compoundExpr
	: collectionExpression
	| path
	| (LEFT_PAREN! ( (expression (COMMA! expression)*) | subQuery ) RIGHT_PAREN!)
	;

exprList :
    ( trimSpec:trimSpecification )? {
        if(#trimSpec != null) #trimSpec.setType(IDENT);
    }
	  (
	  		expression ( (COMMA! expression)+ | FROM { #FROM.setType(IDENT); } expression | AS! identifier )?
	  		| FROM { #FROM.setType(IDENT); } expression
	  )?
			{ #exprList = #([EXPR_LIST,"exprList"], #exprList); }
	;

constant :
    literal
	| NULL
	| TRUE
	| FALSE
;

//literal :
//    numericLiteral
//    | characterLiteral
//    | dateLiteral
//    | timeLiteral
//    | timestampLiteral
//    | intervalLiteral
//;
//
//numericLiteral :
//    UNSIGNED_INTEGER
//    | NUM_INT_LITERAL
//    | NUM_LONG_LITERAL
//    | NUM_DOUBLE_LITERAL
//    | NUM_FLOAT_LITERAL
//;
//
//characterLiteral :
//    CHAR_STRING
//    | NATIONAL_CHAR_STRING_LIT
//	| BIT_STRING_LIT
//	| HEX_STRING_LIT
//;
//
//dateLiteral :
//    DATE^ CHAR_STRING
//;
//
//timeLiteral :
//    TIME^ CHAR_STRING
//;
//
//timestampLiteral :
//    TIMESTAMP^ CHAR_STRING
//;
//
//intervalLiteral :
//    INTERVAL ( PLUS_SIGN | MINUS_SIGN )? CHAR_STRING intervalQualifier
//;
//
//intervalQualifier :
//    intervalStartField ( TO intervalEndField | )
//    | SECOND ( LEFT_PAREN UNSIGNED_INTEGER ( COMMA UNSIGNED_INTEGER )? RIGHT_PAREN )?
//;
//
//intervalStartField :
//    nonSecondDatetimeField ( LEFT_PAREN UNSIGNED_INTEGER RIGHT_PAREN )?
//;
//
//intervalEndField :
//    nonSecondDatetimeField
//	| SECOND ( LEFT_PAREN UNSIGNED_INTEGER RIGHT_PAREN )?
//;
//
//nonSecondDatetimeField :
//    YEAR
//    | MONTH
//    | DAY
//    | HOUR
//    | MINUTE
//;
