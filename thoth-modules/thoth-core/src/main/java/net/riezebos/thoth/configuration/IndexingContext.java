package net.riezebos.thoth.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.riezebos.thoth.markdown.util.ProcessorError;

public class IndexingContext {
  private Map<String, List<String>> indirectReverseIndex = new HashMap<>();
  private Map<String, List<String>> directReverseIndex = new HashMap<>();
  private List<ProcessorError> errors = new ArrayList<>();
  private Set<String> referencedLocalResources = new HashSet<>();

  public Map<String, List<String>> getIndirectReverseIndex() {
    return indirectReverseIndex;
  }

  public Map<String, List<String>> getDirectReverseIndex() {
    return directReverseIndex;
  }

  public List<ProcessorError> getErrors() {
    return errors;
  }

  public Set<String> getReferencedLocalResources() {
    return referencedLocalResources;

  }
}
