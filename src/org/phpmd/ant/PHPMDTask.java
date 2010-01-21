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

import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Main class of the PHPMD ant addon.
 *
 * @author    Manuel Pichler <mapi@phpmd.org>
 * @copyright 2009-2010 Manuel Pichler. All rights reserved.
 * @license   http://www.opensource.org/licenses/bsd-license.php BSD License
 * @version   SVN: $Id$
 * @link      http://phpmd.org
 */
public class PHPMDTask extends Task {

    /**
     * Should this task fail when an error occures?
     */
    private boolean failOnError = false;

    /**
     * The priority thresholds for rules to use.
     */
    private int minimumPriority = -1;

    /**
     * List of source files that will be analyzed by phpmd.
     */
    private SourceFiles sourceFiles = new SourceFiles();

    private RuleSets ruleSets = new RuleSets();

    private Formatter formatter = new Formatter();

    @Override
    public void execute() {
        this.formatter.validate();
        this.ruleSets.validate();

        PHPMDCliTool bin = PHPMDCliTool.find();
        bin.setSources(this.sourceFiles.getFiles());
        bin.setRuleSets(this.ruleSets.getFiles());
        bin.setReportFormat(this.formatter.getType());
        bin.setReportFile(this.formatter.getPathName());
        bin.setMinimumPriority(this.minimumPriority);

        try {
            bin.run();
        } catch (Exception e) {
            this.log(e, Project.MSG_ERR);
            if (this.failOnError) {
                throw new BuildException(e.getMessage());
            }
        }
    }

    /**
     * Enables the fail on error behavior. This means that the execute() method
     * will throw an exception when something fails while executing phpmd.
     *
     * @param failOnError Boolean flag that indicates if the task should throw
     *        an exception when something goes wrong.
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
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
     * Sets the value of the @rulesetfiles attribute that contains a comma
     * separated list of rule set file names or identifiers of the build-in
     * rule sets.
     *
     * @param ruleSetFiles The rule set file string.
     */
    public void setRuleSetFiles(String ruleSetFiles) {
        for (String ruleSetFile : ruleSetFiles.split(",")) {
            this.ruleSets.addRuleSet(new RuleSet(ruleSetFile));
        }
    }

    public void addFileSet(FileSet fileSet) {
        this.sourceFiles.addFileSet(fileSet);
    }

    public void addFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public Formatter createFormatter() {
        return new Formatter();
    }

    public void addRuleSet(RuleSet ruleSet) {
        this.ruleSets.addRuleSet(ruleSet);
    }

    public RuleSet createRuleSet() {
        return new RuleSet();
    }

    class RuleSets {
        private List<RuleSet> ruleSets = new ArrayList<RuleSet>();

        public void addRuleSet(RuleSet ruleSet) {
            this.ruleSets.add(ruleSet);
        }

        public String getFiles() {
            String files = "";
            for (RuleSet rs : this.ruleSets) {
                files += "," + rs.getText();
            }
            return files.substring(1).trim();
        }

        public void validate() {
            if (this.ruleSets.size() > 0) {
                for (RuleSet rs : this.ruleSets) {
                    rs.validate();
                }
                return;
            }
            throw new BuildException("At least 1 rule set must be specified.");
        }
    }

    class SourceFiles {

        private List<FileSet> fileSets = new ArrayList<FileSet>();

        public void addFileSet(FileSet fileSet) {
            this.fileSets.add(fileSet);
        }

        public String getFiles() {
            if (this.concatFileNames().length() == 0) {
                return "";
            }
            return this.concatFileNames().substring(1);
        }

        private String concatFileNames() {
            String fileNames = "";
            for (FileSet fs : this.fileSets) {
                DirectoryScanner ds = fs.getDirectoryScanner();
                for (String f : ds.getIncludedFiles()) {
                    fileNames += "," + ds.getBasedir() + "/" + f;
                }
            }
            return fileNames;
        }
    }
}
