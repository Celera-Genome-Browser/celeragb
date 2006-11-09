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
package client.gui.other.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.roles.PrefEditor;
import client.gui.framework.session_mgr.*;
import client.gui.framework.navigation_tools.AutoNavigationMgr;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies
 * @version $Id$
 */

public class ApplicationSettingsPanel extends JPanel implements PrefEditor{
  private boolean settingsChanged=false;
  private static final String SUBVIEW_FOCUS = "FocusSubviewsUponNavigation";
  JCheckBox subviewFocusCheckBox = new JCheckBox();
  JCheckBox subEditors = new JCheckBox();
  JCheckBox memoryUsage = new JCheckBox();
  JCheckBox navComplete = new JCheckBox();
  SessionMgr sessionMgr=SessionMgr.getSessionMgr();
  MySessionModelListener sessionModelListener = new MySessionModelListener();
  ButtonGroup buttonGroup = new ButtonGroup();
  Map buttonToLookAndFeel=new HashMap();

  public ApplicationSettingsPanel(JFrame parentFrame) {
    try {
      sessionMgr.addSessionModelListener(sessionModelListener);
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }


  public String getDescription() {
    return "Set various browser preferences, including Look and Feel and layout settings.";
  }


  public String getPanelGroup() {
    return PrefController.SYSTEM_EDITOR;
  }

  private void jbInit() throws Exception {
    JPanel pnlLayoutOptions=new JPanel();
    pnlLayoutOptions.setLayout(new BoxLayout(pnlLayoutOptions,BoxLayout.Y_AXIS));
    pnlLayoutOptions.setBorder(new javax.swing.border.TitledBorder("Browser Options"));
    subEditors.setText("Display SubViews When Available");
    subEditors.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        settingsChanged=true;
      }
    });
    subEditors.setSelected(((Boolean)sessionMgr.getModelProperty(SessionMgr.DISPLAY_SUB_EDITOR_PROPERTY)).booleanValue());

    subviewFocusCheckBox.setText("Focus SubViews Upon Navigation");
    subviewFocusCheckBox.setBounds(new Rectangle(25, 199, 222, 19));
    subviewFocusCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SessionMgr.getSessionMgr().setModelProperty(SUBVIEW_FOCUS,
          new Boolean(subviewFocusCheckBox.isSelected()));
      }
    });
    if (SessionMgr.getSessionMgr().getModelProperty(SUBVIEW_FOCUS)==null) {
      SessionMgr.getSessionMgr().setModelProperty(SUBVIEW_FOCUS, Boolean.TRUE);
    }
    else {
      boolean tmpBoolean = ((Boolean)
        SessionMgr.getSessionMgr().getModelProperty(SUBVIEW_FOCUS)).booleanValue();
      subviewFocusCheckBox.setSelected(tmpBoolean);
    }

    memoryUsage.setText("Display Memory Usage Meter");
    memoryUsage.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        settingsChanged=true;
      }
    });

    memoryUsage.setSelected(
        ((Boolean)sessionMgr.getModelProperty(SessionMgr.DISPLAY_FREE_MEMORY_METER_PROPERTY)).booleanValue()
    );

    pnlLayoutOptions.add(Box.createVerticalStrut(5));
    pnlLayoutOptions.add(subEditors);
    pnlLayoutOptions.add(Box.createVerticalStrut(5));
    pnlLayoutOptions.add(subviewFocusCheckBox);
    pnlLayoutOptions.add(Box.createVerticalStrut(5));
    pnlLayoutOptions.add(memoryUsage);
    pnlLayoutOptions.add(Box.createVerticalStrut(5));
    this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    this.add(Box.createVerticalStrut(10));
    this.add(pnlLayoutOptions);

    JPanel popupPanel=new JPanel();
    popupPanel.setBorder(new javax.swing.border.TitledBorder("Pop-up Information Options"));
    navComplete.setText("Show Navigation/Search complete messages");
    navComplete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        settingsChanged=true;
      }
    });
    popupPanel.setLayout(new BoxLayout(popupPanel,BoxLayout.Y_AXIS));
    popupPanel.add(Box.createVerticalStrut(5));
    navComplete.setSelected(AutoNavigationMgr.getAutoNavigationMgr().isShowingNavigationCompleteMsgs());
    popupPanel.add(navComplete);
    popupPanel.add(Box.createVerticalStrut(5));
    this.add(Box.createVerticalStrut(20));
    this.add(popupPanel);


    JPanel pnlLookAndFeelOptions=new JPanel();
    pnlLookAndFeelOptions.setBorder(new javax.swing.border.TitledBorder("Look and Feel Options"));

    pnlLookAndFeelOptions.setLayout(new BoxLayout(pnlLookAndFeelOptions,BoxLayout.Y_AXIS));
    pnlLookAndFeelOptions.add(Box.createVerticalStrut(20));
    UIManager.LookAndFeelInfo[] infos=UIManager.getInstalledLookAndFeels();
    JRadioButton rb;
    for (int i=0;i<infos.length;i++) {
      rb=new JRadioButton(infos[i].getName());
      rb.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          settingsChanged=true;
        }
      });
      if (UIManager.getLookAndFeel().getName().equals(infos[i].getName())) rb.setSelected(true);
      buttonGroup.add(rb);
      buttonToLookAndFeel.put(rb.getModel(),infos[i].getClassName());
      pnlLookAndFeelOptions.add(rb);
      pnlLookAndFeelOptions.add(Box.createVerticalStrut(15));
    }
    this.add(Box.createVerticalStrut(20));
    this.add(pnlLookAndFeelOptions);

  }

  public void dispose(){
     sessionMgr.removeSessionModelListener(sessionModelListener);
  }

  public String getName() { return "Application Settings"; }


  class MySessionModelListener implements SessionModelListener {
    public void browserAdded(BrowserModel browserModel){}
    public void browserRemoved(BrowserModel browserModel){}
    public void sessionWillExit(){}
     public void modelPropertyChanged(Object key, Object oldValue, Object newValue){
        if (key.equals(SessionMgr.DISPLAY_FREE_MEMORY_METER_PROPERTY)) memoryUsage.setSelected(
          ((Boolean)newValue).booleanValue());
        if (key.equals(SUBVIEW_FOCUS)) subviewFocusCheckBox.setSelected(
          ((Boolean)newValue).booleanValue());
        if (key.equals(SessionMgr.DISPLAY_SUB_EDITOR_PROPERTY)) subEditors.setSelected(
          ((Boolean)newValue).booleanValue());
     }
  }

  public void cancelChanges() { settingsChanged = false; }

  public boolean hasChanged() { return settingsChanged; }

  public String[] applyChanges() {
    settingsChanged=false;
    if (memoryUsage.isSelected()!=((Boolean)sessionMgr.
      getModelProperty(SessionMgr.DISPLAY_FREE_MEMORY_METER_PROPERTY)).booleanValue()) {
        sessionMgr.setModelProperty(SessionMgr.DISPLAY_FREE_MEMORY_METER_PROPERTY,
        new Boolean(memoryUsage.isSelected()));
    }
    if (subEditors.isSelected()!=((Boolean)sessionMgr.
      getModelProperty(SessionMgr.DISPLAY_SUB_EDITOR_PROPERTY)).booleanValue()) {
        sessionMgr.setModelProperty(SessionMgr.DISPLAY_SUB_EDITOR_PROPERTY,
        new Boolean(subEditors.isSelected()));
    }
    if (subviewFocusCheckBox.isSelected()!=((Boolean)sessionMgr.
      getModelProperty(SUBVIEW_FOCUS)).booleanValue()) {
        sessionMgr.setModelProperty(SUBVIEW_FOCUS,
        new Boolean(subviewFocusCheckBox.isSelected()));
    }
    AutoNavigationMgr.getAutoNavigationMgr().showNavigationCompleteMsgs(navComplete.isSelected());
    try {
      SessionMgr.getSessionMgr().setLookAndFeel((String)buttonToLookAndFeel.get(buttonGroup.getSelection()));
    }
    catch (Exception ex) {
      SessionMgr.getSessionMgr().handleException(ex);
    }
    return NO_DELAYED_CHANGES;
  }


}