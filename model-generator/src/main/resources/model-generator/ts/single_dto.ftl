<#if imports?has_content>
    <#list imports![] as import, file>
        <#lt>import {${import}} from "${file}";
    </#list>

</#if>
<#if enumValues?has_content>
    <#lt>export enum ${simpleClassName} {
    <#list enumValues![] as value>
        <#lt>    ${value} = "${value}"<#if value?has_next>,</#if>
    </#list>
    }
<#else>
    <#lt>export type ${simpleClassName} = {
    <#list fields![] as field>
        <#lt>    ${field.name}<#if !field.required>?</#if>: ${field.type}
    </#list>
    <#lt>}

    <#if enums?has_content>
        <#list enums![] as enum>
            <#lt>export enum ${enum.name} {
            <#list enum.values![] as value>
                <#lt>    ${value} = "${value}"<#if value?has_next>,</#if>
            </#list>
            <#lt>}
        </#list>
    </#if>
</#if>
