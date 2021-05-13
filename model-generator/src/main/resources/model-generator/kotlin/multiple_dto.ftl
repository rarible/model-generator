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

<#list subclasses![] as subclass>
    <#lt>//--------------- ${subclass.name} ---------------//
    <#if subclass.subclasses?has_content>
        <#lt>sealed class ${subclass.name} : ${name}() {
        <#list subclass.fields![] as field>
            <#lt>    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if>
        </#list>
        <#lt>}

        <#list subclass.subclasses![] as subsubclass>
            <#lt>class ${subsubclass.name} (
            <#list subsubclass.fields![] as field>
                <#lt>    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if>,
            </#list>
            <#lt>) : ${subclass.name}() <#if subsubclass.enums?has_content> {

            <#list subsubclass.enums![] as enum>
                <#lt>    enum class ${enum.name} {
                <#list enum.values![] as value>
                    <#lt>        ${value},
                </#list>
                <#lt>    }

            </#list>
            <#lt>}

        <#else>


        </#if>
        </#list>
    <#else>
        <#lt>class ${subclass.name} (
        <#list subclass.fields![] as field>
            <#lt>    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if>,
        </#list>
        <#lt>) : ${name}()<#if subclass.enums?has_content> {

        <#list subclass.enums![] as enum>
            <#lt>    enum class ${enum.name} {
            <#list enum.values![] as value>
                <#lt>        ${value},
            </#list>
            <#lt>    }

        </#list>
        <#lt>}
    </#if>
    </#if>

</#list>
