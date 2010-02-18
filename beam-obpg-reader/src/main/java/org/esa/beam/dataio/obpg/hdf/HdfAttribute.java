/*
 * $Id: HdfAttribute.java,v 1.1 2006/09/19 07:00:03 SabineE Exp $
 *
 * Copyright (C) 2002,2003  by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.esa.beam.dataio.obpg.hdf;

import org.esa.beam.util.StringUtils;

public class HdfAttribute {

    private String _name;
    private int _hdfType;
    private String _strVal;
    private int _elemCount;

    /**
     * Creates a new object with given name, type and string value
     *
     * @param name    the attribute name
     * @param hdfType the hdf4 data type
     * @param strVal  the value as string
     */
    public HdfAttribute(final String name, final int hdfType, final String strVal, final int elemCount) {
        _name = name;
        _hdfType = hdfType;
        _strVal = strVal;
        _elemCount = elemCount;
    }

    /**
     * Retrieves the name of the attribute
     *
     * @return the name
     */
    public String getName() {
        return _name;
    }

    /**
     * Retrieves the hdf4 data type of the attribute
     *
     * @return the type
     */
    public int getHdfType() {
        return _hdfType;
    }

    /**
     * Retrieves the number of data elements in the attribute.
     *
     * @return number of data elements in the attribute
     */
    public int getElemCount() {
        return _elemCount;
    }

    /**
     * Retrieves the string representation of the attribute
     *
     * @return the string
     */
    public String getStringValue() {
        return _strVal;
    }

    /**
     * Retrieves the attribute as floating point array. If the data cannot be interpreted as float - throws exception
     *
     * @return attribute as floating point array
     */
    public float[] getFloatValues() {
        return StringUtils.toFloatArray(_strVal, ",");
    }

    /**
     * Retrieves the attribute values as integer array. If data cannot be interpreted as integer - throws exception.
     *
     * @return attribute values as integer array
     */
    public int[] getIntValues() {
        return StringUtils.toIntArray(_strVal, ",");
    }

    /**
     * Retrieves the attribute as doubles point array. If the data cannot be interpreted as float - throws exception
     *
     * @return attribute as doubles point array
     */
    public double[] getDoubleValues() {
        return StringUtils.toDoubleArray(_strVal, ",");
    }
}
