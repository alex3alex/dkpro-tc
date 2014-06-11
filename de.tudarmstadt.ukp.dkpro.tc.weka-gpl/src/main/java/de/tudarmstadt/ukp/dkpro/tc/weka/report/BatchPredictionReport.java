package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.reporting.FlexTable;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.ExtractFeaturesAndPredictTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.uima.ExtractFeaturesAndPredictConnector;

/**
 * A report which collects results from all executed @link {@link ExtractFeaturesAndPredictTask}s,
 * and writes them into a human-readable format, one file per configuration.
 * 
 * @author daxenberger
 * 
 */
public class BatchPredictionReport
    extends BatchReportBase
    implements Constants
{

    private static final String report_name = "PredictionReport";
    private static final String predicted_value = "Prediction";

    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

        FlexTable<String> table = FlexTable.forClass(String.class);

        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().startsWith(ExtractFeaturesAndPredictTask.class.getName())) {

                // deserialize file
                FileInputStream f = new FileInputStream(store.getStorageFolder(subcontext.getId(),
                        ExtractFeaturesAndPredictConnector.PREDICTION_MAP_FILE_NAME));
                ObjectInputStream s = new ObjectInputStream(f);
                Map<String, List<String>> resultMap = (Map<String, List<String>>) s.readObject();
                s.close();

                // write one file per batch
                // in files: one line per instance

                for (String id : resultMap.keySet()) {
                    Map<String, String> row = new HashMap<String, String>();
                    row.put(predicted_value, StringUtils.join(resultMap.get(id), ","));
                    table.addRow(id, row);
                }
                getContext().storeBinary(report_name + subcontext.getId() + SUFFIX_EXCEL,
                        table.getExcelWriter());
            }
        }

        // output the location of the batch evaluation folder
        // otherwise it might be hard for novice users to locate this
        File dummyFolder = store.getStorageFolder(getContext().getId(), "dummy");
        // TODO can we also do this without creating and deleting the dummy folder?
        getContext().getLoggingService().message(getContextLabel(),
                "Storing detailed results in:\n" + dummyFolder.getParent() + "\n");
        dummyFolder.delete();
    }
}