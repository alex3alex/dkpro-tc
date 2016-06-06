/*******************************************************************************
 * Copyright 2015
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
package org.dkpro.tc.features.ngram.meta;

import java.io.IOException;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.dkpro.tc.features.ngram.base.LuceneSkipNgramFeatureExtractorBase;
import org.dkpro.tc.features.ngram.base.NGramFeatureExtractorBase;
import org.dkpro.tc.features.ngram.util.NGramUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class LuceneSkipNgramMetaCollector
    extends LuceneBasedMetaCollector
{
    @ConfigurationParameter(name = LuceneSkipNgramFeatureExtractorBase.PARAM_SKIP_NGRAM_MIN_N, mandatory = true, defaultValue = "2")
    private int minN;

    @ConfigurationParameter(name = LuceneSkipNgramFeatureExtractorBase.PARAM_SKIP_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
    private int maxN;

    @ConfigurationParameter(name = LuceneSkipNgramFeatureExtractorBase.PARAM_SKIP_SIZE, mandatory = true, defaultValue = "2")
    private int skipSize;

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_NGRAM_STOPWORDS_FILE, mandatory = false)
    private String stopwordsFile;

    @ConfigurationParameter(name = NGramFeatureExtractorBase.PARAM_FILTER_PARTIAL_STOPWORD_MATCHES, mandatory = true, defaultValue = "false")
    protected boolean filterPartialStopwordMatches;

    @ConfigurationParameter(name = LuceneSkipNgramFeatureExtractorBase.PARAM_SKIP_NGRAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    private boolean ngramLowerCase;

    private Set<String> stopwords;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            stopwords = FeatureUtil.getStopwords(stopwordsFile, ngramLowerCase);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    protected FrequencyDistribution<String> getNgramsFD(JCas jcas)
    {
        TextClassificationUnit fullDoc = new TextClassificationUnit(jcas, 0, jcas.getDocumentText().length());
        return NGramUtils.getDocumentSkipNgrams(jcas, fullDoc, ngramLowerCase,
                filterPartialStopwordMatches, minN, maxN, skipSize, stopwords);
    }

    @Override
    protected String getFieldName()
    {
        return LuceneSkipNgramFeatureExtractorBase.LUCENE_SKIP_NGRAM_FIELD;
    }
}