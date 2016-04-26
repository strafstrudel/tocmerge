package com.example.tocmerge.vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class TEXT {
	
	public static final String NAMESPACE = "http://example.com/tocmerge/text#";

    protected static final Resource resource( String local )
        { return ResourceFactory.createResource( NAMESPACE + local ); }

    protected static final Property property( String local )
        { return ResourceFactory.createProperty( NAMESPACE, local ); }

    public static final Resource Text    = resource("Text");
    public static final Resource Topic   = resource("Topic");
    public static final Resource TOC     = resource("TOC");
    public static final Resource Section = resource("Section");
    
    public static final Property tableOfContents = property("tableOfContents");
    public static final Property subTopicOf      = property("subTopicOf");    
    public static final Property topicOrdinal    = property("topicOrdinal");
    public static final Property topicLabel      = property("topicLabel");
    
    public static final Property subSectionOf    = property("subSectionOf");
    public static final Property sectionText     = property("sectionText");
    public static final Property sectionOrdinal  = property("sectionOrdinal");
    public static final Property sectionTopic    = property("sectionTopic");
       
}
