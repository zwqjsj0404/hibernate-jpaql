/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
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
package org.hibernate.sql.ast.alias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.persister.entity.Queryable;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.dialect.Dialect;
import org.hibernate.util.StringHelper;

/**
 * Defines a termplated implementation of the {@link TableAliasGenerator} contract.
 * <p/>
 * The variance is in the subclass implementation of the {@link #truncateAliasBase} method.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractTableAliasGeneratorTemplate implements TableAliasGenerator {
	private static final Logger log = LoggerFactory.getLogger( TableAliasGenerator.class );

	private final Dialect dialect;
	private int uniqueingValue = 0;

	protected AbstractTableAliasGeneratorTemplate(Dialect dialect) {
		this.dialect = dialect;
	}

	/**
	 * Truncate down the base of the sql alias root to the 'totalAllowableSizeOfAliasBase'.
	 * <p/>
	 * This abstract method provides the variance in the templating routine; different implementations will
	 * define this extact behavior differently.
	 *
	 * @param base The base for the alias root.
	 * @param totalAllowableSizeOfAliasBase The total allowable size of the base after truncating.
	 *
	 * @return The appropriately truncated base.
	 */
	protected abstract String truncateAliasBase(String base, int totalAllowableSizeOfAliasBase);

	/**
	 * {@inheritDoc}
	 */
	public final TableAliasRoot generateSqlAliasRoot(Queryable persister, String sourceAlias) {
		log.trace( "Generating SQL alias root (entity) : " +  sourceAlias );
		String base = sourceAlias;
		if ( ImplicitAliasGenerator.isImplicitAlias( sourceAlias ) ) {
			base = persister.getMappedTableMetadata().getSqlAliasRootBase();
		}
		return generateSqlAliasRoot( base, determineMappedTableCount( persister ) );
	}

	/**
	 * {@inheritDoc}
	 */
	public final TableAliasRoot generateSqlAliasRoot(QueryableCollection persister, String sourceAlias) {
		log.trace( "Generating SQL alias root (collection) : " +  sourceAlias );
		String base = sourceAlias;
		if ( ImplicitAliasGenerator.isImplicitAlias( sourceAlias ) ) {
			base = StringHelper.unqualify( persister.getName() ).toLowerCase();
		}
		return generateSqlAliasRoot( base, determineMappedTableCount( persister ) );
	}

	private int determineMappedTableCount(Queryable persister) {
		return persister.getMappedTableMetadata().getJoinedTables().length + 1;
	}

	private int determineMappedTableCount(QueryableCollection persister) {
		if ( !persister.getElementType().isAssociationType() ) {
			return 1;
		}
		else {
			return determineMappedTableCount( ( Queryable ) persister.getElementPersister() );
		}
	}

	protected final TableAliasRoot generateSqlAliasRoot(String base, int tableCount) {
		base = cleanBase( base );
		base = ensureAliasCapacity( base, tableCount );
		return new TableAliasRoot( base );
	}

	private String cleanBase(String base) {
		base = base.toLowerCase()
		        .replace( '/', '_' )
				.replace( '$', '_' );

		char[] chars = base.toCharArray();
		if ( !Character.isLetter( chars[0] ) ) {
			for ( int i = 1; i < chars.length; i++ ) {
				if ( Character.isLetter( chars[i] ) ) {
					base = base.substring( i );
				}
			}
		}

		if ( Character.isDigit( base.charAt( base.length() - 1 ) ) ) {
			base = base.substring( 0, base.length() - 1 ) + 'x';
		}

		return base;
	}

	private String ensureAliasCapacity(String base, int mappedTableCount) {
		// we need to consider the max-alias-length reported by the dialect against the
		// size of the incoming base + the number of mapped tables
		int nextUniqueingValueSize = Integer.toString( uniqueingValue + 1 ).length();
		int totalAllowableSizeOfAliasBase = dialect.getMaxAliasLength() - nextUniqueingValueSize;
		if ( mappedTableCount > 1 ) {
			totalAllowableSizeOfAliasBase-= Integer.toString( mappedTableCount ).length();
		}
		return buildUniqueAliasBase( truncateAliasBase( base, totalAllowableSizeOfAliasBase ) );
	}

	private String buildUniqueAliasBase(String base) {
		return base + uniqueInteger() + '_';
	}

	private int uniqueInteger() {
		return uniqueingValue++;
	}
}
