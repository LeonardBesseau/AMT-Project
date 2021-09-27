/*
 * Copyright (C) 2021 Heig-vd
 *
 * Licensed under the “Commons Clause” License Condition v1.0. You may obtain a copy of the License at
 *
 * https://github.com/baremaps/ipmap/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ch.heigvd.amt.utils;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Resource loader. used for loading data from resources file */
public class ResourceLoader {

  /**
   * Load a resource from the resource folder
   *
   * @param resource The path to the resource (Root is the resource folder)
   * @return the content of the file
   */
  public static String loadResource(String resource) {
    try {
      return Resources.toString(Resources.getResource(resource), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private ResourceLoader() {}
}
