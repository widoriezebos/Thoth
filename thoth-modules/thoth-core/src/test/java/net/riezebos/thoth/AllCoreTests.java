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
package net.riezebos.thoth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.riezebos.thoth.configuration.PropertyBasedConfigurationTest;
import net.riezebos.thoth.content.ContentManagerBaseTest;
import net.riezebos.thoth.content.skinning.SkinManagerTest;
import net.riezebos.thoth.renderers.CustomRendererTest;
import net.riezebos.thoth.renderers.HtmlRendererTest;
import net.riezebos.thoth.renderers.RawRendererTest;
import net.riezebos.thoth.servlets.ThothServletTest;

@RunWith(Suite.class)
@SuiteClasses({PropertyBasedConfigurationTest.class, ContentManagerBaseTest.class, SkinManagerTest.class, //
    HtmlRendererTest.class, CustomRendererTest.class, RawRendererTest.class, ThothServletTest.class})

public class AllCoreTests {

}
