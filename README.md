# model-generator

Model generator is an untility for generating DTO classes uses OpenAPI schema as model definition.

## Description
The purpose of this plugin is to generate model (DTO) classes for various languages (ATM only Kotlin is supported). Sources are generated based on OpenAPI description in .yaml file.

[OpenAPI Specification](https://spec.openapis.org/oas/v3.0.1)

## Usage

### Resources

For sources generation plugin uses next files by default (specified relatively to project root folder):
```
/openapi.yaml
/src/main/resources/model-generator/${lang}/primitives.json
/src/main/resources/model-generator/${lang}/provided.json
```
All of them are optional, even **openapi.yaml** (for cases when you want to build merged .yaml from external dependencies). If you don't want to use default structure, you can place files wherever you want, but in such case you have to specify thier paths explicitely in plugin conviguration.

### Type definitions
Each language uses different names for primitive types, such as String or Integer. And all of such types, used in your specification, must be specified in **primitivies.json**. For Kotlin plugin already has next types (you can override any of them in your project):
```js
{
  "map": "kotlin.collections.Map",
  "array": "kotlin.collections.List",
  "boolean": "java.lang.Boolean",
  "string": "kotlin.String",
  "byte": "kotlin.Byte",
  "integer": "kotlin.Int",
  "integer:int32": "kotlin.Int",
  "integer:int64": "kotlin.Long",
  "number:float": "kotlin.Float",
  "number:double": "kotlin.Double",
  "string:byte": "kotlin.Byte",
  "string:date-time": "java.time.Instant",
  "string:password": "kotlin.String"
}
```
For primitives type mapping based on Openapi **type** and **format** (optional) properties. In file such types should be specified with semicolon separator: **type:format**.

Provided types are types taken from existing classes - from dependencies or from your project. They should be defined in **provided.json**. Next types included by default in model-generator for Kotlin:
```js
{
  "BigInteger": "java.math.BigInteger",
  "BigDecimal": "java.math.BigDecimal",
  "Address": "scalether.domain.Address",
  "Word": "io.daonomic.rpc.domain.Word",
  "Binary": "io.daonomic.rpc.domain.Binary"
}
```
### Model generation

Minimalistic plugin configuration presented below - in such case plugin suggest all necessary resources placed in default folders.
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
        <!-- Type of schema, ATM only OpenAPI supported -->
        <schema>
            <type>openapi</type>
        </schema>
        
        <!-- Set of model generators for variuos languages-->
        <generators>
            <generator>
                <!-- Language -->
                <lang>kotlin</lang>
                <!-- Package name for generated model classes -->
                <packageName>com.model.test</packageName>
            </generator>
        </generators>
    </configuration>   
    
</plugin>            
```            

Full <schema> configuration with default settings:
```xml
<schema>
    <type>openapi</type>    
    <inputFile>${baseDir}/openapi.yaml</inputFile>
    <!-- If your schema depends on other artifacts,
         plugin will merge specifications in single file and  
         embed it into result jar -->
    <outputFile>${baseDir}/target/classes/openapi.yaml</outputFile>
</schema>
```    
    
Full <generator> configuration with default settings:  
```xml
<generator>    
    <lang>kotlin</lang>
    <packageName>com.model.test</packageName>
    <primitiveTypesFile>${baseDir}/src/main/resources/model-generator/{lang}/primitives.json</primitiveTypesFile>
    <providedTypesFile>${baseDir}/src/main/resources/model-generator/{lang}/provided.json</providedTypesFile>
    <modelOutputDirectory>${baseDir}/target/generated-sources/rarible/src/main/{lang}</modelOutputDirectory>
</generator>
```
    
### Dependencies
You can split your schema into several projects and then build resulting model from set of atrtifacts. For example, there are several modules with OpenAPI specification:
1. model-common
2. service-model   
    
Lets assume **service-model**  depends from **model-common**. Specification of this module is next:
```yaml
openapi: 3.0.1
info:
paths:

components:
  schemas:
    Model:
      required:
        - id
        - name
      type: object
      properties:
        id:
          type: integer
          format: int32
        name:
          type: string
```
Since **service-model** depends from **model-common** we can use all component definition in **service-model** specification:
```yaml
openapi: 3.0.1
info:
paths:

components:
  schemas:
    ServiceModel:
      required:
        - id
        - model
      type: object
      properties:
        id:
          type: integer
        model:
          $ref: '#/components/schemas/Model'
```
    
In such case you need to extend your plugin configuration:    
```xml
<plugin>
    <groupId>com.rarible.protocol</groupId>
    <artifactId>model-generator-maven-plugin</artifactId>
    <configuration>
        
        <schema>
            <type>openapi</type>
        </schema>
        
        <dependencies>
            <dependency>
                <!-- Name of dependency should be included into resulting specification -->
                <name>model-common</name>
                <!-- Package with model classes in this dependecy -->
                <packageName>com.model.test</packageName>
            </dependency>
        </dependencies>
        
        <generators>
            <generator>
                <lang>kotlin</lang>
                <packageName>com.model.test</packageName>
            </generator>
        </generators>
        
    </configuration>   
    
</plugin>            
```          
**NOTE**: all primitive and provided types specified in dependencies will be available for current project, so you don't need to configure same types for different modules, if they uses dependency with such type definitions.
    
### Paths processing
During merge plugin put all **paths** and **components** into single file. In some cases it requires to rename API paths in result file. You can do it for all schemas, used by your project individually:
    
```xml
<plugin>
    <groupId>com.rarible.protocol</groupId>
    <artifactId>model-generator-maven-plugin</artifactId>
    <configuration>
        <pathProcessor>
            <!-- path regex and replacement for specification from current project -->
            <apiPathReplacementRegex>/v0.1</apiPathReplacementRegex>
            <apiPathReplacement>/staging</apiPathReplacement>    
            <!-- template for new path, you can use {originalPath} placeholder here -->
            <template>/protocol/api/v0.1{originalPath}</template>
        </pathProcessor>
        <dependencies>
            <dependency>
                <name>model-common</name>
                <packageName>${generator.package}</packageName>
                <!-- And in same way you can do it for each dependency -->
                <pathProcessor>
                    <apiPathReplacement>/staging</apiPathReplacement>
                    <apiPathReplacementRegex>/v0.1</apiPathReplacementRegex>
                    <template>/protocol/api/common/v0.1{originalPath}</template>
                </pathProcessor>
            </dependency>
        </dependencies>
    </configuration>
</plugin>   
 ```

    
