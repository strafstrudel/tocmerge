# tocmerge

Goal
====

This project is an attempt to develop an algorithm for automatically merging
the tables of contents of two texts of comparable subject matter.  

For validation of the approach, we chose two online introductions to molecular 
biology, found here:

* http://www.web-books.com/MoBio/
* https://en.wikibooks.org/wiki/An_Introduction_to_Molecular_Biology

Approach
========

Text representation
-------------------

The first task is to extract the text contents from the respective websites
and translate them into a machine-processable model that captures the relevant
structure of the texts.  Texts are represented as a table of contents, which
is a nested topic structure preserving the levels and ordering of the original.
Each topic in a table of contents is associated with a section of the text
indexed by that topic.  The model is represented in RDF using a simple vocabulary
and stored in an in-memory triple store (Apache Jena) that can be exposed to
query via SPARQL.

The matching task
-----------------

Given the RDF representations of the two texts, the next task is to attempt to
interleave their tables of contents into a third, synthetic TOC that is, along
some dimension, more "informative" than either of the separate TOCs combined.

The basic mathematical form of the task is clear enough.  Given TOCs _T1_ and _T2_,
synthesize a third TOC _T3_ into which _T1_ and _T2_ are mapped.  This mapping takes
the form of two homomorphisms _h1 : T1 -> T3_ and _h2 : T2 -> T3_.  

The functions _h1_and _h2_ cannot in general be simple mappings from source to target 
topics.  For example, the first source contains a topic "Protein Structure and Function"
where the second divides the discussion between Proteins (organized under Macromolecules) 
and the top-level topic "Function and Structure of Proteins".  Given the early introduction
of the topic of Proteins in the second source, one might expect it to be an introduction
to be filled in in the later section, but it is unclear how one might automatically 
induce this relation and even less clear that it is a good idea to merge it at all.  Some
of the effects noted that would need to be dealt with by any adequate approach would be:

* Granularity - One text might contain only a cursory discussion of a topic which is covered
in depth in a second text
* Ordering - One assumes that the presentation order of topics in a text is cumulative, i.e.,
topics are introduced in relatively simple language early on and later referenced and/or
elaborated on
* Context effects - The first source is ordered traditionally as an exposition of structure
followed by a discussion of the functions of the entities described earlier.  The second
source, by contrast, focuses on a functional view, introducing notions of structure as 
required.

__MORE TO COME__
