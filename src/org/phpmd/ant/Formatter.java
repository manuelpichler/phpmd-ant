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
import org.apache.tools.ant.BuildException;

/**
 * Data container for a &lt;formatter&gt; elementsin an ant build file.
 *
 * @author    Manuel Pichler <mapi@phpmd.org>
 * @copyright 2009-2010 Manuel Pichler. All rights reserved.
 * @license   http://www.opensource.org/licenses/bsd-license.php BSD License
 * @version   SVN: $Id$
 * @link      http://phpmd.org
 */
public class Formatter {
    /**
     * The selected report format.
     */
    private String type = null;

    /**
     * Optional path for a report output file.
     */
    private String pathName = null;

    /**
     * Sets the configured report format.
     *
     * @param type The report format identifier.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the configured report format.
     *
     * @return String
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets an output file for the generated report.
     *
     * @param toFile The output file location.
     */
    public void setToFile(File toFile) {
        this.pathName = toFile.getAbsolutePath();
    }

    /**
     * Returns the path name of a report file or <b>null</b> when no output
     * file was specified.
     *
     * @return String
     */
    public String getPathName() {
        return this.pathName;
    }

    /**
     * Validates the current object state.
     */
    public void validate() {
        if (this.pathName == null || this.pathName.trim().length() == 0) {
            throw new BuildException("Attribute formatter@toFile must be defined.");
        }
        if (this.type == null || this.type.trim().length() == 0) {
            throw new BuildException("Attribute formatter@type must be defined.");
        }
    }
}
