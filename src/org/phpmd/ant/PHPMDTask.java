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
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.phpmd.java.Phpmd;
import org.phpmd.java.Priority;

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

    private static final Integer EXIT_SUCCESS   = Phpmd.EXIT_CODE_SUCCESS,
                                 EXIT_VIOLATION = Phpmd.EXIT_CODE_VIOLATION;

    /**
     * Should this task fail when an error occures?
     */
    private boolean failOnError = false;

    /**
     * Should this task fail when a rule violation occures?
     */
    private boolean failOnRuleViolation = false;

    /**
     * The priority thresholds for rules to use.
     */
    private int minimumPriority = Priority.DEFAULT_PRIORITY;

    /**
     * List of source files that will be analyzed by phpmd.
     */
    private FileSets fileSets = new FileSets();

    /**
     * Custom collection holding all specified rule sets.
     */
    private RuleSets ruleSets = new RuleSets();

    /**
     * Formatter or report output.
     */
    private Formatter formatter = new Formatter();

    @Override
    public void execute() {
        this.validate();

        Integer exitCode = EXIT_SUCCESS;
        try {
            exitCode = this.getPreparedExecutable().run(new File(this.formatter.getPathName()));
        } catch (Exception e) {
            this.log(e, Project.MSG_ERR);
            if (this.failOnError) {
                throw new BuildException(e.getMessage());
            }
        }

        if (exitCode.equals(EXIT_VIOLATION) && this.failOnRuleViolation) {
            throw new BuildException("Stopping build since PHPMD found rule violations in the code");
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
     * Enables the fail on rule violation behavior. This means that the
     * execute() method will throw an exception when one rule violation was
     * found in the analyzed source code.
     *
     * @param failOnError Boolean flag that indicates if the task should throw
     *        an exception when a rule violation occures.
     */
    public void setFailOnRuleViolation(boolean failOnRuleViolation) {
        this.failOnRuleViolation = failOnRuleViolation;
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
            this.ruleSets.add(new RuleSet(ruleSetFile));
        }
    }

    public void addFileSet(FileSet fileSet) {
        this.fileSets.add(fileSet);
    }

    public void addFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public Formatter createFormatter() {
        return new Formatter();
    }

    public void addRuleSet(RuleSet ruleSet) {
        this.ruleSets.add(ruleSet);
    }

    public RuleSet createRuleSet() {
        return new RuleSet();
    }

    private void validate() {
        this.formatter.validate();
        this.fileSets.validate();
        this.ruleSets.validate();
    }

    private Phpmd getPreparedExecutable() {
        return this.prepareExecutable(this.getExecutable());
    }

    private Phpmd prepareExecutable(Phpmd phpmd) {
        phpmd.setMinimumPriority(this.minimumPriority);
        phpmd.setReportFormat(this.formatter.getType());

        for (String source : this.fileSets.getAll()) {
            phpmd.addSource(source);
        }
        for (String ruleSet : this.ruleSets.getAll()) {
            phpmd.addRuleSet(ruleSet);
        }
        return phpmd;
    }

    private Phpmd getExecutable() {
        return new Phpmd();
    }

    abstract class AbstractSets<T> {
        
        private List<T> objects = new ArrayList<T>();

        public void add(T object) {
            this.objects.add(object);
        }

        public List<String> getAll() {
            List<String> all = new ArrayList<String>();
            for (T object : this.objects) {
                this.append(object, all);
            }
            return all;
        }

        public void validate() throws BuildException {
            if (this.objects.isEmpty()) {
                throw new BuildException(this.getValidationMessage());
            }
            for (T object : this.objects) {
                this.doValidate(object);
            }
        }

        protected void doValidate(T object) throws BuildException {
            // Nothing todo here
        }

        protected abstract void append(T object, List<String> list);
        protected abstract String getValidationMessage();
    }

    class RuleSets extends AbstractSets<RuleSet> {

        @Override
        protected void append(RuleSet ruleSet, List<String> list) {
            list.add(ruleSet.getText());
        }

        @Override
        protected void doValidate(RuleSet ruleSet) {
            ruleSet.validate();
        }

        @Override
        protected String getValidationMessage() {
            return "At least 1 rule set must be specified.";
        }
    }

    class FileSets extends AbstractSets<FileSet> {

        @Override
        protected void append(FileSet object, List<String> list) {
            DirectoryScanner ds = object.getDirectoryScanner();
            for (String f : ds.getIncludedFiles()) {
                list.add(ds.getBasedir() + "/" + f);
            }
        }

        @Override
        protected String getValidationMessage() {
            return "At least 1 FileSet must be specified.";
        }
    }
}
