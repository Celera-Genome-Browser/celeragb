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
package shared.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.Socket;


public class AccessionClient {
    public static final int UID_OK = 1;
    public static final int UID_FAILS = 0;
    public static final int UID_ENV_UNIX = 1;
    public static final int UID_ENV_PROP = 2;
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
    private static int UID_MESSAGE_SIZE = 36;
    private static int uid_environment_type = 0;

    // this is an XDR-compliant byte ordering
    private static long[] long_byte_pow = {
        0x0100000000000000L, 0x0001000000000000L, 0x0000010000000000L, 
        0x0000000100000000L, 0x0000000001000000L, 0x0000000000010000L, 
        0x0000000000000100L, 0x0000000000000001L
    };
    private String serverType;
    private String uid_host_name;
    private int uid_host_port;
    private int status = UID_CODE_START;
    private BigInteger[] interval_UID = new BigInteger[4];
    private byte[] message = new byte[UID_MESSAGE_SIZE];
    private BigInteger request_size_UID;
    private BigInteger increment_position_UID = BigInteger.valueOf(0);
    private BigInteger increment_max_UID = BigInteger.valueOf(0);
    private String resourceName;

    /** Do not use this constructor */
    private AccessionClient() {
    }

    public AccessionClient(String hostName, int port) {
        uid_host_name = hostName;
        uid_host_port = port;
        request_size_UID = BigInteger.valueOf(1);
        ClearUIDInterval();
    }

    public AccessionClient(String hostName, int port, BigInteger size) {
        uid_host_name = hostName;
        uid_host_port = port;
        request_size_UID = size;
        ClearUIDInterval();
    }

    public String getServerType() {
        return serverType;
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

    public BigInteger getUIDNumber() {
        BigInteger[] numbers = new BigInteger[4];

        try {
            GetNewUIDInterval(numbers);

            return numbers[0];
        } catch (Exception e) {
            System.out.println(serverType + ": " + e.getMessage());

            return null;
        }
    }

    public void GetNewUIDInterval(BigInteger[] interval)
                           throws Exception {
        // this is the one and only place where status is reset to OK
        // and the current interval is cleared
        status = UID_CODE_OK;
        ClearUIDInterval();

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

        for (int i = 7; i >= 0; i--) // go from least sig to most
        {
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
                clientSocket.close();
                TranslateUIDInterval(interval);
            }
        } catch (Exception e) {
            throw e;
        }
    }
}