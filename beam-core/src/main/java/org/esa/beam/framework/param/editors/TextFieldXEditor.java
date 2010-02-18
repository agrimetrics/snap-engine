/*
 * $Id: TextFieldXEditor.java,v 1.2 2006/10/10 14:47:22 norman Exp $
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
package org.esa.beam.framework.param.editors;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.text.JTextComponent;

import org.esa.beam.framework.param.AbstractParamXEditor;
import org.esa.beam.framework.param.Parameter;

/**
 * An editor which uses a {@link javax.swing.JTextField} or {@link javax.swing.JTextArea}
 * and another {@link javax.swing.JComponent} used to invoke an extra editor which lets the
 * user edit the value in the text field.
 */
public abstract class TextFieldXEditor extends AbstractParamXEditor {

    private JTextComponent _textComponent;
    private JComponent _component;

    public TextFieldXEditor(Parameter parameter) {
        super(parameter, true);
    }

    public JTextComponent getTextComponent() {
        return _textComponent;
    }

    @Override
    public JComponent getEditorComponentChild() {
        return _component;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        String text = getParameter().getValueAsText();
        JTextComponent textField = getTextComponent();
        if (!textField.getText().equals(text)) {
            textField.setText(text);
        }
        if (textField.isEnabled() != isEnabled()) {
            textField.setEnabled(isEnabled());
        }
    }

    @Override
    protected void initUIChild() {
//        super.initUI(); // creates the default label components for us

        int numCols = getParameter().getProperties().getNumCols();
        int numRows = getParameter().getProperties().getNumRows();


        if (numRows <= 1) {
            JTextField textComponent = new JTextField();
            nameEditorComponent(textComponent);
            // Configure text field
            //
            if (numCols <= 0) {
                textComponent.setColumns(24);
            } else {
                textComponent.setColumns(numCols);
            }
            textComponent.addActionListener(getDefaultActionListener());
            setTextComponent(textComponent);
        } else {
            JTextArea textComponent = new JTextArea();
            nameEditorComponent(textComponent);
            textComponent.setRows(numRows);
            if (numCols > 0) {
                textComponent.setColumns(numCols);
            }
            textComponent.setLineWrap(true);
            textComponent.setWrapStyleWord(true);
            textComponent.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
            setTextComponent(textComponent);
        }

        final boolean enabled = !getParameter().getProperties().isReadOnly();
        _textComponent.setEnabled(enabled);
        _component.setEnabled(enabled);
        String toolTiptext = getParameter().getProperties().getDescription();
        _textComponent.setText(getParameter().getValueAsText());
        _textComponent.setToolTipText(toolTiptext);
        _textComponent.setInputVerifier(getDefaultInputVerifier());
    }

    private void setTextComponent(JTextComponent textComponent) {
        _textComponent = textComponent;
        if (_textComponent instanceof JTextArea) {
            _textComponent.setBorder(null);
            _component = new JScrollPane(_textComponent);
            nameComponent(textComponent, "ScrollPane");
        } else {
            _component = _textComponent;
        }
    }

}
