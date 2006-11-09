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
package api.entity_model.access.comparator;

import java.util.Comparator;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies
 * @version $Id$
 *
 * Intended to be used by sub-classes to sort returned value lists that contain toStrings
 *  that are StringNumber, such as Contig 2 vs Contig 10.
 */

public class StringNumberComparator implements Comparator {

       public int compare(Object o1, Object o2) {
         String str1=o1.toString();
         String str2=o2.toString();
         int lastChar1=str1.length();
         int lastChar2=str2.length();
         for (int i=0;i<str1.length();i++) {
           if (Character.isDigit(str1.charAt(i))) {
             lastChar1=i-1;
             break;
           }
         }
         for (int i=0;i<str2.length();i++) {
           if (Character.isDigit(str2.charAt(i))) {
             lastChar2=i-1;
             break;
           }
         }
         int rtn=str1.substring(0,lastChar1).compareTo(str2.substring(0,lastChar2));
         if (rtn!=0) return rtn;

         String num1=str1.substring(lastChar1+1,str1.length());
         String num2=str2.substring(lastChar2+1,str2.length());

         long number1=0;
         long number2=0;

         try {
           number2=Long.parseLong(num2);
           number1=Long.parseLong(num1);
         }
         catch (Exception ex) {
           System.out.println("Exception in StringNumberComparator");
           return 0;
         }

         if (number1>number2) return 1;
         if (number2>number1) return -1;
         return 0;



//         if (str1.length()==str2.length()) return str1.compareTo(str2);
//         if (!Character.isDigit(str1.charAt(str1.length()-1)) || !Character.isDigit(str2.charAt(str2.length()-1)))
//            return str1.compareTo(str2);
//         long number1=0;
//         long number2=0;
//         for (int i=str1.length()-1;i>=0;i--) {
//             if (!Character.isDigit(str1.charAt(i))) {
//                 String number=str1.substring(i+1);
//                 number1=Long.parseLong(number);
//                 break;
//             }
//         }
//         for (int i=str2.length()-1;i>=0;i--) {
//             if (!Character.isDigit(str2.charAt(i))) {
//                 String number=str2.substring(i+1);
//                 number2=Long.parseLong(number);
//                 break;
//             }
//         }
//         if (number1>number2) return 1;
//         if (number2>number1) return -1;
//         return 0;
       }

}