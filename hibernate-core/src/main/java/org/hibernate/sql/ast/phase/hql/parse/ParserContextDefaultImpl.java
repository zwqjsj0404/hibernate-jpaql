package org.hibernate.sql.ast.phase.hql.parse;

import java.util.ArrayList;
import java.util.List;

public class ParserContextDefaultImpl implements ParserContext {

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

}
