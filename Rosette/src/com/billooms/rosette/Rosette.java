package com.billooms.rosette;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import com.billooms.cornlatheprefs.COrnLathePrefs;
import com.billooms.drawables.simple.Curve;
import com.billooms.drawables.Drawable;
import com.billooms.drawables.simple.Circle;
import com.billooms.drawables.simple.Plus;
import com.billooms.patterns.CustomPattern;
import com.billooms.patterns.Pattern;
import com.billooms.patterns.Patterns;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.openide.util.Lookup;
import org.w3c.dom.Element;

/**
 * A Rose Engine rosette.
 *
 * @author Bill Ooms. Copyright 2015 Studio of Bill Ooms. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Rosette extends CLclass {

  /** All Rosette property change names start with this prefix */
  public final static String PROP_PREFIX = "Rosette" + "_";
  /** Property name used for changing the pattern */
  public final static String PROP_PATTERN = PROP_PREFIX + "Pattern";
  /** Property name used for changing the repeat */
  public final static String PROP_REPEAT = PROP_PREFIX + "Repeat";
  /** Property name used for changing the peak-to-peak amplitude */
  public final static String PROP_PTOP = PROP_PREFIX + "PeakToPeak";
  /** Property name used for changing the phase */
  public final static String PROP_PHASE = PROP_PREFIX + "Phase";
  /** Property name used for changing the invert flag */
  public final static String PROP_INVERT = PROP_PREFIX + "Invert";
  /** Property name used for changing the mask */
  public final static String PROP_MASK = PROP_PREFIX + "Mask";
  /** Property name used for changing the maskHiLo flag */
  public final static String PROP_HILO = PROP_PREFIX + "HiLo";
  /** Property name used for changing the mask phase */
  public final static String PROP_MASKPHASE = PROP_PREFIX + "MaskPhase";
  /** Property name used for changing the optional 2nd integer parameter */
  public final static String PROP_N2 = PROP_PREFIX + "N2";
  /** Property name used for changing the optional 2nd amplitude */
  public final static String PROP_AMP2 = PROP_PREFIX + "Amp2";
  /** Property name used for changing the symmetry amplitudes. */
  public final static String PROP_SYMAMP = PROP_PREFIX + "SymmetryAmp";

  /** Default Style Name (currently set to "SINE") */
  public final static String DEFAULT_PATTERN = "SINE";
  /** Default repeat (currently set to 8) */
  public final static int DEFAULT_REPEAT = 8;
  /** Default peak-to-peak amplitude (currently set to 0.1) */
  public final static double DEFAULT_PTOP = 0.1;
  /** Default phase (currently set to 0.0) */
  public final static double DEFAULT_PHASE = 0.0;
  /** Default mask (currently set to "") */
  public final static String DEFAULT_MASK = "";
  /** Default mask phase (currently set to 0.0) */
  public final static double DEFAULT_MASK_PHASE = 0.0;
  /** Default for optional second integer parameter */
  public final static int DEFAULT_N2 = 3;
  /** Default for optional second amplitude parameter */
  public final static double DEFAULT_AMP2 = 0.1;
//  /** The radius of the Rosette for drawing purposes (currently set to 3.5) */
//  public final static double DEFAULT_RADIUS = 3.5;
  public final static String DEFAULT_SYMAMP = "";

  /** Two different ways to mask */
  public static enum Mask {

    /** Mask the rosette to the highest point */
    HIGH,
    /** Mask the rosette to the
     * lowest point */
    LOW
  }

  /** Pattern for the rosette. */
  private Pattern pattern;
  /** Number of repeats around the rosette. */
  private int repeat = DEFAULT_REPEAT;
  /** Peak-to-Peak amplitude. */
  private double pToP = DEFAULT_PTOP;
  /** Phase shift in degrees (0 to 360), +phase is CCW. */
  private double phase = DEFAULT_PHASE;
  /** Flag to invert the pattern (like rubbing on the backside). */
  private boolean invert = false;
  /** Mask some of the pattern repeats -- 0 is skip, 1 is don't skip. */
  private String mask = DEFAULT_MASK;
  /** Flag if mask is to a high point or low point of the rosette. */
  private Mask maskHiLo = Mask.HIGH;
  /** Mask phase shift in degrees (0 to 360). */
  private double maskPhase = DEFAULT_MASK_PHASE;
  /** Optional 2nd integer parameter. */
  private int n2 = DEFAULT_N2;
  /** Optional 2nd amplitude parameter. */
  private double amp2 = DEFAULT_AMP2;
  /** Optional amplitude symmetry parameters. */
  private DoubleArray symmetryAmp = new DoubleArray(DEFAULT_SYMAMP);
  /** Optional phase symmetry parameters. */
  private DoubleArray symmetryPhase = new DoubleArray();

  private static COrnLathePrefs prefs = Lookup.getDefault().lookup(COrnLathePrefs.class);
  private Patterns patternMgr = null;

  /* Information for drawing */
  private final static BasicStroke SOLID_LINE = new BasicStroke(1.0f);
  private final static BasicStroke DOT_LINE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, new float[]{3, 3}, 0);
  private final static Color OUTLINE_COLOR = Color.BLACK;
  private final static Color RADIUS_COLOR = Color.BLUE;
  private final static int NUM_POINTS = 720;	    // draw a point every 1/2 degree
  private final Point2D.Double center = new Point2D.Double(0.0, 0.0);   // center of the rosette is always 0.0, 0.0
  private final ArrayList<Drawable> drawList = new ArrayList<>();   // a list of things to draw for a visual representaiton of the rosette

  /**
   * Construct a rosette with the given values.
   *
   * @param patternName Pattern name of the pattern to use
   * @param repeat Repeat
   * @param amplitude Peak-to-Peak
   * @param phase Phase in degrees, where 360 means one pattern repeat
   * @param patMgr pattern manager with available patterns
   */
  public Rosette(String patternName, int repeat, double amplitude, double phase, Patterns patMgr) {
    this.patternMgr = patMgr;
    this.pattern = patternMgr.getPattern(patternName);
    this.repeat = repeat;
    if (pattern.getName().equals("NONE")) {
      this.pToP = 0.0;
      this.phase = 0.0;
    } else {
      this.pToP = amplitude;
      this.phase = angleCheck(phase);
      if (pattern instanceof CustomPattern) {
        ((CustomPattern) pattern).addPropertyChangeListener(this);
      }
    }
  }

  /**
   * Construct a rosette with default values.
   *
   * @param patMgr pattern manager with available patterns
   */
  public Rosette(Patterns patMgr) {
    this(DEFAULT_PATTERN, DEFAULT_REPEAT, DEFAULT_PTOP, DEFAULT_PHASE, patMgr);
  }

  /**
   * Construct a new rosette with the same values as the given rosette.
   *
   * @param rosette Rosette
   */
  public Rosette(Rosette rosette) {
    this(rosette.pattern.getName(), rosette.getRepeat(), rosette.getPToP(), rosette.getPhase(), rosette.patternMgr);
    this.patternMgr = rosette.patternMgr;
    this.mask = rosette.getMask();
    this.maskHiLo = rosette.getMaskHiLo();
    this.maskPhase = rosette.getMaskPhase();
    this.invert = rosette.getInvert();
    this.amp2 = rosette.getAmp2();
    this.n2 = rosette.getN2();
  }

  /**
   * Define a new RosettePoint from DOM Element.
   *
   * @param element DOM Element
   * @param patMgr pattern manager with available patterns
   */
  public Rosette(Element element, Patterns patMgr) {
    this(CLUtilities.getString(element, "pattern", DEFAULT_PATTERN),
        CLUtilities.getInteger(element, "repeat", DEFAULT_REPEAT),
        CLUtilities.getDouble(element, "amp", DEFAULT_PTOP),
        CLUtilities.getDouble(element, "phase", DEFAULT_PHASE),
        patMgr);
    invert = CLUtilities.getBoolean(element, "invert", false);
    mask = CLUtilities.getString(element, "mask", DEFAULT_MASK);
    maskHiLo = CLUtilities.getEnum(element, "hilo", Mask.class, Mask.HIGH);
    maskPhase = CLUtilities.getDouble(element, "maskPhase", DEFAULT_MASK_PHASE);
    n2 = CLUtilities.getInteger(element, "n2", DEFAULT_N2);
    amp2 = CLUtilities.getDouble(element, "amp2", DEFAULT_AMP2);
    symmetryAmp.setData(CLUtilities.getString(element, "symmetryAmp", DEFAULT_SYMAMP));
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("Rosette.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Listens for changes in CustomPattern
    // If this rosette's pattern is deleted, use the default.
    if (evt.getPropertyName().equals(Patterns.PROP_DELETE)) {
      if (evt.getOldValue().equals(pattern.getName())) {
        setPattern(patternMgr.getDefaultPattern());	// deleted the rosettes pattern --> use default.
      }
    }
    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
  }

  /**
   * Create a string representation for this object.
   *
   * @return string representation.
   */
  @Override
  public String toString() {
    String str = pattern.getName();
    if (invert) {
      str += "(I)";
    }
    str += " " + repeat;
    str += " A:" + F3.format(pToP);
    if (prefs.isFracPhase()) {
      str += " ph:" + F4.format(phase / 360.0);
    } else {
      str += " ph:" + F1.format(phase);
    }
    return str;
  }

  /**
   * The only thing this does is remove any CustomPattern
   * propertyChangeListener.
   */
  public void clear() {
    if (pattern instanceof CustomPattern) {
      ((CustomPattern) pattern).removePropertyChangeListener(this);
    }
  }

  /**
   * Get the pattern of the rosette.
   *
   * @return pattern of the rosette
   */
  public Pattern getPattern() {
    return pattern;
  }

  /**
   * Set the pattern for the rosette.
   *
   * This fires a PROP_PATTERN property change with the old and new patterns.
   *
   * @param p pattern
   */
  public synchronized void setPattern(Pattern p) {
    if (pattern instanceof CustomPattern) {   // quit listening to the old pattern
      ((CustomPattern) pattern).removePropertyChangeListener(this);
    }
    Pattern old = this.pattern;
    this.pattern = p;
    if (pattern.getName().equals("NONE")) {
      this.pToP = 0.0;
      this.phase = 0.0;
    }
    if (pattern instanceof CustomPattern) {   // listen to customs for changes
      ((CustomPattern) pattern).addPropertyChangeListener(this);
    }
    this.pcs.firePropertyChange(PROP_PATTERN, old, pattern);
  }

  /**
   * Get the number of repeats on the rosette.
   *
   * @return number of repeats
   */
  public int getRepeat() {
    return repeat;
  }

  /**
   * Set the number of repeats on the rosette.
   *
   * This fires a PROP_REPEAT property change with the old and new repeats.
   *
   * @param n number of repeats
   */
  public synchronized void setRepeat(int n) {
    int old = repeat;
    this.repeat = Math.max(n, pattern.getMinRepeat());
    this.pcs.firePropertyChange(PROP_REPEAT, old, repeat);
  }

  /**
   * Get the peak-to-peak amplitude of the rosette.
   *
   * @return peak-to-peak amplitude
   */
  public double getPToP() {
    return pToP;
  }

  /**
   * Set the peak-to-peak amplitude of the rosette.
   *
   * This fires a PROP_PTOP property change with the old and new amplitudes.
   *
   * @param p peak-to-peak amplitude
   */
  public synchronized void setPToP(double p) {
    double old = this.pToP;
    this.pToP = p;
    if (pattern.getName().equals("NONE")) {
      this.pToP = 0.0;
    }
    this.pcs.firePropertyChange(PROP_PTOP, old, pToP);
  }

  /**
   * Get the phase of the rosette
   *
   * @return phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of the
   * repeat, etc.
   */
  public double getPhase() {
    return phase;
  }

  /**
   * Get the phase of the rosette as a fraction of a repeat.
   *
   * @return phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public double getPh() {
    return phase / 360.0;
  }

  /**
   * Set the phase of the rosette.
   *
   * Phase is adjusted to be in the range of 0 to 360. This fires a PROP_PHASE
   * property change with the old and new phases.
   *
   * @param ph phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of
   * the repeat, etc.
   */
  public synchronized void setPhase(double ph) {
    double old = this.phase;
    this.phase = angleCheck(ph);
    if (pattern.getName().equals("NONE")) {
      this.phase = 0.0;
    }
    this.pcs.firePropertyChange(PROP_PHASE, old, phase);
  }

  /**
   * Set the phase of the rosette.
   *
   * Values of less than 0.0 will be set to 0.0 and values greater than 1.0 will
   * be set to 1.0. This fires a PROP_PHASE property change with the old and new
   * phases.
   *
   * @param ph phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public synchronized void setPh(double ph) {
    setPhase(ph * 360.0);
  }

  /**
   * Get the invert flag.
   *
   * @return invert flag
   */
  public boolean getInvert() {
    return invert;
  }

  /**
   * Set the invert flag.
   *
   * This fires a PROP_INVERT property change with the old and new values.
   *
   * @param inv true=invert
   */
  public void setInvert(boolean inv) {
    boolean old = invert;
    this.invert = inv;
    this.pcs.firePropertyChange(PROP_INVERT, old, invert);
  }

  /**
   * Get the mask for this rosette.
   *
   * A blank string means all repeat positions are used. A 0 means skip this
   * position, a 1 means use this position. The string is read from left to
   * right, and is re-used as often as needed until the full number of repeats
   * is covered.
   *
   * @return mask
   */
  public String getMask() {
    return mask;
  }

  /**
   * Set the mask for this rosette.
   *
   * A blank string means all repeat positions are used. A 0 means skip this
   * position, a 1 means use this position. The string is read from left to
   * right, and is re-used as often as needed until the full number of repeats
   * is covered. Any other character besides "0" and "1" is interpreted as "1".
   * This fires a PROP_MASK property change with the old and new values.
   *
   * @param m new mask
   */
  public void setMask(String m) {
    String old = this.mask;
    if (m == null) {
      this.mask = "";
    }
    this.mask = m;
    for (int i = 0; i < mask.length(); i++) {	// replace anything besides '0' with '1'
      char c = mask.charAt(i);
      if (c == '1') {
        continue;
      }
      if (c != '0') {
        mask = mask.replace(c, '1');
      }
    }
    pcs.firePropertyChange(PROP_MASK, old, mask);
  }

  /**
   * Get the HiLo flag for masking.
   *
   * @return HiLo flag
   */
  public Mask getMaskHiLo() {
    return maskHiLo;
  }

  /**
   * Set the HiLo flag for masking.
   *
   * This fires a PROP_HILO property change with the old and new values.
   *
   * @param hiLo new HiLo flag
   */
  public void setMaskHiLo(Mask hiLo) {
    Mask old = this.maskHiLo;
    this.maskHiLo = hiLo;
    pcs.firePropertyChange(PROP_HILO, old.toString(), maskHiLo.toString());
  }

  /**
   * Get the mask phase of the rosette
   *
   * @return mask phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of
   * the repeat, etc.
   */
  public double getMaskPhase() {
    return maskPhase;
  }

  /**
   * Get the mask phase of the rosette as a fraction of a repeat.
   *
   * @return phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public double getMaskPh() {
    return maskPhase / 360.0;
  }

  /**
   * Set the mask phase of the rosette.
   *
   * This fires a PROP_MASKPHASE property change with the old and new phases.
   *
   * @param ph phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of
   * the repeat, etc.
   */
  public void setMaskPhase(double ph) {
    double old = this.maskPhase;
    this.maskPhase = angleCheck(ph);
    if (pattern.getName().equals("NONE")) {
      this.maskPhase = 0.0;
    }
    this.pcs.firePropertyChange(PROP_MASKPHASE, old, maskPhase);
  }

  /**
   * Set the mask phase of the rosette.
   *
   * Values of less than 0.0 will be set to 0.0 and values greater than 1.0 will
   * be set to 1.0. This fires a PROP_MASKPHASE property change with the old and
   * new phases.
   *
   * @param ph phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public synchronized void setMaskPh(double ph) {
    setMaskPhase(ph * 360.0);
  }

  /**
   * Get the optional 2nd integer parameter.
   *
   * @return optional 2nd integer parameter
   */
  public int getN2() {
    return n2;
  }

  /**
   * Set the optional 2nd integer parameter.
   *
   * This fires a PROP_N2 property change with the old and new repeats.
   *
   * @param n optional 2nd integer parameter
   */
  public void setN2(int n) {
    int old = n2;
    this.n2 = n;
    this.pcs.firePropertyChange(PROP_N2, old, n2);
  }

  /**
   * Get the optional 2nd amplitude parameter.
   *
   * @return optional 2nd amplitude parameter
   */
  public double getAmp2() {
    return amp2;
  }

  /**
   * Set the optional 2nd amplitude parameter.
   *
   * This fires a PROP_AMP2 property change with the old and new values.
   *
   * @param a optional 2nd amplitude parameter
   */
  public synchronized void setAmp2(double a) {
    double old = this.amp2;
    this.amp2 = a;
    this.pcs.firePropertyChange(PROP_AMP2, old, amp2);
  }
  
  /**
   * Get the optional symmetry amplitudes.
   * 
   * @return DoubleArray of optional symmetry amplitudes
   */
  public DoubleArray getSymmetryAmp() {
    return symmetryAmp;
  }
  
  /**
   * Set the optional symmetry amplitudes.
   *
   * This fires a PROP_SYMAMP property change with the old and new values.
   * 
   * @param newAmps array of optional symmetry amplitudes (or null for no variations)
   */
  public void setSymmetryAmp(DoubleArray newAmps) {
    DoubleArray old = this.symmetryAmp;
    this.symmetryAmp = newAmps;
    this.pcs.firePropertyChange(PROP_SYMAMP, old, newAmps);
  }
  
  /**
   * Set the optional symmetry amplitudes.
   *
   * This fires a PROP_SYMAMP property change with the old and new values.
   * 
   * @param newStr string with new values comma delimited
   */
  public void setSymmetryAmp(String newStr) {
    String old = this.symmetryAmp.toString();
    this.symmetryAmp.setData(newStr);
    this.pcs.firePropertyChange(PROP_SYMAMP, old, symmetryAmp.toString());
  }

  /**
   * Make sure angle is in range 0.0 <= a < 360.0
   *
   * @param ang angle in degrees
   * @return angle in range 0.0 <= a < 360.0
   */
  private double angleCheck(double ang) {
    while (ang < 0.0) {
      ang += 360.0;
    }
    while (ang >= 360.0) {
      ang -= 360.0;
    }
    return ang;
  }

  /**
   * Paint the object.
   *
   * @param g2d Graphics2D
   * @param nomRadius Nominal radius of the drawn rosette
   */
  public void paint(Graphics2D g2d, double nomRadius) {
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setStroke(SOLID_LINE);

    makeDrawables(nomRadius);    // make drawables when needed for painting
    drawList.stream().forEach((item) -> {
      // paint everything in the drawlist
      item.paint(g2d);
    });
  }

  /**
   * Make the rosette appearance based on stored values.
   * 
   * @param nomRadius Nominal radius of the drawn rosette
   */
  private void makeDrawables(double nomRadius) {
    drawList.clear();			// clear out the old drawlist
    drawList.add(new Plus(center, RADIUS_COLOR));  // always draw a center mark
    drawList.add(new Circle(new Point2D.Double(0.0, 0.0), nomRadius, RADIUS_COLOR, DOT_LINE));  // circle at nominal radius

    Point2D.Double[] pts = new Point2D.Double[NUM_POINTS + 1];     // add 1 for wrap-around
    double rad, r;
    for (int i = 0; i <= NUM_POINTS; i++) {
      // Add PI so that the pattern starts on the left side.
      // Minus sign so that pattern goes clockwise such that 
      // a positive spindle rotation brings the feature to the left side.
      rad = -Math.toRadians((double) i) + Math.PI;
      r = nomRadius - getAmplitudeAt((double) i);
      pts[i] = new Point2D.Double(r * Math.cos(rad), r * Math.sin(rad));
    }
    drawList.add(new Curve(pts, OUTLINE_COLOR, SOLID_LINE));
  }

  /**
   * Get the amplitude (offset from nominal radius) of the rosette at a given
   * angle in degrees. A returned value of zero means zero deflection from its
   * nominal radius.
   *
   * @param ang Angle in degrees around the rosette
   * @return amplitude which will be a positive number from 0.0 to pToP
   */
  public double getAmplitudeAt(double ang) {
    if (isMasked(ang)) {
      switch (maskHiLo) {
        case HIGH:
          return 0.0;
        case LOW:
          return pToP;
      }
    }
    double angle = angleCheck(ang);		// must be in range of 0 to 360
    angle = angle + phase / repeat;		// angle relative to the start of first pattern
    double anglePerRepeat = 360.0 / repeat;	// degrees per every repeat of pattern
    int m = (int) (angle / anglePerRepeat);	// which repeat is the pattern in (0 to repeat)
    double patternAngle = angle - m * anglePerRepeat;	// degrees into the pattern
    double dr = pToP * getPatternValue(patternAngle / anglePerRepeat);
    if ((symmetryAmp != null) && (symmetryAmp.size() > 0)) {
      dr = symmetryAmp.getData()[m % symmetryAmp.size()] * dr;    // multiply by the symmetry amplitude
      if (getPatternValue(0.0) >= 0.99) {    // just in case it's not exacly 1.0
        // for patterns that start at 1.0, offset so that repeats match up
        dr = dr + pToP * (1.0 - symmetryAmp.getData()[m % symmetryAmp.size()]);
      }
    }
    if (invert) {
      return pToP - dr;
    }
    return dr;
  }
  
  /**
   * Get a normalized value (in the range of 0 to 1) for the given normalized
   * input (also in the range of 0 to 1).
   * 
   * Use this rather than directly calling pattern.getValue so that we call the 
   * correct method depending on if the pattern requires the repeat or other optional 
   * parameters. 
   *
   * @param n input value (in the range of 0.0 to 1.0)
   * @return normalized pattern value (in the range 0.0 to 1.0)
   */
  private double getPatternValue(double n) {
    if (pattern.needsOptions()) {
      return pattern.getValue(n, repeat, n2, amp2);
    } else if (pattern.needsRepeat()) {
      return pattern.getValue(n, repeat);
    } else {
      return pattern.getValue(n);
    }
  }

  /**
   * Get the amplitude (offset from nominal radius) of the rosette at a given
   * angle in degrees. A returned value of zero means zero deflection from its
   * nominal radius.
   *
   * @param ang Angle in degrees around the rosette
   * @param inv invert the returned value (as if rubbing on the backside of the
   * rosette).
   * @return amplitude which will be a positive number from 0.0 to pToP
   */
  public double getAmplitudeAt(double ang, boolean inv) {
    if (inv) {
      return pToP - getAmplitudeAt(ang);
    }
    return getAmplitudeAt(ang);
  }

  /**
   * Determine if the rosette is masked at the given angle.
   *
   * @param ang Angle in degrees around the rosette
   * @return true=masked, false=not masked
   */
  private boolean isMasked(double ang) {
    if (mask.isEmpty()) {		// nothing is masked
      return false;
    }
    double angle = angleCheck(ang);		// must be in range of 0 to 360
    angle = angle + phase / repeat;		// angle relative to the start of first pattern
    angle = angle - maskPhase / repeat;		// angle relative to the start of first mask
    if (angle < 0.0) {
      angle += 360.0;	// in case we aren't in the first mask portion yet
    }
    double anglePerRepeat = 360.0 / repeat;	// degrees per every repeat of pattern
    int m = (int) (angle / anglePerRepeat);	// which mask is the pattern in

    String fullMask = mask;
    while (fullMask.length() <= getRepeat()) {	// note: "<=" because m can equal repeat!				
      fullMask += mask;			// fill out to full length
    }
    return (fullMask.charAt(m) == '0');
  }

  @Override
  public void writeXML(PrintWriter out) {
    String opt = "";
    if (invert) {
      opt = opt + " invert='" + invert + "'";
    }
    if (!mask.isEmpty()) {
      opt = opt + " mask='" + mask + "'"
          + " hilo='" + maskHiLo.toString() + "'"
          + " maskPhase='" + F1.format(maskPhase) + "'";
    }
    if (pattern.needsN2()) {
      opt = opt + " n2='" + n2 + "'";
    }
    if (pattern.needsAmp2()) {
      opt = opt + " amp2='" + F4.format(amp2) + "'";
    }
    if ((symmetryAmp != null) && (symmetryAmp.size() > 0)) {
      opt = opt + " symmetryAmp='" + symmetryAmp.toString() + "'";
    }
    out.println(indent + "<Rosette"
        + " pattern='" + pattern.getName() + "'"
        + " repeat='" + repeat + "'"
        + " amp='" + F3.format(pToP) + "'"
        + " phase='" + F1.format(phase) + "'"
        + opt
        + "/>");
  }
}
