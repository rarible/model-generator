package ${package}

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
<#list imports![] as import>
    <#lt>import ${import}
</#list>

<#if discriminatorField = "@type">
    <#lt>@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
<#else>
    <#lt>@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "${discriminatorField}", visible=true)
</#if>
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
    <#lt>class ${sublass.name} (
    <#list sublass.fields![] as field>
        <#lt>    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if>,
    </#list>
    <#lt>) : ${name}()

</#list>
<#if enums?has_content>
    <#list enums![] as enum>
        <#lt>enum class ${enum.name} {
        <#list enum.values![] as value>
            <#lt>    ${value},
        </#list>
        <#lt>}

    </#list>
</#if>
