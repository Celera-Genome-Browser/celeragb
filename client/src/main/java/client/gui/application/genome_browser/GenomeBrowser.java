/*
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 1999 - 2006 Applera Corporation.
 301 Merritt 7 
 P.O. Box 5435 
 Norwalk, CT 06856-5435 USA

 This is free software; you can redistribute it and/or modify it under the 
 terms of the GNU Lesser General Public License as published by the 
 Free Software Foundation; version 2.1 of the License.

 This software is distributed in the hope that it will be useful, but 
 WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE. 
 See the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License 
 along with this software; if not, write to the Free Software Foundation, Inc.
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
*/
/*********************************************************************
 *********************************************************************
 CVS_ID:  $Id$
 *********************************************************************/
package client.gui.application.genome_browser;

import api.entity_model.management.ModelMgr;
import api.facade.facade_mgr.FacadeManager;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.other.panels.DataSourceSettings;
import client.gui.other.server_status.ServerStatusReportManager;
import shared.util.SystemPropertyConfigurator;

import javax.swing.*;


/**
 * This class is a very lightweight class to kick off the application
 * by instantiating the first browser.  Once this is complete, the
 * browser manages further instances internally.
 *
 * Initially written by: Peter Davies
 *
 */
public final class GenomeBrowser {
//    static private final String VERSION_NUMBER = "@@version@@";
    static private final String VERSION_NUMBER = "5.0";

    static {
        System.out.println("Java version: " +
                System.getProperty("java.version"));

        java.security.ProtectionDomain pd = GenomeBrowser.class.getProtectionDomain();
        System.out.println("Codesource: " + pd.getCodeSource().getLocation());

        SystemPropertyConfigurator.setDefaultConfigFile(
                "resource.client.DeveloperClientConfig");
        SystemPropertyConfigurator.loadConfiguration();
    }

    public GenomeBrowser() {
        newBrowser();
    }

    public static void main(final String[] args) {
        new GenomeBrowser();
    }

    private static void newBrowser() {

        // Show the Splash Screen
        final SplashScreen splash = new SplashScreen();
        splash.setStatusText("Initializing Application...");
        splash.setVisible(true);

        final SessionMgr sessionMgr = SessionMgr.getSessionMgr();
        try {
            //Browser Setup
            final String versionString = System.getProperty("x.genomebrowser.Version");
            final boolean internal = (versionString != null) &&
                    (versionString.toLowerCase().indexOf("internal") > -1);

            sessionMgr.setNewBrowserTitle(System.getProperty(
                    "x.genomebrowser.Title") + " " + VERSION_NUMBER);
            sessionMgr.setApplicationName(System.getProperty(
                    "x.genomebrowser.Title"));
            sessionMgr.setApplicationVersion(VERSION_NUMBER);
// JCVI LLF, 10/19/06
            // RT 10/27/2006
            sessionMgr.setNewBrowserImageIcon(
                    new ImageIcon(GenomeBrowser.class.getResource(
                            System.getProperty(
                                    "x.genomebrowser.WindowCornerLogo"))));
            sessionMgr.setNewBrowserSize(.8f);
            sessionMgr.setNewBrowserMenuBar(GenomeBrowserMenuBar.class);
            sessionMgr.startExternalListener(30000);
            sessionMgr.setModelProperty("ShowInternalDataSourceInDialogs",
                    new Boolean(internal));

            //Exception Handler Registration
            sessionMgr.registerExceptionHandler(
                    new shared.exception_handlers.PrintStackTraceHandler());
            sessionMgr.registerExceptionHandler(
                    new client.gui.framework.exception_handlers.UserNotificationHandler());
            sessionMgr.registerExceptionHandler(
                    new client.gui.framework.exception_handlers.ExitHandler()); //should be last so that other handlers can complete first.

            // Protocol Registration
            final ModelMgr modelMgr = ModelMgr.getModelMgr();
            // OMIT for CONVERSION
            //modelMgr.registerFacadeManagerForProtocol("ejb",
            //        api.facade.concrete_facade.ejb.jrun.JRunEJBFacadeManager.class,
            //        "Internal Database via EJB Server");
            modelMgr.registerFacadeManagerForProtocol("xmlfeature",
                    api.facade.concrete_facade.xml.XmlInSessionLoadFacadeManager.class,
                    "XML Feature (.gbf) File");
            modelMgr.registerFacadeManagerForProtocol("xmlworkspace",
                    api.facade.concrete_facade.xml.XmlWorkspaceFacadeManager.class,
                    "XML Workspace (.gbw) File");
            modelMgr.registerFacadeManagerForProtocol("xmlgenomicaxis",
                    api.facade.concrete_facade.xml.XmlGenomicAxisFacadeManager.class,
                    "XML Assembly (.gba) File");
            modelMgr.registerFacadeManagerForProtocol("xmlservice",
                    api.facade.concrete_facade.xml.XmlServiceFacadeManager.class,
                    "XML Service URL");
            modelMgr.registerFacadeManagerForProtocol("gff", 
            		api.facade.concrete_facade.gff3.Gff3GenomicAxisFacadeManager.class,
            		"Generic Feature Format, Version 3 (.gff) File");

            // Editor Registration
            //      sessionMgr.registerEditorForType(api.entity_model.model.genetics.Species.class,
            //        client.gui.components.assembly.genome_view.GenomeView.class,"Genome View", "ejb");
            //      sessionMgr.registerEditorForType(api.entity_model.model.assembly.GenomicAxis.class,
            //        client.gui.components.annotation.debug_view.DebugView.class,"Annotation Debug View", "ejb");
            splash.setStatusText("Initializing Visualization Components...");
            final Class vizardEditor = client.gui.components.annotation.axis_annotation.GenomicAxisAnnotationEditor.class;
            // OMIT for CONVERSION
            sessionMgr.registerEditorForType(
                    api.entity_model.model.assembly.GenomicAxis.class,
                    vizardEditor, "Genomic Axis Annotation", "xmlgenomicaxis", true);
            sessionMgr.registerEditorForType(
                    api.entity_model.model.assembly.GenomicAxis.class,
                    vizardEditor, "Genomic Axis Annotation", "gff", false);

            final Class[] editorClasses = new Class[]{vizardEditor};
            Class editorClass;
            for (int i = 0; i < editorClasses.length; i++) {
                editorClass = editorClasses[i];
                //Sub-Editor Registration
                sessionMgr.registerSubEditorForMainEditor(editorClass,
                        client.gui.components.annotation.consensus_sequence_view.ConsensusSequenceView.class);
                sessionMgr.registerSubEditorForMainEditor(editorClass,
                        client.gui.components.annotation.transcript_translate_view.TranscriptTranslateView.class);
                sessionMgr.registerSubEditorForMainEditor(editorClass,
                        client.gui.components.annotation.query_alignment_view.QueryAlignmentView.class);
                sessionMgr.registerSubEditorForMainEditor(editorClass,
                        client.gui.components.annotation.subject_alignment_view.SubjectAlignmentsView.class);
                sessionMgr.registerSubEditorForMainEditor(editorClass,
                        client.gui.components.annotation.ga_feature_report_view.FeatureReportView.class);
                sessionMgr.registerSubEditorForMainEditor(editorClass,
                        client.gui.components.other.subj_seq_report.SubjectSequenceReport.class);
                sessionMgr.registerSubEditorForMainEditor(editorClass,
                        client.gui.components.other.evidence_report.EvidenceReport.class);
                sessionMgr.registerSubEditorForMainEditor(editorClass,
                        client.gui.components.annotation.sequence_analysis_view.SequenceAnalysisResultsView.class);
            }

            // This is for Preference Controller panels
            sessionMgr.registerPreferenceInterface(
                    client.gui.other.panels.BackupPanel.class,
                    client.gui.other.panels.BackupPanel.class);
            sessionMgr.registerPreferenceInterface(
                    client.gui.other.panels.TierPanel.class,
                    client.gui.other.panels.TierPanel.class);
            sessionMgr.registerPreferenceInterface(
                    client.gui.other.panels.ApplicationSettingsPanel.class,
                    client.gui.other.panels.ApplicationSettingsPanel.class);
            sessionMgr.registerPreferenceInterface(
                    client.gui.other.panels.ColorPanel.class,
                    client.gui.other.panels.ColorPanel.class);
            sessionMgr.registerPreferenceInterface(
                    client.gui.other.panels.FeaturePanel.class,
                    client.gui.other.panels.FeaturePanel.class);
            sessionMgr.registerPreferenceInterface(
                    client.gui.other.panels.SpliceFilePanel.class,
                    client.gui.other.panels.SpliceFilePanel.class);
            sessionMgr.registerPreferenceInterface(
                    client.gui.other.panels.ViewSettingsPanel.class,
                    client.gui.other.panels.ViewSettingsPanel.class);
            sessionMgr.registerPreferenceInterface(
                    DataSourceSettings.class,
                    DataSourceSettings.class);
            sessionMgr.registerPreferenceInterface(
                    client.gui.other.panels.TransTransPanel.class,
                    client.gui.other.panels.TransTransPanel.class);

            //sessionMgr.registerPreferenceInterface(client.gui.other.panels.AlignViewPanel.class,client.gui.other.panels.AlignViewPanel.class);
            sessionMgr.registerPreferenceInterface(
                    client.gui.other.panels.ConsSeqViewPanel.class,
                    client.gui.other.panels.ConsSeqViewPanel.class);
            sessionMgr.registerPreferenceInterface(
                    client.gui.other.panels.SequenceAnalysisResultsPanel.class,
                    client.gui.other.panels.SequenceAnalysisResultsPanel.class);
            sessionMgr.registerPreferenceInterface(
                    client.gui.other.panels.GroupSettingsPanel.class,
                    client.gui.other.panels.GroupSettingsPanel.class);


            //Add Load Progress Meter for iiop only
            sessionMgr.setModelProperty("PropertyForInspectorToWatch",
                    "CurationEnabled");

            ServerStatusReportManager.getReportManager()
                    .startCheckingForReport();

            sessionMgr.setSplashPanel(new SplashPanel());

            splash.setStatusText("Connecting to Remote Data Sources...");
            FacadeManager.addProtocolToUseList("xmlgenomicaxis");
            FacadeManager.addProtocolToUseList("xmlservice");
            FacadeManager.addProtocolToUseList("xmlfeature");
            FacadeManager.addProtocolToUseList("gff");

            //Start First Browser
            final Browser mainBrowser = sessionMgr.newBrowser();
            splash.setVisible(false);
            mainBrowser.getBrowserModel()
                    .setModelProperty("DefaultSubViewName",
                            client.gui.components.annotation.consensus_sequence_view.ConsensusSequenceView.DEFAULT_NAME);

            // If there is no login info, pop-up dialog to get it
// JCVI LLF, 10/23/2006
//            if (sessionMgr.getModelProperty(SessionMgr.USER_NAME) == null
//                    || sessionMgr.getModelProperty(SessionMgr.USER_NAME).equals("")
//                    && modelMgr.getNumberOfLoadedGenomeVersions() == 0) {
//                final int answer =
//                        JOptionPane.showConfirmDialog(mainBrowser, "Please enter your CDS login information.", "Information Required", JOptionPane.OK_CANCEL_OPTION);
//                if (answer != JOptionPane.CANCEL_OPTION) {
//                    PrefController.getPrefController().getPrefInterface(DataSourceSettings.class, mainBrowser);
//                }
//            }
            // FacadeManager.addProtocolToUseList("ejb");
            splash.setStatusText("Connected.");
        } catch (Exception ex) {
            SessionMgr.getSessionMgr().handleException(ex);
        } finally {
            splash.setVisible(false);
            splash.dispose();
        }
    }
}