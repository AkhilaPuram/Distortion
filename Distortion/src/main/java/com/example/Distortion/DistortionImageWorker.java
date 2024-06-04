package com.example.Distortion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

@Component
public class DistortionImageWorker {

	@JobWorker(type = "Getdistortion")
	public void ImageUpload(final JobClient client, final ActivatedJob job) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("uploadresponse", "");
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		String filename = (String) job.getVariable("filename");

		// ContentType

		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		String token = (String) job.getVariable("token");
		headers.add("Authorization", token);
		MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
		Resource file1 = new FileSystemResource(filename);
		multipartBodyBuilder.part("file", file1, MediaType.IMAGE_JPEG);
		MultiValueMap<String, HttpEntity<?>> multipartBody = multipartBodyBuilder.build();

		// The complete http request body.

		HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity = new HttpEntity<>(multipartBody, headers);
		ResponseEntity<UploadResponse> responseEntity = restTemplate
				.postForEntity("https://api.bluevision.ai/document/upload", httpEntity, UploadResponse.class);
		System.out.println(responseEntity.getBody().getBatchid());
		map.put("batchid", responseEntity.getBody().getBatchid());
		String url = "https://api.bluevision.ai/document/get_image_result/" + responseEntity.getBody().getBatchid();
		System.out.println(url);
		map.put("destortionurl", url);

		client.newCompleteCommand(job.getKey()).variables(map).send();
	}

	@JobWorker(type = "IdentifyDistortions", autoComplete = false)
	public void IdentifyDistortions(final JobClient client, final ActivatedJob job) {
		Map<String, Object> map = new HashMap<String, Object>();

		Map<String, Object> variablesMap = job.getVariablesAsMap();

		// ArrayList<Integer> testlist = (ArrayList<Integer>) job.getVariable("id");

		/*
		 * for (Map.Entry<String, Object> entry : variablesMap.entrySet()) { if
		 * (entry.getKey() == "id") { String id1 = (String) entry.getValue();
		 * System.out.println("Key = " + entry.getKey() + ", Value = " +
		 * entry.getValue()); } }
		 */

		// String did[]=(String[]) variablesMap.get("id");
		// System.out.println(" AAAA" + testlist.size());
		// System.out.println("BBB" + testlist.get(0));
		// System.out.println("process variables" + variablesMap );
		// System.out.println(variablesMap.get("id"));
		// System.out.println(variablesMap.get("distortion_entity_id"));
		//List<ImageDistortion> distortionlist = (List<ImageDistortion>) job.getVariable("childdistortions");
		
				List<List<Object>> distortionlist = (List<List<Object>>) job.getVariable("childdistortions");
				
				
				
//				List<ImageDistortion> imgdis =  (List<ImageDistortion>) distortionlist.get(0);
				
				List<Object> imgdis =  (List<Object>) distortionlist.get(0);
				//distortionlist = distortionlist.get(0);
				System.out.println("distortion list input data" + imgdis.get(0));
//				map.put("distortionid", distortionlist.get(0).toString());
//				if (testlist.size() > 0 && testlist.get(0) != -1) {
//					map.put("hasDistortion", true);
//				} else {
//					map.put("hasDistortion", false);
//				}
				//Object[] disarray = (ImageDistortion)distortionlist.get(0);
//				System.out.println(disarray);
				Set<ImageDistortion> decisioninput = new HashSet<>();
				map.put("hasDistortion", true);
				for (Object dis : imgdis) {
//					System.out.println(dis.getDistortionclass());
//				}
				
					System.out.println(dis);
					ObjectMapper objectMapper = new ObjectMapper();
					Map<String, Object> disinput = objectMapper.convertValue(dis, new TypeReference<Map<String, Object>>() {
					});
					int distortionid = (int)disinput.get("distortion_type_id");
					if(distortionid == -1) {
						map.put("hasDistortion", false);
						break;
					}
					ImageDistortion imgdistortion = new ImageDistortion();
				    imgdistortion.setDistortionid((int)disinput.get("distortion_type_id"));
				    imgdistortion.setDistortionclass((String)disinput.get("class"));
				   // if(!decisioninput.contains(imgdistortion)) {
				    	decisioninput.add(imgdistortion);
				   // }
			
				    	
				    	
			}
				
		        System.out.println(decisioninput);
				
		        map.put("distortionlist",decisioninput);
		       List<List<String>> imageList = (List<List<String>>) variablesMap.get("image_base64");
		       String  imageBase64 = imageList.get(0).get(0);
		        imageBase64 = imageBase64.substring(3,imageBase64.length()-1);
		        map.put("image_base64", imageBase64);
		        
		        
				client.newCompleteCommand(job.getKey()).variables(map).send();
			}

	@JobWorker(type = "resolvenotification")
	public void SendNotification(final JobClient client, final ActivatedJob job) {
		System.out.println("Distortion has been resolved");
	}
	
	@JobWorker(type = "getManualDisortions",autoComplete = false)
	public void manualDisortions(final JobClient client, final ActivatedJob job) {
		Set<ImageDistortion> decisioninput = new HashSet<>();
		ImageDistortion imgDisortion = new ImageDistortion();
		Map<String,Object> map = job.getVariablesAsMap();
		imgDisortion.setDistortionclass((String) map.get("manualDistortionClass"));
		imgDisortion.setDistortionid((int) map.get("manualDistortionID"));
		decisioninput.add(imgDisortion);
		map.put("distortionlist", decisioninput );
		client.newCompleteCommand(job.getKey()).variables(map).send();
		
	}
}
