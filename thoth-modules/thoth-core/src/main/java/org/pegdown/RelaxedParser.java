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
package org.pegdown;

import org.parboiled.Rule;
import org.pegdown.ast.Node;
import org.pegdown.plugins.PegDownPlugins;

/**
 * Parboiled parser for the standard and extended markdown syntax. Builds an Abstract Syntax Tree (AST) of {@link Node} objects. This is a patched version of
 * the original Parser class. Reason is that the Definition List of the original parser required a whitespace right after the ':' for it to recognize. We want
 * to relax this a bit. Note that the package of this class NEEDS to match the package of the (extended) Parser class because Parboiled will otherwise fail with
 * an IllegalAccess Exception.
 */

public class RelaxedParser extends Parser implements Extensions {

  public RelaxedParser(Integer options, Long maxParsingTimeInMillis, ParseRunnerProvider parseRunnerProvider, PegDownPlugins plugins) {
    super(options, maxParsingTimeInMillis, parseRunnerProvider, plugins);
  }

  @Override
  public Rule DefListBullet() {
    return Sequence(NonindentSpace(), AnyOf(":~")); // Removed OneOrMore(Spacechar()) as an argument here
  }

}
