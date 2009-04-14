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
package org.hibernate.sql.ast.phase.hql.normalize;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.sql.ast.util.ErrorHandlerDelegate;
import org.hibernate.sql.ast.util.ErrorHandlerDelegateImpl;
import org.hibernate.sql.ast.util.ASTPrinter;
import org.hibernate.sql.ast.common.JoinType;
import org.hibernate.sql.ast.common.Node;
import org.hibernate.sql.ast.phase.hql.normalize.path.impl.FromClausePathNormalizationStrategy;
import org.hibernate.sql.ast.phase.hql.normalize.path.impl.BasicPathNormalizationStrategySupport;
import org.hibernate.sql.ast.phase.hql.normalize.path.impl.SelectClausePathNormalizationStrategy;
import org.hibernate.sql.ast.phase.hql.normalize.path.impl.OnFragmentPathNormalizationStrategy;
import org.hibernate.sql.ast.phase.hql.normalize.path.impl.WithFragmentPathNormalizationStrategy;
import org.hibernate.QueryException;
import org.hibernate.MappingException;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.type.Type;
import org.hibernate.sql.ast.phase.hql.normalize.path.PathNormalizationStrategyStack;
import org.hibernate.sql.ast.phase.hql.normalize.path.PathedPropertyReferenceSource;
import org.hibernate.sql.ast.phase.hql.normalize.path.PathNormalizationStrategy;
import org.hibernate.sql.ast.DetailedSemanticException;
import org.hibernate.sql.ast.alias.ImplicitAliasGenerator;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.util.StringHelper;

import antlr.RecognitionException;
import antlr.ASTFactory;
import antlr.SemanticException;
import antlr.collections.AST;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public class HqlNormalizer extends GeneratedHqlNormalizer implements NormalizationContext {
	private static final Logger log = LoggerFactory.getLogger( HqlNormalizer.class );

	private final SessionFactoryImplementor sessionFactory;
	private final PersisterReferenceBuilder persisterReferenceBuilder;
	private final PropertyJoinBuilder propertyJoinBuilder;
	private final ErrorHandlerDelegate parseErrorHandler = new ErrorHandlerDelegateImpl();
	private final ASTPrinter printer = new ASTPrinter( HqlNormalizeTokenTypes.class );
	private final ImplicitAliasGenerator aliasBuilder = new ImplicitAliasGenerator();
	private final StatementStack statementStack = new StatementStack();
	private final PathNormalizationStrategyStack pathNormalizationStrategyStack = new PathNormalizationStrategyStack();

	private int traceDepth = 0;

	public HqlNormalizer(SessionFactoryImplementor sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
		this.persisterReferenceBuilder = new PersisterReferenceBuilder( this );
		this.propertyJoinBuilder = new PropertyJoinBuilder( this );
		super.setASTFactory( new ASTFactoryImpl( this ) );
	}

	public ASTPrinter getPrinter() {
		return printer;
	}


	// handle trace logging ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void traceIn(String ruleName, AST tree) {
		if ( inputState.guessing > 0 ) {
			return;
		}
		String prefix = StringHelper.repeat( '-', (traceDepth++ * 2) ) + "-> ";
		String traceText = ruleName + " (" + buildTraceNodeName(tree) + ")";
		trace( prefix + traceText );
	}

	private String buildTraceNodeName(AST tree) {
		return tree == null
				? "???"
				: tree.getText() + " [" + printer.getTokenTypeName( tree.getType() ) + "]";
	}

	public void traceOut(String ruleName, AST tree) {
		if ( inputState.guessing > 0 ) {
			return;
		}
		String prefix = "<-" + StringHelper.repeat( '-', (--traceDepth * 2) ) + " ";
		trace( prefix + ruleName );
	}

	private void trace(String msg) {
		System.out.println( msg );
//		log.trace( msg );
	}


	// overrides of Antlr infastructure methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void reportError(RecognitionException e) {
		getParseErrorHandler().reportError( e );
	}

	public void reportError(String s) {
		getParseErrorHandler().reportError( s );
	}

	public void reportWarning(String s) {
		getParseErrorHandler().reportWarning( s );
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


	// Normalization context ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private PersisterReferenceContext currentPersisterReferenceContext;

	public PersisterReferenceContext getCurrentPersisterReferenceContext() {
		return currentPersisterReferenceContext;
	}

	public PathNormalizationStrategy getCurrentPathNormalizationStrategy() {
		return pathNormalizationStrategyStack.getCurrent();
	}

	protected void pushStatement(AST statementNode) {
		if ( currentPersisterReferenceContext == null ) {
			currentPersisterReferenceContext = new RootPersisterReferenceContext();
		}
		else {
			currentPersisterReferenceContext = new HierarchicalPersisterReferenceContext( currentPersisterReferenceContext );
		}

		pathNormalizationStrategyStack.push( new BasicPathNormalizationStrategySupport( this ) );
		statementStack.push( ( Statement ) statementNode );
	}

	protected void popStatement() {
		statementStack.pop();
		pathNormalizationStrategyStack.pop();
		if ( currentPersisterReferenceContext instanceof HierarchicalPersisterReferenceContext ) {
			currentPersisterReferenceContext = ( ( HierarchicalPersisterReferenceContext ) currentPersisterReferenceContext ).getParent();
		}
		else {
			currentPersisterReferenceContext = null;
		}
	}

	public ImplicitAliasGenerator getAliasBuilder() {
		return aliasBuilder;
	}

	protected String buildUniqueImplicitAlias() {
		return aliasBuilder.buildUniqueImplicitAlias();
	}

	public SessionFactoryImplementor getSessionFactoryImplementor() {
		return sessionFactory;
	}

	public PersisterReferenceBuilder getPersisterReferenceBuilder() {
		return persisterReferenceBuilder;
	}

	public PropertyJoinBuilder getPropertyJoinBuilder() {
		return propertyJoinBuilder;
	}

	public void registerAssociationFetch(Join join) {
		// todo : implement
	}

	public void registerPropertyFetch(PersisterReference persisterReference) {
		// todo : implement
	}


	// semantic action overrides ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	protected void applyCollectionFilter(AST querySpecIn) {
		if ( isSubquery() ) {
			return;
		}

		AST potentialFilterRole = querySpecIn.getFirstChild();
		if ( potentialFilterRole == null || potentialFilterRole.getType() != FILTER ) {
			return;
		}

		final String collectionRole = potentialFilterRole.getText();
		trace( "applying collection filter for role [" + collectionRole + "]" );

		// Remove the FILTER node from the tree as we are about to expand it...
		querySpecIn.setFirstChild( potentialFilterRole.getNextSibling() );

		QueryableCollection persister = ( QueryableCollection ) getSessionFactoryImplementor().getCollectionPersister( collectionRole );

		if ( !persister.getElementType().isEntityType() ) {
			throw new QueryException( "cannot filter collection of values" );
		}

		String collectionElementEntityName = persister.getElementPersister().getEntityName();

		AST persisterSpace = astFactory.create( PERSISTER_SPACE, "filter-persister-space" );
		AST persisterRef = astFactory.create( ENTITY_PERSISTER_REF, "filter-persister-ref" );
		persisterSpace.setFirstChild( persisterRef );
		persisterRef.addChild( astFactory.create( ENTITY_NAME, collectionElementEntityName ) );
		persisterRef.addChild( astFactory.create( ALIAS, "this" ) );
		persisterRef.addChild( astFactory.create( FILTER, collectionRole ) );

		registerPersisterReference( persisterRef );

		AST selectFromNode = querySpecIn.getFirstChild();
		assert selectFromNode.getType() == SELECT_FROM : "mis-structured AST";

		AST fromClauseNode = selectFromNode.getFirstChild();
		if ( fromClauseNode == null ) {
			fromClauseNode = astFactory.create( FROM, "from" );
			selectFromNode.setFirstChild( fromClauseNode );
		}
		else if ( fromClauseNode.getType() == SELECT ) {
			AST tmp = astFactory.create( FROM, "from" );
			tmp.setFirstChild( fromClauseNode );	// really the SELECT clause
			fromClauseNode = tmp;
			selectFromNode.setFirstChild( fromClauseNode );
		}

		assert fromClauseNode.getType() == FROM : "mis-structured AST";
		fromClauseNode.addChild( persisterSpace );  // make it the first child???
	}


	private boolean processingFunction;

	protected void startingFunction() {
		processingFunction = true;
	}

	protected void endingFunction() {
		processingFunction = false;
	}

	public boolean isCurrentlyProcessingFunction() {
		return processingFunction;
	}

 	protected AST normalizeEntityName(AST node) throws SemanticException {
		String entityName = node.getText();
		if ( !isEntityName( entityName ) ) {
			final String importedName = getSessionFactoryImplementor().getImportedClassName( entityName );
			if ( !isEntityName( importedName ) ) {
				throw new NoSuchEntityNameException( node );
			}
			node.setText( importedName );
		}
		return node;
	}

	private boolean isEntityName(String name) {
		try {
			return getSessionFactoryImplementor().getEntityPersister( name ) != null;
		}
		catch ( MappingException me ) {
			return false;
		}
	}

	protected void registerPersisterReference(AST reference) {
		getCurrentPersisterReferenceContext().registerPersisterReference( ( PersisterReference ) reference );
	}

	protected boolean isPersisterReferenceAlias(AST alias) {
		log.trace( "Checking [" + textOrNull( alias ) + "] as persister-ref alias" );
		return getCurrentPersisterReferenceContext().isContainedAlias( alias.getText() );
	}

	protected boolean isCollectionPersisterReferenceAlias(AST alias) {
		return getCurrentPersisterReferenceContext().locatePersisterReferenceByAlias( alias.getText() ).isCollection();
	}


    protected String locateOwningPersisterAlias(AST property) {
		PersisterReference persisterReference = getCurrentPersisterReferenceContext()
				.locatePersisterReferenceExposingProperty( property.getText() );
		return persisterReference == null ? null : persisterReference.getAlias();
	}


	protected AST normalizeQualifiedRoot(AST alias) {
		log.debug( "normalizing path expression root as alias [" + alias.getText() + "]" );
		return pathNormalizationStrategyStack.getCurrent().handleRoot( getCurrentPersisterReferenceContext().locatePersisterReferenceByAlias( alias.getText() ) );
    }

    protected AST normalizeUnqualifiedRoot(AST propertyName) {
		log.debug( "normalizing path expression root as unqualified property [" + propertyName.getText() + "]" );
		PathedPropertyReferenceSource root = pathNormalizationStrategyStack.getCurrent().handleRoot( getCurrentPersisterReferenceContext().locatePersisterReferenceExposingProperty( propertyName.getText() ) );
		return root.handleIntermediatePathPart( ( Ident ) propertyName );
	}

	protected AST normalizeUnqualifiedPropertyReferenceSource(AST propertyName) {
		return pathNormalizationStrategyStack.getCurrent().handleRoot( getCurrentPersisterReferenceContext().locatePersisterReferenceExposingProperty( propertyName.getText() ) );
	}

	protected AST normalizePropertyPathIntermediary(AST source, AST propertyNameNode) {
		log.trace( "normalizing intermediate path expression [" + textOrNull( propertyNameNode ) + "]" );
		return pathNormalizationStrategyStack.getCurrent().handleIntermediatePathPart( ( PathedPropertyReferenceSource ) source, ( Ident ) propertyNameNode );
    }

    protected AST normalizePropertyPathTerminus(AST source, AST propertyNameNode) {
		log.trace( "normalizing terminal path expression [" + textOrNull( propertyNameNode ) + "]" );
		return pathNormalizationStrategyStack.getCurrent().handleTerminalPathPart( ( PathedPropertyReferenceSource ) source, ( Ident ) propertyNameNode );
    }

    protected AST normalizeIntermediateIndexOperation(AST collectionPath, AST selector) throws SemanticException {
		log.trace( "normalizing intermediate index access [" + textOrNull( collectionPath ) + "]" );
		PathedPropertyReferenceSource collectionSource = ( PathedPropertyReferenceSource ) collectionPath.getFirstChild();
		Ident collectionProperty = ( Ident ) collectionSource.getNextSibling();
		return pathNormalizationStrategyStack.getCurrent().handleIntermediateIndexAccess( collectionSource, collectionProperty, ( Node ) selector );
    }

    protected AST normalizeTerminalIndexOperation(AST collectionPath, AST selector) throws SemanticException {
		log.trace( "normalizing terminal index access [" + textOrNull( collectionPath ) + "]" );
		PathedPropertyReferenceSource collectionSource = ( PathedPropertyReferenceSource ) collectionPath.getFirstChild();
		Ident collectionProperty = ( Ident ) collectionSource.getNextSibling();
		return pathNormalizationStrategyStack.getCurrent().handleTerminalIndexAccess( collectionSource, collectionProperty, ( Node ) selector );
    }

	protected AST normalizeIndexOperation(AST collectionPropertyReference, AST selector) throws SemanticException {
		PropertyReference propertyReference = ( PropertyReference ) collectionPropertyReference;
		Type type = propertyReference.getSource().getPropertyType( propertyReference.getPropertyName() );
		if ( !type.isCollectionType() ) {
			throw new DetailedSemanticException( "Not a collection property reference", collectionPropertyReference );
		}

		// todo : validate the selector type against the indexed collection's key type?

		Join join = getPropertyJoinBuilder().buildIndexOperationJoin(
				propertyReference.getSource().locatePersisterReference(),
				propertyReference.getPropertyName(),
				null,
				( Node ) getASTFactory().create( INNER, "inner" ),
				( Node ) selector
		);

		return join.locateRhs();
	}

	protected AST normalizeIndexedRoot(AST root) {
//		return super.normalizeIndexedRoot( alias );
		return root;
	}

	protected void pushFromClausePropertyPathContext(AST joinTypeNode, AST fetch, AST alias, AST propertyFetch) {
		pathNormalizationStrategyStack.push(
				new FromClausePathNormalizationStrategy(
						this,
						resolveJoinType( joinTypeNode ),
						fetch != null,
						propertyFetch != null,
						textOrNull( alias )
				)
		);
	}

	private JoinType resolveJoinType(AST joinType) {
		int joinTypeType = joinType == null ? INNER : joinType.getType();
		switch ( joinTypeType ) {
			case INNER:
				return JoinType.INNER;
			case LEFT:
				return JoinType.LEFT;
			case RIGHT:
				return JoinType.RIGHT;
			case FULL:
				return JoinType.FULL;
		}
		// if no match found, throw exception
		throw new QueryException( "Unrecognized join type [" + joinType.getText() + "]" );
	}

	protected void popFromClausePropertyPathContext() {
		popPathNormalizationContext();
	}

	private PathNormalizationStrategy popPathNormalizationContext() {
		return pathNormalizationStrategyStack.pop();
	}

	protected void pushSelectClausePropertyPathContext() {
		pathNormalizationStrategyStack.push( new SelectClausePathNormalizationStrategy( this ) );
	}

	protected void popSelectClausePropertyPathContext() {
		popPathNormalizationContext();
	}

	protected void pushOnFragmentPropertyPathContext(AST rhsPersisterReference) {
		pathNormalizationStrategyStack.push( 
				new OnFragmentPathNormalizationStrategy( this, ( PersisterReference ) rhsPersisterReference )
		);
	}

	protected AST popOnFragmentPropertyPathContext() {
		OnFragmentPathNormalizationStrategy strategy = ( OnFragmentPathNormalizationStrategy ) popPathNormalizationContext();
		return strategy.getDiscoveredLHS();
	}

	protected void pushWithFragmentPropertyPathContext(AST rhsPropertyReference) {
		PropertyReference propertyReference = ( PropertyReference ) rhsPropertyReference;
		PersisterReference lhs = propertyReference.getSource().locatePersisterReference();
//		PersisterReference rhs = null;
//		pathNormalizationStrategyStack.push(
//				new WithFragmentPathNormalizationStrategy( this, lhs, rhs )
//		);
		pathNormalizationStrategyStack.push( new WithFragmentPathNormalizationStrategy( this, lhs ) );
	}

	protected void popWithFragmentPropertyPathContext() {
		super.popWithFragmentPropertyPathContext();
	}

	protected void applyWithFragment(AST withFragment) {
		( ( WithFragmentPathNormalizationStrategy ) pathNormalizationStrategyStack.getCurrent() ).applyWithFragment( withFragment );
	}

	protected void postProcessQuery(AST querySpec) {
		AST selectFrom = querySpec.getFirstChild();
		assert selectFrom.getType() == SELECT_FROM;

		AST fromClause = selectFrom.getFirstChild();
		assert fromClause.getType() == FROM;

		AST selectClause = fromClause.getNextSibling();
		assert selectClause == null || selectClause.getType() == SELECT;

		if ( selectClause == null || selectClause.getNumberOfChildren() == 0 ) {
			selectClause = generateDerivedSelectClause( fromClause );
			fromClause.setNextSibling( selectClause );
		}

		if ( propertyFetchAliases != null && !propertyFetchAliases.isEmpty() ) {
			AST fetches = astFactory.create( FETCH, "fetch-aliases" );
			Iterator aliases = propertyFetchAliases.iterator();
			while ( aliases.hasNext() ) {
				final String alias = ( String ) aliases.next();
				fetches.addChild( astFactory.create( ALIAS_REF, alias ) );
			}
			selectClause.addChild( fetches );
			propertyFetchAliases.clear();
		}

		// todo : attach mapping-defined order-by fragments
		//		an issue here is that order-by fragments are attached "above" the "query spec" level according to ANSI SQL
		//		for example, an order by is a function of the combined result in the case of a UNION, INTERSECT, etc...

	}

	private AST generateDerivedSelectClause(AST fromClause) {
		AST selectClause = astFactory.create( SELECT, "derived-select-clause" );
		AST selectList = astFactory.create( SELECT_LIST, "derived-select-list" );
		selectClause.setFirstChild( selectList );

		List implicitlySelectedPersisterReferenceAliases =
				getCurrentPersisterReferenceContext().getPersisterReferencesImplicitInDerivedSelectClause();
		assert implicitlySelectedPersisterReferenceAliases != null : "Found no registered implied select references";


		Iterator itr = implicitlySelectedPersisterReferenceAliases.iterator();
		while ( itr.hasNext() ) {
			final PersisterReference persisterReference = ( PersisterReference ) itr.next();
			final String alias = persisterReference.getAlias();
			AST aliasReference = astFactory.create( ALIAS_REF, alias );
			AST selectItem = astFactory.create( SELECT_ITEM, "derived-select-item" );
			selectItem.setFirstChild( aliasReference );
			selectList.addChild( selectItem );
		}

		return selectClause;
	}

	protected void registerSelectItem(AST selectItem) {
		// todo : this was intended to allow for reusing select elements in other clauses, like say an order by
		trace( "registering select item" );
	}

	protected AST buildRootEntityPersisterReference(AST persisterReferenceNode, AST entityName, AST alias, AST filter, AST propertyFetch) {
		PersisterReference ref = ( PersisterReference ) super.buildRootEntityPersisterReference(
				persisterReferenceNode,
				entityName,
				alias,
				filter,
				propertyFetch
		);

		if ( filter != null ) {
			log.trace( "applying filter [" + filter.getText() + ", " + entityName + "]" );
		}

		getCurrentPersisterReferenceContext().registerPersisterReferenceImplicitInDerivedSelectClause( ref );

		return ref;
	}

	private List propertyFetchAliases;

	protected void registerPropertyFetchNode(AST persisterReference) {
		if ( propertyFetchAliases == null ) {
			propertyFetchAliases = new ArrayList();
		}
		propertyFetchAliases.add( ( ( PersisterReference ) persisterReference ).getAlias() );
	}


	// AST output methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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


	private String textOrNull(AST ast) {
		return ast == null ? null : ast.getText();
	}
}
