package ch.heigvd.amt.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceLoaderTest {

  @Test
  void loadResourceForFileWorks() {
    Assertions.assertEquals(
        "Hello\n" + "Test\n" + "1\n" + "2\n" + "3",
        ResourceLoader.loadResource("Test_loadressources.file"));
  }

  @Test
  void missingFilesThrows() {
    Assertions.assertThrows(
        RuntimeException.class, () -> ResourceLoader.loadResource("doesNotExist.file"));
  }
}
