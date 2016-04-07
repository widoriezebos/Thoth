package net.riezebos.thoth.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class AccessRuleTest {

  @Test
  public void test() {
    
    AccessRule accessRule = new AccessRule("path/*", "group1, group2; group3", true, false);
    
    assertTrue(accessRule.applies("path/something"));
    assertTrue(accessRule.applies("path/something/else"));
    assertTrue(accessRule.applies("/path/something"));
    assertFalse(accessRule.applies("/nope/something"));
    
    Set<String> allGroups = new HashSet<>(Arrays.asList(new String[]{"group1", "group2", "group3"}));
    Set<String> just12 = new HashSet<>(Arrays.asList(new String[]{"group1", "group2"}));
    Set<String> just1 = new HashSet<>(Arrays.asList(new String[]{"group1"}));
    Set<String> other = new HashSet<>(Arrays.asList(new String[]{"other"}));

    assertTrue(accessRule.isAccessAllowed(allGroups));
    assertFalse(accessRule.isAccessAllowed(just12));
    assertFalse(accessRule.isAccessAllowed(just1));
    assertFalse(accessRule.isAccessAllowed(other));
    
    accessRule = new AccessRule("path/*", "group1, group2; group3", false, false);
    assertTrue(accessRule.isAccessAllowed(allGroups));
    assertTrue(accessRule.isAccessAllowed(just12));
    assertTrue(accessRule.isAccessAllowed(just1));
    assertFalse(accessRule.isAccessAllowed(other));
  }

}
