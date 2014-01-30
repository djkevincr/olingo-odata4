/*******************************************************************************
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
 ******************************************************************************/
package org.apache.olingo.odata4.producer.core.uri.queryoption.expression;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata4.commons.api.edm.EdmEnumType;
import org.apache.olingo.odata4.commons.api.exception.ODataApplicationException;
import org.apache.olingo.odata4.producer.api.uri.queryoption.expression.Enumeration;
import org.apache.olingo.odata4.producer.api.uri.queryoption.expression.ExceptionVisitExpression;
import org.apache.olingo.odata4.producer.api.uri.queryoption.expression.ExpressionVisitor;

public class EnumerationImpl extends ExpressionImpl implements Enumeration {

  private EdmEnumType type;
  private List<String> enumValues = new ArrayList<String>();

  @Override
  public List<String> getEnumValues() {
    return enumValues;
  }

  public EnumerationImpl addEnumValue(final String enumValue) {
    enumValues.add(enumValue);
    return this;
  }

  @Override
  public EdmEnumType getType() {
    return type;
  }

  public EnumerationImpl setType(final EdmEnumType enumtype) {
    type = enumtype;
    return this;
  }

  @Override
  public <T> T accept(final ExpressionVisitor<T> visitor) throws ExceptionVisitExpression, ODataApplicationException {
    return visitor.visitEnum(type, enumValues);
  }

}
