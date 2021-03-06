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
package org.apache.olingo.server.tecsvc.processor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Builder;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.commons.api.edm.EdmStructuredType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.processor.ComplexCollectionProcessor;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.PrimitiveCollectionProcessor;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.FixedFormatSerializer;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.PrimitiveValueSerializerOptions;
import org.apache.olingo.server.api.serializer.RepresentationType;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.tecsvc.data.DataProvider;

/**
 * Technical Processor which provides functionality related to primitive and complex types and collections thereof.
 */
public class TechnicalPrimitiveComplexProcessor extends TechnicalProcessor
    implements PrimitiveProcessor, PrimitiveValueProcessor, PrimitiveCollectionProcessor,
    ComplexProcessor, ComplexCollectionProcessor {

  public TechnicalPrimitiveComplexProcessor(final DataProvider dataProvider,
      final ServiceMetadata serviceMetadata) {
    super(dataProvider, serviceMetadata);
  }

  @Override
  public void readPrimitive(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType contentType) throws ODataApplicationException, SerializerException {
    readProperty(request, response, uriInfo, contentType, RepresentationType.PRIMITIVE);
  }

  @Override
  public void updatePrimitive(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, DeserializerException, SerializerException {
    updateProperty(request, response, uriInfo, requestFormat, responseFormat, RepresentationType.PRIMITIVE);
  }

  @Override
  public void deletePrimitive(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException {
    deleteProperty(request, response, uriInfo);
  }

  @Override
  public void readPrimitiveCollection(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType contentType) throws ODataApplicationException, SerializerException {
    readProperty(request, response, uriInfo, contentType, RepresentationType.COLLECTION_PRIMITIVE);
  }

  @Override
  public void updatePrimitiveCollection(final ODataRequest request, ODataResponse response,
      final UriInfo uriInfo, final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, DeserializerException, SerializerException {
    updateProperty(request, response, uriInfo, requestFormat, responseFormat, RepresentationType.COLLECTION_PRIMITIVE);
  }

  @Override
  public void deletePrimitiveCollection(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException {
    deleteProperty(request, response, uriInfo);
  }

  @Override
  public void readComplex(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType contentType) throws ODataApplicationException, SerializerException {
    readProperty(request, response, uriInfo, contentType, RepresentationType.COMPLEX);
  }

  @Override
  public void updateComplex(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, DeserializerException, SerializerException {
    updateProperty(request, response, uriInfo, requestFormat, responseFormat, RepresentationType.COMPLEX);
  }

  @Override
  public void deleteComplex(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException {
    deleteProperty(request, response, uriInfo);
  }

  @Override
  public void readComplexCollection(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType contentType) throws ODataApplicationException, SerializerException {
    readProperty(request, response, uriInfo, contentType, RepresentationType.COLLECTION_COMPLEX);
  }

  @Override
  public void updateComplexCollection(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, DeserializerException, SerializerException {
    updateProperty(request, response, uriInfo, requestFormat, responseFormat, RepresentationType.COLLECTION_COMPLEX);
  }

  @Override
  public void deleteComplexCollection(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException {
    deleteProperty(request, response, uriInfo);
  }

  private void readProperty(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType contentType, final RepresentationType representationType)
          throws ODataApplicationException, SerializerException {
    final UriInfoResource resource = uriInfo.asUriInfoResource();
    validateOptions(resource);
    validatePath(resource);
    final EdmEntitySet edmEntitySet = getEdmEntitySet(resource);

    final List<UriResource> resourceParts = resource.getUriResourceParts();
    final List<String> path = getPropertyPath(resourceParts, 0);

    final Entity entity = readEntity(uriInfo);

    if (entity != null && entity.getETag() != null) {
      checkReadPreconditions(entity.getETag(),
          request.getHeaders(HttpHeader.IF_MATCH),
          request.getHeaders(HttpHeader.IF_NONE_MATCH));
    }

    final Property property = entity == null ?
        getPropertyData(
            dataProvider.readFunctionPrimitiveComplex(((UriResourceFunction) resourceParts.get(0)).getFunction(),
            ((UriResourceFunction) resourceParts.get(0)).getParameters()), path) :
        getPropertyData(entity, path);

    if (property == null) {
      throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
    } else {
      if (property.getValue() == null) {
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
      } else {
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        final EdmProperty edmProperty = path.isEmpty() ? null :
            ((UriResourceProperty) resourceParts.get(resourceParts.size() - 1)).getProperty();
        final EdmType type = edmProperty == null ?
            ((UriResourceFunction) resourceParts.get(0)).getType() :
            edmProperty.getType();
        final EdmReturnType returnType = resourceParts.get(0) instanceof UriResourceFunction ?
            ((UriResourceFunction) resourceParts.get(0)).getFunction().getReturnType() : null;

        final ExpandOption expand = uriInfo.getExpandOption();
        final SelectOption select = uriInfo.getSelectOption();
        final ODataFormat format = ODataFormat.fromContentType(contentType);
        final SerializerResult result = serializeProperty(entity, edmEntitySet, path, property, edmProperty,
            type, returnType, representationType, format, expand, select);
        response.setContent(result.getContent());
        response.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
      }
      if (entity != null && entity.getETag() != null) {
        response.setHeader(HttpHeader.ETAG, entity.getETag());
      }
    }
  }

  private void updateProperty(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat, final RepresentationType representationType)
          throws ODataApplicationException, DeserializerException, SerializerException {
    final UriInfoResource resource = uriInfo.asUriInfoResource();
    validatePath(resource);
    final EdmEntitySet edmEntitySet = getEdmEntitySet(resource);

    Entity entity = readEntity(uriInfo);
    checkChangePreconditions(entity.getETag(),
        request.getHeaders(HttpHeader.IF_MATCH),
        request.getHeaders(HttpHeader.IF_NONE_MATCH));

    final List<UriResource> resourceParts = resource.getUriResourceParts();
    final List<String> path = getPropertyPath(resourceParts, 0);
    final EdmProperty edmProperty = ((UriResourceProperty) resourceParts.get(resourceParts.size() - 1))
        .getProperty();

    checkRequestFormat(requestFormat);
    final Property changedProperty = odata.createDeserializer(ODataFormat.fromContentType(requestFormat))
        .property(request.getBody(), edmProperty).getProperty();
    if (changedProperty.isNull() && !edmProperty.isNullable()) {
      throw new ODataApplicationException("Not nullable.", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
    }

    Property property = getPropertyData(entity, path);

    dataProvider.updateProperty(edmProperty, property, changedProperty, request.getMethod() == HttpMethod.PATCH);
    dataProvider.updateETag(entity);

    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    final ODataFormat format = ODataFormat.fromContentType(responseFormat);
    final SerializerResult result = serializeProperty(entity, edmEntitySet, path, property, edmProperty,
        edmProperty.getType(), null, representationType, format, null, null);
    response.setContent(result.getContent());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    if (entity.getETag() != null) {
      response.setHeader(HttpHeader.ETAG, entity.getETag());
    }
  }

  private void deleteProperty(final ODataRequest request, ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException {
    final UriInfoResource resource = uriInfo.asUriInfoResource();
    validatePath(resource);
    getEdmEntitySet(uriInfo); // including checks

    Entity entity = readEntity(uriInfo);
    checkChangePreconditions(entity.getETag(),
        request.getHeaders(HttpHeader.IF_MATCH),
        request.getHeaders(HttpHeader.IF_NONE_MATCH));

    final List<UriResource> resourceParts = resource.getUriResourceParts();
    final List<String> path = getPropertyPath(resourceParts, 0);

    Property property = getPropertyData(entity, path);

    final EdmProperty edmProperty = ((UriResourceProperty) resourceParts.get(resourceParts.size() - 1))
        .getProperty();

    if (edmProperty.isNullable()) {
      property.setValue(property.getValueType(), edmProperty.isCollection() ? Collections.emptyList() : null);
      dataProvider.updateETag(entity);
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    } else {
      throw new ODataApplicationException("Not nullable.", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
    }
  }

  private Property getPropertyData(final Entity entity, final List<String> path) {
    return getPropertyData(entity.getProperty(path.get(0)), path.subList(1, path.size()));
  }

  private Property getPropertyData(final Property property, final List<String> path) {
    Property result = property;
    for (final String name : path) {
      if (result != null && property.isComplex()) {
        final List<Property> complex = result.asComplex().getValue();
        result = null;
        for (final Property innerProperty : complex) {
          if (innerProperty.getName().equals(name)) {
            result = innerProperty;
            break;
          }
        }
      }
    }
    return result;
  }

  private List<String> getPropertyPath(final List<UriResource> path, final int trailing) {
    List<String> result = new LinkedList<String>();
    int index = path.size() - trailing - 1;
    while (path.get(index) instanceof UriResourceProperty) {
      result.add(0, ((UriResourceProperty) path.get(index)).getProperty().getName());
      index--;
    }
    return result;
  }

  private String buildPropertyPath(final List<String> path) {
    StringBuilder result = new StringBuilder();
    for (final String segment : path) {
      result.append(result.length() == 0 ? "" : '/').append(segment);
    }
    return result.toString();
  }

  private SerializerResult serializeProperty(final Entity entity, final EdmEntitySet edmEntitySet,
      final List<String> path, final Property property, final EdmProperty edmProperty,
      final EdmType type, final EdmReturnType returnType,
      final RepresentationType representationType, final ODataFormat format,
      final ExpandOption expand, final SelectOption select) throws SerializerException {
    ODataSerializer serializer = odata.createSerializer(format);
    final ContextURL contextURL = format == ODataFormat.JSON_NO_METADATA ? null :
        getContextUrl(edmEntitySet, entity, path, type, representationType, expand, select);
    SerializerResult result = null;
    switch (representationType) {
    case PRIMITIVE:
      result = serializer.primitive((EdmPrimitiveType) type, property,
          PrimitiveSerializerOptions.with().contextURL(contextURL)
              .nullable(edmProperty == null ? returnType.isNullable() : edmProperty.isNullable())
              .maxLength(edmProperty == null ? returnType.getMaxLength() : edmProperty.getMaxLength())
              .precision(edmProperty == null ? returnType.getPrecision() : edmProperty.getPrecision())
              .scale(edmProperty == null ? returnType.getScale() : edmProperty.getScale())
              .unicode(edmProperty == null ? null : edmProperty.isUnicode())
              .build());
      break;
    case COMPLEX:
      result = serializer.complex(serviceMetadata, (EdmComplexType) type, property,
          ComplexSerializerOptions.with().contextURL(contextURL)
              .expand(expand).select(select)
              .build());
      break;
    case COLLECTION_PRIMITIVE:
      result = serializer.primitiveCollection((EdmPrimitiveType) type, property,
          PrimitiveSerializerOptions.with().contextURL(contextURL)
              .nullable(edmProperty == null ? returnType.isNullable() : edmProperty.isNullable())
              .maxLength(edmProperty == null ? returnType.getMaxLength() : edmProperty.getMaxLength())
              .precision(edmProperty == null ? returnType.getPrecision() : edmProperty.getPrecision())
              .scale(edmProperty == null ? returnType.getScale() : edmProperty.getScale())
              .unicode(edmProperty == null ? null : edmProperty.isUnicode())
              .build());
      break;
    case COLLECTION_COMPLEX:
      result = serializer.complexCollection(serviceMetadata, (EdmComplexType) type, property,
          ComplexSerializerOptions.with().contextURL(contextURL)
              .expand(expand).select(select)
              .build());
      break;
    default:
      break;
    }
    return result;
  }

  private ContextURL getContextUrl(final EdmEntitySet entitySet, final Entity entity, final List<String> path,
      final EdmType type, final RepresentationType representationType,
      final ExpandOption expand, final SelectOption select) throws SerializerException {
    final UriHelper helper = odata.createUriHelper();
    Builder builder = ContextURL.with();
    builder = entitySet == null ?
        representationType == RepresentationType.PRIMITIVE || representationType == RepresentationType.COMPLEX ?
            builder.type(type) :
            builder.type(type).asCollection() :
        builder.entitySet(entitySet).keyPath(helper.buildKeyPredicate(entitySet.getEntityType(), entity));
    if (entitySet != null && !path.isEmpty()) {
      builder = builder.navOrPropertyPath(buildPropertyPath(path));
    }
    builder = builder.selectList(type.getKind() == EdmTypeKind.PRIMITIVE ? null :
        helper.buildContextURLSelectList((EdmStructuredType) type, expand, select));
    return builder.build();
  }

  @Override
  public void readPrimitiveValue(final ODataRequest request, ODataResponse response, final UriInfo uriInfo,
      final ContentType contentType) throws ODataApplicationException, SerializerException {
    final UriInfoResource resource = uriInfo.asUriInfoResource();
    validateOptions(resource);
    validatePath(resource);
    getEdmEntitySet(uriInfo); // including checks

    final List<UriResource> resourceParts = resource.getUriResourceParts();
    final List<String> path = getPropertyPath(resourceParts, 1);

    final Entity entity = readEntity(uriInfo);
    if (entity != null && entity.getETag() != null) {
      checkReadPreconditions(entity.getETag(),
          request.getHeaders(HttpHeader.IF_MATCH),
          request.getHeaders(HttpHeader.IF_NONE_MATCH));
    }

    final Property property = entity == null ?
        getPropertyData(dataProvider.readFunctionPrimitiveComplex(((UriResourceFunction) resourceParts.get(0))
            .getFunction(),
            ((UriResourceFunction) resourceParts.get(0)).getParameters()), path) :
        getPropertyData(entity, path);

    if (property == null || property.getValue() == null) {
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    } else {
      final EdmProperty edmProperty = path.isEmpty() ? null :
          ((UriResourceProperty) resourceParts.get(resourceParts.size() - 2)).getProperty();
      final EdmPrimitiveType type = (EdmPrimitiveType) (edmProperty == null ?
          ((UriResourceFunction) resourceParts.get(0)).getType() :
          edmProperty.getType());
      final EdmReturnType returnType = resourceParts.get(0) instanceof UriResourceFunction ?
          ((UriResourceFunction) resourceParts.get(0)).getFunction().getReturnType() : null;
      final FixedFormatSerializer serializer = odata.createFixedFormatSerializer();
      response.setContent(type == odata.createPrimitiveTypeInstance(EdmPrimitiveTypeKind.Binary) ?
          serializer.binary((byte[]) property.getValue()) :
          serializer.primitiveValue(type, property.getValue(),
              PrimitiveValueSerializerOptions.with()
                  .nullable(edmProperty == null ? returnType.isNullable() : edmProperty.isNullable())
                  .maxLength(edmProperty == null ? returnType.getMaxLength() : edmProperty.getMaxLength())
                  .precision(edmProperty == null ? returnType.getPrecision() : edmProperty.getPrecision())
                  .scale(edmProperty == null ? returnType.getScale() : edmProperty.getScale())
                  .unicode(edmProperty == null ? null : edmProperty.isUnicode())
                  .build()));
      response.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
      response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    }
    if (entity != null && entity.getETag() != null) {
      response.setHeader(HttpHeader.ETAG, entity.getETag());
    }
  }

  private void validatePath(final UriInfoResource uriInfo) throws ODataApplicationException {
    final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
    for (final UriResource segment : resourcePaths.subList(1, resourcePaths.size())) {
      final UriResourceKind kind = segment.getKind();
      if (kind != UriResourceKind.navigationProperty
          && kind != UriResourceKind.primitiveProperty
          && kind != UriResourceKind.complexProperty
          && kind != UriResourceKind.count
          && kind != UriResourceKind.value) {
        throw new ODataApplicationException("Invalid resource type.",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
      }
    }
  }
}
