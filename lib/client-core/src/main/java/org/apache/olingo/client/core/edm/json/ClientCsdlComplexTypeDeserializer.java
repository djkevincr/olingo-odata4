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
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;

import java.io.IOException;
import java.util.Iterator;

public class ClientCsdlComplexTypeDeserializer extends JsonDeserializer<CsdlComplexType> {

    private static final String DEFAULT_SCHEMA = "http://docs.oasis-open.org/odata/odata-json-csdl/v4.0/edm.json#";
    private static final String CONSTANT_DEFINITION_REFERENCE = DEFAULT_SCHEMA + "/definitions/";

    private String typeName;
    private String nameSpace;

    public ClientCsdlComplexTypeDeserializer(String nameSpace, String typeName) {
        this.nameSpace = nameSpace;
        this.typeName = typeName;
    }

    @Override
    public CsdlComplexType deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws IOException {
        final ObjectNode tree = parser.getCodec().readTree(parser);
        CsdlComplexType type = new CsdlComplexType();
        type.setName(typeName);

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

        if(tree.has("properties")){
            //toDo add properties deserialization here
        }
        return type;
    }
}