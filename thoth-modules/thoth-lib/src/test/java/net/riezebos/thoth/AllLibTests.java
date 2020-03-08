/* Copyright (c) 2020 W.T.J. Riezebos
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

import net.riezebos.thoth.beans.BookClassificationTest;
import net.riezebos.thoth.beans.BookTest;
import net.riezebos.thoth.beans.BookmarkTest;
import net.riezebos.thoth.beans.BookmarkUsageTest;
import net.riezebos.thoth.beans.ContentNodeTest;
import net.riezebos.thoth.beans.MarkDownDocumentTest;
import net.riezebos.thoth.markdown.FileProcessorTest;
import net.riezebos.thoth.markdown.IncludeProcessorTest;
import net.riezebos.thoth.markdown.critics.CommentTranslatorTest;
import net.riezebos.thoth.markdown.critics.CriticMarkupProcessorTest;
import net.riezebos.thoth.markdown.critics.DeleteTranslatorTest;
import net.riezebos.thoth.markdown.critics.HighlightTranslatorTest;
import net.riezebos.thoth.markdown.critics.InsertTranslatorTest;
import net.riezebos.thoth.markdown.critics.SubstitutionTranslatorTest;
import net.riezebos.thoth.markdown.filehandle.BasicFileSystemTest;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystemTest;
import net.riezebos.thoth.markdown.filehandle.FileHandleTest;
import net.riezebos.thoth.markdown.filehandle.ZipFileSystemTest;
import net.riezebos.thoth.markdown.util.DocumentNodeTest;
import net.riezebos.thoth.markdown.util.LineInfoTest;
import net.riezebos.thoth.markdown.util.ProcessorErrorTest;
import net.riezebos.thoth.markdown.util.SoftLinkTranslationTest;
import net.riezebos.thoth.util.ThothUtilTest;

@RunWith(Suite.class)
@SuiteClasses({BookClassificationTest.class, BookmarkTest.class, BookmarkUsageTest.class, BookTest.class, //
    ContentNodeTest.class, CommentTranslatorTest.class, CriticMarkupProcessorTest.class, DeleteTranslatorTest.class, //
    HighlightTranslatorTest.class, InsertTranslatorTest.class, SubstitutionTranslatorTest.class, //
    DocumentNodeTest.class, LineInfoTest.class, ProcessorErrorTest.class, SoftLinkTranslationTest.class, //
    MarkDownDocumentTest.class, FileProcessorTest.class, ThothUtilTest.class, FileHandleTest.class, //
    ClasspathFileSystemTest.class, ZipFileSystemTest.class, IncludeProcessorTest.class, BasicFileSystemTest.class})
public class AllLibTests {

}
