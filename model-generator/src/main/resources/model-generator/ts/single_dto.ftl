<#if imports?has_content>
    <#list imports![] as import, file>
        <#lt>import {${import}} from "./${file}";
    </#list>

</#if>
<#if enumValues?has_content>
    <#lt>export type ${simpleClassName} =
    <#list enumValues![] as enumValue>
        <#lt>	"${enumValue}"<#if enumValue?has_next> |</#if>
    </#list>
<#else>
    <#lt>export type ${simpleClassName} = {
    <#list fields![] as field>
        <#lt>    ${field.name}<#if !field.required>?</#if>: ${field.type}
    </#list>
    <#lt>}

    <#if enums?has_content>
        <#list enums![] as enum>
            <#lt>export type ${enum.name} = <#list enum.values![] as value>"${value}"<#if value?has_next> | </#if></#list>
        </#list>
    </#if>
</#if>
