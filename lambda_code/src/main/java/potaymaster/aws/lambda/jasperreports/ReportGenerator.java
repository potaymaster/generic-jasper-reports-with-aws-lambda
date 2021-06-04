package potaymaster.aws.lambda.jasperreports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import software.amazon.awssdk.utils.BinaryUtils;

public class ReportGenerator {

	static final String outFile = "/tmp/Reports.pdf";
	static final String fileName = "/tmp/template.jrxml";

	private LambdaLogger logger;
	private JRDataSource mainDataSource;

	public ReportGenerator(LambdaLogger logger) {
		this.logger = logger;
		this.mainDataSource = new JREmptyDataSource();
	}

	public String generateBase64EncodedReport(JSONObject postBody) throws JRException, IOException {
		try {
			File file = new File(outFile);
			OutputStream outputSteam = new FileOutputStream(file);
			generateReport(postBody, outputSteam);
			byte[] encoded = BinaryUtils.toBase64Bytes(FileUtils.readFileToByteArray(file));
			return new String(encoded, StandardCharsets.US_ASCII);
		} catch (FileNotFoundException e) {
			logger.log("It was not possible to access the output file: " + e.getMessage());
			throw e;
		} catch (IOException e) {
			logger.log("It was not possible to read and encode the report: " + e.getMessage());
			throw e;
		}
	}

	public void generateReport(JSONObject postBody, OutputStream outputSteam) throws JRException {
		
		JasperReport jasperDesign = JasperCompileManager.compileReport(fileName);
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
						
			processPostBody(postBody, parameters);

			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperDesign, parameters, this.mainDataSource);
			JasperExportManager.exportReportToPdfStream(jasperPrint, outputSteam);
		} catch (JRException e) {
			logger.log("There was an error while generating the report: " + e.getMessage());
			throw e;
		}
	}

	private void processPostBody(JSONObject postBody, Map<String, Object> parameters) {
		String title = "Default title";

		if (postBody.get("title")!=null){
			title = (String) postBody.get("title");
		}
		parameters.put("title", new String(title));

		JSONObject jsonParameters = (JSONObject)postBody.get("parameters");

		for (Object parameterName : jsonParameters.keySet()) {
			addToMap(parameters, parameterName.toString(), (JSONObject)jsonParameters.get(parameterName.toString()));
		}
	}

	private void addToMap(Map<String, Object> parameters, String name, JSONObject jsonValue){
		String type = (String)jsonValue.get("type");
		try {
			switch (type) {
				case "String":
					parameters.put(name,new String((String)jsonValue.get("value")));
					break;
			
				case "Double":
					parameters.put(name, Double.parseDouble((String)jsonValue.get("value")));
					break;
	
				case "Date":
					parameters.put(name, new SimpleDateFormat("dd/MM/yyyy").parse((String)jsonValue.get("value")));
					break;

				case "Boolean":
					parameters.put(name, Boolean.parseBoolean((String)jsonValue.get("value")));
					break;
	
				case "Datasource":
					parameters.put(name, createDataSource((JSONArray)jsonValue.get("value")));
					break;

				case "MainDatasource":
					this.mainDataSource = createDataSource((JSONArray)jsonValue.get("value"));
					break;

				default:
					break;
			}	
		} catch (Exception e) {
			logger.log("Exception parsing value of "+name);
		}
	}

	private JRMapCollectionDataSource createDataSource(JSONArray jsonArray){
		List<Map<String, ?>> elements = new ArrayList<>();

		if (jsonArray!=null && jsonArray.size()>0){
			Iterator<JSONObject> iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				JSONObject jsonElement = iterator.next();
				Map<String, Object> element = new HashMap<String, Object>();
				for (Object parameterName : jsonElement.keySet()) {
					addToMap(element, parameterName.toString(), (JSONObject)jsonElement.get(parameterName.toString()));
				}
				elements.add(element);
			}
		}	

		JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(elements);
		return dataSource;
	}

}
