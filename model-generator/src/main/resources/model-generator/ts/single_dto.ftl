<#if enumValues?has_content>
	type ${simpleClassName} =
	<#list enumValues![] as enumValue>
			"${enumValue}"<#if enumValue?has_next> |</#if>
	</#list>
<#else>
	type ${simpleClassName} = {
  <#list fields![] as field>
  	${field.name}<#if !field.required>?</#if>: ${field.type}
  </#list>
  }
</#if>

<#if enums?has_content>
  <#list enums![] as enum>
type ${enum.name} = <#list enum.values![] as value>"${value}"<#if value?has_next> | </#if></#list>
  </#list>
</#if>
