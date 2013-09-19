import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL; 

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Application {
	//star rating
	//5 star - 100%
	//4 star - 80%
	//3 star - 60%
	//2 star - 40%
	//1 star - 20%
	private static final String USER_AGENT = "Mozilla/5.0";
	private static final String url = "https://play.google.com/store/getreviews";
	private static final int MAX_REVIEW_COUNT = 2000;
	private static final String TEMPLE_RUN_2 = "com.imangi.templerun2";
	private static final String DESPICABLE_ME = "com.gameloft.android.ANMP.GloftDMHM";
	private static final String SUBWAY_SURF = "com.kiloo.subwaysurf";
	
	private String applicationId = "";
	private int pageNo = 0;
	private int review_count = 1;
	private static boolean end = false;
	private static File file;
	
	public Application(String applicationId){
		this.applicationId = applicationId;
		 file = new File(applicationId+".xml");		 
	}
	
	public static void main(String[] args) {
		Application http = new Application(DESPICABLE_ME);		
		try{			
			
			System.out.println("Send Http POST request");
			file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			
			fw.write("<app id="+http.applicationId+"\">\n");
			
			fw.close();
			while(!end){	
				http.sendPost();				
			}
			
		}catch(Exception e){
			System.out.println(e);
		}

	}
	// HTTP POST request
	private void sendPost() throws Exception {	 			
		
 
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		//add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setDoOutput(true);
 
		String urlParameters = "reviewType=0&pageNum="+pageNo+"&id="+applicationId+"&reviewSortOrder=2&xhr=1";
 
		// Send post request		
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		
		StringBuffer response = new StringBuffer();
		in.readLine();
		in.readLine();
		while ((inputLine = in.readLine()) != null) {			
			response.append(inputLine);
		}
	
		//print result
		JSONParser parser=new JSONParser();
		Object o = parser.parse(response.toString());
		JSONArray arr = (JSONArray)o;
		JSONArray o2 = (JSONArray) arr.get(0);
		
		in.close();
		if(o2.size()!=2 && review_count < MAX_REVIEW_COUNT){
			processResult((String)o2.get(2));
			pageNo++;
		}else{
			end = true;		
		}
		con.disconnect();
		
	}
	private void processResult(String body){
		String xmlToWrite = "";
		Document doc = Jsoup.parseBodyFragment(body);
		//System.out.println(doc.html());
		Elements reviews = doc.getElementsByClass("single-review");
		for (Element review : reviews){
			String ratingCss = review.getElementsByClass("current-rating").first().attr("style");
			ratingCss = ratingCss.substring(7,ratingCss.length()-4);
			int rating = Integer.parseInt(ratingCss)/20;
			
			xmlToWrite += "<doc id=\""+review_count+"\">\n";
			xmlToWrite += "<name>"+review.select("a[title~=.*]").first().text() +"</name>\n";
			xmlToWrite += "<date>"+review.getElementsByClass("review-date").first().text()+"</date>\n";			
			xmlToWrite += "<rating>"+rating+"</rating>\n";
			xmlToWrite += "<title>"+review.getElementsByClass("review-title").first().text()+"</title>\n";
			xmlToWrite += "<review>"+review.getElementsByClass("review-body").first().childNode(2)+"</review>\n";
			xmlToWrite += "</doc>\n";
			review_count++;
		}
		try{
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			fw.write(xmlToWrite);
			fw.close();
		}catch(Exception e){
			System.out.println(e);
		}
		
	}
}
