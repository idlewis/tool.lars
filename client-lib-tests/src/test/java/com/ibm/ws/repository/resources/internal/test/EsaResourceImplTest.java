/*******************************************************************************
* Copyright (c) 2017 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.ibm.ws.repository.resources.internal.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.ibm.ws.repository.resources.internal.EsaResourceImpl;
import com.ibm.ws.repository.transport.model.Asset;
import com.ibm.ws.repository.transport.model.WlpInformation;

public class EsaResourceImplTest {

    @Test
    public void testAddAndSetRequireFeature() {
        EsaResourceImpl toTest = new EsaResourceImpl(null);

        // Test adding an 'old' style requireFeature
        toTest.addRequireFeature("foo");
        Collection<String> features = toTest.getRequireFeature();
        assertEquals("Wrong number of features", 1, features.size());
        assertTrue("Features doesn't contain the expected value", features.contains("foo"));

        Map<String, Collection<String>> fwt = toTest.getRequireFeatureWithTolerates();
        assertEquals("Wrong number of features", 1, fwt.size());
        Collection<String> tolerates = fwt.get("foo");
        assertNotNull("The feature foo didn't appear in the collection", tolerates);
        assertEquals("The collection of tolerates should have been empty", 0, tolerates.size());

        // Test adding a new style require feature with tolerates
        Collection<String> tolerated = Collections.singleton("tolerated");
        toTest.addRequireFeatureWithTolerates("bar", tolerated);

        Collection<String> features2 = toTest.getRequireFeature();
        assertEquals("Wrong number of features", 2, features2.size());
        assertTrue("Features doesn't contain the expected value", features2.contains("bar"));

        Map<String, Collection<String>> fwt2 = toTest.getRequireFeatureWithTolerates();
        assertEquals("Wrong number of features", 2, fwt2.size());
        Collection<String> tolerates2 = fwt2.get("bar");
        assertNotNull("The feature bar didn't appear in the collection", tolerates2);
        assertTrue("The tolerate for feature bar didn't appear in the tolerates collection",
                   tolerates2.contains("tolerated"));

        // Test setting an old style feature, should overwrite the previous data.
        toTest.setRequireFeature(Collections.singleton("set_foo_feature"));
        Collection<String> features3 = toTest.getRequireFeature();
        assertEquals("Wrong number of features", 1, features3.size());
        assertTrue("features3 doesn't contain the 'set_foo_feature' feature", features3.contains("set_foo_feature"));

        Map<String, Collection<String>> fwt3 = toTest.getRequireFeatureWithTolerates();
        assertEquals("Wrong number of features", 1, fwt3.size());
        Collection<String> tolerates3 = fwt3.get("set_foo_feature");
        assertNotNull("The feature set_foo_feature didn't appear in the collection", tolerates3);
        assertEquals("There should have been no tolerates", 0, tolerates3.size());

        // Test setting a new style feature with tolerates info
        Collection<String> tolerated4 = Collections.singleton("tolerated4");
        Map<String, Collection<String>> something4 = Collections.singletonMap("feature4", tolerated4);
        toTest.setRequireFeatureWithTolerates(something4);
        Collection<String> features4 = toTest.getRequireFeature();
        assertEquals("Wrong number of features", 1, features4.size());
        assertTrue("features4 doesn't contain the expected 'feature4' feature", features4.contains("feature4"));

        Map<String, Collection<String>> fwt4 = toTest.getRequireFeatureWithTolerates();
        assertEquals("The wrong number of features", 1, fwt4.size());
        Collection<String> tolerates4 = fwt4.get("feature4");
        assertNotNull("There should have been some tolerates", tolerates4);
        assertTrue("tolerates4 didn't contain the 'tolerated4' feature", tolerates4.contains("tolerated4"));

    }

    @Test
    public void testCopyRequireFeatureField() {
        Asset asset = new Asset();
        WlpInformation wlp = new WlpInformation();
        ArrayList<String> required = new ArrayList<String>();
        required.add("la di dah");
        wlp.setRequireFeature(required);
        asset.setWlpInformation(wlp);
        EsaResourceImpl esa = new EsaResourceImpl(null, asset);
        Collection<String> features = esa.getRequireFeature();
        assertEquals("wrong size", 1, features.size());

        // If a new require feature is added, the requireFeatureWithTolerates field
        // should also be updated.
        esa.addRequireFeature("a new feature");
        Collection<String> features2 = esa.getRequireFeature();
        assertEquals("Wrong number of features", 2, features2.size());
        assertTrue("features2 didn't contain the expected feature", features2.contains("a new feature"));
        Map<String, Collection<String>> rfwt2 = esa.getRequireFeatureWithTolerates();
        assertEquals("Wrong number of features", 2, rfwt2.size());
        assertNotNull("The expected feature was not found", rfwt2.get("a new feature"));
        assertNotNull("The expected feature was not found", rfwt2.get("la di dah"));
    }

    @Test
    public void testCopyRequireFeatureField2() {
        // This time testing by adding requirement via the addRequireFeatureWithTolerates method
        Asset asset = new Asset();
        WlpInformation wlp = new WlpInformation();
        ArrayList<String> required = new ArrayList<String>();
        required.add("la di dah");
        wlp.setRequireFeature(required);
        asset.setWlpInformation(wlp);
        EsaResourceImpl esa = new EsaResourceImpl(null, asset);
        Collection<String> features = esa.getRequireFeature();
        assertEquals("wrong size", 1, features.size());

        // If a new require feature is added, the requireFeatureWithTolerates field
        // should also be updated.
        esa.addRequireFeatureWithTolerates("a new feature", Collections.singleton("tolerated"));
        Collection<String> features2 = esa.getRequireFeature();
        assertEquals("Wrong number of features", 2, features2.size());
        assertTrue("features2 didn't contain the expected feature", features2.contains("a new feature"));
        Map<String, Collection<String>> rfwt2 = esa.getRequireFeatureWithTolerates();
        assertEquals("Wrong number of features", 2, rfwt2.size());
        assertNotNull("The expected feature was not found", rfwt2.get("la di dah"));
        Collection<String> tolerates2 = rfwt2.get("a new feature");
        assertTrue("The expected tolerate was not found", tolerates2.contains("tolerated"));
    }
}
