package ${package}

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
    <#list fields![] as field>
        <#lt>    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if><#if field?has_next>,</#if>
    </#list>
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
