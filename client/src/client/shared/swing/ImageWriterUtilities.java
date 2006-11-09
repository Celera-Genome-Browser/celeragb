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

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import java.io.OutputStream;


/**
 * Utility class used to write out Components in various image formats.
 * Currently only supports JPEG output.
 */
public class ImageWriterUtilities {
    private ImageWriterUtilities() {
    }

    /**
     * Creates an image from a component and encodes + writes the image to
     * the output stream. It doesn't close the output stream.
     * @param comp the <code>Component</code> to be encoded.
     * @param out the <code>OutputStream</code> to write the component to.
     */
    public static void JPGEncodeComponent(Component comp, OutputStream out) {
        BufferedImage compImage = new BufferedImage(comp.getWidth(), 
                                                    comp.getHeight(), 
                                                    BufferedImage.TYPE_INT_RGB);
        Graphics g = compImage.getGraphics();
        comp.paint(g);

        try {
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(compImage);
            param.setQuality(0.9f, true);
            encoder.encode(compImage, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}