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
package org.apache.olingo.client.core.v4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

import org.apache.olingo.client.api.EdmEnabledODataClient;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.core.AbstractTest;
import org.apache.olingo.client.core.EdmEnabledODataClientImpl;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.ResWrap;
import org.apache.olingo.client.api.domain.ClientAnnotation;
import org.apache.olingo.client.api.domain.ClientComplexValue;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientInlineEntitySet;
import org.apache.olingo.client.api.domain.ClientLink;
import org.apache.olingo.client.api.domain.ClientLinkType;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.api.domain.ClientValuable;
import org.apache.olingo.client.api.domain.ClientValue;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDuration;
import org.junit.Ignore;
import org.junit.Test;

public class EntityTest extends AbstractTest {

  @Override
  protected ODataClient getClient() {
    return v4Client;
  }

  private EdmEnabledODataClient getEdmEnabledClient() {
    return new EdmEnabledODataClientImpl(null, null, null) {

      private Edm edm;

      @Override
      public Edm getEdm(final String metadataETag) {
        return getCachedEdm();
      }

      @Override
      public Edm getCachedEdm() {
        if (edm == null) {
          edm = getReader().readMetadata(getClass().getResourceAsStream("staticservice-metadata.xml"),ODataFormat.XML);
        }
        return edm;
      }

    };
  }

  private void singleton(final ODataFormat format) throws Exception {
    final InputStream input = getClass().getResourceAsStream("VipCustomer." + getSuffix(format));
    final ClientEntity entity = getClient().getBinder().getODataEntity(
        getClient().getDeserializer(format).toEntity(input));
    assertNotNull(entity);

    assertEquals("Microsoft.Test.OData.Services.ODataWCFService.Customer", entity.getTypeName().toString());

    final ClientProperty birthday = entity.getProperty("Birthday");
    assertTrue(birthday.hasPrimitiveValue());
    assertEquals(EdmDateTimeOffset.getInstance(), birthday.getPrimitiveValue().getType());

    final ClientProperty timeBetweenLastTwoOrders = entity.getProperty("TimeBetweenLastTwoOrders");
    assertTrue(timeBetweenLastTwoOrders.hasPrimitiveValue());
    assertEquals(EdmDuration.getInstance(), timeBetweenLastTwoOrders.getPrimitiveValue().getType());

    int checked = 0;
    for (ClientLink link : entity.getNavigationLinks()) {
      if ("Parent".equals(link.getName())) {
        checked++;
        assertEquals(ClientLinkType.ENTITY_NAVIGATION, link.getType());
      }
      if ("Orders".equals(link.getName())) {
        checked++;
        if (format == ODataFormat.ATOM) {
          assertEquals(ClientLinkType.ENTITY_SET_NAVIGATION, link.getType());
        }
      }
      if ("Company".equals(link.getName())) {
        checked++;
        assertEquals(ClientLinkType.ENTITY_NAVIGATION, link.getType());
      }
    }
    assertEquals(3, checked);

    assertEquals(2, entity.getOperations().size());
    assertEquals("#Microsoft.Test.OData.Services.ODataWCFService.ResetAddress",
        entity.getOperation("Microsoft.Test.OData.Services.ODataWCFService.ResetAddress").getMetadataAnchor());
    assertEquals("#Microsoft.Test.OData.Services.ODataWCFService.GetHomeAddress",
        entity.getOperation("Microsoft.Test.OData.Services.ODataWCFService.GetHomeAddress").getMetadataAnchor());

    // operations won't get serialized
    entity.getOperations().clear();
    final ClientEntity written = getClient().getBinder().getODataEntity(
        new ResWrap<Entity>((URI) null, null, getClient().getBinder().getEntity(entity)));
    assertEquals(entity, written);
    input.close();
  }

  @Test
  public void atomSingleton() throws Exception {
    singleton(ODataFormat.ATOM);
  }

  @Test
  public void jsonSingleton() throws Exception {
    singleton(ODataFormat.JSON_FULL_METADATA);
  }

  private void withEnums(final ODataFormat format) throws Exception {
    final InputStream input = getClass().getResourceAsStream("Products_5." + getSuffix(format));
    final ClientEntity entity = getClient().getBinder().getODataEntity(
        getClient().getDeserializer(format).toEntity(input));
    assertNotNull(entity);

    final ClientProperty skinColor = entity.getProperty("SkinColor");
    assertTrue(skinColor.hasEnumValue());
    assertEquals("Microsoft.Test.OData.Services.ODataWCFService.Color", skinColor.getEnumValue().getTypeName());
    assertEquals("Red", skinColor.getEnumValue().getValue());

    final ClientProperty coverColors = entity.getProperty("CoverColors");
    assertTrue(coverColors.hasCollectionValue());
    for (final Iterator<ClientValue> itor = coverColors.getCollectionValue().iterator(); itor.hasNext();) {
      final ClientValue item = itor.next();
      assertTrue(item.isEnum());
    }

    // operations won't get serialized
    entity.getOperations().clear();
    final ClientEntity written = getClient().getBinder().getODataEntity(
        new ResWrap<Entity>((URI) null, null, getClient().getBinder().getEntity(entity)));
    assertEquals(entity, written);
    input.close();
  }

  @Test
  public void atomWithEnums() throws Exception {
    withEnums(ODataFormat.ATOM);
  }

  @Test
  public void jsonWithEnums() throws Exception {
    withEnums(ODataFormat.JSON_FULL_METADATA);
  }

  private void withInlineEntitySet(final ODataFormat format) throws Exception {
    final InputStream input = getClass().getResourceAsStream(
        "Accounts_101_expand_MyPaymentInstruments." + getSuffix(format));
    final ClientEntity entity = getClient().getBinder().getODataEntity(
        getClient().getDeserializer(format).toEntity(input));
    assertNotNull(entity);

    final ClientLink instruments = entity.getNavigationLink("MyPaymentInstruments");
    assertNotNull(instruments);
    assertEquals(ClientLinkType.ENTITY_SET_NAVIGATION, instruments.getType());

    final ClientInlineEntitySet inline = instruments.asInlineEntitySet();
    assertNotNull(inline);
    assertEquals(3, inline.getEntitySet().getEntities().size());

    // count shouldn't be serialized
    inline.getEntitySet().setCount(3);
    // operations won't get serialized
    entity.getOperations().clear();
    final ClientEntity written = getClient().getBinder().getODataEntity(
        new ResWrap<Entity>((URI) null, null, getClient().getBinder().getEntity(entity)));
    assertEquals(entity, written);
    input.close();
  }

  @Test
  public void atomWithInlineEntitySet() throws Exception {
    withInlineEntitySet(ODataFormat.ATOM);
  }

  @Test
  public void jsonWithInlineEntitySet() throws Exception {
    withInlineEntitySet(ODataFormat.JSON_FULL_METADATA);
  }

  private void mediaEntity(final ODataFormat format) throws Exception {
    final InputStream input = getClass().getResourceAsStream(
        "Advertisements_f89dee73-af9f-4cd4-b330-db93c25ff3c7." + getSuffix(format));
    final ClientEntity entity = getClient().getBinder().getODataEntity(
        getClient().getDeserializer(format).toEntity(input));
    assertNotNull(entity);

    assertTrue(entity.isMediaEntity());
    assertNotNull(entity.getMediaContentSource());
    assertEquals("\"8zOOKKvgOtptr4gt8IrnapX3jds=\"", entity.getMediaETag());

    final ClientEntity written = getClient().getBinder().getODataEntity(
        new ResWrap<Entity>((URI) null, null, getClient().getBinder().getEntity(entity)));
    assertEquals(entity, written);
    input.close();
  }

  @Test
  public void atomMediaEntity() throws Exception {
    mediaEntity(ODataFormat.ATOM);
  }

  @Test
  public void jsonMediaEntity() throws Exception {
    mediaEntity(ODataFormat.JSON_FULL_METADATA);
  }

  private void withStream(final ODataFormat format) throws Exception {
    final InputStream input = getClass().getResourceAsStream("PersonDetails_1." + getSuffix(format));
    final ClientEntity entity = getClient().getBinder().getODataEntity(
        getClient().getDeserializer(format).toEntity(input));
    assertNotNull(entity);

    assertFalse(entity.isMediaEntity());

    final ClientLink editMedia = entity.getMediaEditLink("Photo");
    assertNotNull(editMedia);

    final ClientEntity written = getClient().getBinder().getODataEntity(
        new ResWrap<Entity>((URI) null, null, getClient().getBinder().getEntity(entity)));
    assertEquals(entity, written);
    input.close();
  }

  @Test
  public void atomWithStream() throws Exception {
    withStream(ODataFormat.ATOM);
  }

  @Test
  public void jsonWithStream() throws Exception {
    withStream(ODataFormat.JSON_FULL_METADATA);
  }

  private void ref(final ODataFormat format) throws Exception {
    final InputStream input = getClass().getResourceAsStream("entityReference." + getSuffix(format));
    final ClientEntity entity = getClient().getBinder().getODataEntity(
        getClient().getDeserializer(format).toEntity(input));
    assertNotNull(entity);

    assertNotNull(entity.getId());

    final ClientEntity written = getClient().getBinder().getODataEntity(
        new ResWrap<Entity>((URI) null, null, getClient().getBinder().getEntity(entity)));
    assertEquals(entity, written);
    input.close();
  }

  @Test
  public void atomRef() throws Exception {
    ref(ODataFormat.ATOM);
  }

  @Test
  public void jsonRef() throws Exception {
    ref(ODataFormat.JSON);
  }

  private void complexNavigationProperties(final ODataFormat format) throws Exception {
    final InputStream input = getClass().getResourceAsStream("entity.withcomplexnavigation." + getSuffix(format));
    final ClientEntity entity = getClient().getBinder().getODataEntity(
        getClient().getDeserializer(format).toEntity(input));
    assertNotNull(entity);

    final ClientComplexValue addressValue = entity.getProperty("Address").getComplexValue();
    assertNotNull(addressValue);
    assertNotNull(addressValue.getNavigationLink("Country"));

    // ETag is not serialized
    entity.setETag(null);
    final ClientEntity written = getClient().getBinder().getODataEntity(
        new ResWrap<Entity>((URI) null, null, getClient().getBinder().getEntity(entity)));
    assertEquals(entity, written);
    input.close();
  }

  @Test
  public void atomComplexNavigationProperties() throws Exception {
    complexNavigationProperties(ODataFormat.ATOM);
  }

  @Test
  public void jsonComplexNavigationProperties() throws Exception {
    complexNavigationProperties(ODataFormat.JSON);
  }

  private void annotated(final ODataFormat format) throws EdmPrimitiveTypeException, Exception {
    final InputStream input = getClass().getResourceAsStream("annotated." + getSuffix(format));
    final ClientEntity entity = getClient().getBinder().getODataEntity(
        getClient().getDeserializer(format).toEntity(input));
    assertNotNull(entity);

    assertFalse(entity.getAnnotations().isEmpty());

    ClientAnnotation annotation = entity.getAnnotations().get(0);
    assertEquals("com.contoso.display.highlight", annotation.getTerm());
    assertEquals(true, annotation.getPrimitiveValue().toCastValue(Boolean.class));

    annotation = entity.getAnnotations().get(1);
    assertEquals("com.contoso.PersonalInfo.PhoneNumbers", annotation.getTerm());
    assertTrue(annotation.hasCollectionValue());

    annotation = entity.getProperty("LastName").getAnnotations().get(0);
    assertEquals("com.contoso.display.style", annotation.getTerm());
    assertTrue(annotation.hasComplexValue());

    final ClientLink orders = entity.getNavigationLink("Orders");
    assertFalse(orders.getAnnotations().isEmpty());

    annotation = orders.getAnnotations().get(0);
    assertEquals("com.contoso.display.style", annotation.getTerm());
    assertEquals("com.contoso.display.styleType", annotation.getValue().getTypeName());
    assertTrue(annotation.hasComplexValue());
    assertEquals(2,
        annotation.getValue().asComplex().get("order").getPrimitiveValue().toCastValue(Integer.class), 0);

    final ClientEntity written = getClient().getBinder().getODataEntity(
        new ResWrap<Entity>((URI) null, null, getClient().getBinder().getEntity(entity)));
    assertEquals(entity, written);
    input.close();
  }

  @Test
  @Ignore
  public void atomAnnotated() throws Exception {
    annotated(ODataFormat.ATOM);
  }

  @Test
  @Ignore
  public void jsonAnnotated() throws Exception {
    annotated(ODataFormat.JSON);
  }

  private void derived(final ODataClient client, final ODataFormat format) throws Exception {
    final InputStream input = getClass().getResourceAsStream("Customer." + getSuffix(format));
    final ClientEntity entity = client.getBinder().getODataEntity(client.getDeserializer(format).toEntity(input));
    assertNotNull(entity);

    assertEquals("Microsoft.Test.OData.Services.ODataWCFService.Customer", entity.getTypeName().toString());
    assertEquals("Microsoft.Test.OData.Services.ODataWCFService.CompanyAddress",
        ((ClientValuable) entity.getProperty("HomeAddress")).getValue().getTypeName());
    input.close();
  }

  @Test
  public void derivedFromAtom() throws Exception {
    derived(getClient(), ODataFormat.ATOM);
  }

  @Test
  public void derivedFromJSON() throws Exception {
    derived(getEdmEnabledClient(), ODataFormat.JSON);
  }

  @Test
  public void derivedFromFullJSON() throws Exception {
    derived(getClient(), ODataFormat.JSON_FULL_METADATA);
  }
}
