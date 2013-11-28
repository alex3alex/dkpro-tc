package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaSerializedModel;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * Builds a model from training data and safes it along with all necessary information to be used in
 * follow-up experiments.
 * 
 * @author daxenberger
 * 
 */
public class GenerateModelTask
    extends ExecutableTaskBase
{
    @Discriminator
    private List<String> featureSet;

    @Discriminator
    private List<Object> pipelineParameters;

    @Discriminator
    private List<String> classificationArguments;

    @Discriminator
    private boolean multiLabel;

    @Discriminator
    private boolean isRegressionExperiment;

    @Discriminator
    private String threshold;

    public static final String INPUT_KEY_TRAIN = "input.train";
    public static final String OUTPUT_KEY = "output";
    public static final String MODEL_KEY = "model.ser";
    public static final String TRAINING_DATA_KEY = "training-data.arff.gz";

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {

        File arffFileTrain = new File(aContext.getStorageLocation(INPUT_KEY_TRAIN,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_KEY);

        Instances trainData = TaskUtils.getInstances(arffFileTrain, multiLabel);

        Instances filteredTrainData;

        if (trainData.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME) != null) {

            int instanceIdOffset = // TaskUtils.getInstanceIdAttributeOffset(trainData);

            trainData.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME).index() + 1;

            Remove remove = new Remove();
            remove.setAttributeIndices(Integer.toString(instanceIdOffset));
            remove.setInvertSelection(false);
            remove.setInputFormat(trainData);

            filteredTrainData = Filter.useFilter(trainData, remove);
        }
        else {
            filteredTrainData = new Instances(trainData);
        }

        List<Attribute> attributes = new ArrayList<Attribute>();
        for (int i = 0; i < filteredTrainData.numAttributes(); i++) {
            Attribute att = filteredTrainData.attribute(i);
            attributes.add(att.copy(att.name()));
        }

        // train model
        Classifier trainedClassifier;

        try {
            List<String> mlArgs = classificationArguments
                    .subList(1, classificationArguments.size());
            trainedClassifier = AbstractClassifier.forName(classificationArguments.get(0),
                    new String[] {});
            ((AbstractClassifier) trainedClassifier).setOptions(mlArgs.toArray(new String[0]));
            trainedClassifier.buildClassifier(filteredTrainData);
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

        List<String> labels = new ArrayList<String>();
        for (int j = 0; j < trainData.classIndex(); j++) {
            labels.add(trainData.attribute(j).name().split(WekaDataWriter.CLASS_ATTRIBUTE_PREFIX)[1]);
        }

        WekaSerializedModel model = new WekaSerializedModel(attributes, trainedClassifier,
                threshold, featureSet, labels, pipelineParameters);

        try {
            FileOutputStream fileOut = new FileOutputStream(aContext.getStorageLocation(OUTPUT_KEY,
                    AccessMode.READWRITE).getPath()
                    + "/" + MODEL_KEY);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(model);
            out.close();
            fileOut.close();
        }
        catch (IOException e) {

        }

    }
}