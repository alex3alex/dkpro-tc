/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dkpro.tc.fstore.simple;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.gson.Gson;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.fstore.simple.SparseFeatureStore;

public class SparseFeatureStoreTest
{

    private FeatureStore featureStore;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp()
            throws Exception
    {
        featureStore = new SparseFeatureStore();

        Feature f1 = new Feature("feature1", "value1");
        Feature f2 = new Feature("feature2", "value2");
        List<Feature> features = new ArrayList<>();
        features.add(f1);
        features.add(f2);
        Instance instance = new Instance(features, "outcome");
        featureStore.addInstance(instance);
        featureStore.addInstance(instance);
    }

    private void testValuesOfDefaultFeatureStoreInstance(FeatureStore fs)
    {
        assertEquals(2, fs.getNumberOfInstances());
        assertEquals("outcome", fs.getUniqueOutcomes().first());
        assertEquals(new Feature("feature1", "value1"),
                fs.getInstance(0).getFeatures().iterator().next());
    }

    @Test
    public void testSimple()
            throws Exception
    {
        testValuesOfDefaultFeatureStoreInstance(featureStore);
    }

    @Test
    public void testSerializeJSON()
            throws Exception
    {
        Gson gson = new Gson();
        File tmpFile = File.createTempFile("tempFeatureStore", ".json");
        FileUtils.writeStringToFile(tmpFile, gson.toJson(featureStore));

        // make sure we have correctly filled instance
        testValuesOfDefaultFeatureStoreInstance(featureStore);

        FeatureStore fs = gson
                .fromJson(FileUtils.readFileToString(tmpFile), SparseFeatureStore.class);

        // test deserialized values
        testValuesOfDefaultFeatureStoreInstance(fs);

        FileUtils.deleteQuietly(tmpFile);
    }

    @Test
    public void testNullFeatureValues()
            throws Exception
    {
        FeatureStore fs = new SparseFeatureStore();
        // two instance, both have different features, in unsorted manner
        Instance inst1 = new Instance(new Feature("featZ", "value").asSet(), "outcome1");
        Instance inst2 = new Instance(new Feature("featA", "value").asSet(), "outcome1");

        fs.addInstance(inst1);
        fs.addInstance(inst2);

        Instance retrievedInstance1 = fs.getInstance(0);
        // now it has two features
        assertEquals(1, retrievedInstance1.getFeatures().size());
        // which are sorted by name
        Iterator<Feature> iter = retrievedInstance1.getFeatures().iterator();
        Feature feature1 = iter.next();
        assertEquals("featZ", feature1.getName());
        assertNotNull(feature1.getValue());
    }

    @Test
    public void testInconsistentFeatureVectors()
            throws Exception
    {
        FeatureStore fs = new SparseFeatureStore();
        // two instance, both have different features, in unsorted manner
        Instance inst1 = new Instance(Arrays.asList(new Feature("featZ", "value")), "outcome1");
        Instance inst2 = new Instance(Arrays.asList(new Feature("featA", "value")), "outcome1");

        fs.addInstance(inst1);
        fs.addInstance(inst2);

        Instance retrievedInstance1 = fs.getInstance(0);
        // now it has two features
        assertEquals(1, retrievedInstance1.getFeatures().size());

        Instance inst3 = new Instance(Arrays.asList(new Feature("featB", "value")), "outcome1");

        // adding another instance with newly introduced features would result into
        // inconsistent feature vector
        // must fail -> otherwise retrievedInstance1.getFeatures().size() == 3!!
        exception.expect(TextClassificationException.class);
        fs.addInstance(inst3);
    }
}