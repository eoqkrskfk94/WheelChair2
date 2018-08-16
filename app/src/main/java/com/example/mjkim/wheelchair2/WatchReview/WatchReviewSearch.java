package com.example.mjkim.wheelchair2.WatchReview;

import android.os.AsyncTask;
import android.text.Html;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class WatchReviewSearch extends AsyncTask<String, Void, ArrayList<WatchReviewList>> {

    final static String clientId = "3StaOAzcpbFU3A798Ahq";//애플리케이션 클라이언트 아이디값";
    final static String clientSecret = "NIwOzZz_Ot";//애플리케이션 클라이언트 시크릿값";
    public static int total_num = 0;
    public static int display = 0;


    private ArrayList<String> review_result;


    @Override
    protected void onPreExecute() { super.onPreExecute(); }


    @Override
    protected ArrayList<WatchReviewList> doInBackground(String... strings) {


        String result = getJson(strings[0]);

        String name;
        String link;
        String description;
        String bloggerlink;
        String bloggername;
        String postdate;



        //파서기 시작
        review_result = new ArrayList<String>();
        ArrayList<WatchReviewList> watchReviewLists = new ArrayList<WatchReviewList>();

        try {

            String new_response = getJson(strings[0]); //파서할 새로움 response
            JSONObject jsonObj = new JSONObject(new_response);

            JSONArray items = jsonObj.getJSONArray("items");
            total_num = jsonObj.getInt("total");

            int num = 0;

            for (int i = 0; i < items.length(); i++) {

                JSONObject obj = items.getJSONObject(i);
                name = obj.getString("title");
                name = Html.fromHtml(name).toString();
                link = obj.getString("link");
                link = Html.fromHtml(link).toString();
                description = obj.getString("description");
                description = Html.fromHtml(description).toString();
                bloggerlink = obj.getString("bloggerlink");
                bloggername = obj.getString("bloggername");
                postdate = obj.getString("postdate");


                watchReviewLists.add(num++, new WatchReviewList(name, link, description, bloggerlink,bloggername, postdate));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        //파서기 끝
        return watchReviewLists;

    }



    @Override
    protected void onProgressUpdate(Void...params){ }

    @Override
    protected void onPostExecute(ArrayList<WatchReviewList> result){
        super.onPostExecute(result);
    }




    public String getJson(String string) {

        String response = "";

        try {
            display = 20; //총 결과물 갯수
            String text = URLEncoder.encode(string, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/search/blog?query="+ text + "&display=" + display + "&"; // json 결과
            //String apiURL = "https://openapi.naver.com/v1/search/blog.xml?query="+ text; // xml 결과
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer temp_response = new StringBuffer(); //문자열 담는 객체
            while ((inputLine = br.readLine()) != null) {
                temp_response.append(inputLine);
                temp_response.append("\n");
            }
            br.close();
            response = temp_response.toString();



        } catch (Exception e) {
        }
        return response;
    }
}