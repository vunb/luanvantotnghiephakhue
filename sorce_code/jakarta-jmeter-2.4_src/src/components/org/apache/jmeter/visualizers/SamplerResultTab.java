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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.reflect.Functor;

public abstract class SamplerResultTab implements ResultRenderer {

    // N.B. these are not multi-threaded, so don't make it static
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // ISO format $NON-NLS-1$

    private static final String NL = "\n"; // $NON-NLS-1$

    public static final Color SERVER_ERROR_COLOR = Color.red;

    public static final Color CLIENT_ERROR_COLOR = Color.blue;

    public static final Color REDIRECT_COLOR = Color.green;

    protected static final String TEXT_COMMAND = "text"; // $NON-NLS-1$

    private static final String STYLE_SERVER_ERROR = "ServerError"; // $NON-NLS-1$

    private static final String STYLE_CLIENT_ERROR = "ClientError"; // $NON-NLS-1$

    private static final String STYLE_REDIRECT = "Redirect"; // $NON-NLS-1$

    private JTextPane stats;

    private JPanel resultsPane; /** Response Data pane */
    protected JScrollPane resultsScrollPane; /** Contains results; contained in resultsPane */
    protected JEditorPane results; /** Response Data shown here */

    private JLabel imageLabel;

    private JPanel requestPane;

    private JTextArea sampleDataField; /** request pane content */

    protected JTabbedPane rightSide; /** holds the tabbed panes */

    private int lastSelectedTab;

    private Object userObject = null; // Could be SampleResult or AssertionResult

    private SampleResult sampleResult = null;

    private AssertionResult assertionResult = null;

    protected SearchTextExtension searchTextExtension;

    private JPanel searchPanel = null;

    protected boolean activateSearchExtension = true; // most current subclasses can process text

    private Color backGround;
    
    private static final String[] COLUMNS_RESULT = new String[] {
            " ", // one space for blank header // $NON-NLS-1$ 
            " " }; // one space for blank header  // $NON-NLS-1$

    private static final String[] COLUMNS_HEADERS = new String[] {
            "view_results_table_headers_key", // $NON-NLS-1$
            "view_results_table_headers_value" }; // $NON-NLS-1$

    private static final String[] COLUMNS_FIELDS = new String[] {
            "view_results_table_fields_key", // $NON-NLS-1$
            "view_results_table_fields_value" }; // $NON-NLS-1$

    private ObjectTableModel resultModel = null;

    private ObjectTableModel resHeadersModel = null;

    private ObjectTableModel resFieldsModel = null;

    private JTable tableResult = null;

    private JTable tableResHeaders = null;

    private JTable tableResFields = null;

    private JTabbedPane tabbedResult = null;

    private JScrollPane paneRaw = null;
    
    private JSplitPane paneParsed = null;
    
    // to save last select tab (raw/parsed)
    private int lastResultTabIndex= 0; 

    // Result column renderers
    private static final TableCellRenderer[] RENDERERS_RESULT = new TableCellRenderer[] {
            null, // Key
            null, // Value
    };

    // Response headers column renderers
    private static final TableCellRenderer[] RENDERERS_HEADERS = new TableCellRenderer[] {
            null, // Key
            null, // Value
    };

    // Response fields column renderers
    private static final TableCellRenderer[] RENDERERS_FIELDS = new TableCellRenderer[] {
            null, // Key
            null, // Value
    };

    public SamplerResultTab() {
        // create tables
        resultModel = new ObjectTableModel(COLUMNS_RESULT, RowResult.class, // The object used for each row
                new Functor[] {
                        new Functor("getKey"), // $NON-NLS-1$
                        new Functor("getValue") }, // $NON-NLS-1$
                new Functor[] {
                        null, null }, new Class[] {
                        String.class, String.class });
        resHeadersModel = new ObjectTableModel(COLUMNS_HEADERS,
                RowResult.class, // The object used for each row
                new Functor[] {
                        new Functor("getKey"), // $NON-NLS-1$
                        new Functor("getValue") }, // $NON-NLS-1$
                new Functor[] {
                        null, null }, new Class[] {
                        String.class, String.class });
        resFieldsModel = new ObjectTableModel(COLUMNS_FIELDS, RowResult.class, // The object used for each row
                new Functor[] {
                        new Functor("getKey"), // $NON-NLS-1$
                        new Functor("getValue") }, // $NON-NLS-1$
                new Functor[] {
                        null, null }, new Class[] {
                        String.class, String.class });
    }

    public void clearData() {
        results.setText("");// Response Data // $NON-NLS-1$
        sampleDataField.setText("");// Request Data // $NON-NLS-1$
    }

    public void init() {
        rightSide.addTab(JMeterUtils.getResString("view_results_tab_sampler"), createResponseMetadataPanel()); // $NON-NLS-1$
        // Create the panels for the other tabs
        requestPane = createRequestPanel();
        resultsPane = createResponseDataPanel();
    }

    @SuppressWarnings("boxing")
    public void setupTabPane() {
        StyledDocument statsDoc = stats.getStyledDocument();
        try {
            statsDoc.remove(0, statsDoc.getLength());
            sampleDataField.setText(""); // $NON-NLS-1$
            results.setText(""); // $NON-NLS-1$
            if (userObject instanceof SampleResult) {
                sampleResult = (SampleResult) userObject;
                // We are displaying a SampleResult
                setupTabPaneForSampleResult();

                // load time label
                String sd = sampleResult.getSamplerData();
                if (sd != null) {
                    String rh = sampleResult.getRequestHeaders();
                    if (rh != null) {
                        StringBuilder sb = new StringBuilder(sd.length() + rh.length() + 20);
                        sb.append(sd);
                        sb.append("\n"); //$NON-NLS-1$
                        sb.append(JMeterUtils.getResString("view_results_request_headers")); //$NON-NLS-1$
                        sb.append("\n"); //$NON-NLS-1$
                        sb.append(rh);
                        sd = sb.toString();
                    }
                    sampleDataField.setText(sd);
                }

                final String samplerClass = sampleResult.getClass().getName();
                String typeResult = samplerClass.substring(1 + samplerClass.lastIndexOf('.'));
                
                StringBuilder statsBuff = new StringBuilder(200);
                statsBuff.append(JMeterUtils.getResString("view_results_thread_name")).append(sampleResult.getThreadName()).append(NL); //$NON-NLS-1$
                String startTime = dateFormat.format(new Date(sampleResult.getStartTime()));
                statsBuff.append(JMeterUtils.getResString("view_results_sample_start")).append(startTime).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_load_time")).append(sampleResult.getTime()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_latency")).append(sampleResult.getLatency()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_size_in_bytes")).append(sampleResult.getBytes()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_sample_count")).append(sampleResult.getSampleCount()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_error_count")).append(sampleResult.getErrorCount()).append(NL); //$NON-NLS-1$
                statsDoc.insertString(statsDoc.getLength(), statsBuff.toString(), null);
                statsBuff.setLength(0); // reset for reuse

                String responseCode = sampleResult.getResponseCode();

                int responseLevel = 0;
                if (responseCode != null) {
                    try {
                        responseLevel = Integer.parseInt(responseCode) / 100;
                    } catch (NumberFormatException numberFormatException) {
                        // no need to change the foreground color
                    }
                }

                Style style = null;
                switch (responseLevel) {
                case 3:
                    style = statsDoc.getStyle(STYLE_REDIRECT);
                    break;
                case 4:
                    style = statsDoc.getStyle(STYLE_CLIENT_ERROR);
                    break;
                case 5:
                    style = statsDoc.getStyle(STYLE_SERVER_ERROR);
                    break;
                }

                statsBuff.append(JMeterUtils.getResString("view_results_response_code")).append(responseCode).append(NL); //$NON-NLS-1$
                statsDoc.insertString(statsDoc.getLength(), statsBuff.toString(), style);
                statsBuff.setLength(0); // reset for reuse

                // response message label
                String responseMsgStr = sampleResult.getResponseMessage();

                statsBuff.append(JMeterUtils.getResString("view_results_response_message")).append(responseMsgStr).append(NL); //$NON-NLS-1$
                statsBuff.append(NL);
                statsBuff.append(JMeterUtils.getResString("view_results_response_headers")).append(NL); //$NON-NLS-1$
                statsBuff.append(sampleResult.getResponseHeaders()).append(NL);
                statsBuff.append(NL);
                statsBuff.append(typeResult + " "+ JMeterUtils.getResString("view_results_fields")).append(NL); //$NON-NLS-1$ $NON-NLS-2$
                statsBuff.append("ContentType: ").append(sampleResult.getContentType()).append(NL); //$NON-NLS-1$
                statsBuff.append("DataEncoding: ").append(sampleResult.getDataEncodingNoDefault()).append(NL); //$NON-NLS-1$
                statsDoc.insertString(statsDoc.getLength(), statsBuff.toString(), null);
                statsBuff = null; // Done
                
                // Tabbed results      
                resultModel.clearData(); // clear results table before filling
                // fill table
                resultModel.addRow(new RowResult(getParsedLabel("view_results_thread_name"), sampleResult.getThreadName())); //$NON-NLS-1$
                resultModel.addRow(new RowResult(getParsedLabel("view_results_sample_start"), startTime)); //$NON-NLS-1$
                resultModel.addRow(new RowResult(getParsedLabel("view_results_load_time"), sampleResult.getTime())); //$NON-NLS-1$
                resultModel.addRow(new RowResult(getParsedLabel("view_results_latency"), sampleResult.getLatency())); //$NON-NLS-1$
                resultModel.addRow(new RowResult(getParsedLabel("view_results_size_in_bytes"), sampleResult.getBytes())); //$NON-NLS-1$
                resultModel.addRow(new RowResult(getParsedLabel("view_results_sample_count"), sampleResult.getSampleCount())); //$NON-NLS-1$
                resultModel.addRow(new RowResult(getParsedLabel("view_results_error_count"), sampleResult.getErrorCount())); //$NON-NLS-1$
                resultModel.addRow(new RowResult(getParsedLabel("view_results_response_code"), responseCode)); //$NON-NLS-1$
                resultModel.addRow(new RowResult(getParsedLabel("view_results_response_message"), responseMsgStr)); //$NON-NLS-1$
                
                resHeadersModel.clearData(); // clear response table before filling
                // Parsed response headers
                LinkedHashMap<String, String> lhm = parseResponseHeaders(sampleResult.getResponseHeaders());
                Set<String> keySet = lhm.keySet();
                for (String key : keySet) {
                    resHeadersModel.addRow(new RowResult(key, lhm.get(key)));
                }
                
                resFieldsModel.clearData(); // clear fields table before filling
                resFieldsModel.addRow(new RowResult("Type Result ", typeResult)); //$NON-NLS-1$
                //not sure needs I18N?
                resFieldsModel.addRow(new RowResult("ContentType", sampleResult.getContentType())); //$NON-NLS-1$
                resFieldsModel.addRow(new RowResult("DataEncoding", sampleResult.getDataEncodingNoDefault())); //$NON-NLS-1$
                
                // Reset search
                if (activateSearchExtension) {
                    searchTextExtension.resetTextToFind();
                }

            } else if (userObject instanceof AssertionResult) {
                assertionResult = (AssertionResult) userObject;

                // We are displaying an AssertionResult
                setupTabPaneForAssertionResult();

                StringBuilder statsBuff = new StringBuilder(100);
                statsBuff.append(JMeterUtils.getResString("view_results_assertion_error")).append(assertionResult.isError()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_assertion_failure")).append(assertionResult.isFailure()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_assertion_failure_message")).append(assertionResult.getFailureMessage()).append(NL); //$NON-NLS-1$
                statsDoc.insertString(statsDoc.getLength(), statsBuff.toString(), null);
            }
        } catch (BadLocationException exc) {
            stats.setText(exc.getLocalizedMessage());
        }
    }

    private void setupTabPaneForSampleResult() {
        // restore tabbed pane parsed if needed
        if (tabbedResult.getTabCount() < 2) { 
            tabbedResult.insertTab(JMeterUtils.getResString("view_results_table_result_tab_parsed"), null, paneParsed, null, 1); //$NON-NLS-1$
            tabbedResult.setSelectedIndex(lastResultTabIndex); // select last tab 
        }
        // Set the title for the first tab
        rightSide.setTitleAt(0, JMeterUtils.getResString("view_results_tab_sampler")); //$NON-NLS-1$
        // Add the other tabs if not present
        if(rightSide.indexOfTab(JMeterUtils.getResString("view_results_tab_request")) < 0) { // $NON-NLS-1$
            rightSide.addTab(JMeterUtils.getResString("view_results_tab_request"), requestPane); // $NON-NLS-1$
        }
        if(rightSide.indexOfTab(JMeterUtils.getResString("view_results_tab_response")) < 0) { // $NON-NLS-1$
            rightSide.addTab(JMeterUtils.getResString("view_results_tab_response"), resultsPane); // $NON-NLS-1$
        }
        // restore last selected tab
        if (lastSelectedTab < rightSide.getTabCount()) {
            rightSide.setSelectedIndex(lastSelectedTab);
        }
    }
    
    private void setupTabPaneForAssertionResult() {
        // Remove the other (parsed) tab if present
        if (tabbedResult.getTabCount() >= 2) {
            lastResultTabIndex = tabbedResult.getSelectedIndex();
            int parsedTabIndex = tabbedResult.indexOfTab(JMeterUtils.getResString("view_results_table_result_tab_parsed")); // $NON-NLS-1$
            if(parsedTabIndex >= 0) {
                tabbedResult.removeTabAt(parsedTabIndex);
            }
        }
        // Set the title for the first tab
        rightSide.setTitleAt(0, JMeterUtils.getResString("view_results_tab_assertion")); //$NON-NLS-1$
        // Remove the other tabs if present
        int requestTabIndex = rightSide.indexOfTab(JMeterUtils.getResString("view_results_tab_request")); // $NON-NLS-1$
        if(requestTabIndex >= 0) {
            rightSide.removeTabAt(requestTabIndex);
        }
        int responseTabIndex = rightSide.indexOfTab(JMeterUtils.getResString("view_results_tab_response")); // $NON-NLS-1$
        if(responseTabIndex >= 0) {
            rightSide.removeTabAt(responseTabIndex);
        }
    }

    private Component createResponseMetadataPanel() {
        stats = new JTextPane();
        stats.setEditable(false);
        stats.setBackground(backGround);

        // Add styles to use for different types of status messages
        StyledDocument doc = (StyledDocument) stats.getDocument();

        Style style = doc.addStyle(STYLE_REDIRECT, null);
        StyleConstants.setForeground(style, REDIRECT_COLOR);

        style = doc.addStyle(STYLE_CLIENT_ERROR, null);
        StyleConstants.setForeground(style, CLIENT_ERROR_COLOR);

        style = doc.addStyle(STYLE_SERVER_ERROR, null);
        StyleConstants.setForeground(style, SERVER_ERROR_COLOR);

        paneRaw = GuiUtils.makeScrollPane(stats);
        paneRaw.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        // Set up the 1st table Result with empty headers
        tableResult = new JTable(resultModel);     
        setFirstColumnPreferredSize(tableResult);
        RendererUtils.applyRenderers(tableResult, RENDERERS_RESULT);

        // Set up the 2nd table 
        tableResHeaders = new JTable(resHeadersModel);
        setFirstColumnPreferredSize(tableResHeaders);
        tableResHeaders.getTableHeader().setDefaultRenderer(
                new HeaderAsPropertyRenderer());
        RendererUtils.applyRenderers(tableResHeaders, RENDERERS_HEADERS);

        // Set up the 3rd table 
        tableResFields = new JTable(resFieldsModel);
        setFirstColumnPreferredSize(tableResFields);
        tableResFields.getTableHeader().setDefaultRenderer(
                new HeaderAsPropertyRenderer());
        RendererUtils.applyRenderers(tableResFields, RENDERERS_FIELDS);

        // Prepare the Results tabbed pane
        tabbedResult = new JTabbedPane(JTabbedPane.BOTTOM);

        // Create the split pane
        JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                GuiUtils.makeScrollPane(tableResHeaders),
                GuiUtils.makeScrollPane(tableResFields));
        topSplit.setOneTouchExpandable(true);
        topSplit.setResizeWeight(0.80); // set split ratio

        paneParsed = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                GuiUtils.makeScrollPane(tableResult), topSplit);
        paneParsed.setOneTouchExpandable(true);
        paneParsed.setResizeWeight(0.40); // set split ratio

        // setup bottom tabs, first Raw, second Parsed
        tabbedResult.addTab(JMeterUtils.getResString("view_results_table_result_tab_raw"), paneRaw); //$NON-NLS-1$
        tabbedResult.addTab(JMeterUtils.getResString("view_results_table_result_tab_parsed"), paneParsed); //$NON-NLS-1$

        // Hint to background color on bottom tabs (grey, not blue)
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabbedResult);
        return panel;
    }

    private JPanel createRequestPanel() {
        sampleDataField = new JTextArea();
        sampleDataField.setEditable(false);
        sampleDataField.setLineWrap(true);
        sampleDataField.setWrapStyleWord(true);

        JPanel pane = new JPanel(new BorderLayout(0, 5));
        pane.add(GuiUtils.makeScrollPane(sampleDataField));
        return pane;
    }

    private JPanel createResponseDataPanel() {
        results = new JEditorPane();
        results.setEditable(false);

        resultsScrollPane = GuiUtils.makeScrollPane(results);
        imageLabel = new JLabel();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(resultsScrollPane, BorderLayout.CENTER);

        if (activateSearchExtension) {
            // Add search text extension
            searchTextExtension = new SearchTextExtension();
            searchTextExtension.init(panel);
            searchPanel = searchTextExtension.createSearchTextExtensionPane();
            searchTextExtension.setResults(results);
            searchPanel.setVisible(true);
            panel.add(searchPanel, BorderLayout.PAGE_END);
        }

        return panel;
    }

    private void showImage(Icon image) {
        imageLabel.setIcon(image);
        resultsScrollPane.setViewportView(imageLabel);
    }

    public synchronized void setSamplerResult(Object sample) {
        userObject = sample;
    }

    public synchronized void setRightSide(JTabbedPane side) {
        rightSide = side;
    }

    public void setLastSelectedTab(int index) {
        lastSelectedTab = index;
    }

    public void renderImage(SampleResult sampleResult) {
        byte[] responseBytes = sampleResult.getResponseData();
        if (responseBytes != null) {
            showImage(new ImageIcon(responseBytes)); //TODO implement other non-text types
        }
    }

    public void setBackgroundColor(Color backGround){
        this.backGround = backGround;
    }
    
    /**
     * To get I18N label from properties file
     * @param key in messages.properties
     * @return I18N label without (if exists) last colon ':' and spaces
     */
    private String getParsedLabel(String key) {
        String value = JMeterUtils.getResString(key);
        return value.replaceFirst("(?m)\\s*?:\\s*$", ""); // $NON-NLS-1$ $NON-NLS-2$
    }
    
    private void setFirstColumnPreferredSize(JTable table) {
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMaxWidth(300);
        column.setPreferredWidth(180);
    }

    /**
     * Split line into name/value pairs and remove colon ':'
     */
    private LinkedHashMap<String, String> parseResponseHeaders(String responseHeaders) {
        LinkedHashMap<String, String> linkedHeaders = new LinkedHashMap<String, String>();
        String[] list = responseHeaders.split(NL);
        for (String header : list) {
            int colon = header.indexOf(':'); // $NON-NLS-1$
            if (colon <= 0) {
                linkedHeaders.put(header, ""); // Empty value // $NON-NLS-1$
            } else {
                linkedHeaders.put(header.substring(0, colon).trim(), header
                        .substring(colon + 1).trim());
            }
        }
        return linkedHeaders;
    }
    
    /**
     * For model table
     */
    public static class RowResult {
        private String key;

        private Object value;

        public RowResult(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        /**
         * @return the key
         */
        public synchronized String getKey() {
            return key;
        }

        /**
         * @param key
         *            the key to set
         */
        public synchronized void setKey(String key) {
            this.key = key;
        }

        /**
         * @return the value
         */
        public synchronized Object getValue() {
            return value;
        }

        /**
         * @param value
         *            the value to set
         */
        public synchronized void setValue(Object value) {
            this.value = value;
        }
    }
}