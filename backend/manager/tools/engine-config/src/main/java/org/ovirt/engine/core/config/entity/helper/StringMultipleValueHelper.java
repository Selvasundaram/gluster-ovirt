package org.ovirt.engine.core.config.entity.helper;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.config.entity.ConfigKey;

/**
 * This helper validates multiple values from the allowed
 * values list. Given Values should be comma separated.
 *
 */
public class StringMultipleValueHelper implements ValueHelper {

    @Override
    public String getValue(String value) {
        return value;
    }

    @Override
    public String setValue(String value) {
        return value;
    }

    @Override
    public boolean validate(ConfigKey key, String value) {

        Collection<String> validValues = key.getValidValues();

        boolean isValid = false;
        if (validValues.isEmpty() || value.equals("")) {
            isValid = true;
        } else {
            isValid = CollectionUtils.isSubCollection(Arrays.asList(value.split(",")), validValues);
        }
        return isValid;
    }
}
