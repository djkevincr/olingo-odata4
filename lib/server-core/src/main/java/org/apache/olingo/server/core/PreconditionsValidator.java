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
package org.apache.olingo.server.core;

import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmFunctionImport;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.server.api.CustomETagSupport;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceSingleton;

public class PreconditionsValidator {

  private final CustomETagSupport customETagSupport;
  private final UriInfo uriInfo;
  private final String ifMatch;
  private final String ifNoneMatch;

  public PreconditionsValidator(CustomETagSupport customETagSupport, UriInfo uriInfo, String ifMatch,
      String ifNoneMatch) {
    this.customETagSupport = customETagSupport;
    this.uriInfo = uriInfo;
    this.ifMatch = ifMatch;
    this.ifNoneMatch = ifNoneMatch;
  }

  public void validatePreconditions(boolean isMediaValue) throws PreconditionRequiredException {
    EdmBindingTarget affectedEntitySetOrSingleton = extractInformation();
    if (affectedEntitySetOrSingleton != null) {
      if ((isMediaValue && customETagSupport.hasMediaETag(affectedEntitySetOrSingleton)) ||
          (!isMediaValue && customETagSupport.hasETag(affectedEntitySetOrSingleton))) {
        checkETagHeaderPresent();
      }
    }
  }

  private void checkETagHeaderPresent() throws PreconditionRequiredException {
    if (ifMatch == null && ifNoneMatch == null) {
      throw new PreconditionRequiredException("Expected an if-match or if-none-match header",
          PreconditionRequiredException.MessageKeys.MISSING_HEADER);
    }
  }

  private EdmBindingTarget extractInformation() throws PreconditionRequiredException {
    EdmBindingTarget lastFoundEntitySetOrSingleton = null;
    int counter = 0;
    for (UriResource uriResourcePart : uriInfo.getUriResourceParts()) {
      switch (uriResourcePart.getKind()) {
      case function:
        lastFoundEntitySetOrSingleton = getEntitySetFromFunctionImport((UriResourceFunction) uriResourcePart);
        break;
      case singleton:
        lastFoundEntitySetOrSingleton = ((UriResourceSingleton) uriResourcePart).getSingleton();
        break;
      case entitySet:
        lastFoundEntitySetOrSingleton = getEntitySet((UriResourceEntitySet) uriResourcePart);
        break;
      case navigationProperty:
        lastFoundEntitySetOrSingleton = getEntitySetFromNavigation(lastFoundEntitySetOrSingleton,
                (UriResourceNavigation) uriResourcePart);
        break;
      case value:
      case action:
        // This should not be possible since the URI Parser validates this but to be sure we throw an exception.
        if (counter != uriInfo.getUriResourceParts().size() - 1) {
          throw new PreconditionRequiredException("$Value or Action must be the last segment in the URI.",
              PreconditionRequiredException.MessageKeys.INVALID_URI);
        }
        break;
      default:
        lastFoundEntitySetOrSingleton = null;
        break;
      }
      if (lastFoundEntitySetOrSingleton == null) {
        // Once we loose track of the entity set there is no way to retrieve it.
        break;
      }
      counter++;
    }
    return lastFoundEntitySetOrSingleton;
  }

  private EdmBindingTarget getEntitySetFromFunctionImport(UriResourceFunction uriResourceFunction) {
    EdmFunctionImport functionImport = uriResourceFunction.getFunctionImport();
    if (functionImport != null && functionImport.getReturnedEntitySet() != null
        && !uriResourceFunction.isCollection()) {
      return functionImport.getReturnedEntitySet();
    }
    return null;
  }

  private EdmBindingTarget getEntitySet(UriResourceEntitySet uriResourceEntitySet) {
    if (!uriResourceEntitySet.isCollection()) {
      return uriResourceEntitySet.getEntitySet();
    }
    return null;
  }

  private EdmBindingTarget getEntitySetFromNavigation(EdmBindingTarget lastFoundEntitySetOrSingleton,
                                                      UriResourceNavigation uriResourceNavigation) {
    if (lastFoundEntitySetOrSingleton != null && !uriResourceNavigation.isCollection()) {
      EdmNavigationProperty navProp = uriResourceNavigation.getProperty();
      return lastFoundEntitySetOrSingleton.getRelatedBindingTarget(navProp.getName());
    }
    return null;
  }
}
