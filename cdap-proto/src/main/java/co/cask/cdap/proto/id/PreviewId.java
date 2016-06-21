/*
 * Copyright © 2016 Cask Data, Inc.
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

package co.cask.cdap.proto.id;

import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.element.EntityType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * Uniquely identify the preview.
 */
public class PreviewId extends NamespacedEntityId implements ParentedId<NamespaceId> {

  private final String namespace;
  private final String preview;

  public PreviewId(String namespace, String preview) {
    super(namespace, EntityType.APPLICATION);
    this.namespace = namespace;
    this.preview = preview;
  }

  /**
   *  Return the preview id
   */
  public String getPreview() {
    return preview;
  }

  @Override
  protected Iterable<String> toIdParts() {
    return Collections.unmodifiableList(Arrays.asList(namespace, preview));
  }

  @Override
  public String getEntityName() {
    return getPreview();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!super.equals(o)) {
      return false;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PreviewId that = (PreviewId) o;
    return Objects.equals(namespace, that.namespace) && Objects.equals(preview, that.preview);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), namespace, preview);
  }

  @Override
  public Id toId() {
    throw new UnsupportedOperationException("PreviewId.toId() not supported, should not be used");
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  @Override
  public NamespaceId getParent() {
    return new NamespaceId(namespace);
  }
}
