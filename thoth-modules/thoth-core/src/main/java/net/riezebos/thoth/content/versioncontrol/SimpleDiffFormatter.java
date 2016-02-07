/* Copyright (c) 2016 W.T.J. Riezebos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.riezebos.thoth.content.versioncontrol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;

public class SimpleDiffFormatter extends DiffFormatter {

  public SimpleDiffFormatter() {
    super(new ByteArrayOutputStream());
  }

  @Override
  public ByteArrayOutputStream getOutputStream() {
    return (ByteArrayOutputStream) super.getOutputStream();
  }

  public void reset() {
    getOutputStream().reset();
  }

  public String getFormattedDiff(DiffEntry ent) throws IOException {
    reset();
    format(ent);
    super.format(ent);
    try {
      return getOutputStream().toString("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

}
