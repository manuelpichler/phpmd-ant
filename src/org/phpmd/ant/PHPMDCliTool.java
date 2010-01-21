/**
 * This file is part of PHP_PMD.
 *
 * PHP Version 5
 *
 * Copyright (c) 2009-2010, Manuel Pichler <mapi@phpmd.org>.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   * Neither the name of Manuel Pichler nor the names of his
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * @author    Manuel Pichler <mapi@phpmd.org>
 * @copyright 2009-2010 Manuel Pichler. All rights reserved.
 * @license   http://www.opensource.org/licenses/bsd-license.php BSD License
 * @version   SVN: $Id$
 * @link      http://phpmd.org
 */

package org.phpmd.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class around the phpmd command line script.
 *
 * @author    Manuel Pichler <mapi@phpmd.org>
 * @copyright 2009-2010 Manuel Pichler. All rights reserved.
 * @license   http://www.opensource.org/licenses/bsd-license.php BSD License
 * @version   SVN: $Id$
 * @link      http://phpmd.org
 */
public class PHPMDCliTool {

    /**
     * Name of the PHP Mess Detector cli binary.
     */
    public static final String BINARY = "phpmd";

    /**
     * Option names for the phpmd command line script.
     */
    public static final String OPTION_REPORT_FILE  = "--reportfile",
                               OPTION_MIN_PRIORITY = "--minimumpriority";

    /**
     * Tries to find an installed phpmd binary.
     *
     * @return PHPMDCliTool
     */
    public static PHPMDCliTool find() {
        return find(BINARY);
    }

    /**
     * Tries to find an installed phpmd binary.
     *
     * @param fileName Custom file name or path for the phpmd script.
     *
     * @return PHPMDCliTool
     */
    public static PHPMDCliTool find(String fileName) {
        String pathName = findBinary(fileName);
        if (pathName == null) {
            throw new RuntimeException("Cannot locate phpmd binary.");
        }
        return new PHPMDCliTool(pathName);
    }

    /**
     * The absolute path to a phpmd binary.
     */
    private String pathName = null;

    /**
     * String with source file paths, separated by comma.
     */
    private String sources = null;

    /**
     * String with rule set identifiers or file names, separated by comma.
     */
    private String ruleSets = null;

    /**
     * The used report format.
     */
    private String reportFormat = null;

    /**
     * The output file for the generated report.
     */
    private String reportFile = null;

    /**
     * The priority thresholds for rules to use.
     */
    private int minimumPriority = -1;

    /**
     * Constructs a new cli tool instance.
     *
     * @param pathName Absolute path to an install phpmd command line script.
     */
    public PHPMDCliTool(String pathName) {
        this.pathName = pathName;
    }

    /**
     * Executes a phpmd process and returns the phpmd exit code.
     *
     * @return int
     * @throws Exception When something fails during the phpmd execution.
     */
    public int run() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(this.createCommand());
        
        Process p = pb.start();
        if (p.waitFor() == 0) {
            return 0;
        }
        throw new Exception(this.getErrorMessage(p));
    }

    /**
     * Create a list with strings that build the complete phpmd command.
     *
     * @return List<String>
     */
    private List<String> createCommand() {
        ArrayList<String> command = new ArrayList<String>();
        command.add(this.pathName);
        command.add(this.sources);
        command.add(this.reportFormat);
        command.add(this.ruleSets);

        if (this.reportFile != null) {
            command.add(OPTION_REPORT_FILE);
            command.add(this.reportFile);
        }
        if (this.minimumPriority > -1) {
            command.add(OPTION_MIN_PRIORITY);
            command.add(String.valueOf(this.minimumPriority));
        }

        return command;
    }

    /**
     * Read a error message from the given process instance.
     */
    private String getErrorMessage(Process p) throws Exception {
        if (p.getErrorStream().available() > 0) {
            byte[] error = new byte[p.getErrorStream().available()];
            p.getErrorStream().read(error);
            return new String(error);
        }
        return "Unknown error.";
    }

    /**
     * Sets a comma separated list of input source files.
     *
     * @param sources The source file list.
     */
    public void setSources(String sources) {
        this.sources = sources;
    }

    /**
     * Sets a comma separated list of rule sets to use.
     *
     * @param ruleSets The rule set list.
     */
    public void setRuleSets(String ruleSets) {
        this.ruleSets = ruleSets;
    }

    /**
     * Sets the report format to use.
     *
     * @param reportFormat The report format.
     */
    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    /**
     * Sets an output file for the generated violation report.
     *
     * @param reportFile The report file name.
     */
    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }

    /**
     * Sets the priority threshold. Rules with a lower priority value will not
     * be used.
     *
     * @param priority The minimum priority.
     */
    public void setMinimumPriority(int priority) {
        this.minimumPriority = priority;
    }

    /**
     * Tries to find an installed phpmd command line script.
     *
     * @param fileName File name of the phpmd cli tool.
     *
     * @return String
     */
    private static String findBinary(String fileName) {

        for (String directory : getPaths()) {
            String path = directory + File.separator + fileName;
            if (checkFile(path + ".bat")) {
                return path + ".bat";
            } else if (checkFile(path + ".php")) {
                return path + ".php";
            } else if (checkFile(path)) {
                return path;
            }
        }
        return null;
    }

    /**
     * Returns an array with all configured paths for system executables.
     *
     * @return String[]
     */
    private static String[] getPaths() {
        String directories = System.getenv("PATH");
        if (directories == null || directories.trim().equals("")) {
            return new String[0];
        }
        return directories.split(File.pathSeparator);
    }

    /**
     * Checks that a file for the given path name exists and is executable.
     *
     * @param pathName Possible phpmd command line script path name.
     *
     * @return boolean
     */
    private static boolean checkFile(String pathName) {
        File f = new File(pathName);
        if (f.exists() && checkExec(f.getAbsolutePath())) {
            return true;
        }
        return false;
    }

    /**
     * Checks that the current security policy allows the execution of the
     * given file.
     *
     * @param pathName Absolute path of a found phpmd binary.
     *
     * @return boolean
     */
    private static boolean checkExec(String pathName) {
        SecurityManager sm = System.getSecurityManager();

        if (sm == null) {
            return true;
        }
        try {
            sm.checkExec(pathName);
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }
}
