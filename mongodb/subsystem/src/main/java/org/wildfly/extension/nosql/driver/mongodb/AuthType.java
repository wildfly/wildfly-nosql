package org.wildfly.extension.nosql.driver.mongodb;

/**
 * AuthType
 *
 * @author Scott Marlow
 */
public enum AuthType {
    GSSAPI,
    PLAIN_SASL,
    SCRAM_SHA_1,
    MONGODB_CR,
    MONGODB_X509,
    DEFAULT     // let the MongoDB server choose the best authentication method
}
