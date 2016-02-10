package net.riezebos.thoth.markdown.critics;

import static org.junit.Assert.*;

import org.junit.Test;

public class CriticMarkupProcessorTest {

  @Test
  public void test() {
    CriticMarkupProcessor criticMarkupProcessor = new CriticMarkupProcessor();

    String source = "This is {++added ++}and then {--deleted --}" + //
        "or later {~~changed~>tosomething~~}{>>commented<<} and then {==HIGHLIGHTED==} done.";

    String result = criticMarkupProcessor.processCritics(source, CriticProcessingMode.DO_NOTHING);
    assertEquals(source, result);

    String processed = "This is added and then " + //
        "or later tosomething and then HIGHLIGHTED done.";

    result = criticMarkupProcessor.processCritics(source, CriticProcessingMode.PROCESS);
    assertEquals(processed, result);

    String source2 = "Nothing matches";
    result = criticMarkupProcessor.processCritics(source2, CriticProcessingMode.PROCESS);
    assertEquals(source2, result);

  }

}
