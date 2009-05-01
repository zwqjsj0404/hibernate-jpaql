/*
 * Copyright (c) 2009, Red Hat Middleware LLC or third-party contributors as
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
package org.hibernate.sql.ast.origin.ordering;

import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.sql.ast.render.OrderByRenderer;

/**
 * Subclassing of the Antlr-generated renderer to apply contextual semantics.
 *
 * @author Steve Ebersole
 */
public class ContextualOrderByRenderer extends OrderByRenderer {
	private final TranslationContext translationContext;

	private final StringTemplate quotedIdentifierTemplate;

	private final StringTemplate basicFunctionNoArgsWithoutParens;
	private final StringTemplate basicFunctionNoArgsWithParens;
	private final StringTemplate basicFunctionWithArgs;

	private final StringTemplate castFunction;
	private final StringTemplate trimfunction;

	public ContextualOrderByRenderer(
			TreeNodeStream input,
			TranslationContext translationContext,
			StringTemplateGroup stringTemplateGroup) {
		super( input );
		setTemplateLib( stringTemplateGroup );
		this.translationContext = translationContext;

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // todo : eventually get these directly from the Dialect
		quotedIdentifierTemplate = buildQuotedIdentiferTemplate();

		basicFunctionNoArgsWithoutParens = new StringTemplate( getTemplateLib(), "<name>" );
		basicFunctionNoArgsWithoutParens.defineFormalArgument( "name" );

		basicFunctionNoArgsWithParens = new StringTemplate( getTemplateLib(), "<name>()" );
		basicFunctionNoArgsWithParens.defineFormalArgument( "name" );

		basicFunctionWithArgs = new StringTemplate( getTemplateLib(), "<name>(<arguments; separator=\", \">)" );
		basicFunctionWithArgs.defineFormalArgument( "name" );
		basicFunctionWithArgs.defineFormalArgument( "arguments" );

		castFunction = new StringTemplate( getTemplateLib(), "cast(<expression> as <datatype>)" );
		castFunction.defineFormalArgument( "expression" );
		castFunction.defineFormalArgument( "datatype" );

		trimfunction = new StringTemplate( getTemplateLib(), "trim(<trimSpec> <trimCharacter> from <trimSource >)" );
		trimfunction.defineFormalArgument( "trimSpec" );
		trimfunction.defineFormalArgument( "trimCharacter" );
		trimfunction.defineFormalArgument( "trimSource" );
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	}

	@Override
    protected StringTemplate quotedIdentifier(CommonToken recognizedIdentifierToken) {
        String identifierText = recognizedIdentifierToken.getText();
        if ( identifierText.startsWith( "`" ) ) {
            identifierText = identifierText.substring( 1, identifierText.length() - 1 );
            StringTemplate template = quotedIdentifierTemplate.getInstanceOf();
            template.setAttribute( "identifier", identifierText );
			return template;
        }
        else {
            return super.quotedIdentifier( recognizedIdentifierToken );
        }
    }

    private StringTemplate buildQuotedIdentiferTemplate() {
		String templateContent = translationContext.getDialect().openQuote() +
				"<identifier>" +
				translationContext.getDialect().closeQuote();
        StringTemplate template = new StringTemplate( getTemplateLib(), templateContent );
        template.defineFormalArgument( "identifier" );
        return template;
    }

	@Override
	protected StringTemplate basicFunctionTemplate(String functionName, List arguments) {
		if ( arguments == null || arguments.isEmpty() ) {
			SQLFunction sqlFunction = translationContext.getSqlFunctionRegistry().findSQLFunction( "functionName" );
			StringTemplate template = sqlFunction.hasParenthesesIfNoArguments()
					? basicFunctionNoArgsWithParens
					: basicFunctionNoArgsWithoutParens;
			template.setAttribute( "name", functionName );
			return template;
		}
		else {
			StringTemplate template = basicFunctionWithArgs.getInstanceOf();
			template.setAttribute( "name", functionName );
			template.setAttribute( "arguments", arguments );
			return template;
		}
	}

	@Override
	protected StringTemplate castFunctionTemplate(StringTemplate expression, String datatype) {
		StringTemplate template = castFunction.getInstanceOf();
		template.setAttribute( "expression", expression );
		template.setAttribute( "datatype", datatype );
		return template;
	}

	@Override
    protected StringTemplate trimFunctionTemplate(StringTemplate trimSpec, StringTemplate trimCharacter, StringTemplate trimSource) {
		StringTemplate template = trimfunction.getInstanceOf();
		template.setAttribute( "trimSpec", trimSpec );
		template.setAttribute( "trimCharacter", trimCharacter );
		template.setAttribute( "trimSource", trimSource );
		return template;
    }
}
