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
package org.dkpro.tc.groovyexamples.regression.pair

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException
import org.dkpro.lab.Lab
import org.dkpro.lab.task.Dimension
import org.dkpro.lab.task.BatchTask.ExecutionPolicy
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet
import org.dkpro.tc.core.Constants
import org.dkpro.tc.examples.io.STSReader
import org.dkpro.tc.examples.util.DemoUtils
import org.dkpro.tc.features.pair.core.length.DiffNrOfTokensPairFeatureExtractor
import org.dkpro.tc.ml.ExperimentCrossValidation
import org.dkpro.tc.ml.ExperimentTrainTest
import org.dkpro.tc.ml.report.BatchCrossValidationReport
import org.dkpro.tc.ml.report.BatchTrainTestReport
import org.dkpro.tc.ml.weka.WekaRegressionAdapter

import weka.classifiers.functions.SMOreg
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import org.apache.uima.fit.factory.CollectionReaderFactory;
/**
 * A demo for pair classification with a regression outcome.
 *
 * This uses the Semantic Text Similarity (STS) from the SemEval 2012 task. It computes text
 * similarity features between document pairs and then learns a regression model that predicts
 * similarity of unseen document pairs.
 */
public class RegressionDemo implements Constants {

    // === PARAMETERS===========================================================

    def experimentName = "RegressionExample"
    def NUM_FOLDS = 2
    def String inputFileTrain = "src/main/resources/data/sts2012/STS.input.MSRpar.txt"
    def String goldFileTrain = "src/main/resources/data/sts2012/STS.gs.MSRpar.txt"
    def String inputFileTest = "src/main/resources/data/sts2012/STS.input.MSRvid.txt"
    def String goldFileTest = "src/main/resources/data/sts2012/STS.gs.MSRvid.txt"

    // === DIMENSIONS ===========================================================
    
    def trainreader = CollectionReaderFactory.createReaderDescription(STSReader.class,
        STSReader.PARAM_INPUT_FILE, inputFileTrain,
        STSReader.PARAM_GOLD_FILE, goldFileTrain
        );
    
    def testreader = CollectionReaderFactory.createReaderDescription(STSReader.class,
         STSReader.PARAM_INPUT_FILE, inputFileTest,
         STSReader.PARAM_GOLD_FILE, goldFileTest
        );

    def dimReaders = Dimension.createBundle("readers", [
        readerTrain: trainreader,
        readerTest: testreader,
    ])

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_REGRESSION)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_PAIR)

    def dimClassificationArgs =
    Dimension.create(DIM_CLASSIFICATION_ARGS, [SMOreg.name])

    // yields really bad results. To improve the performance, use a string similarity
    // based feature extractor
    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET, new TcFeatureSet(
        TcFeatureFactory.create(DiffNrOfTokensPairFeatureExtractor.class)
    ))

    // === Experiments =========================================================

    /**
     * Crossvalidation setting
     *
     * @throws Exception
     */
    protected void runCrossValidation() throws Exception
    {

        ExperimentCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessing:  getPreprocessing(),
            machineLearningAdapter: WekaRegressionAdapter,
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimClassificationArgs,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchCrossValidationReport
            ],
            numFolds: NUM_FOLDS]

        Lab.getInstance().run(batchTask)
    }

    /**
     * TrainTest Setting
     *
     * @throws Exception
     */
    protected void runTrainTest() throws Exception
    {

        ExperimentTrainTest batchTask = [
            experimentName: experimentName + "-TrainTest-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-TrainTest-Groovy",
            preprocessing:  getPreprocessing(),
            machineLearningAdapter: WekaRegressionAdapter,
            parameterSpace : [
                dimReaders,
                dimLearningMode,
                dimFeatureMode,
                dimClassificationArgs,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchTrainTestReport
                ]
        ]

        // Run
        Lab.getInstance().run(batchTask)
    }

    private AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class),
        createEngineDescription(OpenNlpPosTagger.class))
    }

    public static void main(String[] args)
    {
		DemoUtils.setDkproHome(RegressionDemo.getSimpleName());
        new RegressionDemo().runTrainTest()
      //  new RegressionDemo().runCrossValidation()
    }
}