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
import net.riezebos.thoth.markdown.filehandle.BasicFileHandleFactoryTest;
import net.riezebos.thoth.markdown.filehandle.BasicFileHandleTest;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileHandleFactoryTest;
import net.riezebos.thoth.markdown.util.DocumentNodeTest;
import net.riezebos.thoth.markdown.util.LineInfoTest;
import net.riezebos.thoth.markdown.util.ProcessorErrorTest;
import net.riezebos.thoth.markdown.util.SoftLinkTranslationTest;
import net.riezebos.thoth.util.ThothUtilTest;

@RunWith(Suite.class)
@SuiteClasses({BookClassificationTest.class, BookmarkTest.class, BookmarkUsageTest.class, BookTest.class, ContentNodeTest.class, CommentTranslatorTest.class,
    CriticMarkupProcessorTest.class, DeleteTranslatorTest.class, HighlightTranslatorTest.class, InsertTranslatorTest.class, SubstitutionTranslatorTest.class,
    DocumentNodeTest.class, LineInfoTest.class, ProcessorErrorTest.class, SoftLinkTranslationTest.class, MarkDownDocumentTest.class, FileProcessorTest.class,
    ThothUtilTest.class, BasicFileHandleFactoryTest.class, BasicFileHandleTest.class, ClasspathFileHandleFactoryTest.class, IncludeProcessorTest.class})
public class AllTests {

}
