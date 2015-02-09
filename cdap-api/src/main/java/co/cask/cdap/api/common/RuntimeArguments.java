/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.api.common;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class to convert String array to Map<String, String>.
 */
public final class RuntimeArguments {

  public static final Map<String, String> NO_ARGUMENTS = Collections.emptyMap();
  private static final String STAR = "*";
  private static final String DOT = ".";

  private RuntimeArguments() {
  }

  /**
   * Converts a POSIX compliant program argument array to a String-to-String Map.
   * @param args Array of Strings where each element is a POSIX compliant program argument (Ex: "--os=Linux" ).
   * @return Map of argument Keys and Values (Ex: Key = "os" and Value = "Linux").
   */
  public static Map<String, String> fromPosixArray(String[] args) {
    Map<String, String> kvMap = Maps.newHashMap();
    for (String arg : args) {
      kvMap.putAll(Splitter.on("--").omitEmptyStrings().trimResults().withKeyValueSeparator("=").split(arg));
    }
    return kvMap;
  }

  /**
   * Converts a String-to-String Map to POSIX compliant program argument array.
   * @param kvMap Map of argument Keys and Values.
   * @return Array of Strings in POSIX compliant format.
   */
  public static String[] toPosixArray(Map<String, String> kvMap) {
    String[] args = new String[kvMap.size()];
    int index = 0;
    for (Map.Entry<String, String> kv : kvMap.entrySet()) {
      args[index++] = String.format("--%s=%s", kv.getKey(), kv.getValue());
    }
    return args;
  }

  /**
   * Converts a Iterable of Map.Entry<String, String> to a POSIX compliant program argument array.
   * @param iterable of Map.Entry of argument Key and Value.
   * @return Array of Strings in POSIX compliant format.
   */
  public static String[] toPosixArray(Iterable<Map.Entry<String, String>> iterable) {
    ImmutableMap.Builder<String, String> userArgMapBuilder = ImmutableMap.builder();
    for (Map.Entry<String, String> kv : iterable) {
      userArgMapBuilder.put(kv);
    }
    return toPosixArray(userArgMapBuilder.build());
  }

  /**
   * Extracts the scope from the argument as identified by its prefix.
   *
   * 1. Argument can be prefixed by "<scopetype>.<name>.". e.g. mapreduce.myMapReduce.read.timeout=30. In this case
   * only MapReduce program named 'myMapReduce' will receive the argument read.timeout=30.
   * 2. Argument can be prefixed by "<scopetype>.*.". e.g. mapreduce.*.read.timeout=30. In this case all the underlying
   * MapReduce programs will receive the argument read.timeout=30.
   * 3. If the argument is not prefixed with any scope, it is considered global and is passed further without any
   * changes. e.g. read.timeout=30
   *
   * Arguments scoped by DATASET are only filtered while inside the dataset scope. They are passed from other
   * scopes without any modifications.
   *
   * Arguments prefixed by "<scopetype>.<name>." have higher precedence than those prefixed by "<scopetype>.*.".
   * Arguments specified at the global level have lowest precedence.
   *
   * @param scope The type of the scope
   * @param name The name of the scope, e.g. "myTable"
   * @param arguments the runtime arguments of the enclosing scope
   * @return a map that contains only the keys that start with &lt;scope>.&lt;name>., with that prefix removed.
   */
  public static Map<String, String> extractScope(Scope scope, String name, Map<String, String> arguments) {
    if (arguments == null || arguments.isEmpty()) {
      return arguments;
    }

    String prefix = scope + DOT + name + DOT;
    String wildCardPrefix = scope + DOT + STAR + DOT;
    Map<String, String> result = Maps.newHashMap();
    Map<String, String> prefixMatchedArgs = Maps.newHashMap();
    Map<String, String> wildCardPrefixMatchedArgs = Maps.newHashMap();
    Map<String, String> noMatchArgs = Maps.newHashMap();

    // Group the arguments into different categories based on wild card prefix match, named prefix match or no match
    for (Map.Entry<String, String> entry : arguments.entrySet()) {
      if (entry.getKey().startsWith(wildCardPrefix)) {
        // Argument is prefixed with "<scope>.*."
        wildCardPrefixMatchedArgs.put(entry.getKey().substring(wildCardPrefix.length()), entry.getValue());
      } else if (entry.getKey().startsWith(prefix)) {
        // Argument is prefixed with "<scope>.<name>."
        prefixMatchedArgs.put(entry.getKey().substring(prefix.length()), entry.getValue());
      } else {
        // Arguments specified for other scopes or without any scope
        noMatchArgs.put(entry.getKey(), entry.getValue());
      }
    }

    // Remove the arguments specified by other scopes. e.g. If the current scope is "MAPREDUCE.myMapReduce", remove
    // the arguments specified with other scopes(except DATASET) such as "MAPREDUCE.yourMapReduce", "SPARK.somespark.".
    // Note that DATASET scope requires special handling. If the current scope is "MAPREDUCE.myMapReduce", we still want
    // to pass all the arguments scoped by DATASET. Arguments scoped by DATASET will only get filtered if current scope
    // is DATASET.
    Iterator<Map.Entry<String, String>> it = noMatchArgs.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, String> entry = it.next();
      for (Scope s : Scope.values()) {
        String scopePrefix = s + DOT;
        if (entry.getKey().startsWith(scopePrefix)) {
          if (!s.equals(Scope.DATASET) || scope.equals(Scope.DATASET)) {
            it.remove();
            break;
          }
        }
      }
    }

    result.putAll(noMatchArgs);
    result.putAll(wildCardPrefixMatchedArgs);
    result.putAll(prefixMatchedArgs);
    return result;
  }

  /**
   * Add a scope prefix to all arguments.
   * @param scope The type of the scope
   * @param name The name of the scope, e.g. "myTable"
   * @param arguments the runtime arguments to be scoped
   * @return a map that contains all keys, prefixed with with &lt;scope>.&lt;name>.
   */
  public static Map<String, String> addScope(Scope scope, String name, Map<String, String> arguments) {
    if (arguments == null || arguments.isEmpty()) {
      return arguments;
    }
    final String prefix = scope + "." + name + ".";
    Map<String, String> result = Maps.newHashMap();
    for (Map.Entry<String, String> entry : arguments.entrySet()) {
        result.put(prefix + entry.getKey(), entry.getValue());
    }
    return result;
  }

}
