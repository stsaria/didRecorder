package si.f5.stsaria.didRecorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.nio.file.Files;

@SpringBootApplication
public class DidRecorderApplication {

	public static void main(String[] args) {
		File recordsDirFile = new File("records");
		try {
			if (recordsDirFile.isFile()) {
				Files.delete(recordsDirFile.toPath());
			}
			if (!recordsDirFile.isDirectory()) {
				Files.createDirectory(recordsDirFile.toPath());
			}
		} catch (Exception ignore) {}
		SpringApplication.run(DidRecorderApplication.class, args);
	}

}
