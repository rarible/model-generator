<#macro classFieldConstraints field>
    <#if field.minimum?has_content>
        <#lt>   @field:Min(${field.minimum})
    </#if><#if field.maximum?has_content>
        <#lt>   @field:Max(${field.maximum})
    </#if>
</#macro>
package ${package}

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
<#if hasConstraints>
    import javax.validation.constraints.Min
    import javax.validation.constraints.Max
</#if><#rt>

<#list imports![] as import>
    <#lt>import ${import}
</#list>

<#if discriminatorField = "@type">
    <#lt>@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
<#else>
    <#lt>@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "${discriminatorField}")
</#if>
@JsonSubTypes(
<#list oneOf![] as type, enum>
    JsonSubTypes.Type(name = "${enum}", value = ${type}::class)<#if type?has_next>,</#if>
</#list>
)
sealed class ${simpleClassName} {
<#list fields![] as field><#rt>
    <@classFieldConstraints field=field />
    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if><#if !field.abstract><#if field.defaultValue?has_content> = ${field.defaultValue}</#if></#if>
</#list>
<#list enums![] as enum>

    <#lt>    enum class ${enum.name} {
    <#list enum.values![] as value>
        <#lt>        ${value}<#if value?has_next>,</#if>
    </#list>
    <#lt>    }

</#list>
}
<#list subclasses![] as subclass>

    <#lt>//--------------- ${subclass.simpleClassName} ---------------//
    <#if subclass.subclasses?has_content>
        <#lt>sealed class ${subclass.simpleClassName} : ${simpleClassName}() {
        <#list subclass.fields![] as field><#rt>
            <@classFieldConstraints field=field />
            <#lt>    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if><#if !field.abstract><#if field.defaultValue?has_content> = ${field.defaultValue}</#if></#if>
        </#list>
        <#list subclass.enums![] as enum>

            <#lt>    enum class ${enum.name} {
            <#list enum.values![] as value>
                <#lt>        ${value}<#if value?has_next>,</#if>
            </#list>
            <#lt>    }

        </#list>
        <#lt>}

        <#list subclass.subclasses![] as subsubclass>
            <#lt><#if subsubclass.fields?has_content>data </#if>class ${subsubclass.simpleClassName} (
            <#list subsubclass.fields![] as field><#rt>
                <@classFieldConstraints field=field />
                <#lt>    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if><#if !field.abstract><#if field.defaultValue?has_content> = ${field.defaultValue}</#if></#if><#if field?has_next>,</#if>
            </#list>
            <#lt>) : ${subclass.simpleClassName}() <#if subsubclass.enums?has_content> {

            <#list subsubclass.enums![] as enum>
                <#lt>    enum class ${enum.name} {
                <#list enum.values![] as value>
                    <#lt>        ${value}<#if value?has_next>,</#if>
                </#list>
                <#lt>    }

            </#list>
            <#lt>}

        <#else>


        </#if>
        </#list>
    <#else>
        <#lt><#if subclass.fields?has_content>data </#if>class ${subclass.simpleClassName} (
        <#list subclass.fields![] as field><#rt>
            <@classFieldConstraints field=field />
            <#lt>    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?<#if !field.abstract><#if field.defaultValue?has_content> = ${field.defaultValue}</#if></#if></#if><#if field?has_next>,</#if>
        </#list>
        <#lt>) : ${simpleClassName}()<#if subclass.enums?has_content> {

        <#list subclass.enums![] as enum>
            <#lt>    enum class ${enum.name} {
            <#list enum.values![] as value>
                <#lt>        ${value}<#if value?has_next>,</#if>
            </#list>
            <#lt>    }

        </#list>
        <#lt>}
    </#if>
    </#if>

</#list>
