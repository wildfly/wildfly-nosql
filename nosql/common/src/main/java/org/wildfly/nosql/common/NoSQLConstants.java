package org.wildfly.nosql.common;

/**
 * NoSQLConstants common shared NoSQL constants
 *
 * @author Scott Marlow
 */
public class NoSQLConstants {

    // MongoDB constants
    public static String MONGOCLIENTCLASS = "com.mongodb.MongoClient";
    public static final String MONGOCLIENTOPTIONSCLASS = "com.mongodb.MongoClientOptions";
    public static final String MONGODATABASECLASS = "com.mongodb.client.MongoDatabase";
    public static final String MONGOBUILDERCLASS = "com.mongodb.MongoClientOptions$Builder";
    public static final String MONGOWRITECONCERNCLASS = "com.mongodb.WriteConcern";
    public static final String MONGOSERVERADDRESSCLASS = "com.mongodb.ServerAddress";
    // MongoDB related constants
    public static final String MONGOCDIEXTENSIONMODULE = "org.wildfly.extension.nosql.mongodb";
    public static final String MONGOCDIEXTENSIONCLASS = "org.wildfly.extension.nosql.cdi.MongoExtension";
}
