package org.esa.beam.framework.gpf;

import com.bc.ceres.binding.XmlConverter;
import com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom;

public interface ParameterXmlConverter extends XmlConverter {
    void insertDomTemplate(Xpp3Dom dom);
}
