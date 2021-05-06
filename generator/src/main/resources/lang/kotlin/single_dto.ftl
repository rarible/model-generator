package ${package}

<#list imports![] as import>
    import ${import}
</#list>

data class ${name} (
<#list fields![] as field>
    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if>,
</#list>
) <#if enums?has_content>{

    <#list enums![] as enum>
        enum class ${enum.name} {
        <#list enum.values![] as value>
            ${value},
        </#list>
        }

    </#list>
    }
</#if>
