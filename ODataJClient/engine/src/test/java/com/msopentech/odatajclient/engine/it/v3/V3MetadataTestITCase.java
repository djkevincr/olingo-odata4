/**
 * Copyright © Microsoft Open Technologies, Inc.
 *
 * All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 *
 * See the Apache License, Version 2.0 for the specific language
 * governing permissions and limitations under the License.
 */
package com.msopentech.odatajclient.engine.it.v3;

import static org.junit.Assert.assertNotNull;
import com.msopentech.odatajclient.engine.client.ODataClientFactory;
import com.msopentech.odatajclient.engine.client.ODataV3Client;
import com.msopentech.odatajclient.engine.metadata.EdmV3Metadata;
import com.msopentech.odatajclient.engine.it.AbstractMetadataTestITCase;
import org.junit.Test;

public class V3MetadataTestITCase extends AbstractMetadataTestITCase {

    @Override
    protected ODataV3Client getClient() {
        return ODataClientFactory.getV3();
    }

    @Test
    public void retrieve() {
        final EdmV3Metadata metadata = getClient().getRetrieveRequestFactory().
                getMetadataRequest(getTestServiceRoot()).execute().getBody();
        assertNotNull(metadata);
    }

}