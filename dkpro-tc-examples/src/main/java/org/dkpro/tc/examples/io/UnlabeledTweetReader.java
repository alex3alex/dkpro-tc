/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.examples.io;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import org.dkpro.tc.api.io.TCReaderSingleLabel;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.Constants;

/**
 * Reads plain text tweets, labels each tweet as sentence, and assigns no label to them.
 */
public class UnlabeledTweetReader
    extends TextReader
    implements TCReaderSingleLabel
{
    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        super.getNext(aCAS);

        JCas jcas;
        try {
            jcas = aCAS.getJCas();
            // consider a tweet to be a sentence
            Sentence sentenceAnno = new Sentence(jcas);
            sentenceAnno.setBegin(0);
            sentenceAnno.setEnd(jcas.getDocumentText().length());
            sentenceAnno.addToIndexes();
        }
        catch (CASException e) {
            throw new CollectionException();
        }

        TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
        outcome.setOutcome(getTextClassificationOutcome(jcas));
        outcome.addToIndexes();
    }

    @Override
    public String getTextClassificationOutcome(JCas jcas)
        throws CollectionException
    {
        return Constants.UNKNOWN_OUTCOME;
    }
}