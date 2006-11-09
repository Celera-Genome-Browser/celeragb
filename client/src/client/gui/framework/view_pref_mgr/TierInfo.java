// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package client.gui.framework.view_pref_mgr;

import shared.preferences.InfoObject;
import shared.preferences.PreferenceManager;

import java.util.Properties;

public class TierInfo extends InfoObject {
  public static final int TIER_HIDDEN =      100;
  public static final int TIER_COLLAPSED =   101;
  public static final int TIER_EXPANDED =    102;
  public static final int TIER_FIXED_SIZE =  103;

  public static int TierPacker_ALIGN_TOP =    1000;
  public static int TierPacker_ALIGN_BOTTOM = 1001;
  public static int TierPacker_ALIGN_CENTER = 1002;

  public static int ALIGN_CENTER =  TierPacker_ALIGN_CENTER;
  public static int ALIGN_TOP =     TierPacker_ALIGN_TOP;
  public static int ALIGN_BOTTOM =  TierPacker_ALIGN_BOTTOM;

  private static int TIER_DEFAULT_STATE                   =TIER_COLLAPSED;
  private static int TIER_DEFAULT_TIER_SPACER             =5;
  private static int TIER_DEFAULT_GLYPH_SPACER            =2;
  private static int TIER_DEFAULT_GLYPH_HEIGHT            =10;
  private static int TIER_DEFAULT_COLLAPSED_ALIGNMENT     =TierInfo.ALIGN_CENTER;
  private static String TIER_DEFAULT_BACKGROUND_COLOR    ="Black";
  private static String TIER_DEFAULT_LABEL_COLOR         ="White";
  private static Boolean TIER_DEFAULT_MIRROR             =Boolean.TRUE;
  private static Boolean TIER_DEFAULT_IS_CURATABLE       =Boolean.FALSE;
  private static Boolean TIER_DEFAULT_USER_EDITABLE      =Boolean.TRUE;
  private static Boolean TIER_DEFAULT_HIDE_WHEN_EMPTY    =Boolean.TRUE;
  private static Boolean TIER_DEFAULT_EXPANDABLE         =Boolean.TRUE;
  private static Boolean TIER_DEFAULT_COLLAPSABLE        =Boolean.TRUE;
  private static Boolean TIER_DEFAULT_HIDABLE            =Boolean.TRUE;
  private static Boolean TIER_DEFAULT_MOVABLE            =Boolean.TRUE;
  private static Boolean TIER_DEFAULT_IS_DOCKED          =Boolean.FALSE;

  private static final String NAME                  ="Name";
  private static final String GLYPH_SPACER          ="GlyphSpacer";
  private static final String STATE                 ="State";
  private static final String EXPANDED              ="Expanded";
  private static final String COLLAPSED             ="Collapsed";
  private static final String FIXED                 ="Fixed";
  private static final String HIDDEN                ="Hidden";
  private static final String DOCKED                ="Docked";

  // Collapsed alignment string statics
  private static final String COLLAPSED_ALIGNMENT   ="CollapsedAlignment";
  private static final String TOP                   ="Top";
  private static final String BOTTOM                ="Bottom";
  private static final String CENTER                ="Center";
  private static final String AWAY_FROM_AXIS        ="AwayFromAxis";
  private static final String TOWARD_AXIS           ="TowardAxis";

  // Color string statics
  private static final String BACKGROUND_COLOR      ="BackgroundColor";
  private static final String LABEL_COLOR           ="LabelColor";

  // Boolean string statics
  private static final String MIRROR                ="Mirror";
  private static final String IS_CURATABLE          ="IsCuratable";
  private static final String IS_USER_EDITABLE      ="IsUserEditable";
  private static final String HIDE_WHEN_EMPTY       ="HideWhenEmpty";
  private static final String IS_EXPANDABLE         ="IsExpandable";
  private static final String IS_COLLAPSABLE        ="IsCollapsable";
  private static final String IS_HIDABLE            ="IsHidable";
  private static final String IS_MOVABLE            ="IsMovable";
  private static final String IS_DOCKED             ="IsDocked";
  private static final String TRUE                  ="true";
  private static final String FALSE                 ="false";

  // mirror so that fixed edge of glyphs is away from axis
  public static int ALIGN_FIXED_AWAY = ALIGN_BOTTOM + 1;

  // mirror so that fixed edge of glyphs is towards axis
  public static int ALIGN_FIXED_TOWARD = ALIGN_BOTTOM + 2;

  // These are the main attributes.
  private int state, glyphSpacer, glyphHeight;
  // Possible values for collapsed_alignment:
  //   center, top, bottom, mirror_toward_axis, mirror_away_from_axis
  private int collapsedAlignment;
  private String backgroundColor, labelColor;
  private Boolean mirror, isCuratable, hideWhenEmpty, isExpandable,
    isCollapsable, isHidable, isMovable, isUserEditable, isDocked;

  /**
   * This is the constructor for TierInfo's not from a property file.
   * All but name set to default values.
   */
  public TierInfo(String name) {
    this.name=name;
    state=TIER_COLLAPSED;
    glyphHeight=TIER_DEFAULT_GLYPH_HEIGHT;
    glyphSpacer=TIER_DEFAULT_GLYPH_SPACER;
    collapsedAlignment = TIER_DEFAULT_COLLAPSED_ALIGNMENT;
    backgroundColor=TIER_DEFAULT_BACKGROUND_COLOR;
    labelColor=TIER_DEFAULT_LABEL_COLOR;
    isCollapsable=TIER_DEFAULT_COLLAPSABLE;
    isExpandable=TIER_DEFAULT_EXPANDABLE;
    isCuratable=TIER_DEFAULT_IS_CURATABLE;
    isHidable=TIER_DEFAULT_HIDABLE;
    isMovable=TIER_DEFAULT_MOVABLE;
    isDocked=TIER_DEFAULT_IS_DOCKED;
    hideWhenEmpty=TIER_DEFAULT_HIDE_WHEN_EMPTY;
    mirror=TIER_DEFAULT_MIRROR;
    isUserEditable=TIER_DEFAULT_USER_EDITABLE;
    this.keyBase=PreferenceManager.getKeyForName(name,true);
  }

  // This constructor should only be used for the clone.
  private TierInfo(String keyBase, String name, int state,
      int glyphSpacer, int glyphHeight,
      int collapsedAlignment, String backgroundColor, String labelColor,
      Boolean isExpandable, Boolean isCollapsable, Boolean isCuratable,
      Boolean isHidable, Boolean isMovable, Boolean isUserEditable,
      Boolean hideWhenEmpty, Boolean isDocked, Boolean mirror, String sourceFile) {
    this.keyBase=keyBase;
    this.name=name;
    this.state=state;
    this.glyphHeight=glyphHeight;
    this.glyphSpacer=glyphSpacer;
    this.collapsedAlignment=collapsedAlignment;
    this.backgroundColor=backgroundColor;
    this.labelColor=labelColor;
    this.isExpandable=isExpandable;
    this.isCollapsable=isCollapsable;
    this.isCuratable=isCuratable;
    this.isHidable=isHidable;
    this.isMovable=isMovable;
    this.isDocked=isDocked;
    this.isUserEditable=isUserEditable;
    this.hideWhenEmpty=hideWhenEmpty;
    this.mirror=mirror;
    this.sourceFile=sourceFile;
  }


  public TierInfo(String keyBase, Properties inputProperties, String sourceFile) {
    this.keyBase=keyBase;
    this.sourceFile=sourceFile;
    String tmpString = new String("");

    tmpString = (String)inputProperties.getProperty(keyBase+"."+NAME);
    if (tmpString!=null) name=tmpString;
    else name="Unknown";

    tmpString = (String)inputProperties.getProperty(keyBase+"."+STATE);
    if (tmpString != null) {
      if (tmpString.equals(EXPANDED)) {
        state = TIER_EXPANDED;
      }
      else if (tmpString.equals(COLLAPSED)) {
        state = TIER_COLLAPSED;
      }
      else if (tmpString.equals(FIXED)) {
        state = TIER_FIXED_SIZE;
      }
      else if (tmpString.equals(HIDDEN)) {
        state = TIER_HIDDEN;
      }
    }
    else state=TIER_DEFAULT_STATE;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+GLYPH_SPACER);
    if (tmpString!=null) glyphSpacer=(new Integer(tmpString)).intValue();
    else glyphSpacer=TIER_DEFAULT_GLYPH_SPACER;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+"GlyphHeight");
    if (tmpString!=null) glyphHeight=(new Integer(tmpString)).intValue();
    else glyphHeight=TIER_DEFAULT_GLYPH_HEIGHT;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+COLLAPSED_ALIGNMENT);
    if (tmpString!=null) {
      if (tmpString.equalsIgnoreCase(TOP)) {
        collapsedAlignment = TierInfo.ALIGN_TOP;
      }
      else if (tmpString.equalsIgnoreCase(CENTER)) {
        collapsedAlignment = TierInfo.ALIGN_CENTER;
      }
      else if (tmpString.equalsIgnoreCase(AWAY_FROM_AXIS)) {
        collapsedAlignment = TierInfo.ALIGN_FIXED_AWAY;
      }
      else if (tmpString.equalsIgnoreCase(TOWARD_AXIS)) {
        collapsedAlignment = TierInfo.ALIGN_FIXED_TOWARD;
      }
      else collapsedAlignment = TierInfo.ALIGN_BOTTOM;
    }
    else collapsedAlignment = TIER_DEFAULT_COLLAPSED_ALIGNMENT;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+BACKGROUND_COLOR);
    if (tmpString!=null) backgroundColor=tmpString;
    else backgroundColor=TIER_DEFAULT_BACKGROUND_COLOR;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+LABEL_COLOR);
    if (tmpString!=null) labelColor=tmpString;
    else labelColor=TIER_DEFAULT_LABEL_COLOR;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+IS_COLLAPSABLE);
    if (tmpString!=null) { isCollapsable=Boolean.valueOf(tmpString); }
    else isCollapsable=TIER_DEFAULT_COLLAPSABLE;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+IS_EXPANDABLE);
    if (tmpString!=null) { isExpandable=Boolean.valueOf(tmpString); }
    else isExpandable=TIER_DEFAULT_EXPANDABLE;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+IS_CURATABLE);
    if (tmpString!=null) { isCuratable=Boolean.valueOf(tmpString); }
    else isCuratable=TIER_DEFAULT_IS_CURATABLE;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+IS_HIDABLE);
    if (tmpString!=null) { isHidable=Boolean.valueOf(tmpString); }
    else isHidable=TIER_DEFAULT_HIDABLE;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+IS_MOVABLE);
    if (tmpString!=null) { isMovable=Boolean.valueOf(tmpString); }
    else isMovable=TIER_DEFAULT_MOVABLE;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+IS_DOCKED);
    if (tmpString!=null) { isDocked=Boolean.valueOf(tmpString); }
    else isDocked=TIER_DEFAULT_IS_DOCKED;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+HIDE_WHEN_EMPTY);
    if (tmpString!=null) { hideWhenEmpty=Boolean.valueOf(tmpString); }
    else hideWhenEmpty=TIER_DEFAULT_HIDE_WHEN_EMPTY;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+MIRROR);
    if (tmpString!=null) { mirror=Boolean.valueOf(tmpString); }
    else mirror=TIER_DEFAULT_MIRROR;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+IS_USER_EDITABLE);
    if (tmpString!=null) { isUserEditable=Boolean.valueOf(tmpString); }
    else isUserEditable=TIER_DEFAULT_USER_EDITABLE;
  }

  public String getBackgroundColor() { return backgroundColor; }
  void setBackgroundColor(String backgroundColor) {
    isDirty=true;
    this.backgroundColor = backgroundColor;
  }

  public String getLabelColor()      { return labelColor; }
  void setLabelColor(String labelColor) {
    isDirty=true;
    this.labelColor = labelColor;
  }

  public int getState()              { return state; }
  void setState(int state) {
    isDirty=true;
    this.state = state;
  }

  public String getKeyName() { return "Tier."+keyBase; }

  public int getGlyphSpacer()             { return glyphSpacer; }
  void setGlyphSpacer(int glyphSpacer)  { isDirty=true; this.glyphSpacer = glyphSpacer; }

  public int getGlyphHeight()             { return glyphHeight; }
  void setGlyphHeight(int glyphHeight)  { isDirty=true; this.glyphHeight = glyphHeight; }

  public int getCollapsedAlignment() { return collapsedAlignment; }
  void setCollapsedAlignment(int collapsedAlignment) {
    isDirty=true;
    this.collapsedAlignment = collapsedAlignment;
  }

  public boolean getCuratable()      { return isCuratable.booleanValue(); }
  void setCuratable(Boolean isCuratable) {
    isDirty=true;
    this.isCuratable = isCuratable;
  }

  public boolean getHideWhenEmpty()  { return hideWhenEmpty.booleanValue(); }
  void setHideWhenEmpty(Boolean hideWhenEmpty) {
    isDirty=true;
    this.hideWhenEmpty = hideWhenEmpty;
  }

  public boolean getMirror()         { return mirror.booleanValue(); }
  void setMirror(Boolean mirror) {
    isDirty=true;
    this.mirror = mirror;
  }

  public boolean getExpandable()     { return isExpandable.booleanValue(); }
  void setExpandable(Boolean isExpandable) {
    isDirty=true;
    this.isExpandable = isExpandable;
  }

  public boolean getCollapsable()    { return isCollapsable.booleanValue(); }
  void setCollapsable(Boolean isCollapsable) {
    isDirty=true;
    this.isCollapsable = isCollapsable;
  }

  public boolean getHidable()        { return isHidable.booleanValue(); }
  void setHideable(Boolean isHideable) {
    isDirty=true;
    this.isHidable = isHideable;
  }

  public boolean getMovable()        { return isMovable.booleanValue(); }
  void setMovable(Boolean isMovable) {
    isDirty=true;
    this.isMovable = isMovable;
  }

  public boolean getDocked()        { return isDocked.booleanValue(); }
  void setDocked(Boolean isDocked) {
    isDirty=true;
    this.isDocked = isDocked;
  }

  public boolean getUserEditable()   { return isUserEditable.booleanValue(); }
  void setUserEditable(Boolean isUserEditable) {
    isDirty=true;
    this.isUserEditable = isUserEditable;
  }

  static public int getOrientedAlignment(int alignment, boolean forward) {
    if (alignment == ALIGN_FIXED_AWAY) {
      if (forward) { return ALIGN_TOP; }
      else { return ALIGN_BOTTOM; }
    }
    else if (alignment == ALIGN_FIXED_TOWARD) {
      if (forward) { return ALIGN_BOTTOM; }
      else { return ALIGN_TOP; }
    }
    else {
      return alignment;
    }
  }

  public Properties getPropertyOutput() {
    Properties outputProperties=new Properties();
    String key = getKeyName()+".";
    outputProperties.put(key+NAME,name);

    if (state == TIER_COLLAPSED) {
      if (state!=TIER_DEFAULT_STATE) outputProperties.put(key+STATE,COLLAPSED);
    }
    else if (state == TIER_FIXED_SIZE) {
      if (state!=TIER_DEFAULT_STATE) outputProperties.put(key+STATE,FIXED);
    }
    else if (state == TIER_HIDDEN) {
      if (state!=TIER_DEFAULT_STATE) outputProperties.put(key+STATE, HIDDEN);
    }
    else { if (state!=TIER_DEFAULT_STATE) outputProperties.put(key+STATE, EXPANDED); }

    if (glyphSpacer!=TIER_DEFAULT_GLYPH_SPACER) outputProperties.put(key+GLYPH_SPACER, Integer.toString(glyphSpacer));

    if (collapsedAlignment == TierInfo.ALIGN_TOP) {
      if (collapsedAlignment!=TIER_DEFAULT_COLLAPSED_ALIGNMENT) outputProperties.put(key+COLLAPSED_ALIGNMENT,TOP);
    }
    else if (collapsedAlignment == TierInfo.ALIGN_CENTER) {
      if (collapsedAlignment!=TIER_DEFAULT_COLLAPSED_ALIGNMENT) outputProperties.put(key+COLLAPSED_ALIGNMENT,CENTER);
    }
    else if (collapsedAlignment == TierInfo.ALIGN_FIXED_AWAY) {
      if (collapsedAlignment!=TIER_DEFAULT_COLLAPSED_ALIGNMENT) outputProperties.put(key+COLLAPSED_ALIGNMENT,AWAY_FROM_AXIS);
    }
    else if (collapsedAlignment == TierInfo.ALIGN_FIXED_TOWARD) {
      if (collapsedAlignment!=TIER_DEFAULT_COLLAPSED_ALIGNMENT) outputProperties.put(key+COLLAPSED_ALIGNMENT,TOWARD_AXIS);
    }
    else { if (collapsedAlignment!=TIER_DEFAULT_COLLAPSED_ALIGNMENT) outputProperties.put(key+COLLAPSED_ALIGNMENT,BOTTOM); }

    if (!backgroundColor.equals(TIER_DEFAULT_BACKGROUND_COLOR)) outputProperties.put(key+BACKGROUND_COLOR,backgroundColor);
    if (!labelColor.equals(TIER_DEFAULT_LABEL_COLOR)) outputProperties.put(key+LABEL_COLOR,labelColor);
    if (!isExpandable.equals(TIER_DEFAULT_EXPANDABLE)) outputProperties.put(key+IS_EXPANDABLE,isExpandable.toString());
    if (!isCollapsable.equals(TIER_DEFAULT_COLLAPSABLE)) outputProperties.put(key+IS_COLLAPSABLE,isCollapsable.toString());
    if (!isCuratable.equals(TIER_DEFAULT_IS_CURATABLE)) outputProperties.put(key+IS_CURATABLE,isCuratable.toString());
    if (!isHidable.equals(TIER_DEFAULT_HIDABLE)) outputProperties.put(key+IS_HIDABLE,isHidable.toString());
    if (!isMovable.equals(TIER_DEFAULT_MOVABLE)) outputProperties.put(key+IS_MOVABLE,isMovable.toString());
    if (!isDocked.equals(TIER_DEFAULT_IS_DOCKED)) outputProperties.put(key+IS_DOCKED,isDocked.toString());
    if (!isUserEditable.equals(TIER_DEFAULT_USER_EDITABLE)) outputProperties.put(key+IS_USER_EDITABLE,isUserEditable.toString());
    if (!hideWhenEmpty.equals(TIER_DEFAULT_HIDE_WHEN_EMPTY)) outputProperties.put(key+HIDE_WHEN_EMPTY,hideWhenEmpty.toString());
    if (!mirror.equals(TIER_DEFAULT_MIRROR)) outputProperties.put(key+MIRROR,mirror.toString());
    return outputProperties;
  }

  public Object clone() {
    TierInfo newTierInfo = new TierInfo(new String(this.keyBase),
      new String(this.name),
      (new Integer(this.state)).intValue(),
      (new Integer(this.glyphSpacer)).intValue(),
      (new Integer(this.glyphHeight)).intValue(),
      (new Integer(this.collapsedAlignment)).intValue(),
      new String(this.backgroundColor),
      new String(this.labelColor),
      new Boolean(this.isExpandable.booleanValue()),
      new Boolean(this.isCollapsable.booleanValue()),
      new Boolean(this.isCuratable.booleanValue()),
      new Boolean(this.isHidable.booleanValue()),
      new Boolean(this.isMovable.booleanValue()),
      new Boolean(this.isUserEditable.booleanValue()),
      new Boolean(this.hideWhenEmpty.booleanValue()),
      new Boolean(this.isDocked.booleanValue()),
      new Boolean(this.mirror.booleanValue()),
      new String(this.sourceFile));
    return newTierInfo;
  }
}

