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
 * Title:        XML Promoter<p>
 * Description:  <p>
 * Company:      <p>
 * @author Peter Davies
 * @version 1.0
 *
 * This class will take an XML file and send it to the promotion routine on the
 * EJB server.
 */
package client.xml_promoter;

import api.stub.data.OID;
import api.stub.data.PromotionReport;
import api.stub.ejb.model.genomicservice.XMLPromoter;
import api.stub.ejb.model.genomicservice.XMLPromoterHome;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
//import api.entity_model.model.genetics.GenomeVersion;

public class Promoter {

  private String serverURL;
  private String fileName;
  private final static String CONTEXT_FACTORY="weblogic.jndi.WLInitialContextFactory" ;
  private final static String APP_SERVER_NAME="";
  private final static String COMPONENT_NAME="XMLPromoter";
  private final static String DEFAULT_DUMP_FILE="dumpfile";
  private static boolean isDebug=false;
  private static String dumpFileName;
  private static boolean validateOnly=false;

  // 67 is the default layer at the time of writing for obsoleted features
  private static int obsoleteDataLayerId=67;

  // 68 is the default layer at the time of writing for obsoleted features
  private static int promoteDataLayerId=68;

  // Species, assembly version and species are used to select a valid
  // genome version for promotion.
  private static String databaseName = null;
  private static String speciesName = null;
  private static String assemblyVersion = null;

  private static OID genomeVersionOID = null;

  private static Hashtable errMsgTable;

  static
  {
    errMsgTable = new Hashtable();
    errMsgTable.put(new Integer(0), "Run successful, ");
    errMsgTable.put(new Integer(1), "-Dx.xml_promoter.FileName option not specified");
    errMsgTable.put(new Integer(2), "-Dx.xml_promoter.ServerURL option not specified");
    errMsgTable.put(new Integer(3), "-Dx.xml_promoter.PromotionType not valid");
    errMsgTable.put(new Integer(4), "-Dx.xml_promoter.Debug not valid");
    errMsgTable.put(new Integer(5), "-Dx.xml_promoter.DumpFileName not valid");

    errMsgTable.put(new Integer(10), "Cannot find the file you specified. ");
    errMsgTable.put(new Integer(11), "The program encountered an exception while reading the file. ");
    errMsgTable.put(new Integer(12), "The EJB server refused your username and/or password. ");
    errMsgTable.put(new Integer(13), "Could not connect to the EJB server. ");
    errMsgTable.put(new Integer(14), "Could not get Home interface specified with JNDI name: ");
    errMsgTable.put(new Integer(15), "Could not get Remote Interface");
    errMsgTable.put(new Integer(16), "Promotion failed. ");
    errMsgTable.put(new Integer(17), "Check aligned features promotable call failed, reason ");
    errMsgTable.put(new Integer(18), "Fail to write to the dump file. ");
    errMsgTable.put(new Integer(19), "Fail to pass promotion check. ");
    errMsgTable.put(new Integer(20), "Fail to promote. ");
  }

  public String getErrorMessage(int errorCode)
  {
    return (String) errMsgTable.get(new Integer(errorCode));
  }

  public Promoter(String serverURL, String fileName) {
     this.serverURL=serverURL;
     this.fileName=fileName;
  }

  /**
   * Return of 0 indicates a successfull promote, any non zero
   * return indicates failure
   */
  public int promote() {

     int promoteSuccess = 0;
     StringBuffer xmlFile=getFileAsStringBuffer();
     InitialContext context=getContext();
     XMLPromoterHome home = getHome(context);
     XMLPromoter promoter=null;



     try {
        promoter= home.create();
     }
     catch (Exception ex) {
        outputError(15, ex.getMessage(), fileName);
        promoteSuccess = 1;
     }
     PromotionReport report = null;
     try
     {
        report = promoter.checkAlignedFeaturesPromotable(genomeVersionOID, xmlFile);
        if ( validateOnly )
        {
          outputReport(report, fileName);
          if (report.wasPromotable() == false) {
            promoteSuccess = 1;
          }
        }
        else
        {
           if ( report.wasPromotable() )
           {
              if (System.getProperty("x.xml_promoter.PromotionType").equalsIgnoreCase("PromoteAsReviewed")) {
                report=promoteAsReviewed(xmlFile,promoter);
              }
              else
              {
                report=promoteRegular(xmlFile,promoter);
              }
              if ( report != null )
                outputReport(report, fileName);
              if ( report.wasPromotable() )
              {
                outputError(0, "Pass the promotion. \n", fileName);
              }
              else
              {
                outputError(20, "", fileName);
                promoteSuccess = 1;
              }
           }
           else
           {
              outputReport(report, fileName);
              outputError(19, "", fileName);
              promoteSuccess = 1;
           }
        }
     }
     catch (Exception ex)
     {
       outputError(17, ex.getMessage(), fileName);
       promoteSuccess = 1;
     }

     return promoteSuccess;
  }

  private PromotionReport promoteRegular(StringBuffer xmlFile, XMLPromoter promoter)
  {
    PromotionReport report = null;
    try
    {
     report = promoter.promoteAlignedFeatures(genomeVersionOID, xmlFile, obsoleteDataLayerId, promoteDataLayerId);
    }
    catch (Exception ex) {
        outputError(16, ex.getMessage(), fileName);
    }
    return report;
  }

  private PromotionReport promoteAsReviewed(StringBuffer xmlFile, XMLPromoter promoter) {
    PromotionReport report = null;
    try {
      report = promoter.promoteAlignedFeaturesAsReviewed(genomeVersionOID, xmlFile, obsoleteDataLayerId, promoteDataLayerId);
    }
    catch (Exception ex) {
        outputError(16, ex.getMessage(), fileName);
    }
    return report;
  }

  private String getJNDIHomeInterfaceName (String applicationServerName, String EJBRemoteObjectName) {
    String jndiHomeInterfaceName = EJBRemoteObjectName;
    if (applicationServerName != null &&
	! applicationServerName.equals(""))
      jndiHomeInterfaceName = applicationServerName + "/" + jndiHomeInterfaceName;
    return jndiHomeInterfaceName;
  }

  private XMLPromoterHome getHome(InitialContext context) {
    try {
      EJBHome proxy = (EJBHome)context.lookup
      (
          getJNDIHomeInterfaceName
            (
              APP_SERVER_NAME,
              COMPONENT_NAME
            )
      );
      return (XMLPromoterHome)proxy;
    }
    catch (Exception ex) {
      outputError(14, getJNDIHomeInterfaceName(APP_SERVER_NAME,COMPONENT_NAME), fileName);
    }
    return null;
  }

  private InitialContext getContext() {
     InitialContext context=null;
     try {
  	 context = new InitialContext(getProperties());
     }
     catch (javax.naming.NamingException ex) {
        if (ex instanceof javax.naming.AuthenticationException) {
          outputError(12, ex.getMessage(), fileName);
        }
        else
        {
          outputError(13, ex.getMessage(), fileName);
        }
     }
     return context;
  }

  private StringBuffer getFileAsStringBuffer() {
     File file = new File(fileName);
     if (!file.canRead()) {
        outputError(10, "", fileName);
     }
     long fileLength=file.length();
     byte[] fileBytes=new byte[(int)fileLength];
     try {
         DataInputStream iStream = new DataInputStream(file.toURI().toURL().openStream());
         iStream.readFully(fileBytes);
         return new StringBuffer(new String(fileBytes));
     }
     catch (Exception ex) {
      outputError(11, ex.getLocalizedMessage(), fileName);
     }
     return null;
  }

  private Properties getProperties() {
    Properties props=new Properties();
    props.put(Context.INITIAL_CONTEXT_FACTORY,CONTEXT_FACTORY);
    if (System.getProperty("x.xml_promoter.UserName")!=null)
       props.put(Context.SECURITY_PRINCIPAL,   System.getProperty("x.xml_promoter.UserName") );
    if (System.getProperty("x.xml_promoter.Password")!=null)
       props.put(Context.SECURITY_CREDENTIALS, System.getProperty("x.xml_promoter.Password") );
    props.put(Context.PROVIDER_URL,serverURL);
    return props;
  }


  static void main (String[] args)
  {
     String debug = System.getProperty("x.xml_promoter.Debug");
     if ( debug.equalsIgnoreCase("true") )
     {
      isDebug = true;
     }
     else
     {
        isDebug = false;
     }

     String validate = System.getProperty("x.xml_promoter.ValidateOnly");
     if ( validate.equalsIgnoreCase("true") )
     {
      validateOnly = true;
     }
     else
     {
      validateOnly = false;
     }

     dumpFileName = System.getProperty("x.xml_promoter.DumpFileName");
     if ( dumpFileName == null || dumpFileName.equals("") )
        dumpFileName = DEFAULT_DUMP_FILE;

     if (System.getProperty("x.xml_promoter.FileName")==null) {
        outputError(1, "", "");
     }
     if (System.getProperty("x.xml_promoter.ServerURL")==null) {
        outputError(2, "", "");
     }

     if (!((System.getProperty("x.xml_promoter.PromotionType").equalsIgnoreCase("Promote")) ||
         (System.getProperty("x.xml_promoter.PromotionType").equalsIgnoreCase("PromoteAsReviewed")))) {
        outputError(3, "", "");
     }

     String tempObsoleteLayerId = System.getProperty("x.xml_promoter.ObsoleteLayerId");
     if ((tempObsoleteLayerId != null) && (!tempObsoleteLayerId.equals("")))
     {
       obsoleteDataLayerId = Integer.parseInt(tempObsoleteLayerId);
     }

     String tempPromoteLayerId = System.getProperty("x.xml_promoter.PromoteLayerId");
     if ((tempPromoteLayerId != null) && (!tempPromoteLayerId.equals("")))
     {
       promoteDataLayerId = Integer.parseInt(tempPromoteLayerId);
     }

     dumpFileName = System.getProperty("x.xml_promoter.DumpFileName");
     if ( dumpFileName == null || dumpFileName.equals("") )

     if (System.getProperty("x.xml_promoter.FileName").equals("") )
     {
        printUsage();
        System.exit(1);
     }

     databaseName = System.getProperty("x.xml_promoter.DatabaseName");
     if ((databaseName == null) || (databaseName.equals(""))) {
        System.out.println("Database name set incorrectly");
        printUsage();
        System.exit(1);
     }

     speciesName = System.getProperty("x.xml_promoter.SpeciesName");
     if ((speciesName == null) || (speciesName.equals(""))) {
        System.out.println("Species name set incorrectly");
        printUsage();
        System.exit(1);
     }

     String assemblyVersion = System.getProperty("x.xml_promoter.AssemblyVersion");
     if ((assemblyVersion == null) || (assemblyVersion.equals(""))) {
        System.out.println("Assembly version set incorrectly");
        printUsage();
        System.exit(1);
     }

     // Create the OID of the genome version against which
     // promotion will take place

     /**
      * @todo to use GenomeVersion will require including a large portion
      * of the system via dependencies so for now avoid this so that
      * can get a test client out for Human C4
      */
//     int genomeVersionId = GenomeVersion.calcGenomeVersionId
//      (
//        speciesName,
//        databaseName,
//        Long.parseLong(assemblyVersion)
//      );
     String keyString = speciesName + databaseName + assemblyVersion;
     int genomeVersionId = keyString.hashCode();
     genomeVersionOID = new OID
      (
        OID.INTERNAL_DATABASE_NAMESPACE, "" + genomeVersionId, genomeVersionId
      );

     Promoter promoter=new Promoter(System.getProperty("x.xml_promoter.ServerURL"),
            System.getProperty("x.xml_promoter.FileName"));
     System.out.println("Using FileName: "+System.getProperty("x.xml_promoter.FileName")+
            " and server: "+System.getProperty("x.xml_promoter.ServerURL"));
     System.setProperty("weblogic.security.SSL.useJava", "true");
     int progSuccess = promoter.promote();
     System.exit(progSuccess);
  }
///usr/local/java/1.2.2-8/bin/java -Djava.compiler=NONE -D$base.FileName=$filename
// -D$base.ServerURL=$server -D$base.Debug=$debug -D$base.DumpFileName=$dump_file_
//name -D$base.ValidateOnly=$validate_only -D%base%.ObsoleteLayerId=%ObsoleteLayer
//Id% -D%base%.PromoteLayerId=%PromoteLayerId% -D$base.PromotionType=$promotion_ty
//pe -D$base.UserName=$username -D$base.Password=$password -classpath $classpath 
//client.xml_promoter.Promoter
  static void printUsage() {
        System.out.println("\nUsage: java <-D options as defined below> client.xml_promoter.Promoter");
        System.out.println("-Dx.xml_promoter.FileName=<filename or dirname for batch>");
        System.out.println("-Dx.xml_promoter.ServerURL=<EJB Server URL>");
        System.out.println("-Dx.xml_promoter.PromotionType=<Promote || PromoteAsReviewed>");
        System.out.println("-Dx.xml_promoter.UserName=<username> (if required by server)");
        System.out.println("-Dx.xml_promoter.Password=<password> (if required by server)");
        System.out.println("-Dx.xml_promoter.Debug=<true || false>");
        System.out.println("-Dx.xml_promoter.ObsoleteLayerId=<IDS data layer id>");
        System.out.println("-Dx.xml_promoter.PromoteLayerId=<IDS data layer id>");
        System.out.println("-Dx.xml_promoter.DumpFileName=<dump_filename>");
        System.out.println("-Dx.xml_promoter.DatabaseName=<eg VIZ_PROMOTE_PRD_HUM>");
        System.out.println("-Dx.xml_promoter.AssemblyVersion=<assembly>");
        System.out.println("-Dx.xml_promoter.SpeciesName=<eg Homo sapiens>");
        System.out.println("Errorlevels: ");
        Enumeration keys = errMsgTable.keys();
        while ( keys.hasMoreElements() )
        {
          Integer key = (Integer) keys.nextElement();
          System.out.println( key + " - " + errMsgTable.get(key));
        }
        System.out.println("\n");
  }
  private static void outputReport(PromotionReport report, String promoteFile)
  {
    if ( isDebug )
    {
      System.out.println("Promote report on " + promoteFile + ": " + report.getReportDetails());
    }
    else
    {
      try
      {
        FileWriter outWriter = new FileWriter(dumpFileName + ".log", true);
        if ( outWriter != null )
        {
          outWriter.write("Promote report on " + promoteFile + ": " + report.getReportDetails());
          outWriter.flush();
          outWriter.close();
        }
      }
      catch(Exception e)
      {
        System.out.println("Fail to write report details to the dump file: " + e.getMessage());
      }
    }
  }
  private static void outputError(int errorCode, String errMsg, String promoteFile)
  {
    String msg = (String) errMsgTable.get(new Integer(errorCode));
    if ( errMsg != null && !errMsg.equals("") )
      msg += errMsg;

    if ( isDebug )
    {
      if ( errorCode == 0 )
      {
        System.out.println("SUCCESS: " + promoteFile + ", " + errorCode + " - " + msg);
      }
      else
      {
        System.out.println("ERROR: " + promoteFile + ", " + errorCode + " - " + msg);
      }
      System.exit(errorCode);
    }
    else
    {
      try
      {
        System.out.println("\nPlease look at the " + dumpFileName + ".log" + " for error message");
        FileWriter outWriter = new FileWriter(dumpFileName + ".log", true);
        if ( outWriter != null )
        {
          if ( errorCode == 0 )
          {
            outWriter.write("SUCCESS: " + promoteFile + ", " + errorCode + " - " + msg);
          }
          else
          {
            outWriter.write( "ERROR: " + promoteFile + ": " + errorCode + " - " + msg + "\n");
          }
          outWriter.flush();
          outWriter.close();
          System.exit(errorCode);
        }
      }
      catch(Exception e)
      {
        System.out.println("Fail to write to the dump file: " + e.getMessage());
        System.exit(18);
      }
    }
  }

}