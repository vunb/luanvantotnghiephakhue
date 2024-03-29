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


package org.apache.jmeter.protocol.smtp.sampler.gui;

import java.awt.BorderLayout;
import java.awt.Component;

import org.apache.jmeter.protocol.smtp.sampler.SmtpSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;

/**
 * Class to build superstructure-gui for SMTP-panel, sets/gets value for a JMeter's testElement-object (i.e. also for save/load-purposes).
 * This class extends AbstractSamplerGui, therefor most implemented methods are defined by JMeter's structure.
 */
public class SmtpSamplerGui extends AbstractSamplerGui {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private SmtpPanel smtpPanel;

    /**
     * Creates new SmtpSamplerGui, standard constructer. Calls init();
     */
    public SmtpSamplerGui() {
        init();
    }

    /**
     * Method to be implemented by interface, overwritten by getStaticLabel(). Method has to be implemented by interface
     * @return Null-String
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getLabelResource()
     */
    public String getLabelResource() {
        return "smtp_sampler_title";
    }

    /**
     * Copy the data from the test element to the GUI, method has to be implemented by interface
     * @param element Test-element to be used as data-input
     * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#configure(org.apache.jmeter.testelement.TestElement)
     */
    @Override
    public void configure(TestElement element) {
        if (smtpPanel == null){
            smtpPanel = new SmtpPanel();
        }
        smtpPanel.setServer(element.getPropertyAsString(SmtpSampler.SERVER));
        smtpPanel.setPort(element.getPropertyAsString(SmtpSampler.SERVER_PORT));
        smtpPanel.setMailFrom(element.getPropertyAsString(SmtpSampler.MAIL_FROM));
        smtpPanel.setReceiverTo(element.getPropertyAsString(SmtpSampler.RECEIVER_TO));
        smtpPanel.setReceiverCC(element.getPropertyAsString(SmtpSampler.RECEIVER_CC));
        smtpPanel.setReceiverBCC(element.getPropertyAsString(SmtpSampler.RECEIVER_BCC));

        smtpPanel.setBody(element.getPropertyAsString(SmtpSampler.MESSAGE));
        smtpPanel.setSubject(element.getPropertyAsString(SmtpSampler.SUBJECT));
        smtpPanel.setIncludeTimestamp(element.getPropertyAsBoolean(SmtpSampler.INCLUDE_TIMESTAMP));
        JMeterProperty headers = element.getProperty(SmtpSampler.HEADER_FIELDS);
        if (headers instanceof CollectionProperty) { // Might be NullProperty
            smtpPanel.setHeaderFields((CollectionProperty)headers);            
        } else {
            smtpPanel.setHeaderFields(new CollectionProperty());
        }
        smtpPanel.setAttachments(element.getPropertyAsString(SmtpSampler.ATTACH_FILE));

        smtpPanel.setUseEmlMessage(element.getPropertyAsBoolean(SmtpSampler.USE_EML));
        smtpPanel.setEmlMessage(element.getPropertyAsString(SmtpSampler.EML_MESSAGE_TO_SEND));

        smtpPanel.setUseSSL(element.getPropertyAsBoolean(SmtpSampler.USE_SSL));
        smtpPanel.setUseStartTLS(element.getPropertyAsBoolean(SmtpSampler.USE_STARTTLS));
        if(!element.getPropertyAsBoolean(SmtpSampler.USE_STARTTLS) && !element.getPropertyAsBoolean(SmtpSampler.USE_SSL)){
            smtpPanel.setUseNoSecurity(true);
        }
        smtpPanel.setTrustAllCerts(element.getPropertyAsBoolean(SmtpSampler.SSL_TRUST_ALL_CERTS));
        smtpPanel.setEnforceStartTLS(element.getPropertyAsBoolean(SmtpSampler.ENFORCE_STARTTLS));
        smtpPanel.setUseLocalTrustStore(element.getPropertyAsBoolean(SmtpSampler.USE_LOCAL_TRUSTSTORE));
        smtpPanel.setTrustStoreToUse(element.getPropertyAsString(SmtpSampler.TRUSTSTORE_TO_USE));

        smtpPanel.setUseAuth(element.getPropertyAsBoolean(SmtpSampler.USE_AUTH));
        smtpPanel.setUsername(element.getPropertyAsString(SmtpSampler.USERNAME));
        smtpPanel.setPassword(element.getPropertyAsString(SmtpSampler.PASSWORD));

        smtpPanel.setMessageSizeStatistic(element.getPropertyAsBoolean(SmtpSampler.MESSAGE_SIZE_STATS));
        smtpPanel.setEnableDebug(element.getPropertyAsBoolean(SmtpSampler.ENABLE_DEBUG));

        super.configure(element);
    }

    /**
     * Creates a new TestElement and set up its data
     * @return Test-element for JMeter
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    public TestElement createTestElement() {
        SmtpSampler sampler = new SmtpSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components
     * @param te TestElement for JMeter
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    public void modifyTestElement(TestElement te) {
        te.clear();
        super.configureTestElement(te);
        te.setProperty(SmtpSampler.SERVER, smtpPanel.getServer());
        te.setProperty(SmtpSampler.SERVER_PORT, smtpPanel.getPort());
        te.setProperty(SmtpSampler.MAIL_FROM, smtpPanel.getMailFrom());
        te.setProperty(SmtpSampler.RECEIVER_TO, smtpPanel.getReceiverTo());
        te.setProperty(SmtpSampler.RECEIVER_CC, smtpPanel.getReceiverCC());
        te.setProperty(SmtpSampler.RECEIVER_BCC, smtpPanel.getReceiverBCC());
        te.setProperty(SmtpSampler.SUBJECT, smtpPanel.getSubject());
        te.setProperty(SmtpSampler.INCLUDE_TIMESTAMP, Boolean.toString(smtpPanel.isIncludeTimestamp()));
        te.setProperty(SmtpSampler.MESSAGE, smtpPanel.getBody());
        te.setProperty(SmtpSampler.ATTACH_FILE, smtpPanel.getAttachments());

        te.setProperty(SmtpSampler.USE_SSL, Boolean.toString(smtpPanel.isUseSSL()));
        te.setProperty(SmtpSampler.USE_STARTTLS, Boolean.toString(smtpPanel.isUseStartTLS()));
        te.setProperty(SmtpSampler.SSL_TRUST_ALL_CERTS, Boolean.toString(smtpPanel.isTrustAllCerts()));
        te.setProperty(SmtpSampler.ENFORCE_STARTTLS, Boolean.toString(smtpPanel.isEnforceStartTLS()));
        te.setProperty(SmtpSampler.USE_LOCAL_TRUSTSTORE, Boolean.toString(smtpPanel.isUseLocalTrustStore()));
        te.setProperty(SmtpSampler.TRUSTSTORE_TO_USE, smtpPanel.getTrustStoreToUse());

        te.setProperty(SmtpSampler.USE_EML, smtpPanel.isUseEmlMessage());
        te.setProperty(SmtpSampler.EML_MESSAGE_TO_SEND, smtpPanel.getEmlMessage());

        te.setProperty(SmtpSampler.USE_AUTH, Boolean.toString(smtpPanel.isUseAuth()));
        te.setProperty(SmtpSampler.PASSWORD, smtpPanel.getPassword());
        te.setProperty(SmtpSampler.USERNAME, smtpPanel.getUsername());

        te.setProperty(SmtpSampler.MESSAGE_SIZE_STATS, Boolean.toString(smtpPanel.isMessageSizeStatistics()));
        te.setProperty(SmtpSampler.ENABLE_DEBUG, Boolean.toString(smtpPanel.isEnableDebug()));

        te.setProperty(smtpPanel.getHeaderFields());
    }

    /**
     * Helper method to set up the GUI screen
     */
    private void init() {
        // Standard setup
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH); // Add the standard title
        add(makeDataPanel(), BorderLayout.CENTER);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();
        if (smtpPanel != null) {
            smtpPanel.clear();
        }
    }
    /**
     * Creates a sampler-gui-object, singleton-method
     * @return Panel for entering the data
     */
    private Component makeDataPanel() {
        if (smtpPanel == null)
            smtpPanel = new SmtpPanel();
        return smtpPanel;
    }
}