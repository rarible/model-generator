package ${package}

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
<#list imports![] as import>
    import ${import}
</#list>

<#if discriminatorField = "@type">
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
<#else>
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "${discriminatorField}", visible=true)
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
    class ${sublass.name} (
    <#list sublass.fields![] as field>
        <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if>,
    </#list>
    ) : ${name}()

</#list>
<#if enums?has_content>
    <#list enums![] as enum>
        enum class ${enum.name} {
        <#list enum.values![] as value>
            ${value},
        </#list>
        }

    </#list>
</#if>
