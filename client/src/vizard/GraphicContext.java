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
package vizard;

import vizard.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.text.*;
import java.awt.font.*;
import java.awt.RenderingHints.*;
import java.util.*;

/**
 * The processing of one Swing-paint request can result in thousands of
 * glyph-paint requests.
 * This implies performance constraints that are more strict
 * for glyph-paint than for AWT or Swing-paint.
 *
 * The purpose of the GraphicContext class is to provide a layer, above
 * Graphics2D, that takes into account these additional performance
 * constraints.
 *
 * CONSTRAINTS
 * 1)
 * After running some tests we discovered the following fact:
 *      case a: a paint request on a Graphics2D object with an identity affine transform.
 *      case b: a paint request on a Graphics2D object with a non-identity affine transform.
 * With jdk1.3.x, the case b is an order of magnitude slower than the case a.
 *
 * Perhaps this excessive slowness will be corrected in future versions of the jdk.
 * But until then, we have to provide useable graphic applications to our clients.
 *
 * 2)
 * The normal usage of Graphics2D implies the creation of new instances
 * of small objects:
 *   - fill(shape) or draw(shape) requires a small shape object.
 *   - getTransform or getClip returns a copy of internal attributes.
 *
 * When there are hundreds of components, these small allocations are not an
 * issue. But with a number of glyphs being typically two orders of magnitude
 * bigger than the number of components, too frequent allocations of small
 * objects will result in too frequent garbage collector execution and will
 * slow down an interactive graphical application.
 *
 * SOLUTION
 * Define a GraphicContext (replacement to Graphics2D) that is used by glyphs
 * during the execution of their paint method.
 * The graphic context:
 * 1) always leaves the underlying Graphics2D with an identity affine transform.
 *    At the time of paint requests, the GraphicContext transforms the coordinates
 *    before forwarding the request to the Graphics2D.
 * 2) provides reusable pre-allocated instances of various shapes.
 *
 * The graphic context is instanciated once by the RootGlyph, which
 * then passes it down the glyph tree during the recursive calls to
 * paint.
 *
 * EXAMPLE
 *
 * public abstract class Rect extends Glyph
 * {
 *     public abstract double x();
 *     public abstract double y();
 *     public abstract double width();
 *     public abstract double height();
 *
 *     public void paint(GraphicContext gc) {
 *         Rectangle2D.Double rect = gc.temporaryRectangle();
 *         rect.x = x();         rect.y = y();
 *         rect.width = width(); rect.height = height();
 *
 *         if (gc.dirtyArea().intersects(rect))
 *             gc.fill(rect);
 *     }
 * }
 *
 * PROS
 * - Improves the performance of an interactive graphical application
 *   by reducing the number of allocations of small objects during
 *   painting or picking.
 * - Having our own GraphicContext, instead of using Graphics2D, leaves
 *   open the possibility of defining a stack-model for the graphic context.
 *
 * CONS
 * - The programmer must be aware that the small pre-allocated objects
 *   provided by a graphic context will be reused by other glyphs and
 *   should not be referenced any more after the call to paint is over.
 *   This risk has been slightly reduced by prefixing the getter names with
 *   "temporary", for example: temporaryRectangle.
 * - The complexity associated with the creation of a new layer on top
 *   of the existing Graphics2D.
 */
public class GraphicContext {
   private static Stroke zeroLineWidthStroke = new BasicStroke(1);

   // Info that is always kept up-to-date
   private AffineTransform deviceTransform;
   private MyTransform transform;
   private Rectangle2D dirtyPixelBounds;
   private Rectangle2D.Double dirtyBounds;

   // Pre-allocated small temp objects
   private Point2D.Double tempPoint = new Point2D.Double();
   private Line2D.Double tempLine = new Line2D.Double();
   private Rectangle2D.Double tempRectangle = new Rectangle2D.Double();
   private Arc2D.Double tempArc = new Arc2D.Double();
   private AffineTransform tempTransform = new AffineTransform();
   private Bounds tempBounds = new Bounds();

   private RootGlyph root;
   private Graphics2D g2d;
   private double[] tempDoubles = new double[8];
   private static double[] staticTempDoubles = new double[8];
   private static int[] xs = new int[128];
   private static int[] ys = new int[128];

   /**
    * Initialize a new GraphicContext with the given Graphics2D.
    *
    * The constructor allocates once all the small objects that might
    * be temporarilly necessary during recursive calls to paint or pick.
    */
   public GraphicContext(RootGlyph root, Graphics2D g2d) {
      this.root = root;
      this.g2d = g2d; //not necessary? (Graphics2D)g2d.create();

      deviceTransform = g2d.getTransform();
      transform = new MyTransform();

      Rectangle r = g2d.getClipBounds();
      if (r != null) {
         //@todo the current implementation of TierGlyph is too complex
         //      (but having being optimized, it is very fast).
         //      I tried to quickly make a new clean&fast version and I failed.
         //      It will require some time.
         //      One of the problem with the current version is that it does not
         //      care much about the extra-pixels in Bounds.
         //      The following is a quick hook to make it work.
         int n = 5;
         dirtyPixelBounds = new Rectangle2D.Double(r.x - n, r.y - n, r.width + 2 * n, r.height + 2 * n);
         dirtyBounds = new Rectangle2D.Double(r.x - n, r.y - n, r.width + 2 * n, r.height + 2 * n);
      }
      else {
         dirtyPixelBounds = new Rectangle2D.Double();
         dirtyBounds = new Rectangle2D.Double();
      }
   }

   /**
    * Return the root-glyph.
    */
   public RootGlyph root() {
      return root;
   }

   /**
    * Return the dirty bounds in pixels.
    */
   public Rectangle2D dirtyPixelBounds() {
      return dirtyPixelBounds;
   }

   /**
    * Return the dirty bounds in user space units.
    */
   public Rectangle2D dirtyBounds() {
      return dirtyBounds;
   }

   /**
    * Return the width of a pixel in user space units.
    */
   public double pixelWidth() {
      //@todo should start with userSpace(1,0) (and then invert) ?
      double d = transform.getDeterminant();
      double w = (transform.getScaleY() - transform.getShearY()) / d;
      return w;
   }

   /**
    * Return the height of a pixel in user space units.
    */
   public double pixelHeight() {
      double d = transform.getDeterminant();
      double h = (transform.getScaleX() - transform.getShearX()) / d;
      return h;
   }

   /**
    * Return a Point2D.Double instance that can be used temporarilly
    * during the execution of paint or pick.
    *
    * The instance has been allocated once, and the same instance can
    * potentially be used by all the glyphs belonging to the same root.
    */
   public Point2D.Double tempPoint() {
      return tempPoint;
   }

   /**
    * Set and return a Point2D.Double instance that can be used
    * temporarilly during the execution of paint or pick.
    *
    * The instance has been allocated once, and the same instance can
    * potentially be used by all the glyphs belonging to the same root.
    */
   public Point2D.Double tempPoint(double x, double y) {
      tempPoint.x = x;
      tempPoint.y = y;
      return tempPoint;
   }

   /**
    * Return a Line2D.Double instance that can be used temporarilly
    * during the execution of paint or pick.
    *
    * The instance has been allocated once, and the same instance can
    * potentially be used by all the glyphs belonging to the same root.
    */
   public Line2D.Double tempLine() {
      return tempLine;
   }

   /**
    * Return a Line2D.Double instance that can be used temporarilly
    * during the execution of paint or pick.
    * The line is reset with the given parameters.
    *
    * The instance has been allocated once, and the same instance can
    * potentially be used by all the glyphs belonging to the same root.
    */
   public Line2D.Double tempLine(double x1, double y1, double x2, double y2) {
      tempLine.x1 = x1;
      tempLine.y1 = y1;
      tempLine.x2 = x2;
      tempLine.y2 = y2;
      return tempLine;
   }

   /**
    * Return a Rectangle2D.Double instance that can be used temporarilly
    * during the execution of paint or pick.
    *
    * The instance has been allocated once, and the same instance can
    * potentially be used by all the glyphs belonging to the same root.
    */
   public Rectangle2D.Double tempRectangle() {
      return tempRectangle;
   }

   /**
    * Set and eturn a Rectangle2D.Double instance that can be used
    * temporarilly during the execution of paint or pick.
    *
    * The instance has been allocated once, and the same instance can
    * potentially be used by all the glyphs belonging to the same root.
    */
   public Rectangle2D.Double tempRectangle(double x, double y, double w, double h) {
      tempRectangle.setRect(x, y, w, h);
      return tempRectangle;
   }

   /**
    * Return an Arc2D.Double instance that can be used temporarilly
    * during the execution of paint or pick.
    *
    * The instance has been allocated once, and the same instance can
    * potentially be used by all the glyphs belonging to the same root.
    */
   public Arc2D.Double tempArc() {
      return tempArc;
   }

   /**
    * Return an AffineTransform instance that can be used temporarilly
    * during the execution of paint or pick.
    *
    * The instance has been allocated once, and the same instance can
    * potentially be used by all the glyphs belonging to the same root.
    */
   public AffineTransform tempTransform() {
      return tempTransform;
   }

   public Bounds tempBounds() {
      tempBounds.reset();
      return tempBounds;
   }

   public double[] tempDoubles() {
      return tempDoubles;
   }

   /**
    * Transform user-space rectangular bounds (srcRect) into
    * the smallest possible pixel-space rectangular bounds (dstRect).
    *
    * It is ok for the destination rectangle to be the same as the
    * source rectangle.
    */
   public void transformBounds(Rectangle2D srcRect, Rectangle2D.Double dstRect) {
      transformBounds(transform, srcRect, dstRect);
   }

   /**
    * Transform user-space rectangular bounds (srcRect) into
    * the smallest possible pixel-space rectangular bounds (dstRect).
    *
    * The user-space is defined by the given transform.
    *
    * It is ok for the destination rectangle to be the same as the
    * source rectangle.
    */
   public static void transformBounds(AffineTransform transform, Rectangle2D srcRect, Rectangle2D.Double dstRect) {
      double[] p = staticTempDoubles;
      p[0] = srcRect.getX();
      p[1] = srcRect.getY();
      p[2] = p[0];
      p[3] = srcRect.getMaxY();
      p[4] = srcRect.getMaxX();
      p[5] = p[3];
      p[6] = p[4];
      p[7] = srcRect.getY();

      transform.transform(p, 0, p, 0, 4);

      dstRect.x = min(p[0], p[2], p[4], p[6]);
      dstRect.y = min(p[1], p[3], p[5], p[7]);
      double xmax = max(p[0], p[2], p[4], p[6]);
      double ymax = max(p[1], p[3], p[5], p[7]);
      dstRect.width = xmax - dstRect.x;
      dstRect.height = ymax - dstRect.y;
   }

   /**
    * Transform pixel-space rectangular bounds (srcRect) into
    * the smallest possible user-space rectangular bounds (dstRect).
    *
    * It is ok for the destination rectangle to be the same as the
    * source rectangle.
    */
   public void inverseTransformBounds(Rectangle2D srcRect, Rectangle2D.Double dstRect) {
      double[] p = tempDoubles;
      p[0] = srcRect.getX();
      p[1] = srcRect.getY();
      p[2] = p[0];
      p[3] = srcRect.getMaxY();
      p[4] = srcRect.getMaxX();
      p[5] = p[3];
      p[6] = p[4];
      p[7] = srcRect.getY();

      try {
         transform.inverseTransform(p, 0, p, 0, 4);
      }
      catch (NoninvertibleTransformException ex) {
      }

      dstRect.x = min(p[0], p[2], p[4], p[6]);
      dstRect.y = min(p[1], p[3], p[5], p[7]);
      double xmax = max(p[0], p[2], p[4], p[6]);
      double ymax = max(p[1], p[3], p[5], p[7]);
      dstRect.width = xmax - dstRect.x;
      dstRect.height = ymax - dstRect.y;
   }

   /**
    * Return the minimum among four double values.
    */
   public static double min(double a, double b, double c, double d) {
      double min;
      if (a < b) {
         if (c < d)
            min = (a < c) ? a : c;
         else
            min = (a < d) ? a : d;
      }
      else {
         if (c < d)
            min = (b < c) ? b : c;
         else
            min = (b < d) ? b : d;
      }
      return min;
   }

   /**
    * Return the maximum among four double values.
    */
   public static double max(double a, double b, double c, double d) {
      double max;
      if (a > b) {
         if (c > d)
            max = (a > c) ? a : c;
         else
            max = (a > d) ? a : d;
      }
      else {
         if (c > d)
            max = (b > c) ? b : c;
         else
            max = (b > d) ? b : d;
      }
      return max;
   }

   /**
    * Sets the line width to be one pixel,
    * independently of the transform.
    */
   public void setZeroLineWidth() {
      g2d.setStroke(zeroLineWidthStroke);
   }

   public static BufferedImage lastTextureImage;
   public static TexturePaint lastTexture;

   //@todo uggly
   public TexturePaint createTexture(BufferedImage i) {
      if (lastTextureImage == i)
         return lastTexture;

      Rectangle2D.Double r = tempRectangle();
      r.setRect(0, 0, i.getWidth(), i.getHeight());
      lastTexture = new TexturePaint(i, r);
      lastTextureImage = i;

      return lastTexture;
   }

   //--- Graphics2D specialization --------------------------------------

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void draw3DRect(int x, int y, int width, int height, boolean raised) {
      tempDoubles[0] = x;
      tempDoubles[1] = y;
      tempDoubles[2] = x + width;
      tempDoubles[3] = y + height;
      transform.transform(tempDoubles, 0, tempDoubles, 0, 2);
      int xmin = (int) tempDoubles[0];
      int ymin = (int) tempDoubles[1];
      int w = (int) tempDoubles[2] - xmin;
      int h = (int) tempDoubles[3] - ymin;
      if (w <= 0)
         w = 1;
      if (h <= 0)
         h = 1;

      g2d.draw3DRect(xmin, ymin, w, h, raised);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void fill3DRect(int x, int y, int w, int h, boolean raised) {
      tempDoubles[0] = x;
      tempDoubles[1] = y;
      tempDoubles[2] = x + w;
      tempDoubles[3] = y + h;
      transform.transform(tempDoubles, 0, tempDoubles, 0, 2);
      int xmin = (int) tempDoubles[0];
      int ymin = (int) tempDoubles[1];
      int width = (int) tempDoubles[2] - xmin;
      int height = (int) tempDoubles[3] - ymin;
      if (width <= 0)
         width = 1;
      if (height <= 0)
         height = 1;

      g2d.fill3DRect(xmin, ymin, width, height, raised);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void draw(Shape s) {
      int i = 0;
      for (PathIterator iterator = s.getPathIterator(transform, 1); !iterator.isDone(); ++i, iterator.next()) {
         if (iterator.currentSegment(tempDoubles) == PathIterator.SEG_CLOSE) {
            xs[i] = xs[0];
            ys[i] = ys[0];
            ++i;
            break;
         }
         xs[i] = (int) tempDoubles[0];
         ys[i] = (int) tempDoubles[1];
      }
      g2d.drawPolyline(xs, ys, i);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
      AffineTransform t = new AffineTransform(transform);
      t.concatenate(xform);
      return g2d.drawImage(img, t, obs);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
      g2d.transform(transform);
      g2d.drawImage(img, op, x, y);
      g2d.setTransform(deviceTransform);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void drawImage(Image img, int x, int y, ImageObserver obs) {
      g2d.transform(transform);
      g2d.drawImage(img, x, y, obs);
      g2d.setTransform(deviceTransform);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
      AffineTransform t = new AffineTransform(transform);
      t.concatenate(xform);
      g2d.drawRenderedImage(img, t);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
      AffineTransform t = new AffineTransform(transform);
      t.concatenate(xform);
      g2d.drawRenderableImage(img, t);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void drawString(String s, double x, double y) {
      tempDoubles[0] = x;
      tempDoubles[1] = y;
      transform.transform(tempDoubles, 0, tempDoubles, 0, 1);
      g2d.drawString(s, (int) tempDoubles[0], (int) tempDoubles[1]);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void drawString(AttributedCharacterIterator iterator, double x, double y) {
      tempDoubles[0] = x;
      tempDoubles[1] = y;
      transform.transform(tempDoubles, 0, tempDoubles, 0, 1);
      g2d.drawString(iterator, (int) tempDoubles[0], (int) tempDoubles[1]);
   }

   public double getStringWidth(String s) {
      Rectangle2D b = getFont().getStringBounds(s, getFontRenderContext());
      tempDoubles[0] = b.getMinX();
      tempDoubles[1] = b.getMinY();
      tempDoubles[2] = b.getMaxX();
      tempDoubles[3] = b.getMaxY();
      try {
         transform.inverseTransform(tempDoubles, 0, tempDoubles, 0, 2);
      }
      catch (java.awt.geom.NoninvertibleTransformException ex) {
      }
      return tempDoubles[2] - tempDoubles[0];
   }

   public void drawChars(char data[], int offset, int length, double x, double y) {
      tempDoubles[0] = x;
      tempDoubles[1] = y;
      transform.transform(tempDoubles, 0, tempDoubles, 0, 1);
      g2d.drawChars(data, offset, length, (int) tempDoubles[0], (int) tempDoubles[1]);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void drawGlyphVector(GlyphVector g, float x, float y) {
      tempDoubles[0] = x;
      tempDoubles[1] = y;
      transform.transform(tempDoubles, 0, tempDoubles, 0, 1);
      g2d.drawGlyphVector(g, (int) tempDoubles[0], (int) tempDoubles[1]);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void fill(Shape s) {
      int i = 0;
      for (PathIterator iterator = s.getPathIterator(transform, 1); !iterator.isDone(); ++i, iterator.next()) {
         if (iterator.currentSegment(tempDoubles) == PathIterator.SEG_CLOSE) {
            xs[i] = xs[0];
            ys[i] = ys[0];
            ++i;
            break;
         }
         xs[i] = (int) tempDoubles[0];
         ys[i] = (int) tempDoubles[1];
      }

      try {
         if (i > 0)
            g2d.fillPolygon(xs, ys, i);
      }
      catch (Throwable t) {
      }
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
      g2d.transform(transform);
      boolean b = g2d.hit(rect, s, onStroke);
      g2d.setTransform(deviceTransform);
      return b;
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public GraphicsConfiguration getDeviceConfiguration() {
      return g2d.getDeviceConfiguration();
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void setComposite(Composite comp) {
      g2d.setComposite(comp);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void setPaint(Paint paint) {
      g2d.setPaint(paint);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void setStroke(Stroke s) {
      g2d.setStroke(s);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void setRenderingHint(Key hintKey, Object hintValue) {
      g2d.setRenderingHint(hintKey, hintValue);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public Object getRenderingHint(Key hintKey) {
      return g2d.getRenderingHint(hintKey);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void setRenderingHints(Map hints) {
      g2d.setRenderingHints(hints);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public void addRenderingHints(Map hints) {
      g2d.addRenderingHints(hints);
   }

   /**
    * Delegate the call to the underlying Graphics2D.
    */
   public RenderingHints getRenderingHints() {
      return g2d.getRenderingHints();
   }

   /**
    * Delegate the call to the underlying Graphics2D
    *
    * (and keep an up-to-date version of the current transform
    * and of the dirty bounds in user space).
    */
   public void translate(double tx, double ty) {
      transform.privateTranslate(tx, ty);
      dirtyBounds.x -= tx;
      dirtyBounds.y -= ty;
   }

   /**
    * Delegate the call to the underlying Graphics2D
    *
    * (and keep an up-to-date version of the current transform
    * and of the dirty bounds).
    */
   public void rotate(double theta) {
      transform.privateRotate(theta);
      inverseTransformBounds(dirtyPixelBounds, dirtyBounds);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    *
    * (and keep an up-to-date version of the current transform
    * and of the dirty bounds).
    */
   public void rotate(double theta, double x, double y) {
      transform.privateRotate(theta, x, y);
      inverseTransformBounds(dirtyPixelBounds, dirtyBounds);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    *
    * (and keep an up-to-date version of the current transform
    * and of the dirty bounds).
    */
   public void scale(double sx, double sy) {
      transform.privateScale(sx, sy);
      dirtyBounds.x /= sx;
      dirtyBounds.y /= sy;
      dirtyBounds.width /= sx;
      dirtyBounds.height /= sy;
   }

   /**
    * Delegate the call to the underlying Graphics2D
    *
    * (and keep an up-to-date version of the current transform
    * and of the dirty bounds).
    */
   public void shear(double shx, double shy) {
      transform.shear(shx, shy);
      inverseTransformBounds(dirtyPixelBounds, dirtyBounds);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    *
    * (and keep an up-to-date version of the current transform
    * and of the dirty bounds).
    */
   public void transform(AffineTransform tx) {
      transform.privateConcatenate(tx);
      inverseTransformBounds(dirtyPixelBounds, dirtyBounds);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    *
    * (and keep an up-to-date version of the current transform
    * and of the dirty bounds).
    */
   public void setTransform(AffineTransform tx) {
      transform.privateSetTransform(tx);
      inverseTransformBounds(dirtyPixelBounds, dirtyBounds);
   }

   /**
    * Return a const version of the current transform.
    */
   public AffineTransform getTransform() {
      return transform;
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public Paint getPaint() {
      return g2d.getPaint();
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public Composite getComposite() {
      return g2d.getComposite();
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public void setBackground(Color color) {
      g2d.setBackground(color);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public Color getBackground() {
      return g2d.getBackground();
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public Stroke getStroke() {
      return g2d.getStroke();
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public void clip(Shape s) {
      g2d.transform(transform);
      g2d.clip(s);
      g2d.setTransform(deviceTransform);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public FontRenderContext getFontRenderContext() {
      return g2d.getFontRenderContext();
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public Shape getClip() {
      g2d.transform(transform);
      Shape clip = g2d.getClip();
      g2d.setTransform(deviceTransform);
      return clip;
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public void setClip(Shape shape) {
      g2d.transform(transform);
      g2d.setClip(shape);
      g2d.setTransform(deviceTransform);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public void setColor(Color color) {
      g2d.setColor(color);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public void fillRect(double x, double y, double w, double h) {
      tempDoubles[0] = x;
      tempDoubles[1] = y;
      tempDoubles[2] = x + w;
      tempDoubles[3] = y + h;
      transform.transform(tempDoubles, 0, tempDoubles, 0, 2);
      int xmin = (int) tempDoubles[0];
      int ymin = (int) tempDoubles[1];
      int width = (int) tempDoubles[2] - xmin;
      int height = (int) tempDoubles[3] - ymin;
      if (width <= 0)
         width = 1;
      if (height <= 0)
         height = 1;
      g2d.fillRect(xmin, ymin, width, height);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public void drawRect(double x, double y, double w, double h) {
      tempDoubles[0] = x;
      tempDoubles[1] = y;
      tempDoubles[2] = x + w;
      tempDoubles[3] = y + h;
      transform.transform(tempDoubles, 0, tempDoubles, 0, 2);
      int xmin = (int) tempDoubles[0];
      int ymin = (int) tempDoubles[1];
      int width = (int) tempDoubles[2] - xmin;
      int height = (int) tempDoubles[3] - ymin;
      if (width <= 0)
         width = 1;
      if (height <= 0)
         height = 1;
      g2d.drawRect(xmin, ymin, width, height);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public void drawLine(int x1, int y1, int x2, int y2) {
      tempDoubles[0] = x1;
      tempDoubles[1] = y1;
      tempDoubles[2] = x2;
      tempDoubles[3] = y2;
      transform.transform(tempDoubles, 0, tempDoubles, 0, 2);
      g2d.drawLine((int) tempDoubles[0], (int) tempDoubles[1], (int) tempDoubles[2], (int) tempDoubles[3]);
   }

   public void drawLine(double x1, double y1, double x2, double y2) {
      tempDoubles[0] = x1;
      tempDoubles[1] = y1;
      tempDoubles[2] = x2;
      tempDoubles[3] = y2;
      transform.transform(tempDoubles, 0, tempDoubles, 0, 2);
      g2d.drawLine((int) tempDoubles[0], (int) tempDoubles[1], (int) tempDoubles[2], (int) tempDoubles[3]);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public void fillPolygon(double xPoints[], double yPoints[], int nPoints) {
      for (int i = 0; i < nPoints; ++i) {
         tempDoubles[0] = xPoints[i];
         tempDoubles[1] = yPoints[i];
         transform.transform(tempDoubles, 0, tempDoubles, 0, 1);
         xs[i] = (int) tempDoubles[0];
         ys[i] = (int) tempDoubles[1];
      }
      g2d.fillPolygon(xs, ys, nPoints);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public void setFont(Font font) {
      g2d.setFont(font);
   }

   /**
    * Delegate the call to the underlying Graphics2D
    */
   public Font getFont() {
      return g2d.getFont();
   }
}

//A const transform.
//Ensures that the transform returned by GraphicContext.getTransform
//cannot be changed by the caller
class MyTransform extends AffineTransform {
   MyTransform() {
   }

   MyTransform(AffineTransform tx) {
      super(tx);
   }

   void privateTranslate(double tx, double ty) {
      super.translate(tx, ty);
   }

   public void translate(double tx, double ty) {
      constError();
   }

   void privateRotate(double theta) {
      super.rotate(theta);
   }

   public void rotate(double theta) {
      constError();
   }

   void privateRotate(double theta, double x, double y) {
      super.rotate(theta, x, y);
   }

   public void rotate(double theta, double x, double y) {
      constError();
   }

   void privateScale(double sx, double sy) {
      super.scale(sx, sy);
   }

   public void scale(double sx, double sy) {
      constError();
   }

   void privateShear(double shx, double shy) {
      super.shear(shx, shy);
   }

   public void shear(double shx, double shy) {
      constError();
   }

   public void setToIdentity() {
      constError();
   }

   public void setToTranslation(double tx, double ty) {
      constError();
   }

   public void setToRotation(double theta) {
      constError();
   }

   public void setToRotation(double theta, double x, double y) {
      constError();
   }

   public void setToScale(double sx, double sy) {
      constError();
   }

   public void setToShear(double shx, double shy) {
      constError();
   }

   public void setTransform(AffineTransform Tx) {
      constError();
   }

   public void privateSetTransform(AffineTransform Tx) {
      super.setTransform(Tx);
   }

   public void setTransform(double m00, double m10, double m01, double m11, double m02, double m12) {
      constError();
   }

   public void concatenate(AffineTransform Tx) {
      constError();
   }

   void privateConcatenate(AffineTransform Tx) {
      super.concatenate(Tx);
   }

   public void preConcatenate(AffineTransform Tx) {
      constError();
   }

   private void constError() {
      Assert.vAssert(false, "attempt to change a const affine transform " + "that belongs to GraphicContext");
   }
}
