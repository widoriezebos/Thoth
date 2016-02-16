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
