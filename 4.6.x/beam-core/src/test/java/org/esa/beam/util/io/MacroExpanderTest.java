/*
 * $Id: MacroExpanderTest.java,v 1.1.1.1 2006/09/11 08:16:51 norman Exp $
 *
 * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package org.esa.beam.util.io;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MacroExpanderTest extends TestCase {

    Properties _p = new Properties();
    private final String _expected =
            "SET BEAM_HOME = usr/local/org/esa/beam\n"
            + "usr/local/java2/bin/java -classpath=../lib/junit.jar:../lib/jaxp.jar Test\n";

    private final String _source1 =
            "SET BEAM_HOME = ${org.esa.beam.home}\n"
            + "${java.home}/bin/java -classpath=${class.path} Test\n";

    private final String _source2 =
            "SET BEAM_HOME = $org.esa.beam.home\n"
            + "$java.home/bin/java -classpath=$class.path Test\n";

    private final String _source3 =
            "SET BEAM_HOME = $org.esa.beam.home$\n"
            + "$java.home$/bin/java -classpath=$class.path$ Test\n";

    public MacroExpanderTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(MacroExpanderTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        _p.setProperty("org.esa.beam.home", "usr/local/org/esa/beam");
        _p.setProperty("java.home", "usr/local/java2");
        _p.setProperty("class.path", "../lib/junit.jar:../lib/jaxp.jar");
    }

    public void testMacroExpander1() {
        assertEquals(_expected, expandMacros(_source1, "${", "}", _p));
    }

    public void testMacroExpander2() {
        assertEquals(_expected, expandMacros(_source2, "$", null, _p));
    }

    public void testMacroExpander3() {
        assertEquals(_expected, expandMacros(_source3, "$", "$", _p));
    }

    public static String expandMacrosRecursive(String source, String prefix, String terminator, Properties properties) {
        String s1 = source;
        while (s1 != expandMacros(s1, prefix, terminator, properties)) {
        }
        return s1;
    }

    public static String expandMacros(String source, String prefix, String terminator, Properties properties) {
        StringBuffer dest = null;
        int lastIndex = 0;
        while (true) {
            int i0 = source.indexOf(prefix, lastIndex);
            if (i0 < 0) {
                break;
            }
            int i1 = i0 + prefix.length();
            int i2 = i1;
            int i3 = i1;
            if (i2 < source.length() && isPropertyNameStart(source.charAt(i2))) {
                i2++;
                while (i2 < source.length() && isPropertyNamePart(source.charAt(i2))) {
                    i2++;
                }
                i3 = i2;
                if (terminator != null && terminator.length() > 0) {
                    if (source.regionMatches(i2, terminator, 0, terminator.length())) {
                        i3 += terminator.length();
                    } else {
                        i2 = i1;
                    }
                }
            } else {
                i2 = i1;
            }
            if (i2 > i1) {
                String key = source.substring(i1, i2);
                String value = properties.getProperty(key, "");
                //System.out.println(key+"="+value);
                if (dest == null) {
                    dest = new StringBuffer(2 + 2 * source.length());
                }
                if (i0 > lastIndex) {
                    dest.append(source.substring(lastIndex, i0));
                }
                dest.append(value);
                lastIndex = i3;
            } else {
                lastIndex++;
            }
        }
        if (lastIndex == 0 || dest == null) {
            return source;
        }
        if (lastIndex < source.length()) {
            dest.append(source.substring(lastIndex));
        }
        return dest.toString();
    }

    private static boolean isPropertyNameStart(char ch) {
        return Character.isLetter(ch) || ch == '_';
    }

    private static boolean isPropertyNamePart(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '.';
    }

}
