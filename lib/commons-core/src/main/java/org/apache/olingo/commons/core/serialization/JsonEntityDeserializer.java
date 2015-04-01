/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.commons.core.serialization;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.Annotation;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.ResWrap;
import org.apache.olingo.commons.api.domain.ODataLinkType;
import org.apache.olingo.commons.api.domain.ODataOperation;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.core.data.AnnotationImpl;
import org.apache.olingo.commons.core.data.EntityImpl;
import org.apache.olingo.commons.core.data.LinkImpl;
import org.apache.olingo.commons.core.edm.provider.EdmTypeInfo;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Reads JSON string into an entity.
 * <br/>
 * If metadata information is available, the corresponding entity fields and content will be populated.
 */
public class JsonEntityDeserializer extends JsonDeserializer {

  public JsonEntityDeserializer(final boolean serverMode) {
    super(serverMode);
  }

  protected ResWrap<Entity> doDeserialize(final JsonParser parser) throws IOException {

    final ObjectNode tree = parser.getCodec().readTree(parser);

    if (tree.has(Constants.VALUE) && tree.get(Constants.VALUE).isArray()) {
      throw new JsonParseException("Expected OData Entity, found EntitySet", parser.getCurrentLocation());
    }

    final EntityImpl entity = new EntityImpl();

    final URI contextURL;
    if (tree.hasNonNull(Constants.JSON_CONTEXT)) {
      contextURL = URI.create(tree.get(Constants.JSON_CONTEXT).textValue());
      tree.remove(Constants.JSON_CONTEXT);
    } else if (tree.hasNonNull(Constants.JSON_METADATA)) {
      contextURL = URI.create(tree.get(Constants.JSON_METADATA).textValue());
      tree.remove(Constants.JSON_METADATA);
    } else {
      contextURL = null;
    }
    if (contextURL != null) {
      entity.setBaseURI(StringUtils.substringBefore(contextURL.toASCIIString(), Constants.METADATA));
    }

    final String metadataETag;
    if (tree.hasNonNull(Constants.JSON_METADATA_ETAG)) {
      metadataETag = tree.get(Constants.JSON_METADATA_ETAG).textValue();
      tree.remove(Constants.JSON_METADATA_ETAG);
    } else {
      metadataETag = null;
    }

    if (tree.hasNonNull(Constants.JSON_ETAG)) {
      entity.setETag(tree.get(Constants.JSON_ETAG).textValue());
      tree.remove(Constants.JSON_ETAG);
    }

    if (tree.hasNonNull(Constants.JSON_TYPE)) {
      entity.setType(new EdmTypeInfo.Builder().setTypeExpression(tree.get(Constants.JSON_TYPE).textValue()).build()
          .internal());
      tree.remove(Constants.JSON_TYPE);
    }

    if (tree.hasNonNull(Constants.JSON_ID)) {
      entity.setId(URI.create(tree.get(Constants.JSON_ID).textValue()));
      tree.remove(Constants.JSON_ID);
    }

    if (tree.hasNonNull(Constants.JSON_READ_LINK)) {
      final LinkImpl link = new LinkImpl();
      link.setRel(Constants.SELF_LINK_REL);
      link.setHref(tree.get(Constants.JSON_READ_LINK).textValue());
      entity.setSelfLink(link);

      tree.remove(Constants.JSON_READ_LINK);
    }

    if (tree.hasNonNull(Constants.JSON_EDIT_LINK)) {
      final LinkImpl link = new LinkImpl();
      if (serverMode) {
        link.setRel(Constants.EDIT_LINK_REL);
      }
      link.setHref(tree.get(Constants.JSON_EDIT_LINK).textValue());
      entity.setEditLink(link);

      tree.remove(Constants.JSON_EDIT_LINK);
    }

    if (tree.hasNonNull(Constants.JSON_MEDIA_READ_LINK)) {
      entity.setMediaContentSource(URI.create(tree.get(Constants.JSON_MEDIA_READ_LINK).textValue()));
      tree.remove(Constants.JSON_MEDIA_READ_LINK);
    }
    if (tree.hasNonNull(Constants.JSON_MEDIA_EDIT_LINK)) {
      entity.setMediaContentSource(URI.create(tree.get(Constants.JSON_MEDIA_EDIT_LINK).textValue()));
      tree.remove(Constants.JSON_MEDIA_EDIT_LINK);
    }
    if (tree.hasNonNull(Constants.JSON_MEDIA_CONTENT_TYPE)) {
      entity.setMediaContentType(tree.get(Constants.JSON_MEDIA_CONTENT_TYPE).textValue());
      tree.remove(Constants.JSON_MEDIA_CONTENT_TYPE);
    }
    if (tree.hasNonNull(Constants.JSON_MEDIA_ETAG)) {
      entity.setMediaETag(tree.get(Constants.JSON_MEDIA_ETAG).textValue());
      tree.remove(Constants.JSON_MEDIA_ETAG);
    }

    final Set<String> toRemove = new HashSet<String>();

    final Map<String, List<Annotation>> annotations = new HashMap<String, List<Annotation>>();
    for (final Iterator<Map.Entry<String, JsonNode>> itor = tree.fields(); itor.hasNext();) {
      final Map.Entry<String, JsonNode> field = itor.next();
      final Matcher customAnnotation = CUSTOM_ANNOTATION.matcher(field.getKey());

      links(field, entity, toRemove, tree, parser.getCodec());
      if (field.getKey().endsWith(getJSONAnnotation(Constants.JSON_MEDIA_EDIT_LINK))) {
        final LinkImpl link = new LinkImpl();
        link.setTitle(getTitle(field));
        link.setRel(Constants.NS_MEDIA_EDIT_LINK_REL + getTitle(field));
        link.setHref(field.getValue().textValue());
        link.setType(ODataLinkType.MEDIA_EDIT.toString());
        entity.getMediaEditLinks().add(link);

        if (tree.has(link.getTitle() + getJSONAnnotation(Constants.JSON_MEDIA_ETAG))) {
          link.setMediaETag(tree.get(link.getTitle() + getJSONAnnotation(Constants.JSON_MEDIA_ETAG)).asText());
          toRemove.add(link.getTitle() + getJSONAnnotation(Constants.JSON_MEDIA_ETAG));
        }

        toRemove.add(field.getKey());
        toRemove.add(setInline(field.getKey(), getJSONAnnotation(Constants.JSON_MEDIA_EDIT_LINK), tree, parser
            .getCodec(), link));
      } else if (field.getKey().endsWith(getJSONAnnotation(Constants.JSON_MEDIA_CONTENT_TYPE))) {
        final String linkTitle = getTitle(field);
        for (Link link : entity.getMediaEditLinks()) {
          if (linkTitle.equals(link.getTitle())) {
            link.setType(field.getValue().asText());
          }
        }
        toRemove.add(field.getKey());
      } else if (field.getKey().charAt(0) == '#') {
        final ODataOperation operation = new ODataOperation();
        operation.setMetadataAnchor(field.getKey());

        final ObjectNode opNode = (ObjectNode) tree.get(field.getKey());
        operation.setTitle(opNode.get(Constants.ATTR_TITLE).asText());
        operation.setTarget(URI.create(opNode.get(Constants.ATTR_TARGET).asText()));

        entity.getOperations().add(operation);

        toRemove.add(field.getKey());
      } else if (customAnnotation.matches() && !"odata".equals(customAnnotation.group(2))) {
        final Annotation annotation = new AnnotationImpl();
        annotation.setTerm(customAnnotation.group(2) + "." + customAnnotation.group(3));
        try {
          value(annotation, field.getValue(), parser.getCodec());
        } catch (final EdmPrimitiveTypeException e) {
          throw new IOException(e);
        }

        if (!annotations.containsKey(customAnnotation.group(1))) {
          annotations.put(customAnnotation.group(1), new ArrayList<Annotation>());
        }
        annotations.get(customAnnotation.group(1)).add(annotation);
      }
    }

    for (Link link : entity.getNavigationLinks()) {
      if (annotations.containsKey(link.getTitle())) {
        link.getAnnotations().addAll(annotations.get(link.getTitle()));
        for (Annotation annotation : annotations.get(link.getTitle())) {
          toRemove.add(link.getTitle() + "@" + annotation.getTerm());
        }
      }
    }
    for (Link link : entity.getMediaEditLinks()) {
      if (annotations.containsKey(link.getTitle())) {
        link.getAnnotations().addAll(annotations.get(link.getTitle()));
        for (Annotation annotation : annotations.get(link.getTitle())) {
          toRemove.add(link.getTitle() + "@" + annotation.getTerm());
        }
      }
    }

    tree.remove(toRemove);

    try {
      populate(entity, entity.getProperties(), tree, parser.getCodec());
    } catch (final EdmPrimitiveTypeException e) {
      throw new IOException(e);
    }

    return new ResWrap<Entity>(contextURL, metadataETag, entity);
  }
}
