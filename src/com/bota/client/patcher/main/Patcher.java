package com.bota.client.patcher.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;

import com.bota.client.patcher.util.PathUtil;

public class Patcher {

	public static Map<String, String> getLocalHash() throws IOException {
		ArrayList<File> files = new ArrayList<File>();
		PathUtil.listf("files", files);
		HashMap<String, String> hashMap = new HashMap<String, String>();

		for (File file : files) {
			FileInputStream fileInputStream = new FileInputStream(file);
			hashMap.put(file.getPath().replace("\\", "/"),DigestUtils.md5Hex(fileInputStream));
		}

		return hashMap;
	}
	public static Map<String, String> getServerHash(String source) throws IOException {
		HashMap<String, String> hashMap = new HashMap<String, String>();

		URL serverURL = new URL(source);
		try (BufferedReader streamReader = new BufferedReader(new InputStreamReader(serverURL.openStream()))) {
			String line;
			while ((line = streamReader.readLine()) != null) {
				String[] parts = line.split("\\$");
				hashMap.put(parts[0], parts[1]);
			}
		}

		return hashMap;
	}
	public static Properties loadConfig() throws IOException {
		File configFile = new File("config.properties");
		if (!configFile.exists()) {
			configFile.createNewFile();
		}
		FileInputStream configInputStream = new FileInputStream(configFile);
		Properties configProperties = new Properties();
		configProperties.load(configInputStream);

		if (!configProperties.containsKey("server.port"))
			configProperties.put("server.port", "80");
		if (!configProperties.containsKey("server.protocol"))
			configProperties.put("server.protocol", "http");
		if (!configProperties.containsKey("server.host"))
			configProperties.put("server.host", "localhost");

		FileOutputStream configOutputStream = new FileOutputStream(configFile);
		configProperties.store(configOutputStream, "BotA Game Patcher Properties File");

		return configProperties;

	}
	public static void downloadFiles(Object[] sources, String baseURL) throws IOException {
		for (int i = 0; i < sources.length; i++) {
			System.out.println("Downloading file: " + baseURL + (String) sources[i]);
			URL website = new URL(baseURL + (String) sources[i]);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			new File((String) sources[i]).getParentFile().mkdirs();
			try (FileOutputStream fos = new FileOutputStream((String) sources[i])) {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			}
		}
	}
	public static void main(String[] args) throws IOException {
		Properties configProperties = loadConfig();
		
		if (!new File("files").exists())
			new File("files").mkdir();
		
		String serverURL = configProperties.getProperty("server.protocol") + "://" + configProperties.getProperty("server.host") + ":" + configProperties.getProperty("server.port") + "/";
		Map<String, String> serverHashMap = getServerHash(serverURL + "hash");

		Map<String, String> localHashMap = getLocalHash();

		Object[] localFilesObjectArray = localHashMap.keySet().toArray();
		Object[] serverFilesObjectArray = serverHashMap.keySet().toArray();

		String[] localFilesArray = Arrays.copyOf(localFilesObjectArray, localFilesObjectArray.length, String[].class);
		String[] serverFilesArray = Arrays.copyOf(serverFilesObjectArray, serverFilesObjectArray.length, String[].class);
		List<String> localFiles = Arrays.asList(localFilesArray);
		List<String> serverFiles = Arrays.asList(serverFilesArray);

		ArrayList<String> targetFiles = new ArrayList<String>();
		for (String file : serverFiles) {
			if (!localFiles.contains(file)) {
				System.out.println("File not found: " + file);
				targetFiles.add(file);
			}
		}
		for (String file : serverFiles) {
			if (targetFiles.contains(file))
				break;
			String localHash = localHashMap.get(file);
			String serverHash =  serverHashMap.get(file);

			if (!localHash.equals(serverHash)) {
				System.out.println("Outdated file: " + file);
				targetFiles.add(file);
			}
		}
		downloadFiles(targetFiles.toArray(), serverURL);

		System.out.println("All files up to date.");
	}

}
