package ${package}

<#list imports![] as import>
    import ${import}
</#list>

class ${name} (
<#list fields![] as field>
    <#if field.abstract>abstract </#if><#if field.overriden>override </#if>val ${field.name} : ${field.type}<#if !field.required>?</#if>,
</#list>
)
