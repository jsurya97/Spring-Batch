package com.jk.spring_batch.listener;

import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.transform.FlatFileFormatException;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class ProductSkipListener {

    private String readerrFileName = "error/read_skipped";
    private String processorerrFileName = "error/processor_skipped";



    @OnSkipInRead
    public void onSkipRead(Throwable t) {
        if (t instanceof FlatFileParseException) {
            FlatFileParseException ffep = (FlatFileParseException) t;
            onSkip(ffep.getInput(), readerrFileName);
        }
    }

    @OnSkipInProcess
    public void onSkipProcess(Object o, Throwable t) {
        if (t instanceof RuntimeException) {
            onSkip(o, processorerrFileName);
        }
    }

    public void onSkip(Object o, String fname) {
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(fname, true);
            fos.write(o.toString().getBytes());
            fos.write("\r\n".getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
