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
 */
package org.hibernate.loader;

/**
 * Defines the column names/aliases we can use to read selected values correlating to various part of a collection from
 * the result set.  For example, {@link #getSuffixedKeyAliases()} identifies the column/alias which represent the
 * values for the collection key (the values mapped using &lt;key/&gt; in the metadata).
 *
 * @author Steve Ebersole
 */
public interface CollectionAliases {
	/**
     * Retrieve the aliases to the collection key values (i.e., the <tt>FK</tt> to its owner).
	 *
	 * @return The collection key result-set column aliases.
	 */
	public String[] getSuffixedKeyAliases();

	/**
     * Retrieve the aliases to the "collection identifer", which is the PK of the entity which owns this collection.
     * Can be different from {@link #getSuffixedKeyAliases()} in the case of &lt;property-ref/&gt;, though usually these
     * will be the same values.
	 *
	 * @return The collection identifier result-set column aliases.
	 */
	public String getSuffixedIdentifierAlias();

	/**
     * Retrieve the aliases to the collection index values (pertinent only for indexed collections like
     * {@link java.util.Map} and {@link java.util.List}).
	 *
	 * @return The collection index result-set column aliases.
	 */
	public String[] getSuffixedIndexAliases();

	/**
     * Retrieve the aliases to the values defining the collection values.  The "values" of a collection of different
     * depending on the type of elements defined in the metadata (is it a value colletion or an entity collection)<ul>
     * <li><b>&lt;elements&gt;</b> - represents a colletion of values (e.g. Strings); the aliases here would represent
     * the columns making up the element values</li>
     * <li><b>&lt;one-to-many&gt;</b> - ???</li>
     * <li><b>&lt;many-to-many&gt;</b> - the aliases here would represent the values from the association table which
     * map to the related element entity's; normally this would be the same as the element entity's PK value but could
     * be different in the case of a &lt;property-ref/&gt;.
	 *
	 * @return The collection element result-set column aliases.
	 */
	public String[] getSuffixedElementAliases();

	/**
	 * Returns the suffix used to unique the column aliases for this particular alias set.
	 *
	 * @return The uniqued column alias suffix.
	 */
	public String getSuffix();
}
