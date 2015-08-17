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

package org.apache.olingo.client.core.edm.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClientCsdlEntityTypeDeserializer extends JsonDeserializer<CsdlEntityType> {

    private static final String DEFAULT_SCHEMA="http://docs.oasis-open.org/odata/odata-json-csdl/v4.0/edm.json#";
    private static final String CONSTANT_DEFINITION_REFERENCE=DEFAULT_SCHEMA+"/definitions/";

    private String typeName;
    private String nameSpace;

    public ClientCsdlEntityTypeDeserializer(String nameSpace,String typeName){
        this.nameSpace = nameSpace;
        this.typeName = typeName;
    }

    @Override
    public CsdlEntityType deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws IOException {
        final ObjectNode tree = parser.getCodec().readTree(parser);
        CsdlEntityType type = new CsdlEntityType();
        type.setName(typeName);
        if (tree.has("keys")) {
            Iterator<JsonNode> itr = tree.get("keys").elements();
            List<CsdlPropertyRef> keys = new ArrayList<CsdlPropertyRef>();
            while (itr.hasNext()) {
                JsonNode key = itr.next();
                CsdlPropertyRef propRef = new CsdlPropertyRef();
                propRef.setName(key.get("name").asText());
                if (key.has("alias")) {
                    propRef.setAlias(key.get("alias").asText());
                }
                keys.add(propRef);
            }
            type.setKey(keys);
        }

        if (tree.has("allOf")) {
            Iterator<JsonNode> itr = tree.get("allOf").elements();
            JsonNode baseTypeNode = itr.next();
            if (baseTypeNode != null) {
                if (baseTypeNode.has("$ref")) {
                    String fqnAsString = baseTypeNode.get("$ref").asText().replace(CONSTANT_DEFINITION_REFERENCE, "");
                    fqnAsString = fqnAsString.trim();
                    type.setBaseType(new FullQualifiedName(fqnAsString));
                }
            }
        }

        if (tree.has("abstract")) {
            type.setAbstract(tree.get("abstract").asBoolean());
        }
        if (tree.has("hasStream")) {
            type.setHasStream(tree.get("hasStream").asBoolean());
        }
        if (tree.has("openType")) {
            type.setOpenType(tree.get("openType").asBoolean());
        }

        if (tree.has("properties")) {
            Iterator<Map.Entry<String, JsonNode>> iterator = tree.get("properties").fields();
            ArrayList<CsdlNavigationProperty> navigationProperties = new ArrayList<CsdlNavigationProperty>();
            ArrayList<CsdlProperty> properties = new ArrayList<CsdlProperty>();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                if (entry.getValue().has("relationship")) {
                    final CsdlNavigationProperty property = new ClientCsdlNavigationPropertyDeserializer(entry.getKey())
                            .deserialize(tree.get("properties").get(entry.getKey()).traverse(parser.getCodec()), ctxt);
                    navigationProperties.add(property);
                } else {
                    final CsdlProperty property = new ClientCsdlPropertyDeserializer(entry.getKey())
                            .deserialize(tree.get("properties").get(entry.getKey()).traverse(parser.getCodec()), ctxt);
                    properties.add(property);

                }
            }
            type.setNavigationProperties(navigationProperties);
            type.setProperties(properties);
        }
        return type;
    }
}
