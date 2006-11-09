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

/**
 * Title:        Your Product Name<p>
 * Description:  This is the main Browser in the System<p>
 * @author Peter Davies
 * @version
 */
package client.gui.framework.navigation_tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import api.entity_model.access.observer.SequenceAnalysisObserver;
import api.entity_model.access.report.BlastParameters;
import api.entity_model.access.report.PropertyReport;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import api.stub.data.GenomeVersionInfo;
import api.stub.data.SequenceAnalysisQueryParameters;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import api.stub.sequence.DNASequenceStorage;
import api.stub.sequence.ProteinSequenceStorage;
import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceHelper;
import client.gui.framework.browser.Browser;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListener;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.other.panels.SequenceAnalysisResultsPanel;
import client.shared.text_component.StandardTextArea;
import shared.util.WhiteSpaceUtils;

public class SequenceAnalysisDialog {
   private static final int      DATA_CHUNK_SIZE         = 350000;
   private static final String   DISABLE_CHUNK_WARNING   = "SequenceAnalysisDisableChunkWarning";
   private static final String   DEFAULT_DEFLINE_STRING  = ">Default";
   private static final String   PROTEIN_BLAST_X_TYPE    = "BlastX (protein)";
   private static final String   NUCLEOTIDE_BLAST_N_TYPE = "BlastN (nucleotide/DNA)";
   private static final String   SIM4_N_TYPE             = "SIM4 (nucleotide/DNA)";
   private static final String   GENEWISE_P_TYPE         = "GENEWISE (protein/DNA)";
   private static final int      MAX_REGION_LIMIT        = 10000000;

   private static SequenceAnalysisDialog userDialog = new SequenceAnalysisDialog();
   private SequenceAnalysisObserver seqAnalysisObserver = new MySequenceAnalysisObserver();
   private BrowserModelListener browserModelListener = new MyBrowserModelListener();
   private String[] names = {"Argument", "Value"};
   private Browser browser;
   private BrowserModel browserModel;
   private ArrayList arguments = new ArrayList();
   private ArrayList argumentValues = new ArrayList();
   private boolean activeSearch = false;
   private Sequence querySequence;
   JDialog mainDialog;
   JTable table = new JTable();
   JPanel mainPanel = new JPanel();
   DefaultComboBoxModel typeModel = new DefaultComboBoxModel();
   DefaultComboBoxModel macroModel = new DefaultComboBoxModel();
   JPanel argumentPanel = new JPanel();
   TitledBorder titledBorder1;
   TitledBorder titledBorder2;
   TitledBorder titledBorder3;
   JScrollPane argumentScrollPane = new JScrollPane();
   JButton searchButton = new JButton();
   JPanel macroPanel = new JPanel();
   TitledBorder titledBorder5;
   JComboBox macroComboBox = new JComboBox();
   JLabel macroLabel = new JLabel();
   JButton newMacroButton = new JButton();
   JButton deleteButton = new JButton();
   Border border1;
   TitledBorder titledBorder4;
   JButton closeButton = new JButton();
   JScrollPane jScrollPane1 = new JScrollPane();
   JPanel fgPanel = new JPanel();
   JPanel sequencePanel = new JPanel();
   Border border2;
   TitledBorder titledBorder6;
   JLabel queryLabel = new JLabel();
   JLabel subjectLabel = new JLabel();
   ButtonGroup subjectButtonGroup = new ButtonGroup();
   JScrollPane subjectScrollPane = new JScrollPane();
   JTextArea subjectTextArea = new MyJTextArea();
   JButton clearTextButton = new JButton();
   JComboBox typeComboBox = new JComboBox();
   JLabel macroTypeLabel = new JLabel();
   JButton optionsButton = new JButton();


   private SequenceAnalysisDialog() {
      this.browser = SessionMgr.getSessionMgr().getActiveBrowser();
      this.browserModel = browser.getBrowserModel();
      try {
         jbInit();
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
      browserModel.addBrowserModelListener(browserModelListener);
      //  Check to see if search sequence is pending.
      handleSequenceRequest();

      // Check for Chunk Warning Preference
      if ( SessionMgr.getSessionMgr().getModelProperty(DISABLE_CHUNK_WARNING)==null ) {
         SessionMgr.getSessionMgr().setModelProperty(DISABLE_CHUNK_WARNING, Boolean.FALSE);
      }
   }

   public static SequenceAnalysisDialog getSequenceAnalysisDialog() { return (userDialog);}
   public void showSearchDialog() {
      mainDialog.show();
      mainDialog.toFront();
   }

   private void jbInit() throws Exception {
      mainDialog = new JDialog(browser, "Sequence Analysis", false);
      titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(149, 142, 130)),"Settings");
      titledBorder2 = new TitledBorder("");
      titledBorder3 = new TitledBorder("Rule Settings");
      titledBorder5 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(149, 142, 130)),"Sequence Analysis");
      titledBorder4 = new TitledBorder("Edit Data");
      border2 = BorderFactory.createEmptyBorder();
      titledBorder6 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(149, 142, 130)),"Sequence");
      argumentPanel.setBorder(titledBorder1);
      argumentPanel.setBounds(new Rectangle(13, 133, 321, 159));
      argumentPanel.setLayout(null);
      mainDialog.getContentPane().setLayout(null);

      table = new JTable(new MyTableModel());
      table.setBorder(BorderFactory.createLineBorder(Color.black));
      table.setPreferredScrollableViewportSize(new Dimension(185, 145));
      table.setColumnSelectionAllowed(false);
      table.getTableHeader().setReorderingAllowed(false);
      table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      //Create the scroll pane and add the table to it.
      argumentScrollPane = new JScrollPane(table);

      //Add the scroll pane to this window.
      mainPanel.setBorder(titledBorder2);
      mainPanel.setBounds(new Rectangle(6, 6, 345, 530));
      mainPanel.setLayout(null);

      closeButton.setToolTipText("Close Sequence Analysis Dialog");
      closeButton.setText("Close");
      closeButton.setBounds(new Rectangle(253, 493, 81, 27));
      closeButton.addActionListener(new java.awt.event.ActionListener() {
                                       public void actionPerformed(ActionEvent e) {
                                          closeButton_actionPerformed(e);
                                       }
                                    });
      searchButton.addActionListener(new java.awt.event.ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                           searchButton_actionPerformed(e);
                                        }
                                     });
      searchButton.addActionListener(new SearchActionListener());
      searchButton.setBounds(new Rectangle(13, 493, 81, 27));
      searchButton.setText("Search");
      macroComboBox.setModel(macroModel);
      macroComboBox.addActionListener(new ActionListener(){
                                         public void actionPerformed(ActionEvent evt) {
                                            if ( evt.getSource()==macroComboBox ) {
                                               setStates();
                                            }
                                         }});
      macroPanel.setBorder(titledBorder5);
      macroPanel.setBounds(new Rectangle(10, 10, 321, 116));
      macroPanel.setLayout(null);
      macroLabel.setText("Macro:");
      macroLabel.setBounds(new Rectangle(16, 48, 51, 24));
      macroComboBox.setBounds(new Rectangle(71, 50, 235, 21));
      newMacroButton.setToolTipText("Add A New Analysis Macro");
      newMacroButton.setText("New");
      newMacroButton.setBounds(new Rectangle(68, 79, 72, 27));
      newMacroButton.addActionListener(new java.awt.event.ActionListener() {
                                          public void actionPerformed(ActionEvent e) {
                                             newMacroButton_actionPerformed(e);
                                          }
                                       });
      deleteButton.addActionListener(new java.awt.event.ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                           deleteButton_actionPerformed(e);
                                        }
                                     });
      deleteButton.setBounds(new Rectangle(171, 79, 72, 27));
      deleteButton.setText("Delete");
      deleteButton.setToolTipText("Delete Selected Analysis Macro");
      jScrollPane1.setBounds(new Rectangle(15, 103, 292, 152));

      argumentScrollPane.setSize(200, 145);
      argumentScrollPane.setBounds(new Rectangle(23, 24, 271, 122));

      fgPanel.setLayout(new BoxLayout(fgPanel,BoxLayout.Y_AXIS));
      sequencePanel.setBorder(titledBorder6);
      sequencePanel.setBounds(new Rectangle(13, 298, 321, 188));
      sequencePanel.setLayout(null);
      queryLabel.setText("Query:  Internal Consensus");
      queryLabel.setBounds(new Rectangle(15, 30, 209, 24));
      subjectLabel.setBounds(new Rectangle(15, 50, 209, 24));
      subjectLabel.setText("Subject:  Type or Paste Sequence");
      //externalDatabaseRadioButton.setText("Database");
      //externalDatabaseRadioButton.setBounds(new Rectangle(19, 63, 95, 22));
      //userDefinedRadioButton.setBounds(new Rectangle(19, 87, 120, 22));
      //userDefinedRadioButton.setText("User-Defined");
      //userDefinedRadioButton.setSelected(true);
      clearTextButton.setText("Clear Text");
      clearTextButton.setBounds(new Rectangle(18, 85, 90, 19));
      //subjectTextArea.setBounds(new Rectangle(15, 45, 200,90));
      clearTextButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent ae) {
                                              subjectTextArea.setEditable(true);
                                              subjectTextArea.setText("");
                                              setButtonStates();
                                           }
                                        });

      // Establish the analysis types.
      Map tmpTypes = SequenceAnalysisMgr.getSequenceAnalysisMgr().getAnalysisTypes();
      for ( Iterator it = tmpTypes.keySet().iterator(); it.hasNext(); ) {
         String keystr = (String)it.next();
         if ( !keystr.equalsIgnoreCase("sim4") ) {
            typeModel.addElement(keystr);
         }
      }
//    typeComboBox.setModel(new BlastAlgorithmComboBoxModel());
      typeComboBox.setBounds(new Rectangle(71, 21, 235, 21));
      typeComboBox.addActionListener(new ActionListener(){
                                        public void actionPerformed(ActionEvent evt) {
                                           if ( evt.getSource()==typeComboBox ) {
                                              analysisTypeChanged();
                                           }
                                        }});
      typeComboBox.setModel(typeModel);
      macroTypeLabel.setBounds(new Rectangle(16, 19, 51, 24));
      macroTypeLabel.setText("Type:");
      optionsButton.addActionListener(new java.awt.event.ActionListener() {
                                         public void actionPerformed(ActionEvent e) {
                                            PrefController.getPrefController().getPrefInterface(
                                                                                               SequenceAnalysisResultsPanel.class, browser);
                                         }
                                      });
      optionsButton.setBounds(new Rectangle(133, 493, 81, 27));
      optionsButton.setText("Options");
      optionsButton.setToolTipText("Edit Preferences");
      //subjectButtonGroup.add(externalDatabaseRadioButton);
      // subjectButtonGroup.add(userDefinedRadioButton);
      subjectScrollPane.setBounds(new Rectangle(20, 111, 288, 67));
      //externalDatabaseComboBox.setBounds(new Rectangle(118, 64, 187, 21));
      mainDialog.getContentPane().add(mainPanel, null);
      mainPanel.add(macroPanel, null);
      macroPanel.add(macroLabel, null);
      macroPanel.add(macroComboBox, null);
      macroPanel.add(macroTypeLabel, null);
      macroPanel.add(typeComboBox, null);
      macroPanel.add(deleteButton, null);
      macroPanel.add(newMacroButton, null);
      jScrollPane1.getViewport().add(fgPanel, null);
      mainPanel.add(sequencePanel, null);
      sequencePanel.add(queryLabel, null);
      sequencePanel.add(subjectLabel, null);
      //sequencePanel.add(userDefinedRadioButton, null);
      sequencePanel.add(subjectScrollPane, null);
      // sequencePanel.add(externalDatabaseRadioButton, null);
      //sequencePanel.add(externalDatabaseComboBox, null);
      sequencePanel.add(clearTextButton, null);
      mainPanel.add(argumentPanel, null);
      argumentPanel.add(argumentScrollPane, null);
      mainPanel.add(searchButton, null);
      mainPanel.add(optionsButton, null);
      mainPanel.add(closeButton, null);
      subjectScrollPane.getViewport().add(subjectTextArea, null);
      jScrollPane1.getViewport().add(fgPanel, null);

      mainDialog.setSize(363, 572);
      typeComboBox.setSelectedIndex(0);
      analysisTypeChanged();
      table.setDefaultEditor(table.getModel().getColumnClass(1), new MyTableCellEditor());
      mainDialog.setLocation(10, 10);
      mainDialog.setVisible(true);
      /**
       * Due to an NT jdk1.3.0.X bug the image icon will not show if isResizable is false.
       * When moving to a new JDK make it false again.
       */
      mainDialog.setResizable(true);
   }


   /** Call this method when any doubt exists as to enable/disable button state */
   private void setButtonStates() {
      if ( activeSearch ) {
         searchButton.setToolTipText("Analysis Currently Running");
         searchButton.setEnabled(false);
      } // Signify outstanding sequence analysis
      else {
         searchButton.setToolTipText("No Analysis Running");
         String sequenceString = subjectTextArea.getText();
         BrowserModel browserModel = browser.getBrowserModel();
         Range subViewFixedRange = browserModel.getSubViewFixedRange();

         //if (userDefinedRadioButton.isSelected()) {
         if ( (subViewFixedRange == null) ||
              (subViewFixedRange.getMagnitude() == 0) || (sequenceString.length() == 0) ) {
            searchButton.setEnabled(false);
         } // No range.
         else if ( subViewFixedRange.getMagnitude() > MAX_REGION_LIMIT ) {
            searchButton.setToolTipText("Region magnitude limit of "+MAX_REGION_LIMIT+" exceeded");
            searchButton.setEnabled(false);
         } // Exceeds limit
         else {
            searchButton.setEnabled(true);
         } // All OK.
         // } // Internal Sequence Analysis
         // else {
         /*
    if (subViewFixedRange.getMagnitude()==0)
           searchButton.setEnabled(false);
         else
           searchButton.setEnabled(true);
      */
         // } // External Sequence Analysis
      } // Must check state to see if should enable.
   } // End method


   /**
    * This method sets up the macro list due to setting of analysis type combo box
    * to zero directly or user actionPerformed on the combo box.
    */
   private void analysisTypeChanged() {
      if ( typeModel.getSelectedItem()==null ) return;
      macroModel.removeAllElements();
      Map tmpMacro = SequenceAnalysisMgr.getSequenceAnalysisMgr().
                     getMacrosForAnalysisType(typeModel.getSelectedItem().toString());
      for ( Iterator it = tmpMacro.keySet().iterator(); it.hasNext(); ) {
         macroModel.addElement(tmpMacro.get(it.next()));
      }
      setStates();
      // Set up the External DB ComboBox here.
      // getRelevantDatabases();
   }


   /**
    * This method is to be used during construction and also whenever someone
    * makes a request to search a given sequence.
    */
   private void handleSequenceRequest() {
      Object tmpObject = browserModel.getModelProperty(SequenceAnalysisQueryParameters.PARAMETERS_PROPERTY_KEY);
      if ( tmpObject == null ) return;

      SequenceAnalysisQueryParameters paramObject = (SequenceAnalysisQueryParameters)tmpObject;
      Sequence ibpSequence = paramObject.getSubjectSequence();
      querySequence = paramObject.getQuerySequence();
      
      if ( ibpSequence==null ) return;
      subjectTextArea.setText(SequenceHelper.toString(ibpSequence));
      if ( paramObject.isInternalSequence() ) {
         // NOTE: this criterion is subject to change.  Later there will
         // be more robust rules for deciding whether or not to set the
         // editability to false.  They should involve such things as:
         // whether the selected database is Internal.  For now: there
         // is only one database.  So this will always be editable==true.
         /*
           if (externalDatabaseComboBox.getItemCount() > 1)
             subjectTextArea.setEditable(false);
          */
      } // Will align features to the axis.
      else {
         subjectTextArea.setEditable(true);
      } // Will not necessarily match the axis.
      setButtonStates();
   }


   private void setStates() {
      if ( macroComboBox.getItemCount()<=0 ||
           macroComboBox.getSelectedItem()==null ) return;

      // Clear out the old table values.
      arguments.clear();
      argumentValues.clear();

      SequenceAnalysisInfo currentMacro = (SequenceAnalysisInfo)macroComboBox.getSelectedItem();
      AnalysisType tmpType = SequenceAnalysisMgr.getSequenceAnalysisMgr().
                             getAnalysisType(typeComboBox.getSelectedItem().toString());
      // Get the argument names according to the Analysis Type.
      TreeMap tmpTypeArgs = tmpType.getArguments();
      // Get the specific Argument values for the Macro selected.
      TreeMap tmpMacroArgs = currentMacro.getArgumentCollection();

      // Base the table according to the args defines in the Analysis Type object.
      // If the current macro knows the args, it sets them; otherwise, insert the default.
      for ( Iterator it = tmpTypeArgs.keySet().iterator(); it.hasNext(); ) {
         AnalysisType.AnalysisProperty tmpProperty = (AnalysisType.AnalysisProperty)tmpTypeArgs.get(it.next());
         String tmpArgName = tmpProperty.getName();
         arguments.add(tmpArgName);
         if ( tmpMacroArgs.get(tmpArgName)!=null ) {
            argumentValues.add(tmpMacroArgs.get(tmpArgName));
         }
         else {
            argumentValues.add(tmpProperty.getDefaultValue());
         }
      }

      jScrollPane1.getVerticalScrollBar().setValue(0);
      updateData();
   }

   private void updateData() {
      ((MyTableModel)table.getModel()).fireTableDataChanged();
   }


   private void searchButton_actionPerformed(ActionEvent e) {
      if ( table.isEditing() ) {
         table.getCellEditor().stopCellEditing();
      }

      saveChanges();
   }

   private void closeButton_actionPerformed(ActionEvent e) {
      if ( table.isEditing() ) {
         table.getCellEditor().stopCellEditing();
      }

      mainDialog.hide();
   }

   private void saveChanges() {
      if ( macroComboBox.getSelectedItem()==null ) return;
      if ( !macroComboBox.getSelectedItem().toString().startsWith("Default") ) {
         // Get the settings for the new macro.
         TreeMap newSettings = new TreeMap();
         for ( int i = 0; i < arguments.size(); i++ ) {
            newSettings.put(arguments.get(i), argumentValues.get(i));
         }

         SequenceAnalysisInfo newInfo = new SequenceAnalysisInfo(macroComboBox.getSelectedItem().toString(),
                                                                 typeComboBox.getSelectedItem().toString(), newSettings);
         SequenceAnalysisMgr.getSequenceAnalysisMgr().addSequenceAnalysisInfo(newInfo);
      }
   }


   private void newMacroButton_actionPerformed(ActionEvent e) {
      SequenceAnalysisWizard newWizard = new SequenceAnalysisWizard(mainDialog, typeModel.getSelectedItem());
      newWizard.setVisible( true );
      analysisTypeChanged();
   }


   private void deleteButton_actionPerformed(ActionEvent e) {
      if ( macroComboBox.getSelectedItem().toString().startsWith("Default") ) return;
      if ( macroComboBox.getSelectedItem()!=null ) {
         String targetName = ((SequenceAnalysisInfo)macroComboBox.getSelectedItem()).getName();
         int answer = JOptionPane.showConfirmDialog(mainDialog, "Deleting macro: "+targetName+".\n"+
                                                    "Are you sure?", "Macro Deletion", JOptionPane.YES_NO_OPTION);
         if ( answer==JOptionPane.NO_OPTION ) return;
         SequenceAnalysisInfo targetInfo = SequenceAnalysisMgr.getSequenceAnalysisMgr().getMacrosByName(targetName);
         SequenceAnalysisMgr.getSequenceAnalysisMgr().deleteSequenceAnalysisInfo(targetInfo);
      }
      analysisTypeChanged();
   }


   private class MyTableModel extends AbstractTableModel {
      public int getColumnCount() { return (names.length);}
      public int getRowCount() { return (arguments.size());}
      public String getColumnName(int column) {return (names[column]);}
      public Class getColumnClass(int c) {return (getValueAt(0, c).getClass());}
      public boolean isCellEditable(int row, int col) {
         if ( macroComboBox.getSelectedItem().toString().startsWith("Default") ) return (false);
         if ( col==1 ) return (true);
         else return (false);
      }

      public Object getValueAt(int row, int col) {
         switch ( col ) {
            case 0: {
                  return (arguments.get(row));
               }
            case 1: {
                  return (argumentValues.get(row));
               }
         }
         return (null);
      }

      public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         if ( aValue==null ) return;
         argumentValues.set(rowIndex, aValue);
         saveChanges();
      }
   }


   private class MyTableCellEditor extends DefaultCellEditor {
      public MyTableCellEditor() {
         super(new JComboBox());
      }

      public Component getTableCellEditorComponent(JTable table, Object value,
                                                   boolean isSelected, int row, int column) {
         delegate.setValue(value);
         return (getSpecialEditorComponent(row, column));
      }

      private Component getSpecialEditorComponent(int row, int column) {
         // The column should always be 1.  The real important part is the row.
         // That will determine the possible values to be set.
         AnalysisType tmpType = SequenceAnalysisMgr.getSequenceAnalysisMgr().
                                getAnalysisType(typeComboBox.getSelectedItem().toString());
         // Get the argument names according to the Analysis Type.
         TreeMap tmpTypeArgs = tmpType.getArguments();
         String targetArgument = (String)arguments.get(row);
         AnalysisType.AnalysisProperty tmpProperty =
         (AnalysisType.AnalysisProperty)tmpTypeArgs.get(targetArgument);

         if ( tmpProperty.getEditor().equals(AnalysisType.STRING_COMBO_EDITOR) ) {
            editorComponent = new JComboBox();
            JComboBox tmpBox = (JComboBox) editorComponent;
            delegate = new EditorDelegate() {
               public void setValue(Object value) {
                  ((JComboBox)editorComponent).setSelectedItem(value);
               }

               public Object getCellEditorValue() {
                  return((JComboBox)editorComponent).getSelectedItem();
               }

               public boolean shouldSelectCell(EventObject anEvent) {
                  if ( anEvent instanceof MouseEvent ) {
                     MouseEvent e = (MouseEvent)anEvent;
                     return (e.getID() != MouseEvent.MOUSE_DRAGGED);
                  }
                  return (true);
               }
            };
            TreeMap options = tmpProperty.getPropertyOptions();
            for ( Iterator it = options.keySet().iterator(); it.hasNext(); ) {
               String tmpValue = (String)options.get(it.next());
               tmpBox.addItem(tmpValue);
            }
            tmpBox.addActionListener(delegate);
            return (tmpBox);
         }
         else if ( tmpProperty.getEditor().equals(AnalysisType.NUMBER_COMBO_EDITOR) ) {
            editorComponent = new JComboBox();
            JComboBox tmpBox = (JComboBox) editorComponent;
            delegate = new EditorDelegate() {
               public void setValue(Object value) {
                  ((JComboBox)editorComponent).setSelectedItem(value);
               }

               public Object getCellEditorValue() {
                  return((JComboBox)editorComponent).getSelectedItem();
               }

               public boolean shouldSelectCell(EventObject anEvent) {
                  if ( anEvent instanceof MouseEvent ) {
                     MouseEvent e = (MouseEvent)anEvent;
                     return (e.getID() != MouseEvent.MOUSE_DRAGGED);
                  }
                  return (true);
               }
            };
            TreeMap options = tmpProperty.getPropertyOptions();
            ArrayList boxItems = new ArrayList();

            for ( Iterator it = options.keySet().iterator(); it.hasNext(); ) {
               String tmpValue = (String)options.get(it.next());
               boxItems.add(tmpValue);
            }

            Object[] test = boxItems.toArray();
            Arrays.sort(test, new MyStringNumberComparator());
            boxItems = new ArrayList(Arrays.asList(test));

            for ( Iterator it = boxItems.iterator(); it.hasNext(); ) {
               tmpBox.addItem(it.next());
            }
            tmpBox.addActionListener(delegate);
            return (tmpBox);
         }
         // Default to the Text Field Editor
         else {
            JTextField tmpField = new JTextField((String)table.getValueAt(row, column));
            editorComponent = tmpField;
            delegate = new EditorDelegate() {
               public void setValue(Object value) {
                  ((JTextField)editorComponent).setText((value != null) ? value.toString() : "");
               }

               public Object getCellEditorValue() {
                  return((JTextField)editorComponent).getText();
               }
            };
            tmpField.addActionListener(delegate);
            return (editorComponent);
         }
      }
   }


   private class MyStringNumberComparator implements Comparator {
      public int compare(Object o1, Object o2) {
         Double d1 = new Double((String)o1);
         Double d2 = new Double((String)o2);
         return (d1.compareTo(d2));
      }
   }


   private class MyStringComparator implements Comparator {
      public int compare(Object key1, Object key2) {
         String keyName1, keyName2;
         try {
            keyName1 = (String)key1;
            keyName2 = (String)key2;
            if ( keyName1==null || keyName2==null ) return (0);
         }
         catch ( Exception ex ) {
            return (0);
         }
         return (keyName1.compareToIgnoreCase(keyName2));
      }
   }

   /**
    * An instance of this class is passed on to the mechanism that runs the
    * Sequence Analysis and broadcasts the results once they come in.
    */
   private class MySequenceAnalysisObserver implements SequenceAnalysisObserver {
      public void noteSequenceAnalysisCompleted(Axis axis, Range rangeOfSearch,
                                                PropertyReport report, boolean isDone) {
         activeSearch = !isDone;
         setButtonStates();
         browserModel.setModelProperty("SequenceAnalysisResults", report);
      }
   }

   /** Picks up sequence analysis start requests and initiates a search. */
   private class SearchActionListener implements ActionListener {
      public void actionPerformed(ActionEvent ae) {
         BrowserModel browserModel = browser.getBrowserModel();
         GenomicAxis genomicAxis =(GenomicAxis)browserModel.getMasterEditorEntity();
         MutableRange subViewFixedRange = browserModel.getSubViewFixedRange().toMutableRange();

         // As the fixed subview range
         boolean isRevComped = ((Boolean)browserModel.getModelProperty(BrowserModel.REV_COMP_PROPERTY)).booleanValue();
         if ( isRevComped ) {
            subViewFixedRange.mirror(genomicAxis.getMagnitude());
         }

         String defLineString = null;
         String sequenceString = subjectTextArea.getText();

         if ( sequenceString.startsWith(">") ) {
            String terminator = System.getProperty("line.separator");
            int endFirstLinePos = nextLineFeed(sequenceString);
            if ( endFirstLinePos == -1 ) {
               endFirstLinePos = sequenceString.indexOf('\n');
               terminator = "\n";
            } // Not line separator
            if ( endFirstLinePos > -1 ) {
               defLineString = sequenceString.substring(0, endFirstLinePos);
               // When capturing remainder of sequence, remove its extraneous ws's.
               sequenceString = WhiteSpaceUtils.removeWhiteSpace(  sequenceString.substring(endFirstLinePos + terminator.length()),
                                                                   false);
            } // Found terminator.
         } // Must keep this as def line, and remove from seq.
         else {
            sequenceString = WhiteSpaceUtils.removeWhiteSpace(  sequenceString,
                                                                false);
         } // Must remove extraneous ws's.

         if ( defLineString == null ) {
            defLineString = DEFAULT_DEFLINE_STRING;
         } // Must HAVE ap def line

         BlastParameters blastParams = buildSequenceAnalysisParameters();

         StringBuffer analysisName = new StringBuffer((String)typeComboBox.getSelectedItem().toString());

         StringBuffer tierNameString = new StringBuffer("Interactive");
         tierNameString.insert(0, analysisName + ":");
         tierNameString.replace(0,1, tierNameString.substring(0,1).toUpperCase());

         if ( subViewFixedRange.getMagnitude() > 0 ) {
            // Check magnitude and possibly alert user.
//            int sizeCheck = subViewFixedRange.getMagnitude()/DATA_CHUNK_SIZE;
//            Boolean disableWarning = (Boolean)SessionMgr.getSessionMgr().getModelProperty(DISABLE_CHUNK_WARNING);
//            if ( sizeCheck > 1 && !disableWarning.booleanValue() ) {
//               JLabel warningText = new JLabel("<html>"+"As the search range is larger than 350kb, the analysis will"+
//                                               "<br>"+"be divided into 350kb chunks."+"</html>");
//               JCheckBox warningCheckBox = new JCheckBox("Never Show This Message Again");
//               JPanel tmpPanel = new JPanel();
//               tmpPanel.setLayout(new BoxLayout(tmpPanel, BoxLayout.Y_AXIS));
//               tmpPanel.add(warningText);
//               tmpPanel.add(warningCheckBox);
//               JOptionPane.showMessageDialog(browser, tmpPanel, "Sequence Analysis Notification",
//                                             JOptionPane.OK_OPTION);
//               if ( warningCheckBox.isSelected() )
//                  SessionMgr.getSessionMgr().setModelProperty(DISABLE_CHUNK_WARNING, Boolean.TRUE);
//            }
            // Run just the right sort of sequence analysis for the circumstances of use.
            // if (userDefinedRadioButton.isSelected()) {
            if ( sequenceString.length() > 0 ) {
               Sequence bioSeq = buildSequence(analysisName.toString(), sequenceString, defLineString);
            
               //System.out.println("Internal Genomic Axis selected.");
               genomicAxis.runSequenceAnalysis( subViewFixedRange, bioSeq, querySequence,
                                                seqAnalysisObserver, tierNameString.toString(),
                                                blastParams);

               setStateForActiveSearch();
            } // Got sequences
            // } // Internal sequence analysis
            // else {
            // Use the axis sequence as the main sequence.
            //Sequence seq = getRelevantAxis().getNucleotideSeq(subViewFixedRange);
            //Sequence externalSeq = DNASequenceStorage.create( seq );
            //genomicAxis.runSequenceAnalysis( subViewFixedRange, externalSeq,
            //                                seqAnalysisObserver, tierNameString.toString(), blastParams);
            //setStateForActiveSearch();
            // }
         }
      }


      /** Creates appropriate sequence to the blast type given. */
      private Sequence buildSequence(String analysisDisplayName, String sequenceString, String defLineString) {
         if ( (PROTEIN_BLAST_X_TYPE.toUpperCase().indexOf(analysisDisplayName.toUpperCase()) >= 0) ||
              (GENEWISE_P_TYPE.toUpperCase().indexOf(analysisDisplayName.toUpperCase()) >= 0) ) {
            return (new ProteinSequenceStorage(  sequenceString, defLineString));
         } // Got sequence and type is protein.
         else if ( (NUCLEOTIDE_BLAST_N_TYPE.toUpperCase().indexOf(analysisDisplayName.toUpperCase()) >= 0) ||
                   (SIM4_N_TYPE.toUpperCase().indexOf(analysisDisplayName.toUpperCase()) >= 0) ) {
            return (DNASequenceStorage.create( sequenceString, defLineString));
         } // Got sequence and type is NOT protein.
         else {
            SessionMgr.getSessionMgr().handleException(new IllegalStateException("Unknown blast type "+analysisDisplayName));
            return (null);
         } // Unknown blast type.
      } // End method


      /** Sets up the view so that controls are indicated properly, etc. */
      private void setStateForActiveSearch() {
         activeSearch = true;
         setButtonStates();
      } // End method


      /** Read widgets to determine proper params to send over for sequence analysis run. */
      private BlastParameters buildSequenceAnalysisParameters() {
         AnalysisType targetType = SequenceAnalysisMgr.getSequenceAnalysisMgr().
                                   getAnalysisType(typeComboBox.getSelectedItem().toString());
         SequenceAnalysisInfo tmpInfo = SequenceAnalysisMgr.getSequenceAnalysisMgr().
                                        getMacrosByName(macroComboBox.getSelectedItem().toString());

         // This is the collection of display name arguments and dislpay name values
         // for the macro.
         TreeMap infoArgMap = tmpInfo.getArgumentCollection();
         // Convert the names and values to actual parameters and proceed.
         TreeMap formattedArgs = new TreeMap();

         for ( Iterator it = infoArgMap.keySet().iterator(); it.hasNext(); ) {
            String displayName = (String)it.next();
            String displayValue = (String)infoArgMap.get(displayName);
            // This is the switch.
            String actualName = targetType.getSwitchForArgumentDisplayName(displayName);
            // This is the real value
            String actualValue = targetType.getValueForValueDisplayName(displayName, displayValue);

            formattedArgs.put(actualName, actualValue);
         }

         // Build parameters object by "seeding" w/ algorithm and db.
         BlastParameters.BlastAlgorithm alg = getBlastAlgorithm();

         ArrayList dbList = new ArrayList(alg.getAvailableDatabases());
         BlastParameters.BlastDatabase selectedDatabase = null;
         for ( Iterator it = dbList.iterator(); it.hasNext(); ) {
            BlastParameters.BlastDatabase db = (BlastParameters.BlastDatabase)it.next();
            if ( db.toString().equals("Internal Genomic Axis") ) {
               selectedDatabase = db;
               break;
            }
         }

         // BlastParameters.BlastDatabase selectedDatabase
         //   = (BlastParameters.BlastDatabase)externalDatabaseComboBox.getSelectedItem();
         BlastParameters parameters = new BlastParameters(alg, selectedDatabase,
                                                          formattedArgs);

         return (parameters);
      }
   }


   private int nextLineFeed(String string) {
      String lineSep=System.getProperty("line.separator");
      int lineSepLoc=string.indexOf(lineSep);
      int lineFeedLoc=string.indexOf(0x00a);
      int charReturnLoc=string.indexOf(0x00d);
      if ( lineSepLoc==-1 ) lineSepLoc=Integer.MAX_VALUE;
      if ( lineFeedLoc==-1 ) lineFeedLoc=Integer.MAX_VALUE;
      if ( charReturnLoc==-1 ) charReturnLoc=Integer.MAX_VALUE;
      int rtn=Math.min(Math.min(lineSepLoc,lineFeedLoc),charReturnLoc);
      if ( rtn==Integer.MAX_VALUE ) return (-1);
      return (rtn);
   }


   /** Line-wrapping text area widget.  Courtesy Pete Davies. */
   private class MyJTextArea extends StandardTextArea {
      /** Override past to append text in standardized fashion. */
      public void paste() {
         Clipboard clipboard = getToolkit().getSystemClipboard();
         Transferable content = clipboard.getContents(this);
         if ( content != null ) {
            try {
               String dstData = (String)(content.getTransferData(DataFlavor.stringFlavor));
               replaceSelection(appendLineBrokenText(dstData));
            } // End try
            catch ( Exception e ) {
               getToolkit().beep();
            } // End catch
         }
         setButtonStates();
      }

      public void setText(String inputText) {
         try {
            super.setText(appendLineBrokenText(inputText));
            setButtonStates();
         }
         catch ( Exception e ) {
            getToolkit().beep();
         }
      }

      /** Adds text in a standardized way, to the text area. */
      private String appendLineBrokenText(String data) throws Exception {
         int maxSize=80;
         String lineSep=System.getProperty("line.separator");
         StringBuffer sb=new StringBuffer();
         while ( data.length()>maxSize ) {
            if ( data.startsWith(">") ) { //leave def line unwrapped
               int endlineIndex=nextLineFeed(data);
               if ( endlineIndex>-1 ) {
                  sb.append(data.substring(0,endlineIndex));
                  data=data.substring(endlineIndex);
                  continue;
               }
            }
            int wrapIndex=nextLineFeed(data);
            if ( wrapIndex>maxSize || wrapIndex==-1 ) {
               sb.append(data.substring(0,maxSize));
               sb.append(lineSep);
               data=data.substring(maxSize);
            }
            else {
               sb.append(data.substring(0,wrapIndex+lineSep.length()));
               data=data.substring(wrapIndex+lineSep.length());
            }
         } // For all segments of the line
         sb.append(data);
         return(sb.toString());
      } // End method
   } // End class


   /** Picks up browser model changes and keeps track of report data for requests. */
   private class MyBrowserModelListener extends BrowserModelListenerAdapter {
      /** Called when user has selected something different in Genomic Axis Annotation View. */
      public void browserCurrentSelectionChanged(GenomicEntity entity) {
         setButtonStates();
      }

      /** Called when the user has selected a different subview range. */
      public void browserSubViewFixedRangeChanged(Range subViewFixedRange) {
         setButtonStates();
      }

      /** Respond to shutdown of browser. */
      public void browserClosing() {
         mainDialog.hide();
         browser.repaint();
      }

      public void modelPropertyChanged(Object key, Object oldValue, Object newValue){
         if ( key.equals(SequenceAnalysisQueryParameters.PARAMETERS_PROPERTY_KEY) ) {
            handleSequenceRequest();
         }
      }
   }


   private BlastParameters.BlastAlgorithm getBlastAlgorithm() {
      String targetAlg = typeComboBox.getSelectedItem().toString();
      // Update the axis for which the algorithms are valid...
      GenomicAxis relevantAxis = (GenomicAxis)browserModel.getMasterEditorEntity();
      if ( relevantAxis == null ) {
         return (null);
      } // No axis

      // Do not bother setting up list if a non-db axis is selected.
      GenomeVersion genomeVersion = relevantAxis.getGenomeVersion();
      GenomeVersionInfo genomeVersionInfo = genomeVersion.getGenomeVersionInfo();
      if ( (browserModel.getMasterEditorEntity() == null) || (! genomeVersionInfo.isDatabaseDataSource()) ) {
         return (null);
      } // No algorithsm to list.

      List algList = (List)relevantAxis.getAvailableBlastAlgorithms();
      for ( Iterator it = algList.iterator(); it.hasNext(); ) {
         BlastParameters.BlastAlgorithm tmpAlg = (BlastParameters.BlastAlgorithm)it.next();
         if ( tmpAlg.getAlgorithmName().equals(targetAlg) ) return (tmpAlg);
      }
      return (null);
   }


   /** Finds the axis available to the browser model. */
   private GenomicAxis getRelevantAxis() {
      if ( browser == null )
         return (null);
      BrowserModel browserModel = browser.getBrowserModel();
      return(GenomicAxis)browserModel.getMasterEditorEntity();
   } // End method


   /** Helper to return all algorithms applicable to context. */
   private void getRelevantDatabases() {
      /*
      externalDatabaseComboBox.removeAllItems();
      BlastParameters.BlastAlgorithm currentAlgorithm = getBlastAlgorithm();
      ArrayList dbList = new ArrayList(currentAlgorithm.getAvailableDatabases());
      // Filter: only add to algorithms list if one or more databases is available.
      for (Iterator it = dbList.iterator(); it.hasNext(); ) {
        externalDatabaseComboBox.addItem((BlastParameters.BlastDatabase)it.next());
      }
      if (dbList.size()<=0) {
        externalDatabaseComboBox.setEnabled(false);
        externalDatabaseRadioButton.setEnabled(false);
      }
      else {
        externalDatabaseComboBox.setEnabled(true);
        externalDatabaseRadioButton.setEnabled(true);
        externalDatabaseComboBox.setSelectedIndex(0);
      }
      */
   } // End method
}