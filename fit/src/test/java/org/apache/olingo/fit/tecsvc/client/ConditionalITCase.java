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
package org.apache.olingo.fit.tecsvc.client;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.ODataBasicRequest;
import org.apache.olingo.client.api.communication.request.cud.ODataDeleteRequest;
import org.apache.olingo.client.api.communication.request.cud.ODataEntityUpdateRequest;
import org.apache.olingo.client.api.communication.request.cud.ODataPropertyUpdateRequest;
import org.apache.olingo.client.api.communication.request.cud.UpdateType;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntityRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataPropertyRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataValueRequest;
import org.apache.olingo.client.api.communication.request.streamed.ODataMediaEntityUpdateRequest;
import org.apache.olingo.client.api.communication.response.ODataDeleteResponse;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.api.http.HttpClientException;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.fit.AbstractBaseTestITCase;
import org.apache.olingo.fit.tecsvc.TecSvcConst;
import org.junit.Test;

public final class ConditionalITCase extends AbstractBaseTestITCase {

  private final ODataClient client = getClient();

  private final URI uriEntity = client.newURIBuilder(TecSvcConst.BASE_URI)
      .appendEntitySetSegment("ESCompAllPrim").appendKeySegment(0).build();
  private final URI uriProperty = client.newURIBuilder(uriEntity.toASCIIString())
      .appendPropertySegment("PropertyComp").appendPropertySegment("PropertyDuration").build();
  private final URI uriPropertyValue = client.newURIBuilder(uriProperty.toASCIIString()).appendValueSegment().build();
  private final URI uriMedia = client.newURIBuilder(TecSvcConst.BASE_URI)
      .appendEntitySetSegment("ESMedia").appendKeySegment(1).appendValueSegment().build();

  @Test
  public void readWithWrongIfMatch() throws Exception {
    ODataEntityRequest<ClientEntity> request = client.getRetrieveRequestFactory().getEntityRequest(uriEntity);
    request.setIfMatch("W/\"1\"");
    assertNotNull(request);
    executeAndExpectError(request, HttpStatusCode.PRECONDITION_FAILED);
  }

  @Test
  public void readNotModified() throws Exception {
    ODataEntityRequest<ClientEntity> request = client.getRetrieveRequestFactory().getEntityRequest(uriEntity);
    request.setIfNoneMatch("W/\"0\"");
    assertNotNull(request);

    final ODataRetrieveResponse<ClientEntity> response = request.execute();
    assertEquals(HttpStatusCode.NOT_MODIFIED.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void updateWithoutIfMatch() throws Exception {
    executeAndExpectError(
        client.getCUDRequestFactory().getEntityUpdateRequest(
            uriEntity, UpdateType.PATCH, client.getObjectFactory().newEntity(null)),
        HttpStatusCode.PRECONDITION_REQUIRED);
  }

  @Test
  public void updateWithWrongIfMatch() throws Exception {
    ODataEntityUpdateRequest<ClientEntity> request = client.getCUDRequestFactory().getEntityUpdateRequest(
        uriEntity, UpdateType.PATCH, client.getObjectFactory().newEntity(null));
    request.setIfMatch("W/\"1\"");
    executeAndExpectError(request, HttpStatusCode.PRECONDITION_FAILED);
  }

  @Test
  public void updateMediaWithWrongIfMatch() throws Exception {
    ODataMediaEntityUpdateRequest<ClientEntity> request = client.getCUDRequestFactory().getMediaEntityUpdateRequest(
        uriMedia, IOUtils.toInputStream("ignored"));
    request.setIfMatch("W/\"42\"");

    try {
      request.payloadManager().getResponse();
      fail("Expected Exception not thrown!");
    } catch (final HttpClientException e) {
      final ODataClientErrorException ex = (ODataClientErrorException) e.getCause().getCause();
      assertEquals(HttpStatusCode.PRECONDITION_FAILED.getStatusCode(), ex.getStatusLine().getStatusCode());
      assertThat(ex.getODataError().getMessage(), containsString("condition"));
    }
  }

  @Test
  public void deleteWithWrongIfMatch() throws Exception {
    ODataDeleteRequest request = client.getCUDRequestFactory().getDeleteRequest(uriEntity);
    request.setIfMatch("W/\"1\"");
    executeAndExpectError(request, HttpStatusCode.PRECONDITION_FAILED);
  }

  @Test
  public void deleteMediaWithWrongIfMatch() throws Exception {
    ODataDeleteRequest request = client.getCUDRequestFactory().getDeleteRequest(uriMedia);
    request.setIfMatch("W/\"42\"");
    executeAndExpectError(request, HttpStatusCode.PRECONDITION_FAILED);
  }

  @Test
  public void indirectEntityChange() throws Exception {
    final String eTag = "W/\"0\"";
    ODataDeleteRequest deleteRequest = client.getCUDRequestFactory().getDeleteRequest(uriProperty);
    deleteRequest.setIfMatch(eTag);
    final ODataDeleteResponse response = deleteRequest.execute();

    ODataEntityUpdateRequest<ClientEntity> request = client.getCUDRequestFactory().getEntityUpdateRequest(
        uriEntity, UpdateType.PATCH, client.getObjectFactory().newEntity(null));
    request.setIfMatch(eTag);
    // This request has to be in the same session as the first in order to access the same data provider.
    request.addCustomHeader(HttpHeader.COOKIE, response.getHeader(HttpHeader.SET_COOKIE).iterator().next());
    executeAndExpectError(request, HttpStatusCode.PRECONDITION_FAILED);
  }

  @Test
  public void readPropertyNotModified() throws Exception {
    ODataPropertyRequest<ClientProperty> request = client.getRetrieveRequestFactory().getPropertyRequest(uriProperty);
    request.setIfNoneMatch("W/\"0\"");
    assertEquals(HttpStatusCode.NOT_MODIFIED.getStatusCode(), request.execute().getStatusCode());
  }

  @Test
  public void readPropertyValueNotModified() throws Exception {
    ODataValueRequest request = client.getRetrieveRequestFactory().getPropertyValueRequest(uriPropertyValue);
    request.setIfNoneMatch("W/\"0\"");
    assertEquals(HttpStatusCode.NOT_MODIFIED.getStatusCode(), request.execute().getStatusCode());
  }

  @Test
  public void updatePropertyWithWrongIfMatch() throws Exception {
    ODataPropertyUpdateRequest request = client.getCUDRequestFactory().getPropertyPrimitiveValueUpdateRequest(
        uriProperty,
        client.getObjectFactory().newPrimitiveProperty("PropertyDuration",
            client.getObjectFactory().newPrimitiveValueBuilder().buildString("PT42S")));
    request.setIfMatch("W/\"1\"");
    executeAndExpectError(request, HttpStatusCode.PRECONDITION_FAILED);
  }

  @Test
  public void deletePropertyWithWrongIfMatch() throws Exception {
    ODataDeleteRequest request = client.getCUDRequestFactory().getDeleteRequest(uriProperty);
    request.setIfMatch("W/\"1\"");
    executeAndExpectError(request, HttpStatusCode.PRECONDITION_FAILED);
  }

  @Test
  public void deletePropertyValueWithWrongIfMatch() throws Exception {
    ODataDeleteRequest request = client.getCUDRequestFactory().getDeleteRequest(uriPropertyValue);
    request.setIfMatch("W/\"1\"");
    executeAndExpectError(request, HttpStatusCode.PRECONDITION_FAILED);
  }

  private void executeAndExpectError(ODataBasicRequest<?> request, final HttpStatusCode status) {
    try {
      request.execute();
      fail("Expected Exception not thrown!");
    } catch (final ODataClientErrorException e) {
      assertEquals(status.getStatusCode(), e.getStatusLine().getStatusCode());
      assertThat(e.getODataError().getMessage(), anyOf(containsString("condition"), containsString("match")));
    }
  }

  @Override
  protected ODataClient getClient() {
    ODataClient odata = ODataClientFactory.getClient();
    odata.getConfiguration().setDefaultPubFormat(ODataFormat.JSON);
    return odata;
  }
}
