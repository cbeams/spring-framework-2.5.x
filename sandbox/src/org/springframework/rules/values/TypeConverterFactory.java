package org.springframework.rules.values;

/**
 * @author Keith Donald
 */
public interface TypeConverterFactory {
    public TypeConverter createTypeConverter(String domainObjectProperty,
            ValueModel valueModel);
}