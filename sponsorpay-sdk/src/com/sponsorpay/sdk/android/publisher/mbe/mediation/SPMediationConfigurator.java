/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2013 SponsorPay. All rights reserved.
 */

package com.sponsorpay.sdk.android.publisher.mbe.mediation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;

public class SPMediationConfigurator {

	private static final String TAG = "SPMediationConfigurator";
	
	public static SPMediationConfigurator INSTANCE = new SPMediationConfigurator();

	private HashMap<String, HashMap<String, Object>> configurations;
	
	private SPMediationConfigurator() {
		configurations = new HashMap<String, HashMap<String, Object>>();
	}
	
	public List<String> getMediationAdaptors() {
		SponsorPayLogger.d(TAG, "Getting compatible adapters for SDK v" + SponsorPay.RELEASE_VERSION_STRING );
		LinkedList<String> adaptors = new LinkedList<String>();
		adaptors.add("com.sponsorpay.sdk.mbe.mediation.MockMediatedAdaptor");
		
		return adaptors;
	}
	
//	public List<String> getMediationAdaptors() {
//		Package package1 = Package.getPackage( "com.sponsorpay.sdk.mbe.mediation");
//		String pkgname = package1.getName();
//		
//		String relPath = pkgname.replace('.', '/');
//
//		// Get a File object for the package
//		URL resource = ClassLoader.getSystemClassLoader().getResource("/" + relPath);
//		
//		if (resource == null) {
//			try {
//				Class.forName("com.sponsorpay.sdk.mbe.mediation.MockMediatedAdaptor");
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
////			throw new RuntimeException("Unexpected problem: No resource for " + relPath);
//		}
////		
////		resource.getPath();
//		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
//		if(resource.toString().startsWith("jar:")) {
//			processJarfile(resource, pkgname, classes);
//		} else {
//			processDirectory(new File(resource.getPath()), pkgname, classes);
//		}
//		
//		
////		for (SPMediationAdaptor service : ServiceLoader.load(SPMediationAdaptor.class)) {
////			SponsorPayLogger.d(TAG, "New service found - " + service.getName());
//////			if (service.videosAvailable()) {
////////			if (service.supports(o)) {
////////				return service.handle(o);
//////			}
////		}
//		
//		
//		URL systemResource = ClassLoader.getSystemResource("/resources");
////		if (systemResource == null) {
////			systemResource =  Thread.currentThread().getContextClassLoader().getResource("/");
////		}
////		
////		if (systemResource == null) {
////			systemResource =  Thread.currentThread().getContextClassLoader().getResource("/data/data/com.sponsorpay.sdk.android.testapp/lib");
////		}
////		
////		
//		if (systemResource == null) {
//			this.getClass().getResource("/resources");
//		}
//		if (systemResource == null) {
//			Thread.currentThread().getContextClassLoader().getResource("/resources");
//		}
//		
//		
//		
//		if (systemResource == null) {
//			try {
//				Class a = Class.forName("com.sponsorpay.sdk.mbe.mediation.MockMediatedAdaptor");
//				a.getPackage();
//				a.getClassLoader();
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
////		URL systemResource = ClassLoader.getSystemResource("/com.sponsorpay.sdk.android.publisher.mbe.mediation.SPMediationAdaptor");
//		
//		File file = new File(systemResource.getPath());
//		if (file.isDirectory()) {
//			file.listFiles();
//		}
//		
//		
//		return null;
//	}
//
//	
//	private static Class<?> loadClass(String className) {
//		try {
//			return Class.forName(className);
//		} 
//		catch (ClassNotFoundException e) {
//			throw new RuntimeException("Unexpected ClassNotFoundException loading class '" + className + "'");
//		}
//	}
//
//	private static void processDirectory(File directory, String pkgname, ArrayList<Class<?>> classes) {
//		SponsorPayLogger.d(TAG, "Reading Directory '" + directory + "'");
//		// Get the list of the files contained in the package
//		String[] files = directory.list();
//		for (int i = 0; i < files.length; i++) {
//			String fileName = files[i];
//			String className = null;
//			// we are only interested in .class files
//			if (fileName.endsWith(".class")) {
//				// removes the .class extension
//				className = pkgname + '.' + fileName.substring(0, fileName.length() - 6);
//			}
//			SponsorPayLogger.d(TAG, "FileName '" + fileName + "'  =>  class '" + className + "'");
//			if (className != null) {
//				classes.add(loadClass(className));
//			}
//			File subdir = new File(directory, fileName);
//			if (subdir.isDirectory()) {
//				processDirectory(subdir, pkgname + '.' + fileName, classes);
//			}
//		}
//	}
//	
//	private static void processJarfile(URL resource, String pkgname, ArrayList<Class<?>> classes) {
//		String relPath = pkgname.replace('.', '/');
//		String resPath = resource.getPath();
//		String jarPath = resPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
//		SponsorPayLogger.d(TAG, "Reading JAR file: '" + jarPath + "'");
//		JarFile jarFile;
//		try {
//			jarFile = new JarFile(jarPath);         
//		} catch (IOException e) {
//			throw new RuntimeException("Unexpected IOException reading JAR File '" + jarPath + "'", e);
//		}
//		Enumeration<JarEntry> entries = jarFile.entries();
//		while(entries.hasMoreElements()) {
//			JarEntry entry = entries.nextElement();
//			String entryName = entry.getName();
//			String className = null;
//			if(entryName.endsWith(".class") && entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length())) {
//				className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
//			}
//			SponsorPayLogger.d(TAG, "JarEntry '" + entryName + "'  =>  class '" + className + "'");
//			if (className != null) {
//				classes.add(loadClass(className));
//			}
//		}
//	}
	
	public HashMap<String, Object> getConfigurationForAdaptor(String adaptor) {
		return configurations.get(adaptor);
	}
	
	public boolean setConfigurationForAdaptor(String adaptor, HashMap<String, Object> configurations) {
		return this.configurations.put(adaptor, configurations) != null;
	}
	
}
