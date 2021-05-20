package com.rarible.protocol.generator.plugin.dependency;

import com.rarible.protocol.generator.plugin.config.FieldProcessorConfig;
import com.rarible.protocol.merger.CombinedFieldNameProcessor;
import com.rarible.protocol.merger.PlaceholderFieldNameProcessor;
import com.rarible.protocol.merger.RegexFieldNameProcessor;
import com.rarible.protocol.merger.SchemaFieldNameProcessor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FieldNameProcessorFactory {

    public SchemaFieldNameProcessor create(FieldProcessorConfig config) {
        if (config == null) {
            return SchemaFieldNameProcessor.Companion.getNO_OP();
        }
        return new CombinedFieldNameProcessor(Arrays.asList(
                createRegexProcessor(config),
                createPlaceholderProcessor(config)
        ));
    }

    private SchemaFieldNameProcessor createPlaceholderProcessor(FieldProcessorConfig config) {
        Map<String, String> placeholders = config.getPlaceholders();
        String template = config.getTemplate();
        if (template == null) {
            return SchemaFieldNameProcessor.Companion.getNO_OP();
        }
        if (placeholders == null) {
            placeholders = new HashMap<>();
        }
        Map<String, String> bracedPlaceholders = new HashMap<>();
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            bracedPlaceholders.put("{" + e.getKey() + "}", e.getValue());
        }
        return new PlaceholderFieldNameProcessor(template, bracedPlaceholders);
    }

    private SchemaFieldNameProcessor createRegexProcessor(FieldProcessorConfig config) {
        String regex = config.getApiPathReplacementRegex();
        String replacement = config.getApiPathReplacement();
        if (StringUtils.isBlank(regex)) {
            return SchemaFieldNameProcessor.Companion.getNO_OP();
        }

        replacement = replacement == null ? "" : replacement;

        return new RegexFieldNameProcessor(regex, replacement);
    }

}
