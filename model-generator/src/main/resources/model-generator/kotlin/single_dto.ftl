package ${package}

<#list imports![] as import>
    <#lt>import ${import}
</#list>

data class ${simpleClassName} (
<#list fields![] as field>
    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if><#if field?has_next>,</#if>
</#list>
) <#if enums?has_content>{

    <#list enums![] as enum>
        <#lt>    enum class ${enum.name} {
        <#list enum.values![] as value>
            <#lt>        ${value}<#if value?has_next>,</#if>
        </#list>
        <#lt>    }

    </#list>
    <#lt>}
</#if>
