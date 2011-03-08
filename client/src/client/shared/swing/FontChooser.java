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
package client.shared.swing;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Locale;

/**
 * This class creates a font chooser panel which can be placed in dialogs for font
 * selection, additionally it contains static methods for font-chooser dialog
 * creation.
 */
public class FontChooser extends JPanel {
  private JTextField fontFamilyTField, fontStyleTField, fontSizeTField;
  private JList fontFamilyJList, fontSizeJList, fontStyleJList;
  private JLabel previewLabel;
  private DocumentListener fontFamilyDocumentListener,
                           fontStyleDocumentListener,
                           fontSizeDocumentListener;
  private JLabel fontFamilyLabel, fontSizeLabel, fontStyleLabel;
  private String[] fontFamilies;
  private final String[] fontStyles = { "Regular", "Bold", "Italic", "Bold Italic" };
  private final String[] fontSizes = { "8", "9", "10", "11", "12", "14", "16",
                                       "18", "20", "22", "24", "26", "28", "36",
                                       "48", "72" };

  private Font font;
  private Font defaultFont;
  private int size;
  private int style;
  private String family;

  /**
   * Default Constructor: the default font will be set to "Arial Plain 12pt".
   */
  public FontChooser() {
    this(new Font("Arial", Font.PLAIN, 12));
  }

  /**
   * Constructor that initializes the <code>FontChooser</code> to represent
   * the given default font.
   * @param defaultFont the default font for this </code>FontChooser</code>
   */
  public FontChooser(Font defaultFont){
    this.defaultFont = defaultFont;
    family = defaultFont.getFamily(Locale.getDefault());
    size = defaultFont.getSize();
    style = defaultFont.getStyle();
    font = defaultFont.deriveFont(defaultFont.getStyle());
    init();
  }

  /**
   *   Initializes this <code>FontChooser</code>.
   */
  protected void init(){
    JPanel fontPanel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    fontPanel.setLayout(gridbag);
    //set up the labels
    fontFamilyLabel = new JLabel("Font", JLabel.CENTER);
    fontSizeLabel = new JLabel("Font Size", JLabel.CENTER);
    fontStyleLabel = new JLabel("Font Style", JLabel.CENTER);
    // set up the text fields
    fontFamilyTField = new JTextField();
    fontStyleTField = new JTextField();
    fontStyleTField.setText(fontStyles[style]);
    fontSizeTField = new JTextField();
    fontSizeTField.setText(String.valueOf(size));//*look at later
    fontSizeTField.setDocument(new IntegerDocument() );
    //set up the font family JList
    GraphicsEnvironment gEnv =
      GraphicsEnvironment.getLocalGraphicsEnvironment();
    fontFamilies = gEnv.getAvailableFontFamilyNames();
    Arrays.sort(fontFamilies);
    fontFamilyJList = new JList(fontFamilies);
    fontFamilyJList.setVisibleRowCount(5);
    fontFamilyJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fontFamilyJList.setSelectedValue(family,true);
    fontFamilyJList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e){
        handleFamilySelectionEvent(e);
      }
    });
    JScrollPane fontFamilyScroll = new JScrollPane(fontFamilyJList);
    //set up the font style JList
    fontStyleJList = new JList(fontStyles);
    fontStyleJList.setVisibleRowCount(5);
    fontStyleJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fontStyleJList.setSelectedIndex(style);
    fontStyleJList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e){
        handleStyleSelectionEvent(e);
      }
    });
    JScrollPane fontStyleScroll = new JScrollPane(fontStyleJList);
    // set up the font size JList
    fontSizeJList = new JList(fontSizes);
    fontSizeJList.setVisibleRowCount(5);
    fontSizeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fontSizeJList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e){
        handleSizeSelectionEvent(e);
      }
    });
    JScrollPane fontSizeScroll = new JScrollPane(fontSizeJList);
    //set up the preview label
    previewLabel = new JLabel();
    JPanel previewPanel = new JPanel();
    previewPanel.add(previewLabel, BorderLayout.CENTER);
    previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
    initPreviewLabel();
    // register all of the textfield Document listeners
    fontFamilyDocumentListener = new FontFamilyDocumentListener(fontFamilyJList);
    fontFamilyTField.getDocument().addDocumentListener(fontFamilyDocumentListener);
    fontStyleDocumentListener = new FontStyleDocumentListener(fontStyleJList);
    fontStyleTField.getDocument().addDocumentListener(fontStyleDocumentListener);
    fontSizeDocumentListener = new FontSizeDocumentListener(fontSizeJList);
    fontSizeTField.getDocument().addDocumentListener(fontSizeDocumentListener);
    fontSizeTField.setText(String.valueOf(defaultFont.getSize()));
    fontFamilyTField.setText(family);
    //add all components to the fontpanel
    fontPanel.add(fontFamilyLabel,
      new GridBagConstraints(0,0,1,1,0.5,0.0,GridBagConstraints.WEST,
            GridBagConstraints.NONE,new Insets(0,4,4,4),0,0));
    fontPanel.add(fontStyleLabel,
      new GridBagConstraints(1,0,1,1,0.5,0.0,GridBagConstraints.WEST,
            GridBagConstraints.NONE,new Insets(0,4,4,4),0,0));
    fontPanel.add(fontSizeLabel,
      new GridBagConstraints(2,0,1,1,0.5,0.0,GridBagConstraints.WEST,
            GridBagConstraints.NONE,new Insets(0,4,4,4),0,0));
    fontPanel.add(fontFamilyTField,
      new GridBagConstraints(0,1,1,1,0.5,0.0,GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL,new Insets(0,4,4,4),0,0));
    fontPanel.add(fontStyleTField,
      new GridBagConstraints(1,1,1,1,0.5,0.0,GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL,new Insets(0,4,4,4),0,0));
    fontPanel.add(fontSizeTField,
      new GridBagConstraints(2,1,1,1,0.5,0.0,GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL,new Insets(0,4,4,4),0,0));
    fontPanel.add(fontFamilyScroll,
      new GridBagConstraints(0,2,1,1,0.5,0.0,GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL,new Insets(0,4,4,4),0,0));
    fontPanel.add(fontStyleScroll,
      new GridBagConstraints(1,2,1,1,0.5,0.0,GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL,new Insets(0,4,4,4),0,0));
    fontPanel.add(fontSizeScroll,
      new GridBagConstraints(2,2,1,1,0.5,0.0,GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL,new Insets(0,4,4,4),0,0));
    fontPanel.add(previewPanel,
      new GridBagConstraints(0,3,3,1,0.0,0.0,GridBagConstraints.CENTER,
            GridBagConstraints.BOTH,new Insets(0,4,4,4),0,0));
    this.add(fontPanel,BorderLayout.NORTH);
  }

  private void initPreviewLabel(){
    previewLabel.setOpaque(true);
    previewLabel.setBackground( Color.white );
    previewLabel.setBorder(BorderFactory.createLineBorder(Color.black));
    previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
    previewLabel.setFont(renderFont(family,style,size));
    previewLabel.setMaximumSize(new Dimension(300,72));
    previewLabel.setMinimumSize(new Dimension(300,72));
    previewLabel.setPreferredSize(new Dimension(300,72));
    previewLabel.setText(family);
  }

  protected Font renderFont(String family, int style, int size){
    return new Font(family,style,size);
  }

  /**
   * Returns the selected font for this <code>FontChooser</code>. Displays
   * error dialogs if the data in the font-chooser fields are invalid.
   * @return the font derived from the user selected fields, or
   *         <code>null</code> if the fields contain invalid data
   */
  public Font getFontChoice(){
    if(areValidSelections())
      return renderFont(family,style,size);
    else
      return null;
  }

  /**
   * This method checks font property selections for validity. It displays a
   * error dialogs, if there there is an invalid font_size or an invalid font name.
   * @returns <code>true</code> if there is a selected font family, and there is
   *          a selected font style, and there is either a selected font size, or
   *          there is a valid font size in the size text field
   */
  protected boolean areValidSelections(){
    String message = "";
    try{
      if(fontFamilyJList.isSelectionEmpty())
        throw new InvalidParameterException("'" + fontFamilyTField.getText() + "'" +
                  " is not a valid font name.");
      int size = Integer.parseInt(fontSizeTField.getText());
      if(size < 0 || size > Integer.MAX_VALUE)
        throw new InvalidParameterException("Font size must be between 1 and " +
                  "368.");
      this.size = size;
    }
    catch(InvalidParameterException ex){
      message = ex.getMessage();
    }
    catch(NumberFormatException numEx){
      message = "You must enter a font size between 1 and " + Integer.MAX_VALUE;
    }
    if(message.equals(""))
      return true;
    else{
      JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
            message, "Invalid Font Input", JOptionPane.ERROR_MESSAGE );
      return false;
    }
  }

  /**
   * Resets the font information to the default font;
   */
  public void resetDefault(){
    fontStyleJList.clearSelection();
    fontStyleJList.setSelectedIndex(defaultFont.getStyle());
    fontFamilyJList.clearSelection();
    fontFamilyJList.setSelectedValue(defaultFont.getFamily(Locale.getDefault()),true);
    fontSizeJList.clearSelection();
    fontSizeJList.setSelectedValue(String.valueOf(defaultFont.getSize()),true);
  }

  /**
   * Creates and returns a new dialog containing the specified
   * <code>FontChooser</code> along with "OK", "Cancel", and "Default"
   * buttons. If the "OK" or "Cancel" buttons are pressed, the dialog is
   * automatically hidden (but not disposed).  If the "Default"
   * button is pressed, the font-chooser's font will be reset to the
   * font set as the default font.
   *
   * @param c              the parent component for the dialog
   * @param title          the title for the dialog
   * @param modal          a boolean. When true, the remainder of the program
   *                       is inactive until the dialog is closed.
   * @param fontChooser    the font-chooser to be placed inside the dialog
   * @param okListener     the ActionListener invoked when "OK" is pressed
   * @param cancelListener the ActionListener invoked when "Cancel" is pressed
   */
  public static JDialog createDialog(Component c, String title, boolean modal,
                                     FontChooser fontChooser,
                                     ActionListener okListener,
                                     ActionListener cancelListener) {

    return new FontChooserDialog(c, title, modal, fontChooser,
                                 okListener, cancelListener);
  }

  /**
   * Shows a modal font-chooser dialog and blocks until the
   * dialog is hidden.  If the user presses the "OK" button, then
   * this method hides/disposes the dialog and returns the selected font
   * if it is valid. If the selected font options are invalid, it returns
   * <code>null</code>. If the user presses the "Cancel" button or closes the
   *  dialog without pressing "OK", then this method hides/disposes the dialog
   *  and returns <code>null</code>.
   *
   * @param component    the parent <code>Component</code> for the dialog
   * @param title        the String containing the dialog's title
   * @param initialFont  the initial Font set when the font-chooser is shown
   */
  public static Font showDialog(Component parentComponent, String title, Font initialFont){
    final FontChooser fc = initialFont != null? new FontChooser(initialFont): new FontChooser();
    FontTracker ft = new FontTracker(fc);
    JDialog dialog = createDialog(parentComponent,title,true,fc,ft,ft);
    dialog.addWindowListener(new WindowAdapter(){
        public void windowClosing(WindowEvent e) {
            Window w = e.getWindow();
            w.setVisible(false);
        }
      });
    dialog.addComponentListener(new ComponentAdapter(){
        public void componentHidden(ComponentEvent e) {
            Window w = (Window)e.getComponent();
            w.dispose();
        }
      });
    dialog.setVisible(true);
    return ft.getFont();
  }

  /**
   * Internal function to handle selection events for the font family JList
   */
  private void handleFamilySelectionEvent(ListSelectionEvent ev ){
    if(ev.getValueIsAdjusting() || fontFamilyJList.isSelectionEmpty() )
      return;
    family = (String)( (JList)ev.getSource() ).getSelectedValue();
    font = renderFont(family,style,size);
    previewLabel.setFont(font);
    previewLabel.setText(family);
    SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          fontFamilyTField.getDocument().removeDocumentListener(fontFamilyDocumentListener);
          fontFamilyTField.setText(family);
          fontFamilyTField.getDocument().addDocumentListener(fontFamilyDocumentListener);
        }
      });
  }

  /**
   * Internal function to handle selection events for the font style JList
   */
  private void handleStyleSelectionEvent(ListSelectionEvent ev ){
    if(ev.getValueIsAdjusting() || fontStyleJList.isSelectionEmpty())
      return;
    style = ((JList)ev.getSource()).getSelectedIndex();
    font = renderFont(family,style,size);
    font = font.deriveFont((float)(size + 0.001));
    previewLabel.setFont(font);
    SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          fontStyleTField.setText(fontStyles[style]);
        }
      });
  }

  /**
   * Internal function to handle selection events for the font stize JList
   */
  private void handleSizeSelectionEvent(ListSelectionEvent ev ){
    if(ev.getValueIsAdjusting() || fontSizeJList.isSelectionEmpty())
      return;
    String sizeString = (String)( (JList)ev.getSource()).getSelectedValue();
    size = Integer.parseInt(sizeString);
    font = renderFont(family,style,size);
    fontSizeTField.setText(sizeString);
    previewLabel.setFont(font);
  }
}

/**
 * Document type used to verify integer input
 */
class IntegerDocument extends PlainDocument {

  /**
   * Called everytime text is inserted into document
   */
  public void insertString(int offs, String str, AttributeSet a)
          throws BadLocationException{

    char[] source = str.toCharArray();
    char[] result = new char[source.length];
    int j = 0;
    for(int i=0; i < source.length; i++){
      if(Character.isDigit(source[i]))
        result[j++] = source[i];
      else
        Toolkit.getDefaultToolkit().beep();
    }
    super.insertString(offs,new String(result,0,j),a);
  }

}

/**
 * Dialog Class used to present the FontChooser dialog
 */
class FontChooserDialog extends JDialog {

  private FontChooser fontChooser;
  private JButton okButton, cancelButton, defaultButton;

  protected FontChooserDialog() {

  }

  /**
   * Creates a new <code>Dialog</code> containing a font-chooser.
   * @param c the parent for the dialog
   * @param title          the title string for the dialog
   * @param modal          a boolean. When true, the remainder of the program
   *                       is inactive until the dialog is closed.
   * @param fontChooser    the font-chooser to be placed inside the dialog
   * @param okListener     the ActionListener invoked when "OK" is pressed
   * @param cancelListener the ActionListener invoked when "Cancel" is pressed
   */
  public FontChooserDialog(Component c, String title, boolean modal,
                           FontChooser fontchooser, ActionListener okListener,
                           ActionListener cancelListener){

      super(JOptionPane.getFrameForComponent(c),title,modal);
      this.fontChooser = fontchooser;
      Container cp = getContentPane();
      cp.setLayout(new BorderLayout());
      Box box = Box.createHorizontalBox();
      defaultButton = new JButton("Default");
      defaultButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          fontChooser.resetDefault();
        }
      });
      okButton = new JButton("Ok");
      getRootPane().setDefaultButton(okButton);
      okButton.setActionCommand("OK");
      if(okListener!=null)
        okButton.addActionListener(okListener);
      okButton.addActionListener(new ActionListener(){// Default Hide Action
          public void actionPerformed(ActionEvent e){
            setVisible(false);
          }
        });
      cancelButton = new JButton("Cancel");
      cancelButton.setActionCommand("CANCEL");
      if(cancelListener!=null)
        cancelButton.addActionListener(cancelListener);
      cancelButton.addActionListener(new ActionListener(){// Default Hide Action
          public void actionPerformed(ActionEvent e){
            setVisible(false);
          }
        });
      box.add(defaultButton);
      box.add(Box.createHorizontalGlue());
      box.add(okButton);
      box.add(Box.createHorizontalStrut(8));
      box.add(cancelButton);
      Box mainBox = Box.createVerticalBox();
      mainBox.add(fontChooser);
      mainBox.add(Box.createVerticalStrut(10));
      mainBox.add(box);
      cp.add(mainBox,BorderLayout.CENTER);
      this.pack();
      setLocationRelativeTo(c);
      this.setResizable(false);
  }
}

/**
 * Default ActionListener to handle "Ok" button actionevents. It gets the selected
 * font from the <code>FontChooser</code>, and stores it for retreival later
 */
class FontTracker implements ActionListener {
  FontChooser fontchooser;
  Font font;

  FontTracker(FontChooser f){
    fontchooser = f;
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getActionCommand().equals("CANCEL"))
      font = null;
    else if(e.getActionCommand().equals("OK"))
      font = fontchooser.getFontChoice();
  }

  public Font getFont(){
    return font;
  }
}

/**
 * Abstract class implementing JList-aware DocumentListeners
 */
abstract class JListDocumentListener implements DocumentListener{
  JList jlist;

  public JListDocumentListener(JList jlist){
    this.jlist = jlist;
  }

  /**
   * Returns a string containing the text of the document that was changed
   */
  protected String getChangedString(DocumentEvent e){
    Document document = (Document)e.getDocument();
    try{
      return document.getText(0,document.getLength());
    }catch(javax.swing.text.BadLocationException err){
      err.printStackTrace();
    }
    return "";
  }
}

/**
 * Listener to handle changes in the font size JTextField
 */
class FontSizeDocumentListener extends JListDocumentListener{

  public FontSizeDocumentListener(JList list){
    super(list);
  }

  /**
   * Gives notification that an attribute or set of attributes changed.
   */
  public void changedUpdate(DocumentEvent e){
    //Don't care about these types of changes
  }

  /**
   * Gives notification that there was an insert into the document
   */
  public void insertUpdate(DocumentEvent e){
    processChange(e);
  }

  /**
   * Gives notification that a portion of the document has been removed.
   */
  public void removeUpdate(DocumentEvent e){
    processChange(e);
  }

  protected void processChange(DocumentEvent e){
    String changed = getChangedString(e);
    String toComp = "";
    int found = -1;
    ListModel model = jlist.getModel();
    for(int i=0; i < model.getSize(); i++){
      toComp = model.getElementAt(i).toString();
      if(changed.equals(toComp))
        found = i;
    }
    if(found!=-1){
      final int index = found;
      SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            jlist.setSelectedValue(
              jlist.getModel().getElementAt(index),true);
          }
        });
      return;
    }
  }
}

/**
 * Listener to handle changes in the font style JTextField
 */
class FontStyleDocumentListener extends JListDocumentListener{

  public FontStyleDocumentListener(JList list){
    super(list);
  }

  /**
   * Gives notification that an attribute or set of attributes changed.
   */
  public void changedUpdate(DocumentEvent e){
    //Don't care about these types of changes
  }

  /**
   * Gives notification that there was an insert into the document
   */
  public void insertUpdate(DocumentEvent e){
    processChange(e);
  }

  /**
   * Gives notification that a portion of the document has been removed.
   */
  public void removeUpdate(DocumentEvent e){
    processChange(e);
  }

  protected void processChange(DocumentEvent e){
    String changed = getChangedString(e).toLowerCase();
    String toComp = "";
    ListModel model = jlist.getModel();
    for(int i=0; i < model.getSize(); i++){
      toComp = model.getElementAt(i).toString().toLowerCase();
      if(changed.equals(toComp)){
        jlist.setSelectedValue(model.getElementAt(i),true);
        return;
      }
    }
  }
}

/**
 * Listener to handle changes in the font family JTextField
 */
class FontFamilyDocumentListener extends JListDocumentListener{

  public FontFamilyDocumentListener(JList list){
    super(list);
  }
  /**
   * Gives notification that an attribute or set of attributes changed.
   */
  public void changedUpdate(DocumentEvent e){
    //Don't care about these types of changes
  }

  /**
   * Gives notification that there was an insert into the document
   */
  public void insertUpdate(DocumentEvent e){
    String subText = getChangedString(e).toLowerCase();
    int selected = jlist.getFirstVisibleIndex();
    // Negative selected means there is no visible index yet.
    if (selected < 0) {
    	return;
    } 
    ListModel model = jlist.getModel();
    String currText = (String)model.getElementAt(selected);
    currText = currText.toLowerCase();
    int index = selected;
    while( index < model.getSize() && currText.compareTo(subText) < 0){
      index++;
      currText = (String)model.getElementAt(index);
      currText = currText.toLowerCase();
    }
    selected = selected > 0 ? selected - 1 : 0;
    if(subText.equalsIgnoreCase((String)model.getElementAt(index)))
      jlist.setSelectedValue(model.getElementAt(index),true);
    else{
      jlist.clearSelection();
      jlist.ensureIndexIsVisible(index);
    }
  }

  /**
   * Gives notification that a portion of the document has been removed.
   */
  public void removeUpdate(DocumentEvent e){
    String subText = getChangedString(e).toLowerCase();
    int selected = jlist.getLastVisibleIndex();
    ListModel model = jlist.getModel();
    String currText = (String)model.getElementAt(selected);
    currText = currText.toLowerCase();
    int index = selected;
    while( index > 0 && currText.compareTo(subText) > 0 ){
      index--;
      currText = (String)model.getElementAt(index);
      currText = currText.toLowerCase();
    }
    if( index!=0)
      index = index < model.getSize()-1 ? index + 1 : index;
    if(subText.equalsIgnoreCase((String)model.getElementAt(index)))
      jlist.setSelectedValue(model.getElementAt(index),true);
    else{
      jlist.clearSelection();
      jlist.ensureIndexIsVisible(index);
    }
  }


}

