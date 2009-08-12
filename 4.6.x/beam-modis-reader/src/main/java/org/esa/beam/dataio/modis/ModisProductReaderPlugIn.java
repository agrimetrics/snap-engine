/*
 * $Id: ModisProductReaderPlugIn.java,v 1.10 2006/10/17 15:07:32 marcop Exp $
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
package org.esa.beam.dataio.modis;

import org.esa.beam.dataio.modis.hdf.lib.HDF;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.util.SystemUtils;
import org.esa.beam.util.io.BeamFileFilter;

import java.io.File;
import java.util.Locale;

public class ModisProductReaderPlugIn implements ProductReaderPlugIn {

    // This is here just to keep the property name
//    private static final String HDF4_PROPERTY_KEY = "ncsa.hdf.hdflib.HDFLibrary.hdflib";

    private static boolean hdfLibAvailable = false;

    static {
        hdfLibAvailable = SystemUtils.loadHdf4Lib(ModisProductReaderPlugIn.class) != null;
    }

    /**
     * @return whether or not the HDF4 library is available.
     */
    public static boolean isHdf4LibAvailable() {
        return hdfLibAvailable;
    }

    /**
     * Checks whether the given object is an acceptable input for this product reader and if so, the method checks if it
     * is capable of decoding the input's content.
     */
    public DecodeQualification getDecodeQualification(Object input) {
        if (!isHdf4LibAvailable()) {
            return DecodeQualification.UNABLE;
        }

        DecodeQualification bRet = DecodeQualification.UNABLE;
        File file = null;

        if (input instanceof String) {
            file = new File((String) input);
        } else if (input instanceof File) {
            file = (File) input;
        }

        if ((file != null) && (file.exists()) && (file.isFile())) {
            if (file.getPath().toLowerCase().endsWith(ModisConstants.DEFAULT_FILE_EXTENSION)) {
                try {
                    if (HDF.getWrap().Hishdf(file.getPath())) {
                        bRet = DecodeQualification.SUITABLE;
                    }
                } catch (Exception e) {
                    // nothing to do, return value is already false
                }
            }
        }

        return bRet;
    }

    /**
     * Returns an array containing the classes that represent valid input types for this reader.
     * <p/>
     * <p> Intances of the classes returned in this array are valid objects for the <code>setInput</code> method of the
     * <code>ProductReader</code> interface (the method will not throw an <code>InvalidArgumentException</code> in this
     * case).
     *
     * @return an array containing valid input types, never <code>null</code>
     */
    public Class[] getInputTypes() {
        if (!isHdf4LibAvailable()) {
            return new Class[0];
        }

        return new Class[]{String.class, File.class};
    }

    /**
     * Creates an instance of the actual product reader class. This method should never return <code>null</code>.
     *
     * @return a new reader instance, never <code>null</code>
     */
    public ProductReader createReaderInstance() {
        if (!isHdf4LibAvailable()) {
            return null;
        }

        return new ModisProductReader(this);
    }

    public BeamFileFilter getProductFileFilter() {
        String[] formatNames = getFormatNames();
        String formatName = "";
        if (formatNames.length > 0) {
            formatName = formatNames[0];
        }
        return new BeamFileFilter(formatName, getDefaultFileExtensions(), getDescription(null));
    }

    /**
     * Gets the default file extensions associated with each of the format names returned by the <code>{@link
     * #getFormatNames}</code> method. <p>The string array returned shall always have the same lenhth as the array
     * returned by the <code>{@link #getFormatNames}</code> method. <p>The extensions returned in the string array shall
     * always include a leading colon ('.') character, e.g. <code>".hdf"</code>
     *
     * @return the default file extensions for this product I/O plug-in, never <code>null</code>
     */
    public String[] getDefaultFileExtensions() {
        if (!isHdf4LibAvailable()) {
            return new String[0];
        }

        return new String[]{ModisConstants.DEFAULT_FILE_EXTENSION};
    }

    /**
     * Gets a short description of this plug-in. If the given locale is set to <code>null</code> the default locale is
     * used.
     * <p/>
     * <p> In a GUI, the description returned could be used as tool-tip text.
     *
     * @param locale the local for the given decription string, if <code>null</code> the default locale is used
     *
     * @return a textual description of this product reader/writer
     */
    public String getDescription(Locale locale) {
        return ModisConstants.READER_DESCRIPTION;
    }

    /**
     * Gets the names of the product formats handled by this product I/O plug-in.
     *
     * @return the names of the product formats handled by this product I/O plug-in, never <code>null</code>
     */
    public String[] getFormatNames() {
        if (!isHdf4LibAvailable()) {
            return new String[0];
        }

        return new String[]{ModisConstants.FORMAT_NAME};
    }
}
