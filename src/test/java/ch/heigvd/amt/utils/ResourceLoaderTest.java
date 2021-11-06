/*
 * Copyright (C) 2021 Heig-vd
 *
 * Licensed under the “Commons Clause” License Condition v1.0. You may obtain a copy of the License at
 *
 * https://github.com/heigvd-software-engineering/netscan/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
