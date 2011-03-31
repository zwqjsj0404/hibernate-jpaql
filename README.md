# Hibernate ANTLR parser
Hibernate uses ANTLR to parse the Hibernate Query Language and JPA queries.
The stable parser being used by Hibernate 3.6 and 4 lives in the hibernate-core module, this is a temporary spin-off to update and refactor it independently:

* re-shape as an independent module
* update to ANTLR3 usage, more literature and tools are available
* reuse the common grammar defining the HQL language to make queries on non-SQL storage engines (see the Hibernate OGM project)
* eventually make it possible to be merged back in the Hibernate core, or have it depend on this module

## Rebase warning
This is meant as a temporary repository to ease quick prototyping cycles, and is started from an import of the old ANTLR3 branch which was not ported from SVN when Hibernate moved to github.

Original code location:
http://anonsvn.jboss.org/repos/hibernate/core/branches/antlr3

In early days we might need to rewrite history to fix importing issues, might be important to keep it easy to eventually merge back in hibernate-core.
Contributions are much appreciated, but if you clone this make sure to get in touch so that we can discuss eventual needs of rebasing.

## Feedback
Please provide feedback using the mailing list or IRC.
http://hibernate.org/community/mailinglists
http://hibernate.org/community/irc
