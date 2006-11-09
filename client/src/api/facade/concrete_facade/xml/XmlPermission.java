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
package api.facade.concrete_facade.xml;

import api.facade.facade_mgr.FacadeManager;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies (peter.davies)
 * @version $Id$
 */

public class XmlPermission {

  private static final XmlPermission instance=new XmlPermission();
  private static final String PUBLIC_CERTIFICATE_FILE_NAME="/resource/client/PublicCertificate";
  private static final String INTERNAL_CERTIFICATE_FILE_NAME="/resource/client/InternalCertificate";

  private static final String SIGNATURE_BEGIN_STRING="<!--SIGNATURE:";
  private static final String SIGNATURE_END_STRING="-->";
  private static final long DAYS_1=1000L*60*60*24;
  private static final long DAYS_15=DAYS_1*15;
  private static final int[] pubFp=new int[]{0x71,0xc8,0xe7,0x6a,0x71,0x03,0x75,0x1B,0x92,
                0xfe,0xa3,0xd2,0xde,0x5f,0xa3,0x0d,0xa8,0x10,0x5b,0x60};
  private static byte[] publicFingerPrint;
  private static final int[] internalFp=new int[]{0x12,0x6F,0x30,0xC2,0xD2,0xA8,0xD8,0xC4,0x71,
                0xC3,0xB5,0x5A,0x57,0x8F,0x4C,0x04,0x35,0x1E,0x0C,0x1D};
  private static byte[] internalFingerPrint;
  private PublicKey publicKey;
  private static final byte CERTIFICATE_STATE_UNKNOWN=0;
  private static final byte CERTIFICATE_STATE_VALID=1;
  private static final byte CERTIFICATE_STATE_INVALID=2;
  private byte internalCertificateState=CERTIFICATE_STATE_UNKNOWN;
  private byte publicCertificateState=CERTIFICATE_STATE_UNKNOWN;

  static {
      publicFingerPrint=new byte[pubFp.length];
      for (int i=0;i<pubFp.length;i++) {
        publicFingerPrint[i]=(byte)pubFp[i];
      }
      internalFingerPrint=new byte[internalFp.length];
      for (int i=0;i<internalFp.length;i++) {
        internalFingerPrint[i]=(byte)internalFp[i];
      }
  }
  private XmlPermission(){ } //singleton enforcement --PED 6/7/01

  public static XmlPermission getXmlPermission() {
    return instance;
  }

  public boolean canReadFile(File xmlFile) {
    if (!xmlFile.canRead()) return false;
    if (!xmlFile.getName().toLowerCase().endsWith(".gba")) return true;
    if (hasValidInternalCertificate()==CERTIFICATE_STATE_VALID) return true;
    else return validatePublicFile(xmlFile);
  }

  private boolean validatePublicFile(File file) {
    try {
      PublicKey pubKey=getPublicKey();
      Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
      sig.initVerify(pubKey);

      FileInputStream dataFis = new FileInputStream(file);
      byte[] fileData = new byte[dataFis.available()];
      dataFis.read(fileData);
      dataFis.close();
      String fileString=new String(fileData);

      int dataEnd=fileString.indexOf(SIGNATURE_BEGIN_STRING);
      if (dataEnd<0) return false;
      int sigBegin=dataEnd+SIGNATURE_BEGIN_STRING.length();
      int sigEnd=fileString.indexOf(SIGNATURE_END_STRING,sigBegin);

      //Get the signature
      byte[] signature=new byte[sigEnd-sigBegin];
      System.arraycopy(fileData,sigBegin,signature,0,signature.length);

      //Convert the signature from hex encoded ascii to real bytes
      List byteList=new ArrayList();
      StringBuffer tmpString=new StringBuffer();
      for (int i=0;i<signature.length;i++) {
          if (signature[i]!=':') {
            tmpString.append((char)signature[i]);
          }
          else {
            byteList.add(new Byte((byte)Integer.parseInt(tmpString.toString(),16)));
            tmpString=new StringBuffer();
          }
      }
      byteList.add(new Byte((byte)Integer.parseInt(tmpString.toString(),16)));
      signature = new byte[byteList.size()];
      for (int i=0;i<byteList.size();i++) {
        signature[i]=((Byte)byteList.get(i)).byteValue();
      }

      //Get the data
      byte[] data=new byte[dataEnd];
      System.arraycopy(fileData,0,data,0,dataEnd);

      //Update signature with data
      sig.update(data);

      //Verify the data with the signature
      return sig.verify(signature);
    }
    catch (Exception ex) {
      return false;
    }
  }

  private byte hasValidInternalCertificate() {
	return CERTIFICATE_STATE_VALID;
// JCVI LLF: 10/20/2006
//  Removed, but can return if needed.  Since software is open, probably should remove altogether.
//	
//    if (internalCertificateState!=CERTIFICATE_STATE_UNKNOWN) {
//      return internalCertificateState;
//    }
//    java.security.cert.Certificate cert= getValidCertificate("Internal Certificate",
//      INTERNAL_CERTIFICATE_FILE_NAME, internalFingerPrint);
//    if (cert!=null) {
//      internalCertificateState=CERTIFICATE_STATE_VALID;
//      System.out.println("Using Internal Certificate for unsigned XML Files");
//    }
//    else internalCertificateState=CERTIFICATE_STATE_INVALID;
//    return internalCertificateState;
  }


  private PublicKey getPublicKey() {
    if (publicKey!=null) return publicKey;
    if (publicCertificateState==CERTIFICATE_STATE_INVALID) return null;
    java.security.cert.Certificate cert=getValidCertificate("Public Certificate",
      PUBLIC_CERTIFICATE_FILE_NAME, publicFingerPrint);
    if (cert==null) {
      publicCertificateState=CERTIFICATE_STATE_INVALID;
      return null;
    }
    System.out.println("Using Public Certificate for signed XML Files");
    publicKey = cert.getPublicKey();
    publicCertificateState=CERTIFICATE_STATE_VALID;
    return publicKey;
  }

  /**
   * @return null if not valid
   */
  private java.security.cert.Certificate getValidCertificate(
    String certificateName, String certificateFile, byte[] fingerPrint) {
    try {
      //Load the certificate
      DataInputStream dataCertIs = new DataInputStream(getClass().
        getResourceAsStream(certificateFile));

      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      byte[] bytes = new byte[dataCertIs.available()];
      dataCertIs.readFully(bytes);
      ByteArrayInputStream baCertIs = new ByteArrayInputStream(bytes);
      java.security.cert.Certificate certificate=null;

      while (baCertIs.available() > 0) {
         certificate = cf.generateCertificate(baCertIs);
      }

      //Verify Date
      try {
        //Do X509 validation
        X509Certificate certificateX509=(X509Certificate)certificate;
        certificateX509.checkValidity();
        //Show warning of approaching date
        Date notAfter=certificateX509.getNotAfter();
        long dateDiff=notAfter.getTime()-new Date().getTime();
        if (dateDiff<DAYS_15) {
          FacadeManager.handleException(new XMLSecurityException("Warning: "+
            certificateName+" will expire in "+Math.floor(dateDiff/DAYS_1)+" days."));
        }
      }
      catch (Exception ex) {
         return null;
      }

      //Verify the certificate's fingerprint
      MessageDigest md=MessageDigest.getInstance("SHA1");
      byte[] digest=md.digest(certificate.getEncoded());
      for (int i=0;i<digest.length;i++) {
        if (digest[i]!=fingerPrint[i]) {
          System.out.println(certificateName+" has been tampered with.");
          return null;
        }
      }

      return certificate;
    }
    catch (Exception ex) {
      return null;
    }
  }
}