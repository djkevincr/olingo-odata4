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
package org.apache.olingo.fit.proxy.v4.demo.odatademo.types;

// CHECKSTYLE:OFF (Maven checkstyle)
import org.apache.olingo.ext.proxy.api.annotations.Key;

// CHECKSTYLE:ON (Maven checkstyle)

@org.apache.olingo.ext.proxy.api.annotations.Namespace("ODataDemo")
@org.apache.olingo.ext.proxy.api.annotations.EntityType(name = "Supplier",
    openType = false,
    hasStream = false,
    isAbstract = false)
public interface Supplier
    extends org.apache.olingo.ext.proxy.api.Annotatable,
    org.apache.olingo.ext.proxy.api.EntityType<Supplier>, org.apache.olingo.ext.proxy.api.StructuredQuery<Supplier> {

  @Key
  @org.apache.olingo.ext.proxy.api.annotations.Property(name = "ID",
      type = "Edm.Int32",
      nullable = false,
      defaultValue = "",
      maxLenght = Integer.MAX_VALUE,
      fixedLenght = false,
      precision = 0,
      scale = 0,
      unicode = true,
      collation = "",
      srid = "")
  java.lang.Integer getID();

  void setID(java.lang.Integer _iD);

  @org.apache.olingo.ext.proxy.api.annotations.Property(name = "Name",
      type = "Edm.String",
      nullable = true,
      defaultValue = "",
      maxLenght = Integer.MAX_VALUE,
      fixedLenght = false,
      precision = 0,
      scale = 0,
      unicode = true,
      collation = "",
      srid = "")
  java.lang.String getName();

  void setName(java.lang.String _name);

  @org.apache.olingo.ext.proxy.api.annotations.Property(name = "Address",
      type = "ODataDemo.Address",
      nullable = true,
      defaultValue = "",
      maxLenght = Integer.MAX_VALUE,
      fixedLenght = false,
      precision = 0,
      scale = 0,
      unicode = true,
      collation = "",
      srid = "")
  org.apache.olingo.fit.proxy.v4.demo.odatademo.types.Address getAddress();

  void setAddress(org.apache.olingo.fit.proxy.v4.demo.odatademo.types.Address _address);

  @org.apache.olingo.ext.proxy.api.annotations.Property(name = "Location",
      type = "Edm.GeographyPoint",
      nullable = true,
      defaultValue = "",
      maxLenght = Integer.MAX_VALUE,
      fixedLenght = false,
      precision = 0,
      scale = 0,
      unicode = true,
      collation = "",
      srid = "")
  org.apache.olingo.commons.api.edm.geo.Point getLocation();

  void setLocation(org.apache.olingo.commons.api.edm.geo.Point _location);

  @org.apache.olingo.ext.proxy.api.annotations.Property(name = "Concurrency",
      type = "Edm.Int32",
      nullable = false,
      defaultValue = "",
      maxLenght = Integer.MAX_VALUE,
      fixedLenght = false,
      precision = 0,
      scale = 0,
      unicode = true,
      collation = "",
      srid = "")
  java.lang.Integer getConcurrency();

  void setConcurrency(java.lang.Integer _concurrency);

  @org.apache.olingo.ext.proxy.api.annotations.NavigationProperty(name = "Products",
      type = "ODataDemo.Product",
      targetSchema = "ODataDemo",
      targetContainer = "DemoService",
      targetEntitySet = "Products",
      containsTarget = false)
  org.apache.olingo.fit.proxy.v4.demo.odatademo.types.ProductCollection getProducts();

  void setProducts(org.apache.olingo.fit.proxy.v4.demo.odatademo.types.ProductCollection _products);

  Operations operations();

  interface Operations extends org.apache.olingo.ext.proxy.api.Operations {
    // No additional methods needed for now.
  }

  Annotations annotations();

  interface Annotations {

    @org.apache.olingo.ext.proxy.api.annotations.AnnotationsForProperty(name = "ID",
        type = "Edm.Int32")
    org.apache.olingo.ext.proxy.api.Annotatable getIDAnnotations();

    @org.apache.olingo.ext.proxy.api.annotations.AnnotationsForProperty(name = "Name",
        type = "Edm.String")
    org.apache.olingo.ext.proxy.api.Annotatable getNameAnnotations();

    @org.apache.olingo.ext.proxy.api.annotations.AnnotationsForProperty(name = "Address",
        type = "ODataDemo.Address")
    org.apache.olingo.ext.proxy.api.Annotatable getAddressAnnotations();

    @org.apache.olingo.ext.proxy.api.annotations.AnnotationsForProperty(name = "Location",
        type = "Edm.GeographyPoint")
    org.apache.olingo.ext.proxy.api.Annotatable getLocationAnnotations();

    @org.apache.olingo.ext.proxy.api.annotations.AnnotationsForProperty(name = "Concurrency",
        type = "Edm.Int32")
    org.apache.olingo.ext.proxy.api.Annotatable getConcurrencyAnnotations();

    @org.apache.olingo.ext.proxy.api.annotations.AnnotationsForNavigationProperty(name = "Products",
        type = "ODataDemo.Product")
    org.apache.olingo.ext.proxy.api.Annotatable getProductsAnnotations();
  }

}
