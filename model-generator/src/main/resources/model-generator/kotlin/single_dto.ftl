package ${package}

<#list imports![] as import>
    <#lt>import ${import}
</#list>

data class ${simpleClassName} (
<#list fields![] as field>
    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if>,
</#list>
) <#if enums?has_content>{

    <#list enums![] as enum>
        <#lt>    enum class ${enum.name} {
        <#list enum.values![] as value>
            <#lt>        ${value},
        </#list>
        <#lt>    }

    </#list>
    <#lt>}
</#if>
