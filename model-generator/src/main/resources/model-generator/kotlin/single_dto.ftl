<#macro classFields data>
    <#list data as field><#rt>
        <#if field.minimum?has_content>
            <#lt>   @field:Min(${field.minimum})
        </#if><#if field.maximum?has_content>
        <#lt>   @field:Max(${field.maximum})
    </#if>
        <#lt>   <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if><#if field.defaultValue?has_content> = ${field.defaultValue}</#if><#if field?has_next>,</#if>
    </#list>
</#macro>
package ${package}

<#if hasConstraints>
import javax.validation.constraints.Min
import javax.validation.constraints.Max
</#if><#rt>
<#list imports![] as import>
    <#lt>import ${import}
</#list>

<#if enumValues?has_content>
    enum class ${simpleClassName} {
    <#list enumValues![] as enumValue>
        ${enumValue}<#if enumValue?has_next>,</#if>
    </#list>
    }
<#else>
    <#lt>data class ${simpleClassName} (
        <@classFields data=fields />
    <#lt>) <#if enums?has_content>{

    <#list enums![] as enum>
        <#lt>    enum class ${enum.name} {
        <#list enum.values![] as value>
            <#lt>        ${value}<#if value?has_next>,</#if>
        </#list>
        <#lt>    }

    </#list>
    <#lt>}
</#if>
</#if>
