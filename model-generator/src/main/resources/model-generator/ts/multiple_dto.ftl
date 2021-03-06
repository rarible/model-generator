<#if imports?has_content>
    <#list imports![] as import, file>
        <#lt>import {${import}} from "${file}";
    </#list>

</#if>
<#macro field_name name><#if name?starts_with("@")>"${name}"<#else>${name}</#if></#macro>
<#macro subclass class>

    <#if class.enums?has_content>
        <#list class.enums![] as enum>
            <#lt>export enum ${enum.name} {
            <#list enum.values![] as value>
                <#lt>    ${value} = "${value}"<#if value?has_next>,</#if>
            </#list>
            <#lt>}

        </#list>
    </#if>
    <#if class.subclasses?has_content>
        <#lt>//-------------------- ${class.simpleClassName} ---------------------//
        <#lt>export type ${class.simpleClassName} = <#list class.subclasses![] as class>${class.name}<#if class?has_next> | </#if></#list>
        <#list class.subclasses![] as class><@subclass class/></#list>
    <#else>
        <#lt>export type ${class.simpleClassName} = {
        <#lt>    <@field_name discriminatorField/>: "${oneOf[class.simpleClassName]}"
        <#list class.fields![] as field><#if discriminatorField != field.name>
            <#lt>    <@field_name field.name/><#if !field.required>?</#if>: ${field.type}
        </#if></#list>
        <#lt>}
    </#if>
</#macro>
export type ${simpleClassName} = <#list subclasses![] as class>${class.name}<#if class?has_next> | </#if></#list>
<#list subclasses![] as class><@subclass class/></#list>

<#if enums?has_content>
    <#list enums![] as enum>
        <#lt>export enum ${enum.name} {
        <#list enum.values![] as value>
            <#lt>    ${value} = "${value}"<#if value?has_next>,</#if>
        </#list>
        <#lt>}

    </#list>
</#if>
