package com.seaboxdata.etlman.sql;

import com.seaboxdata.etlman.metastore.ETLLoadBatch;
import com.seaboxdata.etlman.metastore.ETLLoadGroup;
import com.seaboxdata.etlman.metastore.ETLSourceTable;
import com.seaboxdata.etlman.metastore.ETLTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaoy on 1/5/2017.
 */
public abstract class JobSQLGenerator {

    protected boolean hasIncSourceData;
    protected ETLTask etlTask;
    protected String preProcessScript = null;
    protected String postProcessScript = null;

    public JobSQLGenerator(ETLTask etlTask) {
        this.etlTask = etlTask;
        hasIncSourceData = hasIncrementalSource();
    }

    protected String genJobPreprocess() throws Exception {
        return "";
    }

    protected String genJobPostprocess() throws Exception {
        return "";
    }

    protected String genBatchPreprocess(ETLLoadBatch loadBatch) throws Exception {
        return "\n-- Script for batch " + loadBatch.getLoadBatch() + " begin";
    }

    protected String genBatchPostprocess(ETLLoadBatch loadBatch) throws Exception {
        return "\n-- Script for batch " + loadBatch.getLoadBatch() + " end";
    }

    protected String genGroupPreprocess(ETLLoadGroup loadGroup) throws Exception {
        return "\n-- Script for group " + loadGroup.getLoadGroup() + " begin";
    }

    protected String genGroupPostprocess(ETLLoadGroup loadGroup) throws Exception {
        return "\n-- Script for group " + loadGroup.getLoadGroup() + " end";
    }

    protected String genGroupBody(ETLLoadGroup loadGroup) throws Exception {
        return "";
    }

    protected String genGroupScript(ETLLoadGroup loadGroup) throws Exception {
        return genGroupPreprocess(loadGroup) + genGroupBody(loadGroup) + genGroupPostprocess(loadGroup);
    }

    protected String genBatchBody(ETLLoadBatch loadBatch) throws Exception {
        StringBuilder buffer = new StringBuilder();

        for (ETLLoadGroup loadGroup : loadBatch.getLoadGroupList())
            buffer.append(genGroupScript(loadGroup));

        return buffer.toString();
    }

    private String genBatchScript(ETLLoadBatch loadBatch) throws Exception {
        loadBatch.setBatchScript(genBatchPreprocess(loadBatch) +
                genBatchBody(loadBatch) + genBatchPostprocess(loadBatch));
        return loadBatch.getBatchScript();
    }


    public Map<Integer, String> genJobScript() throws Exception {
        Map<Integer, String> scripts = new HashMap<>();

        for (ETLLoadBatch loadBatch : etlTask.getEtlEntity().getEtlLoadBatches())
            scripts.put(loadBatch.getLoadBatch(), genBatchScript(loadBatch));

        return scripts;
    }

    private boolean hasIncrementalSource() {
        // If any one of the source tables contains incremental data, a working table is needed.
        for (ETLLoadBatch loadBatch : etlTask.getEtlEntity().getEtlLoadBatches()) {
            for (ETLSourceTable sourceTable : loadBatch.getSourceTableList())
                if (sourceTable.isIncExtract()) {
                    return true;
                }
        }
        return false;
    }
}
