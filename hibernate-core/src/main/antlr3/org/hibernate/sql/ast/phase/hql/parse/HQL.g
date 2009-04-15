grammar HQL;

options {
	output=AST;
}

tokens {
//VIRTUAL TOKENS
	FILTER;

	ALIAS_NAME;

	EXPR;
	LOGICAL_EXPR;
	
	IS_NOT_NULL;
	IS_NULL;
	IS_NOT_EMPTY;
	NOT_IN;
	NOT_BETWEEN;
	NOT_LIKE;

	BETWEEN_LIST;
	SIMPLE_CASE;
	SEARCHED_CASE;
	UNARY_MINUS;
	UNARY_PLUS;	
	IN_LIST;

	DOT_CLASS;
	COLLECTION_EXPRESSION;
	GENERAL_FUNCTION_CALL;
	JAVA_CONSTANT;
	GENERIC_ELEMENT;
	NAMED_PARAM;
	JPA_PARAM;

	COLLATE;

	VECTOR_EXPR;

	QUERY;
	QUERY_SPEC;
	GROUPING_VALUE;
	SELECT_FROM;
	SELECT_LIST;
	PERSISTER_SPACE;
	
	PERSISTER_JOIN;
	DYNAMIC_INSTANTIATION_ARG;
	SELECT_ITEM;
	DYNAMIC_INSTANTIATION;
	ENTITY_PERSISTER_REF;
	PROPERTY_JOIN;
	PROP_FETCH;
	SORT_SPEC;
	
	ASSIGNMENT_FIELD;

	INSERTABILITY_SPEC;
	INSERTABLE_PROPERTY;

	QUALIFIED_JOIN;
	ENTITY_NAME;
	PROPERTY_REFERENCE;
	ALIAS_REF;

//SOFT KEYWORDS
	ELSE;
	CLASS;
	NEW;
	OBJECT;
	WHERE;
	ASC;
	DESC;
	UNION;
	INTERSECT;
	ALL;
	EXCEPT;
	DISTINCT;
	SELECT;
	AS;
	IN;
	PROPERTIES;
	FETCH;
	ELEMENTS;
	INDICES;
	CROSS;
	JOIN;
	INNER;
	OUTER;
	LEFT;
	RIGHT;
	FULL;
	WITH;
	ON;
	GROUP_BY;
	HAVING;
	INSERT;
	INTO;
	DELETE;
	SET;
	VERSIONED;
	UPDATE;
	OR;
	AND;
	NOT;
	IS_EMPTY;
	IS;
	ESCAPE;
	BETWEEN;
	LIKE;
	MEMBER_OF;
	THEN;
	END;
	WHEN;
	NULLIF;
	COALESCE;
	EXISTS;
	SOME;
	ANY;
	SUBSTRING;
	CONCAT;
	CAST;
	LEADING;
	TRAILING;
	BOTH;
	TRIM;
	UPPER;
	LOWER;
	LENGTH;
	LOCATE;
	ABS;
	SQRT;
	MOD;
	SIZE;
	INDEX;
	CURRENT_DATE;
	CURRENT_TIME;
	CURRENT_TIMESTAMP;
	EXTRACT;
	SECOND;
	YEAR;
	MONTH;
	DAY;
	HOUR;
	MINUTE;
	TIMEZONE_HOUR;
	TIMEZONE_MINUTE;
	POSITION;
	CHARACTER_LENGTH;
	OCTET_LENGTH;
	BIT_LENGTH;
	SUM;
	AVG;
	MAX;
	MIN;
	COUNT;
	MAXELEMENT;
	MAXINDEX;
	MINELEMENT;
	MININDEX;
	ORDER_BY;
	FROM;
}

@parser::header {
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

@lexer::header {
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
}


@lexer::members {
	/** The standard method called to automatically emit a token at the
	 *  outermost lexical rule.  The token object should point into the
	 *  char buffer start..stop.  If there is a text override in 'text',
	 *  use that to set the token's text.  Override this method to emit
	 *  custom Token objects.
	 
	public Token emit() {
		Token t = new DroolsToken(input, state.type, state.channel, state.tokenStartCharIndex, getCharIndex()-1);
		t.setLine(state.tokenStartLine);
		t.setText(state.text);
		t.setCharPositionInLine(state.tokenStartCharPositionInLine);
		emit(t);
		return t;
	} */

}

@parser::members {
    private List<String> errorMessages = new LinkedList<String>();

	private boolean validateIdentifierKey(String text) {
		return validateLT(1, text);
	}
	
	private boolean validateLT(int LTNumber, String text) {
		String text2Validate = retrieveLT( LTNumber );
		return text2Validate == null ? false : text2Validate.equalsIgnoreCase(text);
	}

	private String retrieveLT(int LTNumber) {
      		if (null == input)
			return null;
		if (null == input.LT(LTNumber))
			return null;
		if (null == input.LT(LTNumber).getText())
			return null;
	
		return input.LT(LTNumber).getText();
	}

	public boolean hasErrors(){
	   if ( errorMessages.size() > 0 ) {
	       return true;
       }
       return false;
	}
	
	public List<String> getErrorMessages(){
	   return errorMessages;
	}

    @Override
    public void reportError( RecognitionException e ) {
        errorMessages.add(generateError(getRuleInvocationStack(e, this.getClass().getName()), this.getTokenNames(), e));
        super.reportError(e);
    }

    private String generateError( List<Object> invocationStack,
                                String[] tokenNames,
                                RecognitionException e ) {
        String localization = invocationStack + ": line " + e.line + ":" + e.charPositionInLine + " ";
        return generateError(localization, tokenNames, e);
    }

    private String generateError( String localization,
                                String[] tokenNames,
                                RecognitionException e ) {
        String message = "";
        if (e instanceof MismatchedTokenException) {
            MismatchedTokenException mte = (MismatchedTokenException)e;
            String tokenName = "<unknown>";
            if (mte.expecting == Token.EOF) {
                tokenName = "EOF";
            } else {
                if (tokenNames != null) {
                    tokenName = tokenNames[mte.expecting];
                }
            }
            message = localization + "mismatched token: " + e.token + "; expecting type " + tokenName;
        } else if (e instanceof MismatchedTreeNodeException) {
            MismatchedTreeNodeException mtne = (MismatchedTreeNodeException)e;
            String tokenName = "<unknown>";
            if (mtne.expecting == Token.EOF) {
                tokenName = "EOF";
            } else {
                tokenName = tokenNames[mtne.expecting];
            }
            message = localization + "mismatched tree node: " + mtne.node + "; expecting type " + tokenName;
        } else if (e instanceof NoViableAltException) {
            NoViableAltException nvae = (NoViableAltException)e;
            message = localization + "state " + nvae.stateNumber + " (decision=" + nvae.decisionNumber
                      + ") no viable alt; token=" + e.token;
        } else if (e instanceof EarlyExitException) {
            EarlyExitException eee = (EarlyExitException)e;
            message = localization + "required (...)+ loop (decision=" + eee.decisionNumber + ") did not match anything; token="
                      + e.token;
        } else if (e instanceof MismatchedSetException) {
            MismatchedSetException mse = (MismatchedSetException)e;
            message = localization + "mismatched token: " + e.token + "; expecting set " + mse.expecting;
        } else if (e instanceof MismatchedNotSetException) {
            MismatchedNotSetException mse = (MismatchedNotSetException)e;
            message = localization + "mismatched token: " + e.token + "; expecting set " + mse.expecting;
        } else if (e instanceof FailedPredicateException) {
            FailedPredicateException fpe = (FailedPredicateException)e;
            message = localization + "rule " + fpe.ruleName + " failed predicate: {" + fpe.predicateText + "}?";
        }
        
        return message;
    }

}

filterStatement[String collectionRole]
	:	selectClause? from_key? whereClause? ( groupByClause havingClause?)? orderByClause?
		-> ^(QUERY ^(QUERY_SPEC["filter-query-spec"] FILTER[$collectionRole] 
				selectClause? from_key? whereClause? ( groupByClause havingClause?)? orderByClause?))
	//TODO throw an exception here when using from
	;

statement
	:	updateStatement
	|	deleteStatement
	|	insertStatement
	|	selectStatement
	;

updateStatement
	:	udpate_key^ versioned_key? from_key!? entityName aliasClause setClause whereClause?
	;

setClause
	:	set_key^ assignment (COMMA! assignment)*
	;

assignment
	:	assignmentField EQUALS^ newValue
	;

assignmentField
	:	dotIdentifierPath -> ^(ASSIGNMENT_FIELD dotIdentifierPath)
	;

newValue
	:	concatenation -> ^(EXPR concatenation)
	;

deleteStatement
	:	delete_key^ from_key!? entityName aliasClause whereClause?
	;

insertStatement
	:	insert_key^ intoClause selectStatement
	;

intoClause
	:	into_key^ entityName insertabilitySpecification
	;

insertabilitySpecification
	:	LEFT_PAREN insertablePropertySpecification ( COMMA insertablePropertySpecification )* RIGHT_PAREN
		-> ^(INSERTABILITY_SPEC insertablePropertySpecification+ )
	;

insertablePropertySpecification
	:	dotIdentifierPath -> ^(INSERTABLE_PROPERTY dotIdentifierPath)
	;

selectStatement
	:	queryExpression orderByClause?
		-> ^(QUERY queryExpression orderByClause?)
	;

queryExpression
	:	querySpec ( ( union_key^ | intersect_key^ | except_key^ ) all_key? querySpec )*
	;

querySpec
	:	selectFrom whereClause? ( groupByClause havingClause? )?
		-> ^(QUERY_SPEC selectFrom whereClause? groupByClause? havingClause?)
	;

groupByClause
	:	group_by_key^ groupingSpecification
	;

havingClause
	:	having_key^ logicalExpression
	;

groupingSpecification
	:	groupingValue ( COMMA! groupingValue )*
	;

groupingValue
	:	concatenation collationSpecification?
		-> ^(GROUPING_VALUE ^(EXPR concatenation) collationSpecification?)
	;

whereClause
	:	where_key^ logicalExpression
	;

selectFrom
	:	selectClause? fromClause
		-> ^(SELECT_FROM fromClause selectClause?)
	;

subQuery
	:	queryExpression
		-> ^(QUERY queryExpression)
	;

fromClause
	:	from_key^ persisterSpaces
	;

persisterSpaces
	:	persisterSpace ( COMMA persisterSpace )*
		-> ^(PERSISTER_SPACE persisterSpace)+
	;

persisterSpace
	:	persisterSpaceRoot ( qualifiedJoin | crossJoin )*
	;

crossJoin
	:	cross_key join_key mainEntityPersisterReference
		-> ^(PERSISTER_JOIN[$join_key.start,"persister-join"] cross_key mainEntityPersisterReference) 
	;

qualifiedJoin
@init {boolean isEntityReference = false;}
	:	nonCrossJoinType join_key fetch_key? path aliasClause
	(	on_key {isEntityReference = true;} logicalExpression 
	|	propertyFetch? withClause?
	)
	-> {isEntityReference}? ^(PERSISTER_JOIN[$join_key.start,"persister-join"] nonCrossJoinType ^(ENTITY_PERSISTER_REF ENTITY_NAME[$path.start, $path.text] aliasClause?) ^(on_key logicalExpression))
	-> ^(PROPERTY_JOIN[$join_key.start, "property-join"] nonCrossJoinType fetch_key? aliasClause? propertyFetch? ^(PROPERTY_REFERENCE path) withClause?)
	;

withClause
	:	with_key^ logicalExpression
	;

nonCrossJoinType
	:	inner_key
	|	outerJoinType outer_key?
	|	-> INNER
	;

outerJoinType
	:	left_key
	|	right_key
	|	full_key
	;

persisterSpaceRoot
options{
backtrack=true;
}	:	mainEntityPersisterReference
	|	jpaCollectionReference
	|	hibernateLegacySyntax
	;

mainEntityPersisterReference
	:	entityName aliasClause propertyFetch?
		-> ^(ENTITY_PERSISTER_REF entityName aliasClause? propertyFetch?)
	;

propertyFetch
	:	fetch_key all_key properties_key
		-> PROP_FETCH[$fetch_key.start, "property-fetch"]
	;

hibernateLegacySyntax
	:	aliasValue in_key
	(	class_key entityName -> ^(ENTITY_PERSISTER_REF entityName aliasValue) 
	|	collectionExpression -> ^(PROPERTY_JOIN INNER[$in_key.start, "inner"] aliasValue collectionExpression)
	)
	;

jpaCollectionReference
	:	in_key LEFT_PAREN propertyReference RIGHT_PAREN aliasClause
		-> ^(PROPERTY_JOIN INNER[$in_key.start, "inner"] aliasClause? propertyReference) 
	;

selectClause
	:	select_key^ distinct_key? rootSelectExpression 
	;

rootSelectExpression
	:	rootDynamicInstantiation
	|	jpaSelectObjectSyntax
	|	explicitSelectList
	;

explicitSelectList
	:	explicitSelectItem ( COMMA explicitSelectItem )*
		-> ^(SELECT_LIST explicitSelectItem+)
	;

explicitSelectItem
	:	selectExpression
	;

selectExpression
	:	expression aliasClause
		-> ^(EXPR expression) aliasClause?
	;

aliasClause
options{
    k=2;
}	:
	|	aliasValue
	|	as_key! aliasValue
	;

aliasValue
	:	IDENTIFIER -> ALIAS_NAME[$IDENTIFIER]
	;

rootDynamicInstantiation
	:	new_key dynamicInstantiationTarget LEFT_PAREN dynamicInstantiationArgs RIGHT_PAREN
		-> ^(SELECT_ITEM ^(DYNAMIC_INSTANTIATION[$dynamicInstantiationTarget.start, $dynamicInstantiationTarget.text] dynamicInstantiationArgs))
	;

dynamicInstantiationTarget
	:	dotIdentifierPath
	;

dynamicInstantiationArgs
	:	dynamicInstantiationArg ( COMMA! dynamicInstantiationArg )*
	;

dynamicInstantiationArg
	:	selectExpression -> ^(DYNAMIC_INSTANTIATION_ARG selectExpression)
	|	rootDynamicInstantiation -> ^(DYNAMIC_INSTANTIATION_ARG rootDynamicInstantiation)
	;

jpaSelectObjectSyntax
	:	object_key LEFT_PAREN aliasReference RIGHT_PAREN
		-> ^(SELECT_ITEM ^(object_key aliasReference)) 
	;

orderByClause
	:	order_by_key^ sortSpecification ( COMMA! sortSpecification )*
	;

sortSpecification
	:	sortKey collationSpecification? orderingSpecification?
		-> ^(SORT_SPEC sortKey collationSpecification? orderingSpecification?)
	;

sortKey
	:	concatenation
		-> ^(EXPR concatenation)
	;

collationSpecification
	:	collate_key collateName
	->	COLLATE[$collateName.start, $collateName.text]
	;

collateName
	:	dotIdentifierPath
	;

orderingSpecification
	:	ascending_key
	|	descending_key
	;

logicalExpression
	:	expression
		-> ^(LOGICAL_EXPR expression)
	;

expression
	:	logicalOrExpression
	;

logicalOrExpression
	:	logicalAndExpression ( or_key^ logicalAndExpression )*
	;

logicalAndExpression
	:	negatedExpression ( and_key^ negatedExpression )*
	;

negatedExpression
	:	not_key^ negatedExpression
	|	equalityExpression
	;

equalityExpression
@init{ boolean isNull = false; boolean isNegated = false;}
	:	(relationalExpression -> relationalExpression) 
	(	is_key (not_key {isNegated = true;})? (NULL {isNull = true;}|empty_key)
		-> {isNull && isNegated}? ^(IS_NOT_NULL[$not_key.start, "is not null"] $equalityExpression)
		-> {isNull && !isNegated}? ^(IS_NULL[$NULL, "is null"] $equalityExpression)
		-> {!isNull && isNegated}? ^(NOT ^(EXISTS ^(QUERY ^(QUERY_SPEC ^(SELECT_FROM ^(FROM $equalityExpression))))))
		-> ^(EXISTS ^(QUERY ^(QUERY_SPEC ^(SELECT_FROM ^(FROM $equalityExpression)))))
	|	( op=EQUALS | op=NOT_EQUAL ) relationalExpression
		-> ^($op $equalityExpression relationalExpression)
	)*
	;

relationalExpression
@init {boolean isNegated = false;} 
	:	(concatenation -> concatenation)
	( 
	(	( op=LESS | op=GREATER | op=LESS_EQUAL | op=GREATER_EQUAL ) additiveExpression
			-> ^($op $relationalExpression additiveExpression) 
		)+
	|  (not_key {isNegated = true;} )?
		(	in_key inList
			-> {isNegated}? ^(NOT_IN[$not_key.start, "not in"] $relationalExpression inList)
			-> ^(in_key $relationalExpression inList) 
		|	between_key betweenList
			-> {isNegated}? ^(NOT_BETWEEN[$not_key.start, "not between"] $relationalExpression betweenList)
			-> ^(between_key $relationalExpression betweenList)
		|	like_key concatenation likeEscape?
			-> {isNegated}? ^(NOT_LIKE[$not_key.start, "not like"] $relationalExpression concatenation likeEscape?) 
			-> ^(like_key $relationalExpression concatenation likeEscape?)
		|	member_of_key propertyReference
			-> {isNegated}? ^(NOT_IN[$not_key.start, "not in"] ^(IN_LIST ^(QUERY ^(SELECT_FROM ^(FROM propertyReference)))))
			-> ^(IN[$member_of_key.start, "in"] ^(IN_LIST ^(QUERY ^(SELECT_FROM ^(FROM propertyReference)))))
		)
	)?
	;

likeEscape
	:	escape_key concatenation
		-> ^(escape_key ^(EXPR concatenation))
	;

inList
	:	collectionExpression
		-> ^(IN_LIST collectionExpression)
	|	LEFT_PAREN ( expression (COMMA expression)* | subQuery ) RIGHT_PAREN
		-> ^(IN_LIST ^(EXPR expression)* subQuery?)
	;

betweenList
	:	concatenation and_key concatenation
		-> ^(BETWEEN_LIST ^(EXPR concatenation)+)
	;

concatenation
	:	additiveExpression (DOUBLE_PIPE^ additiveExpression)*
	;

additiveExpression
	:	multiplyExpression ( ( PLUS^ | MINUS^ ) multiplyExpression )*
	;

multiplyExpression
	:	unaryExpression ( ( ASTERISK^ | SOLIDUS^ ) unaryExpression )*
	;

unaryExpression
	:	MINUS unaryExpression -> ^(UNARY_MINUS[$MINUS] unaryExpression)
	|	PLUS unaryExpression -> ^(UNARY_PLUS[$PLUS] unaryExpression)
	|	caseExpression
	|	quantifiedExpression
	|	standardFunction
	|	setFunction
	|	collectionFunction
	|	collectionExpressionSimple
	|	atom
	;

caseExpression
	:	caseAbbreviation
	|	caseSpecification
	;

caseAbbreviation
	:	nullif_key LEFT_PAREN concatenation COMMA concatenation RIGHT_PAREN
		-> ^(nullif_key ^(EXPR concatenation)+)
	|	coalesce_key LEFT_PAREN concatenation (COMMA concatenation)* RIGHT_PAREN
		-> ^(coalesce_key ^(EXPR concatenation)+)
	;

caseSpecification
options{
backtrack=true;
}	:	simpleCase
	|	searchedCase
	;

simpleCase
	:	case_key concatenation simpleCaseWhenClause+ elseClause? end_key
	->	^(SIMPLE_CASE[$case_key.start, $case_key.text] ^(EXPR concatenation) simpleCaseWhenClause+ elseClause?)
	;

simpleCaseWhenClause
	:	when_key concatenation then_key concatenation
		-> ^(when_key ^(EXPR concatenation)+)
	;

elseClause
	:	else_key concatenation
		-> ^(else_key ^(EXPR concatenation))
	;

searchedCase
	:	case_key searchedWhenClause+ elseClause? end_key
	->	^(SEARCHED_CASE[$case_key.start, $case_key.text] searchedWhenClause+ elseClause?)
	;

searchedWhenClause
	:	when_key logicalExpression then_key concatenation
		-> ^(when_key logicalExpression ^(EXPR concatenation))
	;

quantifiedExpression
	:	( some_key^ | exists_key^ | all_key^ | any_key^ ) 
	(	aliasValue
	|	collectionExpression
	|	LEFT_PAREN! subQuery RIGHT_PAREN!
	)
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
	:	cast_key LEFT_PAREN concatenation as_key dataType RIGHT_PAREN
		-> ^(cast_key ^(EXPR concatenation) dataType)
	;

concatFunction
	:	concat_key LEFT_PAREN concatenation ( COMMA concatenation )+ RIGHT_PAREN
		-> ^(concat_key ^(EXPR concatenation)+)
	;

substringFunction
	:	substring_key LEFT_PAREN concatenation COMMA concatenation ( COMMA concatenation)? RIGHT_PAREN
		-> ^(substring_key ^(EXPR concatenation)+)
	;

trimFunction
	:	trim_key LEFT_PAREN trimOperands RIGHT_PAREN
		-> ^(trim_key trimOperands)
	;

trimOperands
options{
k=2;
}
@init {boolean hasSecondExpression = false;}
	:	trimSpecification from_key concatenation -> ^(trimSpecification ^(EXPR STRING_LITERAL[" "]) ^(EXPR concatenation))
	|	trimSpecification concatenation from_key concatenation -> ^(trimSpecification ^(EXPR concatenation)+)
	|	from_key concatenation -> ^(BOTH ^(EXPR STRING_LITERAL[" "]) ^(EXPR concatenation))
	|	cn=concatenation ( from_key concatenation {hasSecondExpression = true;} )?
		-> {hasSecondExpression}? ^(BOTH ^(EXPR concatenation)+)
		-> ^(BOTH ^(EXPR STRING_LITERAL[" "]) ^(EXPR $cn))
	;

trimSpecification
	:	leading_key
	|	trailing_key
	|	both_key
	;

upperFunction
	:	upper_key LEFT_PAREN concatenation RIGHT_PAREN
		-> ^(upper_key ^(EXPR concatenation))
	;

lowerFunction
	:	lower_key LEFT_PAREN concatenation RIGHT_PAREN
		-> ^(lower_key ^(EXPR concatenation))
	;

lengthFunction
	:	length_key LEFT_PAREN concatenation RIGHT_PAREN
		-> ^(length_key ^(EXPR concatenation))
	;

locateFunction
	:	locate_key LEFT_PAREN concatenation COMMA concatenation ( COMMA concatenation )? RIGHT_PAREN
		-> ^(locate_key ^(EXPR concatenation)+)
	;

absFunction
	:	abs_key LEFT_PAREN concatenation RIGHT_PAREN
		-> ^(abs_key ^(EXPR concatenation))
	;

sqrtFunction
	:	sqrt_key LEFT_PAREN concatenation RIGHT_PAREN
		-> ^(sqrt_key ^(EXPR concatenation))
	;

modFunction
	:	mod_key LEFT_PAREN concatenation COMMA concatenation RIGHT_PAREN
		-> ^(mod_key ^(EXPR concatenation)+)
	;

sizeFunction
	:	size_key LEFT_PAREN propertyReference RIGHT_PAREN
		-> ^(size_key propertyReference)
	;

indexFunction
	:	index_key LEFT_PAREN aliasReference RIGHT_PAREN
		-> ^(index_key aliasReference)
	;

currentDateFunction
	:	current_date_key^ ( LEFT_PAREN! RIGHT_PAREN! )?
	;

currentTimeFunction
	:	current_time_key^ ( LEFT_PAREN! RIGHT_PAREN! )?
	;

currentTimestampFunction
	:	current_timestamp_key^ ( LEFT_PAREN! RIGHT_PAREN! )?
	;

extractFunction
	:	extract_key LEFT_PAREN extractField from_key concatenation RIGHT_PAREN
	-> ^(extract_key extractField ^(EXPR concatenation))
	;

extractField
	:	datetimeField
	|	timeZoneField
	;

datetimeField
	:	nonSecondDatetimeField
	|	second_key
	;

nonSecondDatetimeField
	:	year_key
	|	month_key
	|	day_key
	|	hour_key
	|	minute_key
	;

timeZoneField
	:	timezone_hour_key
	|	timezone_minute_key
	;

positionFunction
	:	position_key LEFT_PAREN concatenation in_key concatenation RIGHT_PAREN
		-> ^(position_key ^(EXPR concatenation)+)
	;

charLengthFunction
	:	character_length_key LEFT_PAREN concatenation RIGHT_PAREN
		-> ^(character_length_key ^(EXPR concatenation))
	;

octetLengthFunction
	:	octet_length_key LEFT_PAREN concatenation RIGHT_PAREN
		-> ^(octet_length_key ^(EXPR concatenation))	
	;

bitLengthFunction
	:	bit_length_key LEFT_PAREN concatenation RIGHT_PAREN
		-> ^(bit_length_key ^(EXPR concatenation))
	;

setFunction
	:	( sum_key^ | avg_key^ | max_key^ | min_key^ ) LEFT_PAREN! additiveExpressionWrapper RIGHT_PAREN!
	|	count_key LEFT_PAREN ( ASTERISK | ( ( distinct_key | all_key )? countFunctionArguments ) ) RIGHT_PAREN
		-> ^(count_key ASTERISK? distinct_key? all_key? countFunctionArguments?)
	;

additiveExpressionWrapper
	:	additiveExpression
		-> ^(EXPR additiveExpression)
	;

countFunctionArguments
@init { int type = -1;}
	:	path
		-> {type == 1}? ^(ELEMENTS path)
	    -> {type == 2}? ^(INDICES path)
		-> ^(PROPERTY_REFERENCE path)
	//TODO if ends with:
	//  .elements or .indices -> it is a collectionExpression
	//if not -> it is a property reference
	|	collectionExpressionSimple
	|	numeric_literal
	;

collectionFunction
	:	( maxelement_key^ | maxindex_key^ | minelement_key^ | minindex_key^ ) LEFT_PAREN! propertyReference RIGHT_PAREN!
	;

collectionExpression
	:	(elements_key^|indices_key^) LEFT_PAREN! propertyReference RIGHT_PAREN!
	|	propertyReference DOT! (elements_key^|indices_key^)
	;

collectionExpressionSimple
	:	(elements_key^|indices_key^) LEFT_PAREN! propertyReference RIGHT_PAREN!
	;

atom
@init { int type = -1;}
	:	identPrimary
	    //TODO  if ends with:
	    //  .class -> class type
	    //  .elements or .indices -> it is a collectionExpression
	    //  if contains "()" it is a function call 
	    //  if it is constantReference (using context)
	    //  otherwise it will be a generic element to be resolved on the next phase (1st tree walker)
	    -> {type == 0}? ^(DOT_CLASS identPrimary)
	    -> {type == 1}? ^(ELEMENTS identPrimary)
	    -> {type == 2}? ^(INDICES identPrimary)
	    -> {type == 3}? ^(GENERAL_FUNCTION_CALL identPrimary)
	    -> {type == 4}? ^(JAVA_CONSTANT identPrimary)
	    -> ^(GENERIC_ELEMENT identPrimary)
	|	constant
	|	parameterSpecification
	//TODO: validate using Scopes if it is enabled or not to use parameterSpecification.. if not generate an exception 
	|	LEFT_PAREN! (expressionOrVector | subQuery) RIGHT_PAREN!
	;

parameterSpecification
@init {boolean isJpaParam = false;}
	:	COLON IDENTIFIER -> NAMED_PARAM[$IDENTIFIER]
	|	PARAM (INTEGER_LITERAL {isJpaParam = true;})?
		-> {isJpaParam}? JPA_PARAM[$INTEGER_LITERAL]
		-> PARAM	
	;

expressionOrVector
@init {boolean isVectorExp = false;}
	:	expression (vectorExpr {isVectorExp = true;})?
		-> {isVectorExp}? ^(VECTOR_EXPR ^(EXPR expression) vectorExpr) 
		-> ^(EXPR expression)
	;

vectorExpr
	:	COMMA expression (COMMA expression)*
		-> ^(EXPR expression)+
	;

identPrimary
	: 	IDENTIFIER
		(	DOT^ IDENTIFIER
		|	LEFT_SQUARE^ expression RIGHT_SQUARE!
		|	LEFT_SQUARE^ RIGHT_SQUARE!
		|	LEFT_PAREN^ exprList RIGHT_PAREN!
		)*
	;

exprList
	:	expression? (COMMA expression)*
	->	^(EXPR expression)*
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

aliasReference
	:	IDENTIFIER -> ALIAS_REF[$IDENTIFIER] 
	;

entityName
	:	dotIdentifierPath -> ENTITY_NAME[$dotIdentifierPath.start, $dotIdentifierPath.text]
	//TODO: semantic validation
	;

propertyReference
	:	path
		-> ^(PROPERTY_REFERENCE path)
	;

dataType
	:	IDENTIFIER
	;

dotIdentifierPath
	:	IDENTIFIER 
		(	DOT^ IDENTIFIER		)*
	;

path
	:	IDENTIFIER 
		(	DOT^ IDENTIFIER
		|	LEFT_SQUARE^ expression RIGHT_SQUARE
		|	LEFT_SQUARE^ RIGHT_SQUARE
		)*
	;

class_key
	:	{(validateIdentifierKey("class"))}?=>  id=IDENTIFIER
		->	CLASS[$id]
	;

new_key
	:	{(validateIdentifierKey("new"))}?=>  id=IDENTIFIER
		->	NEW[$id]
	;

else_key
	:	{(validateIdentifierKey("else"))}?=>  id=IDENTIFIER
		->	ELSE[$id]
	;

object_key
	:	{(validateIdentifierKey("object"))}?=>  id=IDENTIFIER
        	->	OBJECT[$id]
	;

case_key
	:	{(validateIdentifierKey("case"))}?=>  IDENTIFIER
	;

current_date_key
	:	{(validateIdentifierKey("current_date"))}?=>  id=IDENTIFIER
		->	CURRENT_DATE[$id]
	;

current_time_key
	:	{(validateIdentifierKey("current_time"))}?=>  id=IDENTIFIER
		->	CURRENT_TIME[$id]
	;

current_timestamp_key
	:	{(validateIdentifierKey("current_timestamp"))}?=>  id=IDENTIFIER
		->	CURRENT_TIMESTAMP[$id]
	;

timezone_hour_key
	:	{(validateIdentifierKey("timezone_hour"))}?=>  id=IDENTIFIER
		->	TIMEZONE_HOUR[$id]
	;

timezone_minute_key
	:	{(validateIdentifierKey("timezone_minute"))}?=>  id=IDENTIFIER
		->	TIMEZONE_MINUTE[$id]
	;

character_length_key
	:	{(validateIdentifierKey("character_length") || validateIdentifierKey("char_length"))}?=>  id=IDENTIFIER
		->	CHARACTER_LENGTH[$id]
	;

octet_length_key
	:	{(validateIdentifierKey("octet_length"))}?=>  id=IDENTIFIER
		->	OCTET_LENGTH[$id]
	;

bit_length_key
	:	{(validateIdentifierKey("bit_length"))}?=>  id=IDENTIFIER
		->	BIT_LENGTH[$id]
	;

extract_key
	:	{(validateIdentifierKey("extract"))}?=>  id=IDENTIFIER
		->	EXTRACT[$id]
	;

second_key
	:	{(validateIdentifierKey("second"))}?=>  id=IDENTIFIER
		->	SECOND[$id]
	;

year_key
	:	{(validateIdentifierKey("year"))}?=>  id=IDENTIFIER
		->	YEAR[$id]
	;

month_key
	:	{(validateIdentifierKey("month"))}?=>  id=IDENTIFIER
		->	MONTH[$id]
	;

day_key
	:	{(validateIdentifierKey("day"))}?=>  id=IDENTIFIER
		->	DAY[$id]
	;

hour_key
	:	{(validateIdentifierKey("hour"))}?=>  id=IDENTIFIER
		->	HOUR[$id]
	;

minute_key
	:	{(validateIdentifierKey("minute"))}?=>  id=IDENTIFIER
		->	MINUTE[$id]
	;

position_key
	:	{(validateIdentifierKey("position"))}?=>  id=IDENTIFIER
		->	POSITION[$id]
	;

sum_key
	:	{(validateIdentifierKey("sum"))}?=>  id=IDENTIFIER
		->	SUM[$id]
	;

avg_key
	:	{(validateIdentifierKey("avg"))}?=>  id=IDENTIFIER
		->	AVG[$id]
	;

max_key
	:	{(validateIdentifierKey("max"))}?=>  id=IDENTIFIER
		->	MAX[$id]
	;

min_key
	:	{(validateIdentifierKey("min"))}?=>  id=IDENTIFIER
		->	MIN[$id]
	;

count_key
	:	{(validateIdentifierKey("count"))}?=>  id=IDENTIFIER
		->	COUNT[$id]
	;

maxelement_key
	:	{(validateIdentifierKey("maxelement"))}?=>  id=IDENTIFIER
		->	MAXELEMENT[$id]
	;

maxindex_key
	:	{(validateIdentifierKey("maxindex"))}?=>  id=IDENTIFIER
		->	MAXINDEX[$id]
	;

minelement_key
	:	{(validateIdentifierKey("minelement"))}?=>  id=IDENTIFIER
		->	MINELEMENT[$id]
	;

minindex_key
	:	{(validateIdentifierKey("minindex"))}?=>  id=IDENTIFIER
		->	MININDEX[$id]
	;

locate_key
	:	{(validateIdentifierKey("locate"))}?=>  id=IDENTIFIER
		->	LOCATE[$id]
	;

abs_key
	:	{(validateIdentifierKey("abs"))}?=>  id=IDENTIFIER
		->	ABS[$id]
	;

sqrt_key
	:	{(validateIdentifierKey("sqrt"))}?=>  id=IDENTIFIER
		->	SQRT[$id]
	;

mod_key
	:	{(validateIdentifierKey("mod"))}?=>  id=IDENTIFIER
		->	MOD[$id]
	;

size_key
	:	{(validateIdentifierKey("size"))}?=>  id=IDENTIFIER
		->	SIZE[$id]
	;

index_key
	:	{(validateIdentifierKey("index"))}?=>  id=IDENTIFIER
		->	INDEX[$id]
	;

leading_key
	:	{(validateIdentifierKey("leading"))}?=>  id=IDENTIFIER
		->	LEADING[$id]
	;

trailing_key
	:	{(validateIdentifierKey("trailing"))}?=>  id=IDENTIFIER
		->	TRAILING[$id]
	;

upper_key
	:	{(validateIdentifierKey("upper"))}?=>  id=IDENTIFIER
		->	UPPER[$id]
	;

lower_key
	:	{(validateIdentifierKey("lower"))}?=>  id=IDENTIFIER
		->	LOWER[$id]
	;

length_key
	:	{(validateIdentifierKey("length"))}?=>  id=IDENTIFIER
		->	LENGTH[$id]
	;

both_key
	:	{(validateIdentifierKey("both"))}?=>  id=IDENTIFIER
		->	BOTH[$id]
	;

trim_key
	:	{(validateIdentifierKey("trim"))}?=>  id=IDENTIFIER
		->	TRIM[$id]
	;
	
substring_key
	:	{(validateIdentifierKey("substring"))}?=>  id=IDENTIFIER
		->	SUBSTRING[$id]
	;

concat_key
	:	{(validateIdentifierKey("concat"))}?=>  id=IDENTIFIER
		->	CONCAT[$id]
	;

cast_key
	:	{(validateIdentifierKey("cast"))}?=>  id=IDENTIFIER
		->	CAST[$id]
	;

any_key
	:	{(validateIdentifierKey("any"))}?=>  id=IDENTIFIER
		->	ANY[$id]
	;

exists_key
	:	{(validateIdentifierKey("exists"))}?=>  id=IDENTIFIER
		->	EXISTS[$id]
	;

some_key
	:	{(validateIdentifierKey("some"))}?=>  id=IDENTIFIER
		->	SOME[$id]
	;

then_key
	:	{(validateIdentifierKey("then"))}?=>  id=IDENTIFIER
		->	THEN[$id]
	;

end_key
	:	{(validateIdentifierKey("end"))}?=>  id=IDENTIFIER
		->	END[$id]
	;


when_key
	:	{(validateIdentifierKey("when"))}?=>  id=IDENTIFIER
		->	WHEN[$id]
	;

nullif_key
	:	{(validateIdentifierKey("nullif"))}?=>  id=IDENTIFIER
		->	NULLIF[$id]
	;

coalesce_key
	:	{(validateIdentifierKey("coalesce"))}?=>  id=IDENTIFIER
		->	COALESCE[$id]
	;

escape_key
	:	{(validateIdentifierKey("escape"))}?=>  id=IDENTIFIER
		->	ESCAPE[$id]
	;

like_key
	:	{(validateIdentifierKey("like"))}?=>  id=IDENTIFIER
		->	LIKE[$id]
	;

between_key
	:	{(validateIdentifierKey("between"))}?=>  id=IDENTIFIER
		->	BETWEEN[$id]
	;

member_of_key
@init{
	String text = "";
}	:	{(validateIdentifierKey("member") && validateLT(2, "of"))}?=>  id=IDENTIFIER IDENTIFIER {text = $text;}
		->	MEMBER_OF[$id]
	;

empty_key
	:	{(validateIdentifierKey("empty"))}?=>  id=IDENTIFIER
	;

is_key	:	{(validateIdentifierKey("is"))}?=>  id=IDENTIFIER
		->	IS[$id]
	;

or_key	:	{(validateIdentifierKey("or"))}?=>  id=IDENTIFIER
		->	OR[$id]
	;

and_key	:	{(validateIdentifierKey("and"))}?=>  id=IDENTIFIER
		->	AND[$id]
	;

not_key	:	{(validateIdentifierKey("not"))}?=>  id=IDENTIFIER
		->	NOT[$id]
	;

set_key
	:	{(validateIdentifierKey("set"))}?=>  id=IDENTIFIER
		->	SET[$id]
	;

versioned_key
	:	{(validateIdentifierKey("versioned"))}?=>  id=IDENTIFIER
		->	VERSIONED[$id]
	;

udpate_key
	:	{(validateIdentifierKey("update"))}?=>  id=IDENTIFIER
		->	UPDATE[$id]
	;

delete_key
	:	{(validateIdentifierKey("delete"))}?=>  id=IDENTIFIER
		->	DELETE[$id]
	;

insert_key
	:	{(validateIdentifierKey("insert"))}?=>  id=IDENTIFIER
		->	INSERT[$id]
	;

into_key
	:	{(validateIdentifierKey("into"))}?=>  id=IDENTIFIER
		->	INTO[$id]
	;

having_key
	:	{(validateIdentifierKey("having"))}?=>  id=IDENTIFIER
		->	HAVING[$id]
	;

with_key
	:	{(validateIdentifierKey("with"))}?=>  id=IDENTIFIER
		->	WITH[$id]
	;

on_key
	:	{(validateIdentifierKey("on"))}?=>  id=IDENTIFIER
		->	ON[$id]
	;

indices_key
	:	{(validateIdentifierKey("indices"))}?=>  id=IDENTIFIER
		->	INDICES[$id]
	;

cross_key
	:	{(validateIdentifierKey("cross"))}?=>  id=IDENTIFIER
		->	CROSS[$id]
	;

join_key
	:	{(validateIdentifierKey("join"))}?=>  id=IDENTIFIER
		->	JOIN[$id]
	;

inner_key
	:	{(validateIdentifierKey("inner"))}?=>  id=IDENTIFIER
		->	INNER[$id]
	;

outer_key
	:	{(validateIdentifierKey("outer"))}?=>  id=IDENTIFIER
		->	OUTER[$id]
	;

left_key
	:	{(validateIdentifierKey("left"))}?=>  id=IDENTIFIER
		->	LEFT[$id]
	;

right_key
	:	{(validateIdentifierKey("right"))}?=>  id=IDENTIFIER
		->	RIGHT[$id]
	;

full_key
	:	{(validateIdentifierKey("full"))}?=>  id=IDENTIFIER
		->	FULL[$id]
	;

elements_key
	:	{(validateIdentifierKey("elements"))}?=>  id=IDENTIFIER
		->	ELEMENTS[$id]
	;

properties_key
	:	{(validateIdentifierKey("properties"))}?=>  id=IDENTIFIER
		->	PROPERTIES[$id]
	;

fetch_key
	:	{(validateIdentifierKey("fetch"))}?=>  id=IDENTIFIER
		->	FETCH[$id]
	;

in_key
	:	{(validateIdentifierKey("in"))}?=>  id=IDENTIFIER
		->	IN[$id]
	;

as_key
	:	{(validateIdentifierKey("as"))}?=>  id=IDENTIFIER
		->	AS[$id]
	;

where_key
	:	{(validateIdentifierKey("where"))}?=>  id=IDENTIFIER
		->	WHERE[$id]
	;

select_key
	:	{(validateIdentifierKey("select"))}?=>  id=IDENTIFIER
		->	SELECT[$id]
	;

distinct_key
	:	{(validateIdentifierKey("distinct"))}?=>  id=IDENTIFIER
		->	DISTINCT[$id]
	;

union_key
	:	{(validateIdentifierKey("union"))}?=>  id=IDENTIFIER
		->	UNION[$id]
	;

intersect_key
	:	{(validateIdentifierKey("intersect"))}?=>  id=IDENTIFIER
		->	INTERSECT[$id]
	;

except_key
	:	{(validateIdentifierKey("except"))}?=>  id=IDENTIFIER
		->	EXCEPT[$id]
	;

all_key
	:	{(validateIdentifierKey("all"))}?=>  id=IDENTIFIER
		->	ALL[$id]
	;

ascending_key
	:	{(validateIdentifierKey("ascending") || validateIdentifierKey("asc"))}?=>  id=IDENTIFIER
		->	ASC[$id]
	;

descending_key
	:	{(validateIdentifierKey("descending") || validateIdentifierKey("desc"))}?=>  id=IDENTIFIER
		->	DESC[$id]
	;

collate_key
	:	{(validateIdentifierKey("collate"))}?=>  IDENTIFIER
	;

order_by_key
@init{
	String text = "";
}	:	{(validateIdentifierKey("order") && validateLT(2, "by"))}?=>  id=IDENTIFIER IDENTIFIER {text = $text;}
		->	ORDER_BY[$id]
	;

group_by_key
@init{
	String text = "";
}	:	{(validateIdentifierKey("group") && validateLT(2, "by"))}?=>  id=IDENTIFIER IDENTIFIER {text = $text;}
		->	GROUP_BY[$id]
	;

from_key
	:	{(validateIdentifierKey("from"))}?=>  id=IDENTIFIER
        	->	FROM[$id]
	;

WS      :       (	' '
                |	'\t'
                |	'\f'
                |	EOL
                )+
                { $channel=HIDDEN; }
        ;

fragment
EOL 	:	     
   		(       ( '\r\n' )=> '\r\n'  // Evil DOS
                |       '\r'    // Macintosh
                |       '\n'    // Unix (the right way)
                )
        ;

HEX_LITERAL : '0' ('x'|'X') HEX_DIGIT+ INTEGER_TYPE_SUFFIX? ;

INTEGER_LITERAL : ('0' | '1'..'9' '0'..'9'*) ;

DECIMAL_LITERAL : ('0' | '1'..'9' '0'..'9'*) INTEGER_TYPE_SUFFIX ;

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

CHARACTER_LITERAL
    :   '\'' ( ESCAPE_SEQUENCE | ~('\''|'\\') ) '\''
    ;

STRING_LITERAL
    :  '"' ( ESCAPE_SEQUENCE | ~('\\'|'"') )* '"'
    |  ('\'' ( ESCAPE_SEQUENCE | ~('\\'|'\'') )* '\'')+
    |  '`' ( ESCAPE_SEQUENCE | ~('\\'|'`') )* '`'
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

TRUE
	:	'true'
	;	

FALSE
	:	'false'
	;

NULL
	:	'null'
	;

EQUALS
	:	'='
	;

SEMICOLON
	:	';'
	;

COLON
	:	':'
	;

NOT_EQUAL
	:	'!='
	|	'^='
	|	'<>'
	;

PIPE
	:	'|'
	;

DOUBLE_PIPE
	:	'||'
	;

PARAM	:	'?'
	;

GREATER
	:	'>'
	;

GREATER_EQUAL
	:	'>='
	;

LESS
	:	'<'
	;

LESS_EQUAL
	:	'<='
	;

ARROW
	:	'->'
	;

IDENTIFIER
	:	('a'..'z'|'A'..'Z'|'_'|'$'|'\u0080'..'\ufffe')('a'..'z'|'A'..'Z'|'_'|'$'|'0'..'9'|'\u0080'..'\ufffe')*
	;

LEFT_PAREN
        :	'('
        ;

RIGHT_PAREN
        :	')'
        ;

LEFT_SQUARE
        :	'['
        ;

RIGHT_SQUARE
        :	']'
        ;        

COMMA	:	','
	;
	
DOT	:	'.'
	;

PLUS	:	'+'
	;

MINUS	:	'-'
	;

ASTERISK
	:	'*'	
	;

SOLIDUS	:	'/'	
	;

PERCENT	:	'%'	
	;

AMPERSAND
	:	'&'	
	;