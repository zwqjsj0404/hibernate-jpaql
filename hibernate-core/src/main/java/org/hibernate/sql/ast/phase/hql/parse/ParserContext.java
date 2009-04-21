package org.hibernate.sql.ast.phase.hql.parse;

import java.util.List;

public interface ParserContext {

	public boolean isJavaConstant(String text);

	public boolean isEntityName(String text);

	public List getEntityImplementors(String text);
	
	public String buildUniqueImplicitAlias();
}
