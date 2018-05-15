package com.racetime.xsad.service;

import java.util.List;

public interface MediaFileValidate {
	
	public List<String> MediaRTBValidate(String filePath);
	
	public List<String> MediaPMPValidate(String filePath);
	
	public List<String> MediaMediaMaterial(String filePath,String descDir);
	
	
}
