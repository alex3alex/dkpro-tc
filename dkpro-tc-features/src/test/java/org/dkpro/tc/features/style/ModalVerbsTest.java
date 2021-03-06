/*******************************************************************************
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
 ******************************************************************************/
package org.dkpro.tc.features.style;

import static org.dkpro.tc.testing.FeatureTestUtil.assertFeatures;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Set;

import org.junit.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.style.ModalVerbsFeatureExtractor;

public class ModalVerbsTest
{
    @Test
    public void modalVerbsFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE,
                        "en"));
        AnalysisEngine engine = createEngine(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("I can. I could. You might. You may. I must. He should. He must. We will. They would. You shall.");
        engine.process(jcas);
        
        TextClassificationTarget target = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
        target.addToIndexes();

        ModalVerbsFeatureExtractor extractor = new ModalVerbsFeatureExtractor();
        Set<Feature> features = extractor.extract(jcas, target);

        Assert.assertEquals(11, features.size());

        assertFeatures(ModalVerbsFeatureExtractor.FN_CAN, 10.0, features, 0.001);
        assertFeatures(ModalVerbsFeatureExtractor.FN_COULD, 10.0, features, 0.001);
        assertFeatures(ModalVerbsFeatureExtractor.FN_MIGHT, 10.0, features, 0.001);
        assertFeatures(ModalVerbsFeatureExtractor.FN_MAY, 10.0, features, 0.001);
        assertFeatures(ModalVerbsFeatureExtractor.FN_MUST, 20.0, features, 0.001);
        assertFeatures(ModalVerbsFeatureExtractor.FN_SHOULD, 10.0, features, 0.001);
        assertFeatures(ModalVerbsFeatureExtractor.FN_WILL, 10.0, features, 0.001);
        assertFeatures(ModalVerbsFeatureExtractor.FN_WOULD, 10.0, features, 0.001);
        assertFeatures(ModalVerbsFeatureExtractor.FN_SHALL, 10.0, features, 0.001);
        assertFeatures(ModalVerbsFeatureExtractor.FN_ALL, 100.0, features, 0.001); // all verbs are modal
                                                                              // here
        assertFeatures(ModalVerbsFeatureExtractor.FN_UNCERT, 70.0, features, 0.001); // 70% of the verbs
                                                                                // express
                                                                                // uncertainty

    }
}