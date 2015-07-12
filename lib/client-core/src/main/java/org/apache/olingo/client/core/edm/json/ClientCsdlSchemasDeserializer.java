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
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


public class ClientCsdlSchemasDeserializer extends JsonDeserializer<ClientJsonSchemaCsdl> {
    private ClientJsonSchemaCsdl jsonCsdl;

    public ClientCsdlSchemasDeserializer(ClientJsonSchemaCsdl jsonCsdl){
        this.jsonCsdl=jsonCsdl;
    }

    @Override
    public ClientJsonSchemaCsdl deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws IOException {

        final ObjectNode tree = parser.getCodec().readTree(parser);
        Iterator<Map.Entry<String, JsonNode>> iterator = tree.fields();
        while (iterator.hasNext()) {
            CsdlSchema schema = new CsdlSchema();
            jsonCsdl.getSchemas().add(schema);
            Map.Entry<String, JsonNode> schemaNode = iterator.next();
            String nameSpace= schemaNode.getKey();
            schema.setNamespace(nameSpace);
            schema.setAlias(schemaNode.getValue().get("alias").asText());
            JsonNode entityContainer = schemaNode.getValue().get("entityContainer");
            if(entityContainer!=null){
                CsdlEntityContainer container= new CsdlEntityContainer();
                schema.setEntityContainer(container);
                container.setName(entityContainer.get("name").asText());
                Iterator<Map.Entry<String, JsonNode>> itr = entityContainer.get("entitySets").fields();
                while(itr.hasNext()){
                    Map.Entry<String, JsonNode> entitySetEntry = itr.next();
                    JsonNode entitySetNode= entitySetEntry.getValue();
                    CsdlEntitySet entitySet = new ClientCsdlEntitySetDeserializer(schema,entitySetEntry.getKey())
                            .deserialize(entitySetNode.traverse(parser.getCodec()),ctxt);
                    container.getEntitySets().add(entitySet);
                }
            }
        }
        return jsonCsdl;
    }

}
