package si.f5.stsaria.didRecorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MyProperties extends Properties {
    public int getPropertyInt(String key){
        return Integer.parseInt(getProperty(key));
    }
    public void setDefaultProperty(String key, String value){
        if (this.getProperty(key) == null){
            this.setProperty(key, value);
        }
    }
    public void loadAndSetDefaultProperty() throws IOException {
        if (new File("records/didRecorder.properties").isFile()) {
            FileInputStream propertiesInput = new FileInputStream("records/didRecorder.properties");
            this.load(propertiesInput);
            propertiesInput.close();
        }

        this.setDefaultProperty("nameMaxByteSize", "20");
        this.setDefaultProperty("passMaxByteSize", "30");
        this.setDefaultProperty("passMinByteSize", "4");
        this.setDefaultProperty("maxRecordLines", "5");
        this.setDefaultProperty("minTimeHours", "10");
        this.setDefaultProperty("maxTimeHours", "18");
        this.setDefaultProperty("dayChangeThresholdSeconds", "54000");
        this.setDefaultProperty("maxRecordContentByteSize", "512");

        FileOutputStream propertiesOutput = new FileOutputStream("records/didRecorder.properties");
        this.store(propertiesOutput, "DidRecorder Properties");
    }

}
