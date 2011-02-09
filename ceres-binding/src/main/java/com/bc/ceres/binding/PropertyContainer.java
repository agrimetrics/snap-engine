/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package com.bc.ceres.binding;

import com.bc.ceres.binding.accessors.ClassFieldAccessor;
import com.bc.ceres.binding.accessors.DefaultPropertyAccessor;
import com.bc.ceres.binding.accessors.MapEntryAccessor;
import com.bc.ceres.core.Assert;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A convenience implementation of the {@link PropertySet} interface.
 * {@link PropertyContainer} is basically an implementation of the <i>Property List</i> design pattern.
 *
 * @author Norman Fomferra
 * @since 0.6
 */
public class PropertyContainer implements PropertySet {

    private final HashMap<String, Property> propertyMap;
    private final ArrayList<Property> propertyList;
    private final PropertyChangeSupport propertyChangeSupport;

    /**
     * Constructs a new, empty property container.
     */
    public PropertyContainer() {
        propertyMap = new HashMap<String, Property>(10);
        propertyList = new ArrayList<Property>(10);
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Creates a property container for the given object.
     * The factory method will not modify the object, thus not setting any default values.
     *
     * @param object the backing object
     *
     * @return The property container.
     */
    public static PropertyContainer createObjectBacked(Object object) {
        return createObjectBacked(object,
                                  new DefaultPropertyDescriptorFactory());
    }

    /**
     * Creates a property container for the given object.
     * The factory method will not modify the object, thus not setting any default values.
     *
     * @param object            the backing object
     * @param descriptorFactory a factory used to create {@link PropertyDescriptor}s of the fields of the object's type
     *
     * @return The property container.
     */
    public static PropertyContainer createObjectBacked(Object object,
                                                       PropertyDescriptorFactory descriptorFactory) {
        return createForFields(object.getClass(),
                               descriptorFactory,
                               new ObjectBackedPropertyAccessorFactory(object),
                               false);
    }

    /**
     * Creates a property container for a map backing the values.
     * The factory method will not modify the given map, thus not setting any default values.
     *
     * @param map the map which backs the values
     *
     * @return The property container.
     */
    public static PropertyContainer createMapBacked(Map<String, Object> map) {
        PropertyContainer vc = new PropertyContainer();
        for (Entry<String, Object> entry : map.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            vc.addProperty(new Property(PropertyDescriptor.createPropertyDescriptor(name, value.getClass()),
                                        new MapEntryAccessor(map, name)));
        }
        return vc;
    }

    /**
     * Creates a property container for the given template type and map backing the values.
     * The factory method will not modify the given map, thus not setting any default values.
     *
     * @param map          the map which backs the values
     * @param templateType the template type
     *
     * @return The property container.
     */
    public static PropertyContainer createMapBacked(Map<String, Object> map,
                                                    Class<?> templateType) {
        return createMapBacked(map,
                               templateType,
                               new DefaultPropertyDescriptorFactory());
    }

    /**
     * Creates a property container for the given template type and map backing the values.
     * The factory method will not modify the given map, thus not setting any default values.
     *
     * @param map               the map which backs the values
     * @param templateType      the template type
     * @param descriptorFactory a factory used to create {@link PropertyDescriptor}s of the fields of the template type
     *
     * @return The property container.
     */
    public static PropertyContainer createMapBacked(Map<String, Object> map,
                                                    Class<?> templateType,
                                                    PropertyDescriptorFactory descriptorFactory) {
        return createForFields(templateType,
                               descriptorFactory,
                               new MapBackedPropertyAccessorFactory(map), false);
    }

    /**
     * Creates a property container for the given template type.
     * All properties will have their values set to default values (if specified).
     *
     * @param templateType the template type
     *
     * @return The property container.
     */
    public static PropertyContainer createValueBacked(Class<?> templateType) {
        return createValueBacked(templateType,
                                 new DefaultPropertyDescriptorFactory());
    }

    /**
     * Creates a property container for the given template type.
     * All properties will have their values set to default values (if specified).
     *
     * @param templateClass     the template class used to derive the descriptors from
     * @param descriptorFactory a factory used to create {@link PropertyDescriptor}s of the fields of the template type
     *
     * @return The property container.
     */
    public static PropertyContainer createValueBacked(Class<?> templateClass,
                                                      PropertyDescriptorFactory descriptorFactory) {
        return createForFields(templateClass,
                               descriptorFactory,
                               new ValueBackedPropertyAccessorFactory(),
                               true);
    }

    /**
     * Creates a property container for the given template type. Properties are generated
     * using the {@code descriptorFactory} and the {@code accessorFactory} which
     * are called for each non-static and non-transient class field.
     *
     * @param fieldProvider     Thje Java class providing the fields.
     * @param descriptorFactory The property descriptor factory.
     * @param accessorFactory   The property accessor factory.
     * @param initValues        If {@code true}, properties are initialised by their default values, if specified.
     *
     * @return The property container.
     */
    public static PropertyContainer createForFields(Class<?> fieldProvider,
                                                    PropertyDescriptorFactory descriptorFactory,
                                                    PropertyAccessorFactory accessorFactory,
                                                    boolean initValues) {
        PropertyContainer container = new PropertyContainer();
        collectProperties(container, fieldProvider, descriptorFactory, accessorFactory);
        if (initValues) {
            try {
                container.setDefaultValues();
            } catch (ValidationException e) {
                throw new IllegalStateException(e);
            }
        }
        return container;
    }

    @Override
    public Property[] getProperties() {
        return propertyList.toArray(new Property[propertyList.size()]);
    }

    @Override
    public boolean isPropertyDefined(String name) {
        return propertyMap.containsKey(name);
    }

    @Override
    public Property getProperty(String name) {
        Assert.notNull(name, "name");
        return propertyMap.get(name);
    }

    @Override
    public void addProperty(Property property) {
        if (propertyMap.put(property.getName(), property) != property) {
            final String alias = property.getDescriptor().getAlias();
            if (alias != null && !alias.isEmpty()) {
                propertyMap.put(alias, property);
            }
            propertyList.add(property);
            property.setContainer(this);
        }
    }

    @Override
    public void addProperties(Property... properties) {
        for (Property property : properties) {
            addProperty(property);
        }
    }

    @Override
    public void removeProperty(Property property) {
        if (propertyMap.remove(property.getName()) != null) {
            final String alias = property.getDescriptor().getAlias();
            if (alias != null && !alias.isEmpty()) {
                propertyMap.remove(alias);
            }
            propertyList.remove(property);
            property.setContainer(null);
        }
    }

    @Override
    public void removeProperties(Property... properties) {
        for (Property property : properties) {
            removeProperty(property);
        }
    }

    @Override
    public Object getValue(String name) {
        final Property property = getProperty(name);
        if (property == null) {
            return null;
        }
        return property.getValue();
    }

    @Override
    public void setValue(String name, Object value) throws IllegalArgumentException {
        try {
            getProperty(name).setValue(value);
        } catch (ValidationException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public PropertyDescriptor getDescriptor(String name) {
        final Property property = getProperty(name);
        if (property == null) {
            return null;
        }
        return getProperty(name).getDescriptor();
    }

    @Override
    public void setDefaultValues() throws ValidationException {
        for (final Property property : getProperties()) {
            final PropertyDescriptor descriptor = property.getDescriptor();
            if (descriptor.getDefaultValue() != null) {
                property.setValue(descriptor.getDefaultValue());
            }
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getPropertyChangeSupport().addPropertyChangeListener(l);
    }

    @Override
    public void addPropertyChangeListener(String name, PropertyChangeListener l) {
        getPropertyChangeSupport().addPropertyChangeListener(name, l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getPropertyChangeSupport().removePropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(String name, PropertyChangeListener l) {
        getPropertyChangeSupport().removePropertyChangeListener(name, l);
    }

    PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    private static void collectProperties(PropertyContainer container,
                                          Class<?> fieldProvider,
                                          PropertyDescriptorFactory descriptorFactory,
                                          PropertyAccessorFactory accessorFactory) {
        if (!fieldProvider.equals(Object.class)) {
            collectProperties(container, fieldProvider.getSuperclass(), descriptorFactory, accessorFactory);
            Field[] declaredFields = fieldProvider.getDeclaredFields();
            for (Field field : declaredFields) {
                final int mod = field.getModifiers();
                if (!Modifier.isTransient(mod) && !Modifier.isStatic(mod)) {
                    final PropertyDescriptor descriptor = PropertyDescriptor.createPropertyDescriptor(field,
                                                                                                      descriptorFactory);
                    if (descriptor != null) {
                        final PropertyAccessor accessor = accessorFactory.createValueAccessor(field);
                        if (accessor != null) {
                            container.addProperty(new Property(descriptor, accessor));
                        }
                    }
                }
            }
        }
    }

    private static class ObjectBackedPropertyAccessorFactory implements PropertyAccessorFactory {

        private final Object object;

        private ObjectBackedPropertyAccessorFactory(Object object) {
            this.object = object;
        }

        @Override
        public PropertyAccessor createValueAccessor(Field field) {
            return new ClassFieldAccessor(object, field);
        }
    }

    private static class MapBackedPropertyAccessorFactory implements PropertyAccessorFactory {

        private final Map<String, Object> map;

        private MapBackedPropertyAccessorFactory(Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public PropertyAccessor createValueAccessor(Field field) {
            return new MapEntryAccessor(map, field.getName());
        }
    }

    private static class ValueBackedPropertyAccessorFactory implements PropertyAccessorFactory {

        @Override
        public PropertyAccessor createValueAccessor(Field field) {
            return new DefaultPropertyAccessor();
        }
    }

    private static class DefaultPropertyDescriptorFactory implements PropertyDescriptorFactory {

        @Override
        public PropertyDescriptor createValueDescriptor(Field field) {
            return new PropertyDescriptor(field.getName(), field.getType());
        }
    }
}
