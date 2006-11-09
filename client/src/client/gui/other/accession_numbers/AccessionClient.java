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
/**********************************************************************
**********************************************************************/
/**********************************************************************
$Source$cm/cvs/enterprise/src/client/gui/other/accession_numbers/AccessionClient.java,v $
$Revision$
$Date$
$Author$
$Log$
Revision 1.4  2003/03/05 19:03:54  grahamkj
No changes for now just a test GB-123456789

Revision 1.3  2002/11/06 16:41:37  lblick
Fixed problems with unused variables and obsolete imports.

Revision 1.2  2000/03/31 16:05:22  dwu
Accession Server GUI Client is completed with instructions panel.

Revision 1.1  2000/03/29 21:03:37  dwu
New GUI interface for easy testing of Accession number servers.

Revision 1.1  1999/10/13 18:12:46  WuDC
Replaced SYS_UIDclient file with AccessionClient class to access Accession numbers UID servers

Revision 1.1  1999/08/18 17:13:32  BaxendJn
Code for retrieving a new UID from a known UID server.


**********************************************************************/
package client.gui.other.accession_numbers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.reflect.Array;

import java.math.BigInteger;

import java.net.Socket;
import java.net.UnknownHostException;

import java.util.Properties;


public class AccessionClient {
    public static final int UID_OK = 1;
    public static final int UID_FAILS = 0;
    public static final int UID_ENV_UNIX = 1;
    public static final int UID_ENV_PROP = 2;

    //option flags to connect CG server or CT server
    public static final int CG_ACCESSION = 1;
    public static final int CT_ACCESSION = 2;
    private static int UID_CODE_OK = 101;
    private static int UID_CODE_UNKNOWN_ERROR = 102;
    private static int UID_CODE_RAN_OUT_OF_SPACE = 103;
    private static int UID_CODE_POS_BOUNDS_ERROR = 104;
    private static int UID_CODE_POS_CONFIG_ERROR = 105;
    private static int UID_CODE_BLOCK_TOO_LARGE = 106;
    private static int UID_CODE_REQUEST_ERROR = 107;
    private static int UID_CODE_NEED_SIZE_INFO = 108;

    // server side - never seen by client
    private static int UID_CODE_REGISTER_CONN_FAILED = 201;
    private static int UID_CODE_ACTIVATE_CONN_FAILED = 202;
    private static int UID_CODE_ACCEPT_CONN_FAILED = 203;
    private static int UID_CODE_SEND_ERROR = 204;
    private static int UID_CODE_SERVER_KILL = 205;

    // client side only
    private static int UID_CODE_NULL_TRANSMISSION = 0;
    private static int UID_CODE_START = 301;
    private static int UID_CODE_CREATE_CONN_FAILED = 302;
    private static int UID_CODE_CONFIGURE_CONN_FAILED = 303;
    private static int UID_CODE_CANT_CONNECT = 304;
    private static int UID_CODE_CANT_READ = 305;
    private static int UID_CODE_NULL_INTERVAL_PTR = 306;
    private static int UID_CODE_INCREMENT_OVERFLOW = 307;
    private static String default_uid_host_name = new String("dsc101a");
    private static String uid_host_name = null;
    private static int default_uid_host_port = 5007;
    private static int uid_host_port = 5007;
    private static String default_failsafe_uid_host_name = 
            new String("dsc101a");
    private static String failsafe_uid_host_name = null;
    private static int default_failsafe_uid_host_port = 5007;
    private static int failsafe_uid_host_port = 5007;
    private static int UID_MESSAGE_SIZE = 36;
    private static int uid_environment_type = 0;
    private static String uid_properties_filename = new String("SYS_UID.properties");

    // this is an XDR-compliant byte ordering
    private static long[] long_byte_pow = {
        0x0100000000000000L, 0x0001000000000000L, 0x0000010000000000L, 
        0x0000000100000000L, 0x0000000001000000L, 0x0000000000010000L, 
        0x0000000000000100L, 0x0000000000000001L
    };
    private String serverType;
    private int status = UID_CODE_START;
    private BigInteger[] interval_UID = new BigInteger[4];
    private byte[] message = new byte[UID_MESSAGE_SIZE];
    private BigInteger request_size_UID;
    private BigInteger increment_position_UID;
    private BigInteger increment_max_UID;
    private String resourceName;

    public AccessionClient() {
        uid_environment_type = UID_ENV_PROP;
        request_size_UID = BigInteger.valueOf(1);
        Initialize();
    }

    public AccessionClient(int type) {
        request_size_UID = BigInteger.valueOf(1);
        setMode(type);
        Initialize();
    }

    public AccessionClient(int type, BigInteger size) {
        request_size_UID = size;
        setMode(type);
        Initialize();
    }

    public AccessionClient(int type, String hn, int p) {
        request_size_UID = BigInteger.valueOf(1);
        setMode(type);
        status = UID_CODE_START;
        increment_position_UID = BigInteger.valueOf(0);
        increment_max_UID = BigInteger.valueOf(0);

        ClearUIDInterval();
        uid_host_name = hn;
        uid_host_port = p;
    }

    private void setMode(int type) {
        if (type == CG_ACCESSION) {
            serverType = "Internal Gene Accession Server";
            resourceName = "/resource/geneInfo.properties";
        } else {
            serverType = "Internal Transcript Accession Server";
            resourceName = "/resource/transcriptInfo.properties";
        }
    }

    public String getServerType() {
        return serverType;
    }

    private void Initialize() {
        status = UID_CODE_START;
        increment_position_UID = BigInteger.valueOf(0);
        increment_max_UID = BigInteger.valueOf(0);

        ClearUIDInterval();


        // read host/port configuration information - assumes the
        // uid_environment_type is set
        // REVISIT: No longer needed done inside the static initializer
        //GetEnvironmentInfo();
        uid_host_name = "dsc101a";
        uid_host_port = 5007;
    }

    private String GetUnixCmd(String cmd) {
        String result_string = null;
        char[] result_array;

        result_array = new char[300];

        try {
            Process child = Runtime.getRuntime().exec(cmd);
            InputStream in = child.getInputStream();
            int c;
            int count = 0;

            while ((c = in.read()) != -1) {
                result_array[count] = (char) c;
                count++;

                if (count >= 299) {
                    break;
                }
            }

            in.close();
            child.waitFor();

            result_string = new String(result_array, 0, count);
        } catch (Exception e) {
            result_string = null;
        }

        return result_string;
    }

    private String GetFirstNumber(String line) {
        int length = line.length();
        int start = 0;
        String result = null;
        int i = 0;

        // find start
        for (i = 0; i < length; i++) {
            //if (IsNumber(line.charAt(i)) == 1)
            if (Character.isDigit(line.charAt(i))) {
                break;
            }
        }

        if (i == length) {
            return result; // null at this stage
        }

        start = i;

        // find first non-number character
        for (; i < length; i++) {
            //if (IsNumber(line.charAt(i)) == 0)
            if (!Character.isDigit(line.charAt(i))) {
                break;
            }
        }

        result = new String("");
        result = line.substring(start, i);

        return result;
    }

    private String GetFirstAlphaNumeric(String line) {
        int length = line.length();
        int start = 0;
        String result = null;
        int i = 0;

        // find start
        for (i = 0; i < length; i++)
            if (IsAlphaNumeric(line.charAt(i)) == 1) {
                break;
            }

        if (i == length) {
            return result; // null at this stage
        }

        start = i;

        // find first non-number character
        for (; i < length; i++)
            if (IsAlphaNumeric(line.charAt(i)) == 0) {
                break;
            }

        result = new String("");
        result = line.substring(start, i);

        return result;
    }

    /* Should be replaced using Character.isDigit ()
    private int IsNumber(char c)
    {
      char num_arr[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
      int i = 0;
    
      for (i = 0; i< 10; i++)
         if (c == num_arr[i])
            break;
    
      if (i == 10)
         return 0;
    
      return 1;
    }
    */
    private int IsAlphaNumeric(char c) {
        String an_arr = new String("0123456789-.!@@#$%^&*()_+:/,<>;abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        int an_length = an_arr.length();
        int i = 0;

        for (i = 0; i < an_length; i++)
            if (c == an_arr.charAt(i)) {
                break;
            }

        if (i == an_length) {
            return 0;
        }

        return 1;
    }

    private void GetEnvironmentInfo() {
        String port_string = new String("");

        Properties props = new Properties();

        try {
            InputStream fileIn = this.getClass()
                                     .getResourceAsStream(resourceName);
            props.load(fileIn);
            fileIn.close();
        } catch (Exception e) {
            SYS_UIDerrorMessages.ReportError("Problem loading properties file");
            System.out.println(e.getMessage());

            /*
            uid_host_name = default_uid_host_name;
            uid_host_port = default_uid_host_port;
            failsafe_uid_host_name = default_failsafe_uid_host_name;
            failsafe_uid_host_port = default_failsafe_uid_host_port;
            */
            return;
        }

        System.setProperties(props);


        // attempt to access vars
        uid_host_name = props.getProperty("host_name");
        port_string = props.getProperty("host_port");

        //failsafe_uid_host_name = props.getProperty("failsafe_host_name");
        //failsafe_port_string   = props.getProperty("failsafe_host_port");
        // perform type conversions
        if (port_string != null) {
            uid_host_port = Integer.parseInt(port_string);
        }

        //if (failsafe_port_string != null)
        //failsafe_uid_host_port = Integer.parseInt(failsafe_port_string);
        // perform default checks
        if (uid_host_name == null) {
            uid_host_name = default_uid_host_name;
        }

        if (uid_host_port == 0) {
            uid_host_port = default_uid_host_port;
        }

        //if (failsafe_uid_host_name == null)
        //failsafe_uid_host_name = default_failsafe_uid_host_name;
        //if (failsafe_uid_host_port == 0)
        //failsafe_uid_host_port = default_failsafe_uid_host_port;

        /*
        if (uid_environment_type == UID_ENV_PROP)
        {
        //Properties props = new Properties(System.getProperties());
        try
        {
        props.load(new BufferedInputStream(new
        FileInputStream(uid_properties_filename)));
        }
        catch (Exception e)
        {
        SYS_UIDerrorMessages.ReportError("Problem loading properties file");
        uid_host_name = default_uid_host_name;
        uid_host_port = default_uid_host_port;
        failsafe_uid_host_name = default_failsafe_uid_host_name;
        failsafe_uid_host_port = default_failsafe_uid_host_port;
        return;
        }
        System.setProperties(props);
        
        // attempt to access vars
        uid_host_name = props.getProperty("SYS_UIDclient.uid_host_name");
        port_string   = props.getProperty("SYS_UIDclient.uid_host_port");
        failsafe_uid_host_name = props.getProperty("SYS_UIDclient.failsafe_uid_host_name");
        failsafe_port_string   = props.getProperty("SYS_UIDclient.failsafe_uid_host_port");
        
        } else
        if (uid_environment_type == UID_ENV_UNIX)
        {
        String cmd;
        
        cmd = "printenv SYS_UID_SERVER_PORT";
        port_string = GetFirstNumber(GetUnixCmd(cmd));
        cmd = "printenv SYS_UID_SERVER_HOST_NAME";
        uid_host_name = GetFirstAlphaNumeric(GetUnixCmd(cmd));
        cmd = "printenv SYS_UID_FAILSAFE_SERVER_PORT";
        failsafe_port_string = GetFirstNumber(GetUnixCmd(cmd));
        cmd = "printenv SYS_UID_FAILSAFE_SERVER_HOST_NAME";
        failsafe_uid_host_name = GetFirstAlphaNumeric(GetUnixCmd(cmd));
        }
        
        // perform type conversions
        if (port_string != null)
        uid_host_port = Integer.parseInt(port_string);
        if (failsafe_port_string != null)
        failsafe_uid_host_port = Integer.parseInt(failsafe_port_string);
        
        // perform default checks
        if (uid_host_name == null)
        uid_host_name = default_uid_host_name;
        if (uid_host_port == 0)
        uid_host_port = default_uid_host_port;
        if (failsafe_uid_host_name == null)
        failsafe_uid_host_name = default_failsafe_uid_host_name;
        if (failsafe_uid_host_port == 0)
        failsafe_uid_host_port = default_failsafe_uid_host_port;
        */
    }

    public void SetUIDSize(BigInteger size) {
        request_size_UID = size;
    }

    public BigInteger GetMaxUIDSize() throws Exception {
        BigInteger[] query_result = new BigInteger[4];

        try {
            QueryServer(UID_CODE_NEED_SIZE_INFO, query_result);
        } catch (Exception e) {
            throw e;
        }

        return query_result[0];
    }

    public BigInteger getUIDNumber() throws Exception {
        BigInteger[] numbers = new BigInteger[4];

        try {
            GetNewUIDInterval(numbers);

            return numbers[0];
        } catch (Exception e) {
            System.out.println(serverType + ": " + e.getMessage());
            throw e;

            //return null;
        }
    }

    public void GetNewUIDInterval(BigInteger[] interval)
                           throws Exception {
        // this is the one and only place where status is reset to OK
        // and the current interval is cleared
        status = UID_CODE_OK;
        ClearUIDInterval();

        //if (Array.getLength(interval) ) {
        if (interval.length != 4) {
            status = UID_CODE_NULL_INTERVAL_PTR;
            throw new Exception("Length of UID interval array is not 4");
        }

        try {
            QueryServer(UID_CODE_OK, interval_UID);
        } catch (Exception e) {
            interval[0] = BigInteger.valueOf(0);
            interval[1] = BigInteger.valueOf(0);
            interval[2] = BigInteger.valueOf(0);
            interval[3] = BigInteger.valueOf(0);
            throw e;
        }

        increment_position_UID = BigInteger.valueOf(0);
        increment_max_UID = interval_UID[1].add(interval_UID[3]);

        interval[0] = interval_UID[0];
        interval[1] = interval_UID[1];
        interval[2] = interval_UID[2];
        interval[3] = interval_UID[3];
    }

    public void GetLastUIDInterval(BigInteger[] interval)
                            throws Exception {
        if (Array.getLength(interval) != 4) {
            status = UID_CODE_NULL_INTERVAL_PTR;
            throw new Exception("Array length of argument is not 4");
        }

        if (status != UID_CODE_OK) {
            throw new Exception("status not OK");
        }

        interval[0] = interval_UID[0];
        interval[1] = interval_UID[1];
        interval[2] = interval_UID[2];
        interval[3] = interval_UID[3];
    }

    private void TranslateUIDInterval(BigInteger[] interval) {
        byte[] ulongbytes = { 0, 0, 0, 0, 0, 0, 0, 0 };
        int i = 0;

        // first interval start
        for (i = 0; i < 8; i++)
            ulongbytes[i] = message[i];

        interval[0] = UlongXdrBytesToBigInteger(ulongbytes);

        // first interval size
        for (i = 8; i < 16; i++)
            ulongbytes[i - 8] = message[i];

        interval[1] = UlongXdrBytesToBigInteger(ulongbytes);

        // second interval start
        for (i = 16; i < 24; i++)
            ulongbytes[i - 16] = message[i];

        interval[2] = UlongXdrBytesToBigInteger(ulongbytes);

        // second interval size
        for (i = 24; i < 32; i++)
            ulongbytes[i - 24] = message[i];

        interval[3] = UlongXdrBytesToBigInteger(ulongbytes);
    }

    private void TranslateUIDStatus() {
        byte[] intbytes = { 0, 0, 0, 0 };

        for (int i = 32; i < 36; i++)
            intbytes[i - 32] = message[i];

        status = IntXdrBytesToInt(intbytes);
    }

    private void ClearUIDInterval() {
        BigInteger Zero = BigInteger.valueOf(0);

        interval_UID[0] = Zero;
        interval_UID[1] = Zero;
        interval_UID[2] = Zero;
        interval_UID[3] = Zero;

        increment_position_UID = BigInteger.valueOf(0);
        increment_max_UID = BigInteger.valueOf(0);
    }

    private int IntXdrBytesToInt(byte[] intval) {
        int result = 0;

        // array bounds check
        if (Array.getLength(intval) != 4) {
            return 0;
        }

        result = (((int) (intval[0]) & 0xff) << 24) | 
                 (((int) (intval[1]) & 0xff) << 16) | 
                 (((int) (intval[2]) & 0xff) << 8) | 
                 ((int) (intval[3]) & 0xff);

        return result;
    }

    private int IntegerToXdrIntBytes(int value, byte[] intbytes) {
        // size check
        if (Array.getLength(intbytes) != 4) {
            return UID_FAILS;
        }

        intbytes[0] = (byte) ((value >> 24) & 0xff);
        intbytes[1] = (byte) ((value >> 16) & 0xff);
        intbytes[2] = (byte) ((value >> 8) & 0xff);
        intbytes[3] = (byte) (value & 0xff);

        return UID_OK;
    }

    private BigInteger UlongXdrBytesToBigInteger(byte[] ulongval) {
        long lpmax = 0x7fffffffffffffffL;
        BigInteger result = BigInteger.valueOf(0);

        // size check
        if (Array.getLength(ulongval) != 8) {
            return result; // zero at this stage
        }

        for (int i = 0; i < 8; i++) {
            // deal with special two-step case first for largest case
            if ((ulongval[i] < 0) && (i == 0)) {
                // add the first half of the long-space
                result = result.add(BigInteger.valueOf(lpmax));


                // add the trimmed second half
                result = result.add(BigInteger.valueOf(
                                            long_byte_pow[i] * (128L + ulongval[i])));
            } else if (ulongval[i] < 0) {
                result = result.add(BigInteger.valueOf(
                                            long_byte_pow[i] * (256L + ulongval[i])));
            } else {
                result = result.add(BigInteger.valueOf(
                                            long_byte_pow[i] * ulongval[i]));
            }
        }

        return result;
    }

    private int BigIntegerToXdrUlongBytes(BigInteger Bi, byte[] ulbytes) {
        // size check
        if (Array.getLength(ulbytes) != 8) {
            return UID_FAILS;
        }

        // test Bi size boundaries first
        BigInteger ulmax = new BigInteger("ffffffffffffffff", 16);
        BigInteger neg1 = new BigInteger("-1");

        if ((ulmax.compareTo(Bi) < 0) || (Bi.compareTo(neg1) != 1)) {
            return UID_FAILS;
        }

        // convert - implements XDR byte order
        byte[] ulstrip = Bi.toByteArray();

        int ulsize = Array.getLength(ulstrip);

        for (int i = 7; i >= 0; i--) { // go from least sig to most

            if ((7 - i) < ulsize) // if inside valid ulstrip bytes

            {
                ulbytes[i] = ulstrip[i - (8 - ulsize)];
            } else
            {
                ulbytes[i] = 0;
            }
        }

        return UID_OK;
    }

    public BigInteger GetNextUID() throws Exception {
        BigInteger uid = null;

        // check limit
        if (increment_position_UID.compareTo(increment_max_UID) > -1) {
            throw new Exception("UID increment out of range: " + 
                                increment_position_UID + " GT " + 
                                increment_max_UID);
        }

        // check which interval
        if (increment_position_UID.compareTo(interval_UID[1]) < 0) {
            uid = new BigInteger((increment_position_UID.add(interval_UID[0]))
                                     .toString());
        } else {
            uid = new BigInteger((interval_UID[2].add(
                                         increment_position_UID.subtract(
                                                 interval_UID[1]))).toString());
        }


        // increment for next call
        increment_position_UID = increment_position_UID.add(BigInteger.valueOf(1));

        // check for status exception after increment
        if (status != UID_CODE_OK) {
            throw new Exception("status code not OK");
        }

        return uid;
    }

    public BigInteger GetLastUID() throws Exception {
        BigInteger uid = null;

        // check for status exception
        if (status != UID_CODE_OK) {
            throw new Exception("status code not OK");
        }

        // check to see if incrementer already called - thereby uses incrementer as
        // an implicit bounds check
        if (increment_position_UID.compareTo(BigInteger.valueOf(0)) < 1) {
            throw new Exception("UID increment not initialized");
        }

        // check which interval
        if (increment_position_UID.compareTo(interval_UID[1]) < 0) {
            uid = new BigInteger((increment_position_UID.add(interval_UID[0]))
                                     .toString());
        } else {
            uid = new BigInteger((interval_UID[2].add(
                                         increment_position_UID.subtract(
                                                 interval_UID[1]))).toString());
        }

        return uid;
    }

    public int GetUIDStatus() {
        return status;
    }

    private void QueryServer(int code, BigInteger[] interval)
                      throws Exception {
        byte[] status_bytes = new byte[4];
        byte[] size_bytes = new byte[8];
        byte[] request_bytes = new byte[12];

        try {
            Socket clientSocket = new Socket(uid_host_name, uid_host_port);
            BufferedInputStream inbound = new BufferedInputStream(
                                                  clientSocket.getInputStream());
            OutputStream outbound = clientSocket.getOutputStream();

            if (((BigIntegerToXdrUlongBytes(request_size_UID, size_bytes)) == UID_OK) && 
                    ((IntegerToXdrIntBytes(code, status_bytes)) == UID_OK)) {
                for (int i = 0; i < 12; i++) {
                    if (i < 4) {
                        request_bytes[i] = status_bytes[i];
                    } else {
                        request_bytes[i] = size_bytes[i - 4];
                    }
                }

                outbound.write(request_bytes, 0, 12);
                inbound.read(message, 0, UID_MESSAGE_SIZE);
                System.out.println("request_bytes: " + request_bytes);
                System.out.println("message: " + message);
                clientSocket.close();
                TranslateUIDInterval(interval);
            }
        } catch (Exception e) {
            throw e;

            //failsafe_flag = 1;
        }
    }

    private void QueryFailsafeServer(int code, BigInteger[] interval)
                              throws Exception {
        int read_count = 0;
        int ul_flag = 0;
        int in_flag = 0;
        byte[] status_bytes = new byte[4];
        byte[] size_bytes = new byte[8];
        byte[] request_bytes = new byte[12];

        try {
            Socket clientSocket = new Socket(failsafe_uid_host_name, 
                                             failsafe_uid_host_port);
            BufferedInputStream inbound = new BufferedInputStream(
                                                  clientSocket.getInputStream());
            OutputStream outbound = clientSocket.getOutputStream();

            if (((ul_flag = BigIntegerToXdrUlongBytes(request_size_UID, 
                                                      size_bytes)) == UID_OK) && 
                    ((in_flag = IntegerToXdrIntBytes(code, status_bytes)) == UID_OK)) {
                for (int i = 0; i < 12; i++) {
                    if (i < 4) {
                        request_bytes[i] = status_bytes[i];
                    } else {
                        request_bytes[i] = size_bytes[i - 4];
                    }
                }

                outbound.write(request_bytes, 0, 12);
                read_count = inbound.read(message, 0, UID_MESSAGE_SIZE);
                clientSocket.close();
            }
        } catch (UnknownHostException e) {
            SYS_UIDerrorMessages.ReportError(
                    "UidClient: could not create server connection");
            interval[0] = BigInteger.valueOf(0);
            interval[1] = BigInteger.valueOf(0);
            interval[2] = BigInteger.valueOf(0);
            interval[3] = BigInteger.valueOf(0);
            status = UID_CODE_CREATE_CONN_FAILED;
            throw new Exception(
                    "Could not create connection to failsafe server");
        } catch (IOException e) {
            SYS_UIDerrorMessages.ReportError("UidClient: IO exception");
            interval[0] = BigInteger.valueOf(0);
            interval[1] = BigInteger.valueOf(0);
            interval[2] = BigInteger.valueOf(0);
            interval[3] = BigInteger.valueOf(0);
            status = UID_CODE_CANT_CONNECT;
            throw new Exception(
                    "Could not connect to failsafe server: IO exception");
        }

        if (read_count != UID_MESSAGE_SIZE) {
            String rc_err_msg = new String("UidClient: read count is " + 
                                           read_count + " instead of " + 
                                           UID_MESSAGE_SIZE);
            SYS_UIDerrorMessages.ReportError(rc_err_msg);
            interval[0] = BigInteger.valueOf(0);
            interval[1] = BigInteger.valueOf(0);
            interval[2] = BigInteger.valueOf(0);
            interval[3] = BigInteger.valueOf(0);
            status = UID_CODE_CANT_READ;
            throw new Exception(
                    "Read count incorrect in message from failsafe server");
        }

        if ((ul_flag != UID_OK) || (in_flag != UID_OK)) {
            String rc_err_msg = new String("Blocksize too large or status invalid");
            SYS_UIDerrorMessages.ReportError(rc_err_msg);
            interval[0] = BigInteger.valueOf(0);
            interval[1] = BigInteger.valueOf(0);
            interval[2] = BigInteger.valueOf(0);
            interval[3] = BigInteger.valueOf(0);
            status = UID_CODE_CONFIGURE_CONN_FAILED;
            throw new Exception(
                    "Blocksize is too large or status invalid for failsafe server");
        }

        TranslateUIDStatus();
        TranslateUIDInterval(interval);

        if (status != UID_CODE_OK) {
            throw new Exception("QueryFailsafeServer failed...");
        }
    }

    public static void main(String[] args) {
        AccessionClient clientCG = new AccessionClient(CG_ACCESSION);
        BigInteger result = null;

        try {
            result = clientCG.getUIDNumber();

            if (result != null) {
                String str = new String("CG" + result.toString());
                System.out.println(clientCG.getServerType() + ": " + str);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            AccessionClient clientCT = new AccessionClient(CT_ACCESSION);
            result = clientCT.getUIDNumber();

            if (result != null) {
                String str = new String("CT" + result.toString());
                System.out.println(clientCT.getServerType() + ": " + str);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}