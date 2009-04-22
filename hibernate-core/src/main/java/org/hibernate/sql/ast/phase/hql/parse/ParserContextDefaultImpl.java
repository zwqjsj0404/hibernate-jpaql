package org.hibernate.sql.ast.phase.hql.parse;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sql.ast.alias.ImplicitAliasGenerator;

public class ParserContextDefaultImpl implements ParserContext {

	private final ImplicitAliasGenerator implicitAliasGenerator = new ImplicitAliasGenerator(); 
	
	public List getEntityImplementors(String text) {
		List implementors = new ArrayList();
		implementors.add(text);
		return implementors;
	}

	public boolean isEntityName(String text) {
		return true;
	}

	public boolean isJavaConstant(String text) {
		return false;
	}

	public String buildUniqueImplicitAlias() {
		return implicitAliasGenerator.buildUniqueImplicitAlias();
	}

}
