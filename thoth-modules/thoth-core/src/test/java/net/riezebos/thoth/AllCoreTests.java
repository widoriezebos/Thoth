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

import net.riezebos.thoth.commands.BrowseCommandTest;
import net.riezebos.thoth.commands.ContextIndexCommandTest;
import net.riezebos.thoth.commands.DiffCommandTest;
import net.riezebos.thoth.commands.ErrorPageCommandTest;
import net.riezebos.thoth.commands.IndexCommandTest;
import net.riezebos.thoth.commands.LoginCommandTest;
import net.riezebos.thoth.commands.ManageUsersCommandTest;
import net.riezebos.thoth.commands.MetaCommandTest;
import net.riezebos.thoth.commands.PullCommandTest;
import net.riezebos.thoth.commands.ReindexCommandTest;
import net.riezebos.thoth.commands.RevisionsCommandTest;
import net.riezebos.thoth.commands.SearchCommandTest;
import net.riezebos.thoth.commands.UserProfileCommandTest;
import net.riezebos.thoth.commands.ValidationReportCommandTest;
import net.riezebos.thoth.configuration.CacheManagerTest;
import net.riezebos.thoth.configuration.HotReloadableConfigurationTest;
import net.riezebos.thoth.configuration.PropertyBasedConfigurationTest;
import net.riezebos.thoth.configuration.persistence.dbs.DatabaseIdiomFactoryTest;
import net.riezebos.thoth.configuration.persistence.dbs.SqlStatementTest;
import net.riezebos.thoth.content.AutoRefresherTest;
import net.riezebos.thoth.content.ContentManagerBaseTest;
import net.riezebos.thoth.content.ThothContextTest;
import net.riezebos.thoth.content.comments.dao.CommentDaoTest;
import net.riezebos.thoth.content.impl.FSContentManagerTest;
import net.riezebos.thoth.content.impl.GitContentManagerTest;
import net.riezebos.thoth.content.impl.ZipContentManagerTest;
import net.riezebos.thoth.content.search.FragmentTest;
import net.riezebos.thoth.content.search.IndexerTest;
import net.riezebos.thoth.content.search.SearcherTest;
import net.riezebos.thoth.content.skinning.SkinManagerTest;
import net.riezebos.thoth.content.versioncontrol.CommitTest;
import net.riezebos.thoth.content.versioncontrol.SourceDiffTest;
import net.riezebos.thoth.context.BasicContextManagerTest;
import net.riezebos.thoth.exceptions.ExceptionTest;
import net.riezebos.thoth.renderers.CustomRendererTest;
import net.riezebos.thoth.renderers.HtmlRendererTest;
import net.riezebos.thoth.renderers.RawRendererTest;
import net.riezebos.thoth.servlets.ThothServletTest;
import net.riezebos.thoth.user.BasicUserManagerTest;
import net.riezebos.thoth.user.PasswordUtilTest;
import net.riezebos.thoth.util.DiscardingListTest;
import net.riezebos.thoth.util.ThothCoreUtilTest;
import net.riezebos.thoth.util.diff_match_patchTest;

@RunWith(Suite.class)
@SuiteClasses({PropertyBasedConfigurationTest.class, ContentManagerBaseTest.class, SkinManagerTest.class, //
    HtmlRendererTest.class, CustomRendererTest.class, RawRendererTest.class, ThothServletTest.class, //
    FragmentTest.class, IndexerTest.class, AutoRefresherTest.class, ThothContextTest.class, //
    ExceptionTest.class, CommitTest.class, SourceDiffTest.class, SearcherTest.class, CacheManagerTest.class, //
    DiscardingListTest.class, BrowseCommandTest.class, ContextIndexCommandTest.class, //
    DiffCommandTest.class, ErrorPageCommandTest.class, IndexCommandTest.class, MetaCommandTest.class, //
    PullCommandTest.class, ReindexCommandTest.class, RevisionsCommandTest.class, SearchCommandTest.class, //
    ValidationReportCommandTest.class, GitContentManagerTest.class, FSContentManagerTest.class, //
    ZipContentManagerTest.class, ThothCoreUtilTest.class, diff_match_patchTest.class, //
    HotReloadableConfigurationTest.class, BasicUserManagerTest.class, PasswordUtilTest.class, DatabaseIdiomFactoryTest.class, //
    SqlStatementTest.class, LoginCommandTest.class, ManageUsersCommandTest.class, UserProfileCommandTest.class, //
    BasicContextManagerTest.class, CommentDaoTest.class})

public class AllCoreTests {

}
