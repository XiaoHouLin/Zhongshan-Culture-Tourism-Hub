package com.example.tourism_rear.pack.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wrapper used when editing a cultural product so that the previous identifier is preserved.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class EditCulturalProduct extends CulturalProductInfo {
    private String oldProductCode;
}
