# model-generator

Model generator is an untility for generating DTO classes uses OpenAPI schema as model definition.

## Usage


```xml
<plugin>
    <groupId>com.rarible.protocol</groupId>
    <artifactId>model-generator-maven-plugin</artifactId>

    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>

    <configuration>
        <tasks>
            <task>
                <schema>
                    <type>openapi</type>
                    <fileName>${basedir}/src/main/resources/openapi.yaml</fileName>
                </schema>

                <providedTypesFile>${basedir}/src/main/resources/model-generator/kotlin/provided.json</providedTypesFile>
                <primitiveTypesFile>${basedir}/src/main/resources/model-generator/kotlin/primitives.json</primitiveTypesFile>

                <generator>
                    <lang>kotlin</lang>
                    <properties>
                        <property>
                            <name>withInheritance</name>
                            <value>true</value>
                        </property>
                        <property>
                            <name>packageName</name>
                            <value>com.rarible.protocol.dto</value>
                        </property>
                    </properties>
                </generator>
                
                <dependencies>
                    <dependency>
                        <name>some-external-model</name>
                        <properties>
                            <property>
                                <name>packageName</name>
                                <value>com.rarible.protocol.dto</value>
                            </property>
                        </properties>
                    </dependency>
                </dependencies>
                
            </task>
        </tasks>
    </configuration>
</plugin>
            
```            
