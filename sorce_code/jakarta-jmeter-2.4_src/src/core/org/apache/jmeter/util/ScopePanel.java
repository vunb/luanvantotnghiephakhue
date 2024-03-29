/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Scope panel so users can choose whether
 * to apply the test element to the parent sample, the child samples or both.
 *
 */
public class ScopePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 240L;

    private final JRadioButton parentButton;
    private final JRadioButton childButton;
    private final JRadioButton allButton;
    private final JRadioButton variableButton;
    private final JTextField variableName;

    public ScopePanel(){
        this(false);
    }

    public ScopePanel(boolean enableVariableButton) {
        allButton = new JRadioButton(JMeterUtils.getResString("sample_scope_all")); //$NON-NLS-1$
        parentButton = new JRadioButton(JMeterUtils.getResString("sample_scope_parent")); //$NON-NLS-1$
        childButton = new JRadioButton(JMeterUtils.getResString("sample_scope_children")); //$NON-NLS-1$
        if (enableVariableButton) {
            variableButton = new JRadioButton(JMeterUtils.getResString("sample_scope_variable")); //$NON-NLS-1$
            variableName = new JTextField(10);
        } else {
            variableButton = null;
            variableName = null;
        }
        init();
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init() {
        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("sample_scope"))); //$NON-NLS-1$

        parentButton.setSelected(true);

        JPanel buttonPanel = new HorizontalPanel();
        ButtonGroup group = new ButtonGroup();
        group.add(allButton);
        group.add(parentButton);
        group.add(childButton);
        buttonPanel.add(parentButton);
        buttonPanel.add(childButton);
        buttonPanel.add(allButton);
        if (variableButton != null){
            variableButton.addActionListener(this);
            group.add(variableButton);
            buttonPanel.add(variableButton);
            buttonPanel.add(variableName);
        }
        add(buttonPanel);
    }

    public void clearGui() {
        parentButton.setSelected(true);
    }

    public int getSelection(){
        if (parentButton.isSelected()){
            return 0;
        }
        return 1;
    }

    public void setScopeAll() {
        allButton.setSelected(true);
    }

    public void setScopeChildren() {
        childButton.setSelected(true);
    }

    public void setScopeParent() {
        parentButton.setSelected(true);
    }

    public void setScopeVariable(String value){
        variableButton.setSelected(true);
        variableName.setText(value);
    }

    public boolean isScopeParent() {
        return parentButton.isSelected();
    }

    public boolean isScopeChildren() {
        return childButton.isSelected();
    }

    public boolean isScopeAll() {
        return allButton.isSelected();
    }

    public boolean isScopeVariable() {
        return variableButton.isSelected();
    }

    public void actionPerformed(ActionEvent e) {
        variableName.setEnabled(variableButton.isSelected());
    }

    public String getVariable() {
        return variableName.getText();
    }
}
