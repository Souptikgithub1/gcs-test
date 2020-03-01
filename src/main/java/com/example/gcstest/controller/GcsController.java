package com.example.gcstest.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@RestController
@RequestMapping("/gcs")
public class GcsController {

	@Value("${BUCKET_NAME}")
	private String bucketName;
	
	@Value("${PROJECT_ID}")
	private String projectId;
	
	@Value("${PATHTO_JSONKEY}")
	private String pathToJsonKey;
	
	private Storage storage;
	private StorageOptions options;
	
	
	
	@GetMapping("/test")
	public String test() {
		return "Test";
	}
	
	@GetMapping("/write")
	public String writeGcsFile() throws IOException{
		try {
			Resource resource = new ClassPathResource("gcstest-9806d0f0ab52.json");
			File credentialsFile = new File(resource.getURI());
			FileInputStream input = new FileInputStream(credentialsFile);
			
			StorageOptions options = StorageOptions.newBuilder()
												   .setProjectId(projectId)
											   	   .setCredentials(GoogleCredentials.fromStream(input))
											   	   .build();
			storage = options.getService();
			
			
			//this.saveFileToGcs();
			//this.saveTxtToGcs();
			this.readeFileFromGcs();
			return "successfull";
		} catch (FileNotFoundException exception) {
			System.out.println("File Not Found");
			return "File not FOund";
		}
	}
	
	
	public String saveTxtToGcs() {
		Blob blob = null;
		try {
			InputStream content = new ByteArrayInputStream("Hello World".getBytes("UTF-8"));
			BlobId blobId = BlobId.of(bucketName, "gcp_test_text");
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
			blob = storage.create(blobInfo, content);
			return "Success";
		} catch(Exception e) {
			return "Failed";			
		}

	}
	
	private void saveFileToGcs() throws IOException {
		Blob blob = null;
		
		XSSFWorkbook workbook = createExcel();
		byte[] fileByteArrResource = convertFileToByteArr(workbook);
		
		BlobId blobId = BlobId.of(bucketName, "Test.xlsx");
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
									.setContentType("application/vnd.ms-excel")
									.build();
		InputStream content = new ByteArrayInputStream(fileByteArrResource);
		blob = storage.create(blobInfo, fileByteArrResource);
	}
	
	private BufferedReader readeFileFromGcs() throws IOException {
		String filename = "test_text_to_read.txt";
		Blob blob = storage.get(bucketName, filename);
		ReadChannel readChannel = blob.reader();
		BufferedReader br = new BufferedReader(Channels.newReader(readChannel, "UTF-8"));
		String strCurrentLine;
		while ((strCurrentLine = br.readLine()) != null) {
			System.out.println(strCurrentLine);
		}
		
		System.out.println("Created File Reader Succesfully to read file :{} from  bucket :{}" + filename + bucketName);
		
		return br;
	}
	
	public byte[] convertFileToByteArr(XSSFWorkbook workbook) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "force_download"));
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Test.xlsx");
		return workbookToBytes(workbook);
	}
	
	public byte[] workbookToBytes(XSSFWorkbook workbook) throws IOException {
		try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			workbook.write(stream);
			workbook.close();
			return stream.toByteArray();
		}
	}
	
	public XSSFWorkbook createExcel() {
		XSSFWorkbook workbook = new XSSFWorkbook();
		//OutputStream fileOutputStream = new FileOutputStream();
		XSSFSheet sheet = workbook.createSheet("Test_Sheet");
		return workbook;
	}
}
