package com.andspot.teflon;



import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.tidy.Tidy;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.andspot.jsonk.JSONArray;
import com.andspot.jsonk.JSONException;
import com.andspot.jsonk.JSONObject;

public class Teflon {

	public JSONObject FindByURL(String url) throws JSONException{
		String packagename = url.replace("https://market.android.com/details?id=", "").split("&")[0];
		return FindByPackageName(packagename);

	
	}
	
	public JSONObject FindByPackageName(String packagename) throws JSONException{
		URL u;
		try {
			u = new URL("https://market.android.com/details?id="+packagename);
		
		System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
        Document document;
	
			document = Jsoup.parse(u,90000);
	
        String rating;
		String votes;
		try{
			rating = document.getElementsByClass("average-rating-value").get(0).html();
			votes = document.getElementsByClass("votes").get(0).html();
        }catch(Exception e){
        	 rating = "0";
        	 votes = "0";
        }
        String appName = document.getElementsByClass("doc-banner-title").html();
        String desc = document.getElementById("doc-original-text").html();
        String downloadtext = "";
        	
        String entireDoc = document.toString();
        
        		if(entireDoc.contains("5,000 - 10,000")){
        			downloadtext = "5,000 - 10,000";
        		}else if(entireDoc.contains("50,000 - 100,000")){
        			downloadtext = "50,000 - 100,000";
        		}else if(entireDoc.contains("100,000 - 500,000")){
        			downloadtext = "100,000 - 500,000";
        		}else if(entireDoc.contains("500,000 - 1,00,000")){
        			downloadtext = "500,000 - 1,00,000";
        		}else if(entireDoc.contains("1,000,000 - 5,000,000")){
        			downloadtext = "1,000,000 - 5,000,000";
        		}else if(entireDoc.contains("1,000 - 5,000")){
        			downloadtext = "1,000 - 5,000";
        		}else if(entireDoc.contains("500 - 1,000")){
        			downloadtext = "500 - 1,000";
        		}else{
        			downloadtext = "0 - 500";
        		}
        		
        Document sc = Jsoup.parse(document.getElementsByClass("doc-overview-screenshots").html());
        Elements imgs = sc.select("img");
        ArrayList<String> screenshots = new ArrayList<String>();
        
        for (Element img : imgs) {
            screenshots.add(img.attr("src"));
            
        }
        
        screenshots.remove(0);
        screenshots.remove(screenshots.size()-1);
 
        
        Document ic = Jsoup.parse(document.getElementsByClass("doc-banner-icon").html());
        Elements icons  = ic.select("img");
        String icon = "";
        for (Element img : icons) {
        	 icon = img.attr("src");
        }
        
        Document pr = Jsoup.parse(document.getElementsByClass("doc-banner-image-container").html());
        Elements promo = pr.select("img");
        String promoURL = "";
        for (Element img : promo){
        	promoURL = img.attr("src");
        }
        String category ;
     
       Document cr = Jsoup.parse(u,9000);
       Elements a = cr.getElementsByClass("doc-metadata-list");
       String contentRating = cr.getElementsByAttributeValue("itemprop", "contentRating").html();
       String version = cr.getElementsByAttributeValue("itemprop","softwareVersion").html();
       String lastUpdate = cr.getElementsByAttributeValue("itemprop", "datePublished").html();
       downloadtext = cr.getElementsByAttributeValue("itemprop", "numDownloads").html();
       String filesize = cr.getElementsByAttributeValue("itemprop", "fileSize").html();
       String price = cr.getElementsByAttributeValue("itemprop", "price").html();
       Elements b=  a.select("a");
      category = b.get(0).text();
       

       
   


  
	        JSONObject jb = new JSONObject();
	        jb.put("RATING", rating);
	        jb.put("VOTES", votes);
	        jb.put("APPNAME", appName);
	        jb.put("DESC", desc);
	        jb.put("SCREENSHOTS", screenshots);
	        jb.put("ICON", icon);
	        jb.put("PROMOURL", promoURL);
	        jb.put("CATEGORY", category);
	        jb.put("PRICE",price);      
	        jb.put("DOWNLOADTEXT",  downloadtext);
	        jb.put("LASTUPDATE", lastUpdate);
	        jb.put("CONTENTRATING", contentRating);
	        jb.put("VERSION", version);
	        jb.put("FILESIZE", filesize);
	  
			return jb;
			
		} catch (IOException e1) {
			e1.printStackTrace();
			return new JSONObject();
		}
		
  
	
	}
	
	public boolean uploadS3(String bucket,String filename,String url) throws IOException{
	
		 URL u = new URL(url);
		    URLConnection uc = u.openConnection();
		    String contentType = uc.getContentType();
		    int contentLength = uc.getContentLength();
		    if (contentType.startsWith("text/") || contentLength == -1) {
		      throw new IOException("This is not a binary file.");
		    }
		    InputStream raw = uc.getInputStream();
		    InputStream in = new BufferedInputStream(raw);
		  AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAJEWZOWEIE5FLA6TQ","yKYzd4BObXCCy5BSfTogrxFHEs85n9rALu3pgx9m");
		AmazonS3 s3 = new AmazonS3Client(awsCredentials);
		s3.putObject(new PutObjectRequest(bucket, filename, in, null));
		
		return true;
	}
	public String getVersionNumber(String aaptOutput){
		
		int x = aaptOutput.indexOf("sdkVersion:'")+12;
		int y = aaptOutput.indexOf("'", x);
		String sdkVersion = aaptOutput.substring(x, y);
		
		return sdkVersion;
	}
}
