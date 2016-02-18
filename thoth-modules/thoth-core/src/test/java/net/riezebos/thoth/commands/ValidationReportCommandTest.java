package net.riezebos.thoth.commands;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class ValidationReportCommandTest extends CommandTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"Very good, there are currently no validation errors"};
    String[] jsonExists = new String[] {"\"errors\":[]"};

    testCommand(new ValidationReportCommand(), "/main/Main.md", "validationreport", htmlExists, jsonExists);
  }

}
