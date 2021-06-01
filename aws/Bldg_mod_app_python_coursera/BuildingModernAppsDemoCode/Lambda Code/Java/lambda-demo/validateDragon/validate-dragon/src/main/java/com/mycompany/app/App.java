// # Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// #
// # Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except
// # in compliance with the License. A copy of the License is located at
// #
// # https://aws.amazon.com/apache-2-0/
// #
// # or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
// # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
// # specific language governing permissions and limitations under the License.

package com.mycompany.app;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.JSONInput;
import com.amazonaws.services.s3.model.JSONOutput;
import com.amazonaws.services.s3.model.CompressionType;
import com.amazonaws.services.s3.model.ExpressionType;
import com.amazonaws.services.s3.model.InputSerialization;
import com.amazonaws.services.s3.model.OutputSerialization;
import com.amazonaws.services.s3.model.SelectObjectContentEvent;
import com.amazonaws.services.s3.model.SelectObjectContentEventVisitor;
import com.amazonaws.services.s3.model.SelectObjectContentRequest;
import com.amazonaws.services.s3.model.SelectObjectContentResult;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import java.util.*;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class App implements RequestHandler<Map<String,String>, String> {

    private static final AWSSimpleSystemsManagement ssm = AWSSimpleSystemsManagementClientBuilder.defaultClient();
    private static final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    @Override
    public String handleRequest(Map<String,String> event, Context context) {
        readDragonData(event);
        return "Dragon validated";
    }

    protected static void readDragonData(Map<String,String> event) {
        String bucketName = getBucketName();
        String key = getKey();
        String query = getQuery(event);
        SelectObjectContentRequest request = generateJSONRequest(bucketName, key, query);
        final AtomicBoolean isResultComplete = new AtomicBoolean(false);

        SelectObjectContentResult result = s3Client.selectObjectContent(request);

        // Anonymous inner class implementation
        // to define what actions to complete 
        // for every object in the result stream
        InputStream resultInputStream = result.getPayload().getRecordsInputStream(
                new SelectObjectContentEventVisitor() {
                    @Override
                    public void visit(SelectObjectContentEvent.StatsEvent event)
                    {
                        System.out.println(
                                "Received Stats, Bytes Scanned: " + event.getDetails().getBytesScanned()
                                        +  " Bytes Processed: " + event.getDetails().getBytesProcessed());
                    }

                    /*
                     * An End Event informs that the request has finished successfully.
                     */
                    @Override
                    public void visit(SelectObjectContentEvent.EndEvent event)
                    {
                        isResultComplete.set(true);
                    }
                }
        );

        String text = null;
        try {
            text = IOUtils.toString(resultInputStream, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            // In the real world, you should do actual error handling here
            // do not just log a message and move on in a production system
            System.out.println(e.getMessage());
        }
        if(text != null && !(text.equals(""))) {
            throw new DragonValidationException("Duplicate dragon reported", new RuntimeException());
        }

    }


    private static SelectObjectContentRequest generateJSONRequest(String bucketName, String key, String query) {
        SelectObjectContentRequest request = new SelectObjectContentRequest();
        request.setBucketName(bucketName);
        request.setKey(key);
        request.setExpression(query);
        request.setExpressionType(ExpressionType.SQL);

        InputSerialization inputSerialization = new InputSerialization();
        inputSerialization.setJson(new JSONInput().withType("Document"));
        inputSerialization.setCompressionType(CompressionType.NONE);
        request.setInputSerialization(inputSerialization);

        OutputSerialization outputSerialization = new OutputSerialization();
        outputSerialization.setJson(new JSONOutput());
        request.setOutputSerialization(outputSerialization);

        return request;
    }

    private static String getBucketName() {
        GetParameterRequest bucketParameterRequest = new GetParameterRequest().withName("dragon_data_bucket_name").withWithDecryption(false);
        GetParameterResult bucketResult = ssm.getParameter(bucketParameterRequest);
        return bucketResult.getParameter().getValue();
    }

    private static String getKey() {
        GetParameterRequest keyParameterRequest = new GetParameterRequest().withName("dragon_data_file_name").withWithDecryption(false);
        GetParameterResult keyResult = ssm.getParameter(keyParameterRequest);
        return keyResult.getParameter().getValue();
    }

    private static String getQuery(Map<String,String> event) {
        return "select * from S3Object[*][*] s where s.dragon_name_str =  '" + event.get("dragon_name_str") + "'";
    }
}