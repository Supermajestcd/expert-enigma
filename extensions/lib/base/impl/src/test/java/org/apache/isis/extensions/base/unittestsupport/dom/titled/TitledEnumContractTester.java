package org.apache.isis.extensions.base.unittestsupport.dom.titled;

import org.apache.isis.extensions.base.dom.TitledEnum;

/**
 * @deprecated - use superclass
 * @param <T>
 */
@Deprecated
public class TitledEnumContractTester<T extends TitledEnum> extends
        org.apache.isis.extensions.base.dom.titled.TitledEnumContractTester {

    /**
     * @param enumType
     */
    public TitledEnumContractTester(Class<? extends Enum<?>> enumType) {
        super(enumType);
    }

}
