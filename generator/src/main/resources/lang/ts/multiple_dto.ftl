<#macro field_name name><#if name?starts_with("@")>"${name}"<#else>${name}</#if></#macro>
type ${name} = <#list subclasses![] as subclass>${subclass.name}<#if subclass?has_next> | </#if></#list>

<#list subclasses![] as subclass>
type ${subclass.name} = {
	<@field_name discriminatorField/>: "${oneOf[subclass.name]}"
<#list subclass.fields![] as field><#if discriminatorField != field.name>
	<@field_name field.name/><#if !field.required>?</#if>: ${field.type}
</#if></#list>
}
</#list>

<#if enums?has_content>
  <#list enums![] as enum>
type ${enum.name} = <#list enum.values![] as value>"${value}"<#if value?has_next> | </#if></#list>
  </#list>
</#if>
