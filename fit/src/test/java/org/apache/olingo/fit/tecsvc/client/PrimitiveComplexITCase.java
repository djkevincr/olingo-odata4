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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.cud.ODataDeleteRequest;
import org.apache.olingo.client.api.communication.request.cud.ODataPropertyUpdateRequest;
import org.apache.olingo.client.api.communication.request.cud.UpdateType;
import org.apache.olingo.client.api.communication.request.retrieve.ODataPropertyRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataValueRequest;
import org.apache.olingo.client.api.communication.response.ODataDeleteResponse;
import org.apache.olingo.client.api.communication.response.ODataPropertyUpdateResponse;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientCollectionValue;
import org.apache.olingo.client.api.domain.ClientComplexValue;
import org.apache.olingo.client.api.domain.ClientPrimitiveValue;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.api.domain.ClientValue;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.fit.AbstractBaseTestITCase;
import org.apache.olingo.fit.tecsvc.TecSvcConst;
import org.junit.Test;

public class PrimitiveComplexITCase extends AbstractBaseTestITCase {

  private static final String SERVICE_URI = TecSvcConst.BASE_URI;

  @Test
  public void readSimpleProperty() throws Exception {
    final ODataPropertyRequest<ClientProperty> request = getClient().getRetrieveRequestFactory()
        .getPropertyRequest(getClient().newURIBuilder(SERVICE_URI)
            .appendEntitySetSegment("ESTwoPrim")
            .appendKeySegment(32766)
            .appendPropertySegment("PropertyString")
            .build());
    assertNotNull(request);

    ODataRetrieveResponse<ClientProperty> response = request.execute();
    assertEquals(HttpStatusCode.OK.getStatusCode(), response.getStatusCode());
    assertThat(response.getContentType(), containsString(ContentType.APPLICATION_JSON.toContentTypeString()));

    final ClientProperty property = response.getBody();
    assertNotNull(property);
    assertNotNull(property.getPrimitiveValue());
    assertEquals("Test String1", property.getPrimitiveValue().toValue());
  }

  @Test
  public void readSimplePropertyContextURL() throws Exception {
    ODataPropertyRequest<ClientProperty> request = getClient().getRetrieveRequestFactory()
        .getPropertyRequest(getClient().newURIBuilder(SERVICE_URI)
            .appendEntitySetSegment("ESTwoPrim")
            .appendKeySegment(32766)
            .appendPropertySegment("PropertyString")
            .build());
    ODataRetrieveResponse<ClientProperty> response = request.execute();
    String expectedResult =
        "{\"@odata.context\":\"$metadata#ESTwoPrim(32766)/PropertyString\"," +
            "\"value\":\"Test String1\"}";
    assertEquals(expectedResult, IOUtils.toString(response.getRawResponse(), "UTF-8"));
  }

  @Test
  public void deletePrimitive() throws Exception {
    final URI uri = getClient().newURIBuilder(SERVICE_URI)
        .appendEntitySetSegment("ESTwoPrim").appendKeySegment(32766).appendPropertySegment("PropertyString")
        .build();
    final ODataDeleteRequest request = getClient().getCUDRequestFactory().getDeleteRequest(uri);
    final ODataDeleteResponse response = request.execute();
    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), response.getStatusCode());

    // Check that the property is really gone.
    // This check has to be in the same session in order to access the same data provider.
    ODataPropertyRequest<ClientProperty> propertyRequest = getClient().getRetrieveRequestFactory()
        .getPropertyRequest(uri);
    propertyRequest.addCustomHeader(HttpHeader.COOKIE, response.getHeader(HttpHeader.SET_COOKIE).iterator().next());
    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), propertyRequest.execute().getStatusCode());

    try {
      getClient().getCUDRequestFactory().getDeleteRequest(getClient().newURIBuilder(SERVICE_URI)
          .appendEntitySetSegment("ESTwoPrim").appendKeySegment(32766).appendPropertySegment("PropertyInt16")
          .build()).execute();
      fail("Expected exception not thrown!");
    } catch (final ODataClientErrorException e) {
      assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), e.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void deletePrimitiveCollection() throws Exception {
    final URI uri = getClient().newURIBuilder(SERVICE_URI)
        .appendEntitySetSegment("ESMixPrimCollComp").appendKeySegment(7).appendPropertySegment("CollPropertyString")
        .build();
    final ODataDeleteResponse response = getClient().getCUDRequestFactory().getDeleteRequest(uri).execute();
    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), response.getStatusCode());

    // Check that the property is not gone but empty now.
    // This check has to be in the same session in order to access the same data provider.
    ODataPropertyRequest<ClientProperty> request = getClient().getRetrieveRequestFactory()
        .getPropertyRequest(uri);
    request.addCustomHeader(HttpHeader.COOKIE, response.getHeader(HttpHeader.SET_COOKIE).iterator().next());
    final ODataRetrieveResponse<ClientProperty> propertyResponse = request.execute();
    assertEquals(HttpStatusCode.OK.getStatusCode(), propertyResponse.getStatusCode());
    final ClientProperty property = propertyResponse.getBody();
    assertNotNull(property);
    assertNotNull(property.getCollectionValue());
    assertTrue(property.getCollectionValue().isEmpty());
  }

  @Test
  public void readComplexProperty() throws Exception {
    final ODataPropertyRequest<ClientProperty> request = getClient().getRetrieveRequestFactory()
        .getPropertyRequest(getClient().newURIBuilder(SERVICE_URI)
            .appendEntitySetSegment("ESMixPrimCollComp")
            .appendKeySegment(7)
            .appendPropertySegment("PropertyComp")
            .build());
    ODataRetrieveResponse<ClientProperty> response = request.execute();
    assertEquals(HttpStatusCode.OK.getStatusCode(), response.getStatusCode());
    assertThat(response.getContentType(), containsString(ContentType.APPLICATION_JSON.toContentTypeString()));

    final ClientProperty property = response.getBody();
    assertNotNull(property);
    assertNotNull(property.getComplexValue());
    assertEquals("TEST B", property.getComplexValue().get("PropertyString").getPrimitiveValue().toValue());
  }

  @Test
  public void readComplexPropertyContextURL() throws Exception {
    final ODataPropertyRequest<ClientProperty> request = getClient().getRetrieveRequestFactory()
        .getPropertyRequest(getClient().newURIBuilder(SERVICE_URI)
            .appendEntitySetSegment("ESMixPrimCollComp")
            .appendKeySegment(7)
            .appendPropertySegment("PropertyComp")
            .build());
    ODataRetrieveResponse<ClientProperty> response = request.execute();
    String expectedResult =
        "{\"@odata.context\":\"$metadata#ESMixPrimCollComp(7)/PropertyComp\"," +
            "\"PropertyInt16\":222,\"PropertyString\":\"TEST B\"}";
    assertEquals(expectedResult, IOUtils.toString(response.getRawResponse(), "UTF-8"));
  }

  @Test
  public void deleteComplex() throws Exception {
    final URI uri = getClient().newURIBuilder(SERVICE_URI)
        .appendEntitySetSegment("ESMixPrimCollComp").appendKeySegment(7).appendPropertySegment("PropertyComp")
        .build();
    final ODataDeleteRequest request = getClient().getCUDRequestFactory().getDeleteRequest(uri);
    final ODataDeleteResponse response = request.execute();
    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), response.getStatusCode());

    // Check that the property is really gone.
    // This check has to be in the same session in order to access the same data provider.
    ODataPropertyRequest<ClientProperty> propertyRequest = getClient().getRetrieveRequestFactory()
        .getPropertyRequest(uri);
    propertyRequest.addCustomHeader(HttpHeader.COOKIE, response.getHeader(HttpHeader.SET_COOKIE).iterator().next());
    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), propertyRequest.execute().getStatusCode());
  }

  @Test
  public void readUnknownProperty() throws Exception {
    final ODataPropertyRequest<ClientProperty> request = getClient().getRetrieveRequestFactory()
        .getPropertyRequest(getClient().newURIBuilder(SERVICE_URI)
            .appendEntitySetSegment("ESTwoPrim")
            .appendKeySegment(32766)
            .appendPropertySegment("Unknown")
            .build());
    try {
      request.execute();
      fail("Expected exception not thrown!");
    } catch (final ODataClientErrorException e) {
      assertEquals(HttpStatusCode.NOT_FOUND.getStatusCode(), e.getStatusLine().getStatusCode());
    }
  }

  @Test
  public void readNoContentProperty() throws Exception {
    final ODataPropertyRequest<ClientProperty> request = getClient().getRetrieveRequestFactory()
        .getPropertyRequest(getClient().newURIBuilder(SERVICE_URI)
            .appendEntitySetSegment("ESTwoPrim")
            .appendKeySegment(-32766)
            .appendPropertySegment("PropertyString")
            .build());
    final ODataRetrieveResponse<ClientProperty> response = request.execute();
    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), response.getStatusCode());
  }

  @Test
  public void updatePrimitiveProperty() throws Exception {
    final ODataPropertyUpdateRequest request =
        getClient().getCUDRequestFactory().getPropertyPrimitiveValueUpdateRequest(
            getClient().newURIBuilder(SERVICE_URI)
                .appendEntitySetSegment("ESTwoPrim").appendKeySegment(32766)
                .appendPropertySegment("PropertyString")
                .build(),
            getClient().getObjectFactory().newPrimitiveProperty("PropertyString",
                getClient().getObjectFactory().newPrimitiveValueBuilder().buildString("Test String1")));
    assertNotNull(request);

    final ODataPropertyUpdateResponse response = request.execute();
    assertEquals(HttpStatusCode.OK.getStatusCode(), response.getStatusCode());
    assertThat(response.getContentType(), containsString(ContentType.APPLICATION_JSON.toContentTypeString()));

    final ClientProperty property = response.getBody();
    assertNotNull(property);
    assertNotNull(property.getPrimitiveValue());
    assertEquals("Test String1", property.getPrimitiveValue().toValue());
  }

  @Test
  public void patchComplexProperty() throws Exception {
    final ODataPropertyUpdateRequest request =
        getClient().getCUDRequestFactory().getPropertyComplexValueUpdateRequest(
            getClient().newURIBuilder(SERVICE_URI)
                .appendEntitySetSegment("ESMixPrimCollComp").appendKeySegment(7)
                .appendPropertySegment("PropertyComp")
                .build(),
            UpdateType.PATCH,
            getClient().getObjectFactory().newComplexProperty("PropertyComp",
                getClient().getObjectFactory().newComplexValue(null).add(
                    getClient().getObjectFactory().newPrimitiveProperty("PropertyString",
                        getClient().getObjectFactory().newPrimitiveValueBuilder().buildString("Test String42")))));
    assertNotNull(request);

    final ODataPropertyUpdateResponse response = request.execute();
    assertEquals(HttpStatusCode.OK.getStatusCode(), response.getStatusCode());

    final ClientProperty property = response.getBody();
    assertNotNull(property);
    assertNotNull(property.getComplexValue());
    final ClientComplexValue value = property.getComplexValue();
    assertEquals("Test String42", value.get("PropertyString").getPrimitiveValue().toValue());
    assertEquals(222, value.get("PropertyInt16").getPrimitiveValue().toValue());
  }

  @Test
  public void updatePrimitiveCollection() throws Exception {
    final ODataPropertyUpdateRequest request =
        getClient().getCUDRequestFactory().getPropertyCollectionValueUpdateRequest(
            getClient().newURIBuilder(SERVICE_URI)
                .appendEntitySetSegment("ESMixPrimCollComp").appendKeySegment(7)
                .appendPropertySegment("CollPropertyString")
                .build(),
            getClient().getObjectFactory().newCollectionProperty("CollPropertyString",
                getClient().getObjectFactory().newCollectionValue(null)
                    .add(getClient().getObjectFactory().newPrimitiveValueBuilder().buildString("Test String1"))
                    .add(getClient().getObjectFactory().newPrimitiveValueBuilder().buildString("Test String2"))));
    assertNotNull(request);

    final ODataPropertyUpdateResponse response = request.execute();
    assertEquals(HttpStatusCode.OK.getStatusCode(), response.getStatusCode());

    final ClientProperty property = response.getBody();
    assertNotNull(property);
    final ClientCollectionValue<ClientValue> value = property.getCollectionValue();
    assertNotNull(value);
    Iterator<ClientValue> iterator = value.iterator();
    assertTrue(iterator.hasNext());
    assertEquals("Test String1", iterator.next().asPrimitive().toValue());
    assertEquals("Test String2", iterator.next().asPrimitive().toValue());
    assertFalse(iterator.hasNext());
  }

  @Test
  public void updateComplexCollection() throws Exception {
    final ODataPropertyUpdateRequest request =
        getClient().getCUDRequestFactory().getPropertyCollectionValueUpdateRequest(
            getClient().newURIBuilder(SERVICE_URI)
                .appendEntitySetSegment("ESMixPrimCollComp").appendKeySegment(7)
                .appendPropertySegment("CollPropertyComp")
                .build(),
            getClient().getObjectFactory().newCollectionProperty("CollPropertyComp",
                getClient().getObjectFactory().newCollectionValue(null)));
    assertNotNull(request);

    final ODataPropertyUpdateResponse response = request.execute();
    assertEquals(HttpStatusCode.OK.getStatusCode(), response.getStatusCode());

    final ClientProperty property = response.getBody();
    assertNotNull(property);
    final ClientCollectionValue<ClientValue> value = property.getCollectionValue();
    assertNotNull(value);
    assertFalse(value.iterator().hasNext());
  }

  @Test
  public void readPropertyValue() throws Exception {
    final ODataValueRequest request = getClient().getRetrieveRequestFactory()
        .getPropertyValueRequest(getClient().newURIBuilder(SERVICE_URI)
            .appendEntitySetSegment("ESTwoPrim")
            .appendKeySegment(32766)
            .appendPropertySegment("PropertyString")
            .appendValueSegment()
            .build());
    final ODataRetrieveResponse<ClientPrimitiveValue> response = request.execute();
    assertEquals("Test String1", response.getBody().toValue());
  }

  @Override
  protected ODataClient getClient() {
    ODataClient odata = ODataClientFactory.getClient();
    odata.getConfiguration().setDefaultPubFormat(ODataFormat.JSON);
    return odata;
  }
}
