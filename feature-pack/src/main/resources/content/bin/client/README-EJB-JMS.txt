jboss-client.jar is a combined client jar for JBoss AS7, for use in non-maven environments. This jar should be used
with standalone clients only, not with deployments are that deployed to an AS7 instance.

This jar contains the classes required for remote JMS and EJB usage, and consists of the following shaded artifacts:

org.jboss.spec.javax.jms:jboss-jms-api_2.0_spec
org.jboss.spec.javax.transaction:jboss-transaction-api_1.1_spec
org.jboss.spec.javax.ejb:jboss-ejb-api_3.2_spec

io.netty:netty-all
org.jboss:jboss-ejb-client
org.jboss:jboss-remote-naming
org.jboss.logging:jboss-logging
org.jboss.marshalling:jboss-marshalling
org.jboss.marshalling:jboss-marshalling-river
org.jboss.remoting:jboss-remoting
org.jboss.remotingjmx:remoting-jmx
org.jboss.sasl:jboss-sasl
org.jboss.xnio:xnio-api
org.jboss.xnio:xnio-nio
org.apache.activemq:artemis-commons
org.apache.activemq:artemis-core-client
org.apache.activemq:artemis-jms-client


Maven users should not use this jar, but should use the following BOM dependencies instead

    <dependencies>
        <dependency>
            <groupId>org.jboss.as</groupId>
            <artifactId>jboss-as-ejb-client-bom</artifactId>
            <type>pom</type>
        </dependency>
        <dependency>
            <groupId>org.jboss.as</groupId>
            <artifactId>jboss-as-jms-client-bom</artifactId>
            <type>pom</type>
        </dependency>
    </dependencies>

This is because using maven with a shaded jar has a very high chance of causing class version conflicts, which is why
we do not publish this jar to the maven repository.
