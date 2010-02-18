/*
 * $Id: PropertyMapTest.java,v 1.1.1.1 2006/09/11 08:16:51 norman Exp $
 *
 * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.esa.beam.util;

import java.awt.Color;
import java.awt.Font;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PropertyMapTest extends TestCase {

    public PropertyMapTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(PropertyMapTest.class);
    }


    /**
     * Tests the functionality of set and getPropertyBool
     */
    public void testSetGetPropertyBool() {
        PropertyMap prop = new PropertyMap();

        // "a" is not in the set
        assertEquals(false, prop.getPropertyBool("a"));
        assertEquals(true, prop.getPropertyBool("a", true));
        assertEquals(false, prop.getPropertyBool("a", false));
        assertEquals(null, prop.getPropertyBool("a", null));
        assertEquals(Boolean.TRUE, prop.getPropertyBool("a", Boolean.TRUE));
        assertEquals(Boolean.FALSE, prop.getPropertyBool("a", Boolean.FALSE));

        // "b" is in the set and true
        prop.setPropertyBool("b", true);
        assertEquals(true, prop.getPropertyBool("b"));
        assertEquals(true, prop.getPropertyBool("b", true));
        assertEquals(true, prop.getPropertyBool("b", false));
        assertEquals(Boolean.TRUE, prop.getPropertyBool("b", null));
        assertEquals(Boolean.TRUE, prop.getPropertyBool("b", Boolean.TRUE));
        assertEquals(Boolean.TRUE, prop.getPropertyBool("b", Boolean.FALSE));

        // "c" is in the set and false
        prop.setPropertyBool("c", false);
        assertEquals(false, prop.getPropertyBool("c"));
        assertEquals(false, prop.getPropertyBool("c", true));
        assertEquals(false, prop.getPropertyBool("c", false));
        assertEquals(Boolean.FALSE, prop.getPropertyBool("c", null));
        assertEquals(Boolean.FALSE, prop.getPropertyBool("c", Boolean.TRUE));
        assertEquals(Boolean.FALSE, prop.getPropertyBool("c", Boolean.FALSE));
    }

    /**
     * Tests the functionality of set and getPropertyColor
     */
    public void testSetGetPropertyColor() {
        PropertyMap prop = new PropertyMap();

        // "a" is not in the set
        assertEquals(Color.black, prop.getPropertyColor("a"));
        assertEquals(null, prop.getPropertyColor("a", null));
        assertEquals(Color.yellow, prop.getPropertyColor("a", Color.yellow));

        // "b" is in the set and red
        prop.setPropertyColor("b", Color.red);
        assertEquals(Color.red, prop.getPropertyColor("b"));
        assertEquals(Color.red, prop.getPropertyColor("b", null));
        assertEquals(Color.red, prop.getPropertyColor("b", Color.yellow));
    }

    /**
     * Tests the functionality for set and getPropertyDouble
     */
    public void testSetGetPropertyDouble() {
        PropertyMap prop = new PropertyMap();

        // "a" is not in the set
        assertEquals(0.0, prop.getPropertyDouble("a"), 1e-10);  // test default
        assertEquals(8.34, prop.getPropertyDouble("a", 8.34), 1e-10);
        assertEquals(null, prop.getPropertyDouble("a", null));
        assertEquals(new Double(8.34), prop.getPropertyDouble("a", new Double(8.34)));

        // "b" is in the set and 23.4
        prop.setPropertyDouble("b", 23.4);
        assertEquals(23.4, prop.getPropertyDouble("b"), 1e-10);
        assertEquals(23.4, prop.getPropertyDouble("b", 43.2), 1e-10);
        assertEquals(new Double(23.4), prop.getPropertyDouble("b", new Double(43.2)));
        assertEquals(new Double(23.4), prop.getPropertyDouble("b", null));
    }

    /**
     * Checks the functionality of set and getPropertyFont
     */
    public void testSetGetPropertyFont() {
        PropertyMap prop = new PropertyMap();

        Font font1 = new Font("SansSerif", Font.PLAIN, 12); // default
        Font font2 = new Font("SansSerif", Font.BOLD, 10);

        // "a" is not in the set
        assertEquals(font1, prop.getPropertyFont("a"));  // test default
        assertEquals(null, prop.getPropertyFont("a", null));
        assertEquals(font2, prop.getPropertyFont("a", font2));

        // "b" is in the set and font2
        prop.setPropertyFont("b", font2);
        assertEquals(font2, prop.getPropertyFont("b"));
        assertEquals(font2, prop.getPropertyFont("b", null));
        assertEquals(font2, prop.getPropertyFont("b", font1));
    }

    /**
     * Tests functionality of set and getPropertyInt
     */
    public void testSetGetPropertyInt() {
        PropertyMap prop = new PropertyMap();

        // "a" is not in the set
        assertEquals(0, prop.getPropertyInt("a"));  // test default
        assertEquals(8, prop.getPropertyInt("a", 8));
        assertEquals(null, prop.getPropertyInt("a", null));
        assertEquals(new Integer(8), prop.getPropertyInt("a", new Integer(8)));

        // "b" is in the set and 23
        prop.setPropertyInt("b", 23);
        assertEquals(23, prop.getPropertyInt("b"));
        assertEquals(23, prop.getPropertyInt("b", 43));
        assertEquals(new Integer(23), prop.getPropertyInt("b", new Integer(43)));
        assertEquals(new Integer(23), prop.getPropertyInt("b", null));
    }

    /**
     * Tests the functionality of set and getPropertyString
     */
    public void testSetGetPropertyString() {
        PropertyMap prop = new PropertyMap();

        // "a" is not in the set
        assertEquals("", prop.getPropertyString("a"));  // test default
        assertEquals("CBA", prop.getPropertyString("a", "CBA"));
        assertEquals(null, prop.getPropertyString("a", null));

        // "b" is in the set and "ABC"
        prop.setPropertyString("b", "ABC");
        assertEquals("ABC", prop.getPropertyString("b"));
        assertEquals("ABC", prop.getPropertyString("b", "CBA"));
        assertEquals("ABC", prop.getPropertyString("b", null));
    }
}
