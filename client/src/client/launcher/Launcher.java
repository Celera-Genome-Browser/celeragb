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
package client.launcher;

import client.tools.FileUpdater;
import client.tools.ProgressDisplayer;
import client.tools.installer.FileScanner;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Properties;

public class Launcher {
	public static final String defaultPath = "C:/Genomics_Genomics/";
	private static final char fs = File.separatorChar;
	private Properties appProps = new Properties();

	public Launcher() {
	}

	/**
	 *
	 * This extracts the file from the resource files in the classpath.
	 * Any jar file is considered a resource file.
	 *
	 * @param path is the path of the target directory
	 * @param filename is name of the file to extract and write
	 */
	public void writeFile(String path, String filename) {
		writeFile(path, filename, null);
	}

	/**
	 *
	 * This extracts the file from the resource files in the classpath.
	 * Any jar file is considered a resource file.
	 *
	 * @param path path of the target directory
	 * @param filename name of the file to extract and write
	 * @param toFilename is the rename file optional string
	 */
	public void writeFile(String path, String filename, String toFilename) {
		System.out.println(
			"Extracting:  "
				+ path
				+ Launcher.fs
				+ filename
				+ ((toFilename != null)
					? (" to " + path + Launcher.fs + toFilename)
					: ""));

		try {
			InputStream is = null;
			URL url = Launcher.class.getResource("/" + filename);
			is = url.openStream();

			int BUFSIZ = 1024;
			byte[] buf = new byte[BUFSIZ];
			int n = 0;
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			try {
				while ((n = is.read(buf)) > 0) {
					out.write(buf, 0, n);
				}
			} catch (IOException e) {
				System.out.println("IOException 1");
				e.printStackTrace();
			} finally {
				try {
					//System.out.println("finally: Read in... " + out.size() + " bytes");
					is.close();
				} catch (IOException e) {
					System.out.println("IOException 2");
					e.printStackTrace();
				}
			}

			try {
				File file =
					new File(
						path
							+ Launcher.fs
							+ ((toFilename == null) ? filename : toFilename));

				//Create directories
				file.getParentFile().mkdirs();

				FileOutputStream fos = new FileOutputStream(file);
				fos.write(out.toByteArray());
				fos.flush();
				fos.close();
			} catch (Exception e) {
				System.out.println("IOException 3");
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * This loads the properties file parameter into a class variable Properties
	 * object:  appProps.
	 *
	 * @param an name of the property file without the '.properties' extension
	 * @return success is true failure is false
	 */
	public boolean loadProperties(String propertiesName) {
		boolean resp = false;

		try {
			File f =
				new File(
					System.getProperty("user.home")
						+ "/"
						+ propertiesName
						+ ".properties");

			if (f.exists()) {
				InputStream is = new FileInputStream(f);
				appProps.load(is);
				is.close();
				resp = true;
			}
		} catch (Exception e) {
			//file corrupt
			e.printStackTrace();
		}

		return resp;
	}

	public boolean isAppInstalled(String an) {
		boolean resp = false;

		if (loadProperties(an)) {
			//Application properties does exists...
			//Test for the application directory
			String pd = appProps.getProperty("programdir");

			if (pd != null) {
				File f = new File(pd);

				if (f.exists()) {
					resp = true;
				}
			}
		}

		return resp;
	}

	/**
	 *
	 * This checks whether the installation/launching resources are old. The
	 * version number is passed in as a parameter.
	 *
	 * @param ver Version number of the Installer
	 * @return If the current version installed is older than the new one response is true, otherwise false
	 */
	public boolean isOlderVersion(String ver) {
		boolean resp = true;

		if (appProps.getProperty("version") == null) {
			return false;
		}

		if (ver.compareTo(appProps.getProperty("version")) == 0) {
			resp = false;
		}

		System.out.println(
			"Is Older Version Installed?" + (resp ? "yes" : "no"));

		return resp;
	}

	/**
	 *
	 * This removes the sub-directory in the Genomics_Genomics main directory for the
	 * application.  It also deletes the link file associated with the application.
	 *
	 * @param programName is the sub-directory name to delete
	 * @param lnkFileName is the absolute path to the link file
	 *
	 */
	public void deleteApplication(String programName, String lnkFileName) {
		System.out.println("Deleting Link File... " + lnkFileName);
		System.out.println(
			"Deleting Application... " + defaultPath + programName);

		File lf = new File(lnkFileName);

		if (lf.exists()) {
			lf.delete();
		}

		//Just remove the whole directory
		File d = new File(defaultPath + programName);

		if (d.exists()) {
			d.delete();
		}
	}

	/**
	 *
	 * This is win32 specific code that launches the application link file
	 * which in turn launches an executable.
	 *
	 * @param pn the Program Name of the link file
	 *
	 */
	public void launchApp(String pn) {
		System.out.println("Launching... " + pn);

		String osName = System.getProperty("os.name");
		int os = 0;

		if (osName.startsWith("Windows")) {
			os = 10;

			if (osName.endsWith("XP")) {
				os = os + 6;
			} else if (osName.endsWith("2000")) {
				os = os + 5;
			} else if (osName.endsWith("NT")) {
				os = os + 4;
			} else if (osName.endsWith("ME")) {
				os = os + 3;
			} else if (osName.endsWith("98")) {
				os = os + 2;
			} else if (osName.endsWith("95")) {
				os = os + 1;
			}
		}

		if (osName.startsWith("Unix")) {
			os = 20;
		}

		try {
			switch (os) {
				case 16 :
				case 15 :
				case 14 :
				case 13 :
				case 12 :
				case 11 :
					Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + pn);

					break;

				default :
					System.out.println(
						"Unplanned for Operating System.  Please contact our "
							+ "technical support and ask them to add URL handling for "
							+ System.getProperty("os.name")
							+ " into this product");

					break;
			}
		} catch (Exception ex) {
			System.out.println("Exception in startup..." + pn);
			ex.printStackTrace();
		}
	}

	/**
	 *
	 * This mechanism tries to send the URL specified to the Application via port 30000.
	 * This is a way to send information from the browser (IE or Netscape) to the application.
	 *
	 * @param urlHost the host name including the protocol (http://, etc) and port
	 * @param params the url params being sent to the program (via port 30000) (includes 'URL=')
	 *
	 * @return Success or Failure on connecting and sending the params.
	 */
	public boolean sendUrlToBrowser(String urlHost, String params) {
		//default to 4 tries
		return sendUrlToBrowser(urlHost, params, 4);
	}

	public boolean sendUrlToBrowser(String urlHost, String params, int tries) {
		boolean resp = false;
		String urlStr = urlHost + params.substring(4);
		//Strip off the "URL=" from argument

		if (urlStr.length() > 0) {
			try {
				URL url = new URL(urlStr);
				int i = 0;

				while ((i < tries) || resp) {
					try {
						System.out.println("Connecting to URL: " + urlStr);

						URLConnection urlConn = url.openConnection();
						BufferedReader stream =
							new BufferedReader(
								new InputStreamReader(
									urlConn.getInputStream()));

						if (stream != null) {
							resp = true;
						}

						i++;
					} catch (IOException e) {
						//If server not accepting connections then retry
						try {
							Thread.sleep(750 * i);
						} catch (InterruptedException ie) {
						}
					}
				}
			} catch (MalformedURLException me) {
				System.out.println("URL=" + urlStr);
				me.printStackTrace();
			}

			System.out.println(
				"Was search passed to the standalone visualization application? "
					+ (resp ? "yes" : "no"));
		} else {
			System.out.println("No search string found in jnlp file");
		}

		return resp;
	}

	/**
	 *
	 * This checks whether the Application is running... accepting connections on 30000.
	 *
	 * @param host URL host including the protocol (http://... etc)
	 * @param port TCP/IP port number of the local application
	 *
	 */
	public boolean isBrowserRunning(String host, int port) {
		boolean resp = false;

		try {
			Socket s = new Socket(host, port);
			s.close();
			resp = true;
		} catch (IOException e) {
			//e.printStackTrace();
		}

		System.out.println(
			"Is genome browser running? " + (resp ? "yes" : "no"));

		return resp;
	}

	private void setupCustomCfgFile(
		String programdir,
		String config,
		String javaLocation,
		String updateScrUrl) {
		System.out.println(
			"setupCustomCfgFile "
				+ programdir
				+ ","
				+ config
				+ ","
				+ javaLocation);

		try {
			File f = new File(programdir + fs + "custom.cfg");

			//Make sure path exists
			f.getParentFile().mkdirs();

			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write(
				"java.location="
					+ javaLocation
					+ "\r\n"
					+ "client.config="
					+ config
					+ "\r\n"
					+ "updateScript="
					+ updateScrUrl
					+ "\r\n");
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is called by the Applet.  It is the only entry point to execute the launcher mechanism.
	 * The Launcher takes in a set of arguments which are:
	 *
	 * args[0] - arguments for searching (Form:  URL=...) if after '=' is blank no searching takes place
	 * args[1] - program name (no spaces)
	 * args[2] - version of program
	 * args[3] - link filename
	 * args[4-n] - files to install for program
	 *
	 **/
	public void launch(Hashtable args) throws InstallException {
		Launcher l = new Launcher();

		String url = (String) args.get("url");
		String program = (String) args.get("program");
		String originallink = (String) args.get("originallink");
		String newLinkName = (String) args.get("link");
		int fileCount = Integer.parseInt((String) args.get("fileCount"));

		String installdir = (String) args.get("installdir");
		String launchonly = (String) args.get("launch");
		String programexe = (String) args.get("programexe");
		String programico = (String) args.get("programico");
		String clientconfig = (String) args.get("clientconfig");
		String clientversion = (String) args.get("clientversion");
		String updateScript = (String) args.get("updatescript");

		String programdir = installdir + program;
		String javaLocation = null;
		String updateScriptUrl = (String) args.get("urlRoot") + updateScript;

		//Always install/extract the exe,cfg,etc. files into the install directory.
		//skip:  step 1.  Check if installer Application exists (Side affect is loading of properties)
		//step 2.  Install the new Application
		if (launchonly.equals("false")) {
			try {
				//Find the required jvm version (Look in C:, D:, E:)
				FileScanner fscanner = new FileScanner();
				fscanner.setCaseSensitive(false);
				fscanner.setStopOnFirstHit(true);

				//C:\Program Files\Java\j2re1.4.1_01\bin
				String[] jvm = { "j2re1.4.1*" };
				String[] dirs = new String[1];
				File[] ret = null;
				ProgressDisplayer progressMeter =
					(ProgressDisplayer) args.get("progressbar");
				progressMeter.setVisible(true);
				progressMeter.setLabel("Searching for JRE 1.4.x...");

				//First search C:
				dirs[0] =
					"/C:"
						+ Launcher.fs
						+ "Program Files"
						+ Launcher.fs
						+ "Java";
				ret = fscanner.fileSearch(dirs, jvm);

				if (ret.length == 0) {
					//Second search D: if nothing is found
					dirs[0] =
						"/D:"
							+ Launcher.fs
							+ "Program Files"
							+ Launcher.fs
							+ "Java";
					ret = fscanner.fileSearch(dirs, jvm);

					//Second search E: if nothing is found
					if (ret.length == 0) {
						dirs[0] =
							"/E:"
								+ Launcher.fs
								+ "Program Files"
								+ Launcher.fs
								+ "Java";
						ret = fscanner.fileSearch(dirs, jvm);
					}
				}

				//If not found in Program Files search the whole drive ... this may take a while
				if (ret.length == 0) {
					//Search their whole drive one at a time
					dirs[0] = "/C:/";
					progressMeter.setProgress(3, 100);
					progressMeter.setProgress(50, 100);
					progressMeter.setLabel("Searching c:\\ for JRE 1.4.x...");
					ret = fscanner.fileSearch(dirs, jvm);

					if (ret.length == 0) {
						dirs[0] = "/D:/";
						progressMeter.setProgress(3, 100);
						progressMeter.setProgress(50, 100);
						progressMeter.setLabel(
							"Searching d:\\ for JRE 1.4.x...");
						ret = fscanner.fileSearch(dirs, jvm);

						if (ret.length == 0) {
							dirs[0] = "/E:/";
							progressMeter.setProgress(3, 100);
							progressMeter.setProgress(50, 100);
							progressMeter.setLabel(
								"Searching e:\\ for JRE 1.4.x...");
							ret = fscanner.fileSearch(dirs, jvm);
						}
					}
				}

				if (ret.length == 0) {
					throw new InstallException(
						"Unable to locate Java JRE 1.4.x on your computer"
							+ "\n"
							+ "Please install this version found on the installation"
							+ "\n"
							+ "page of this application.");

					//failed to find jre on drive c:
				} else {
					progressMeter.setProgress(100, 100);
				}

				//Clear out the progress meter for file updates...
				progressMeter.setLabel("    ");
				progressMeter.setVisible(false);

				javaLocation =
					ret[0].getAbsolutePath() + fs + "bin" + fs + "java.exe";

				//Setup the custom file to allow C++ code to determine which java.exe to use and client config
				setupCustomCfgFile(
					programdir,
					clientconfig,
					javaLocation,
					updateScriptUrl);

				FileWriter fw =
					new FileWriter(
						System.getProperty("user.home")
							+ Launcher.fs
							+ program
							+ ".properties");
				fw.write("programdir=" + programdir + "\r\n");

				String userDir = System.getProperty("user.home");
				String allUsersDir =
					userDir.substring(0, userDir.lastIndexOf(Launcher.fs))
						+ Launcher.fs
						+ "All Users";

				String linkFileLocation = allUsersDir;

				//Write the lnk file to the all user's desktop
				l.writeFile(
					linkFileLocation + Launcher.fs + "Desktop",
					originallink,
					newLinkName);

				//Test to see if file was installed correctly (Could be a permission Problem)
				File lnkFile =
					new File(
						linkFileLocation
							+ Launcher.fs
							+ "Desktop"
							+ Launcher.fs
							+ newLinkName);

				if (!lnkFile.exists()) {
					l.writeFile(
						userDir + Launcher.fs + "Desktop",
						originallink,
						newLinkName);
					linkFileLocation = userDir;
				}

				l.writeFile(programdir, (String) args.get("programexe"));
				l.writeFile(programdir, (String) args.get("programcfg"));
				l.writeFile(programdir, (String) args.get("programico"));

				for (int i = 1; i <= fileCount; i++) {
					fw.write(
						"file"
							+ i
							+ "="
							+ (String) args.get("file" + i)
							+ "\r\n");
					l.writeFile(programdir, (String) args.get("file" + i));
				}

				fw.close();

				//Change the path in the lnk file
				String[] scArgs =
					{
						programdir + Launcher.fs + "shortcut.exe",
						"-f",
						"-c",
						"-n",
						linkFileLocation
							+ Launcher.fs
							+ "Desktop"
							+ Launcher.fs
							+ newLinkName,
						"-t",
						programdir + Launcher.fs + programexe,
						"-i",
						programdir + Launcher.fs + programico,
						"-d",
						programdir,
						"-a",
						clientversion };
				Process p = Runtime.getRuntime().exec(scArgs);
				p.waitFor();

				new FileUpdater(updateScriptUrl, progressMeter, programdir);
			} catch (Exception e) {
				e.printStackTrace();
				throw new InstallException(
					"Error while trying to Execute the application: "
						+ e.getMessage());
			}
		}

		//step 3.  If this is purely a fresh install don't try to navigate (ie INSTALL in the URL parameter)
		//step 3.1  Check if Genome Browser is running... then send url params to localhost...
		//step 3.2  Run the Application  (the need for a '\' is a bug in windows...)
		//        if (l.isBrowserRunning("localhost", 30000)) {
		//            l.sendUrlToBrowser("http://localhost:30000?", url, 10);
		//        } else {
		//Check if flag is set to launch
		if (launchonly.equals("true")) {
			String userDir = System.getProperty("user.home");
			String allUsersDir =
				userDir.substring(0, userDir.lastIndexOf(Launcher.fs))
					+ Launcher.fs
					+ "All Users";
			String linkFileLocation = allUsersDir;

			File lnkFile =
				new File(
					linkFileLocation
						+ Launcher.fs
						+ "Desktop"
						+ Launcher.fs
						+ newLinkName);

			if (!lnkFile.exists()) {
				linkFileLocation = userDir;
			}

			l.launchApp(
				linkFileLocation
					+ Launcher.fs
					+ "Desktop"
					+ Launcher.fs
					+ newLinkName);

			//        l.launchApp(System.getProperty("user.home")+Launcher.fs+"Desktop"+Launcher.fs+link);
			if ((url != null) && !url.equalsIgnoreCase("URL=INSTALL")) {
				//try to send the parameters to the genome browser
				l.sendUrlToBrowser("http://localhost:30000?", url, 20);
			}
		}
		//        }
	}
}