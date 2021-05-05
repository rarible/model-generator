package ${package}

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
<#list imports![] as import>
    import ${import}
</#list>

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
<#list oneOf![] as type, enum>
    JsonSubTypes.Type(name = "${enum}", value = ${type}::class),
</#list>
)
sealed class ${name} {
<#list fields![] as field>
    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if>
</#list>
}

<#list subclasses![] as sublass>
    class ${sublass.name} (
    <#list sublass.fields![] as field>
        <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if>,
    </#list>
    ) : ${name}()

</#list>

