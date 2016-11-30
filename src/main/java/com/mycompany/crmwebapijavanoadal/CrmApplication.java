package com.mycompany.crmwebapijavanoadal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class CrmApplication {

    //This was registered in Azure AD as a NATIVE CLIENT APPLICATION
    //Azure Application Client ID
    private final static String CLIENT_ID = "00000000-0000-0000-0000-000000000000";
    //CRM URL
    private final static String RESOURCE = "https://org.crm.dynamics.com";
    //O365 credentials for authentication w/o login prompt
    private final static String USERNAME = "crmadmin@org.onmicrosoft.com";
    private final static String PASSWORD = "password";
    //Azure Directory OAUTH 2.0 AUTHORIZATION ENDPOINT
    private final static String AUTHORITY = "https://login.microsoftonline.com/00000000-0000-0000-0000-000000000000/oauth2/token";

    public static void main(String args[]) throws Exception {

        //No prompt for credentials
        String token = GetToken();
        System.out.println("Access Token - " + token);

        String userId = WhoAmI(token);
        System.out.println("UserId - " + userId);

        String fullname = FindFullname(token, userId);
        System.out.println("Fullname: " + fullname);

        String accountId = CreateAccount(token, "Java Test");
        System.out.println("Created: " + accountId);

        accountId = UpdateAccount(token, accountId);
        System.out.println("Updated: " + accountId);

        accountId = DeleteAccount(token, accountId);
        System.out.println("Deleted: " + accountId);
    }

    private static String DeleteAccount(String token, String accountId) throws MalformedURLException, IOException {
        HttpURLConnection connection = null;
        URL url = new URL(RESOURCE + "/api/data/v8.0/accounts(" + accountId + ")");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("OData-MaxVersion", "4.0");
        connection.setRequestProperty("OData-Version", "4.0");
        connection.addRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        connection.connect();

        int responseCode = connection.getResponseCode();

        return accountId;
    }

    private static String UpdateAccount(String token, String accountId) throws MalformedURLException, IOException, URISyntaxException {
        JSONObject account = new JSONObject();
        account.put("websiteurl", "http://www.microsoft.com");

        HttpURLConnection connection = null;
        URL url = new URL(RESOURCE + "/api/data/v8.0/accounts(" + accountId + ")");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        connection.setRequestMethod("POST");
        connection.setRequestProperty("OData-MaxVersion", "4.0");
        connection.setRequestProperty("OData-Version", "4.0");
        connection.setRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Authorization", "Bearer " + token);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.connect();

        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write(account.toJSONString());
        out.flush();
        out.close();

        int responseCode = connection.getResponseCode();

        return accountId;
    }

    private static String CreateAccount(String token, String name) throws MalformedURLException, IOException {
        JSONObject account = new JSONObject();
        account.put("name", name);
        account.put("primarycontactid@odata.bind", "/contacts(A33605D9-A6A0-E611-80EA-C4346BACDA3C)");

        HttpURLConnection connection = null;
        URL url = new URL(RESOURCE + "/api/data/v8.0/accounts");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("OData-MaxVersion", "4.0");
        connection.setRequestProperty("OData-Version", "4.0");
        connection.setRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Authorization", "Bearer " + token);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setDoOutput(true);
        connection.connect();

        BufferedWriter out
                = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        out.write(account.toJSONString());
        out.close();

        int responseCode = connection.getResponseCode();

        String headerId = connection.getHeaderField("OData-EntityId");

        String accountId = headerId.split("[\\(\\)]")[1];
        return accountId;
    }

    private static String FindFullname(String token, String userId) throws MalformedURLException, IOException {
        HttpURLConnection connection = null;
        URL url = new URL(RESOURCE + "/api/data/v8.0/systemusers(" + userId + ")?$select=fullname");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("OData-MaxVersion", "4.0");
        connection.setRequestProperty("OData-Version", "4.0");
        connection.setRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Authorization", "Bearer " + token);

        int responseCode = connection.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Object jResponse;
        jResponse = JSONValue.parse(response.toString());
        JSONObject jObject = (JSONObject) jResponse;
        String fullname = jObject.get("fullname").toString();
        return fullname;
    }

    private static String WhoAmI(String token) throws MalformedURLException, IOException {
        HttpURLConnection connection = null;
        URL url = new URL(RESOURCE + "/api/data/v8.0/WhoAmI");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("OData-MaxVersion", "4.0");
        connection.setRequestProperty("OData-Version", "4.0");
        connection.setRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Authorization", "Bearer " + token);

        int responseCode = connection.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Object jResponse;
        jResponse = JSONValue.parse(response.toString());
        JSONObject jObject = (JSONObject) jResponse;
        String userId = jObject.get("UserId").toString();
        return userId;
    }

    private static String GetToken() throws MalformedURLException, IOException {

        String body = "resource=" + RESOURCE
                + "&client_id=" + CLIENT_ID
                + "&grant_type=password&username=" + USERNAME
                + "&password=" + PASSWORD
                + "&scope=openid";

        HttpURLConnection connection = null;
        URL url = new URL(AUTHORITY);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        connection.setDoOutput(true);
        connection.connect();

        BufferedWriter out
                = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        out.write(body);
        out.close();

        int responseCode = connection.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Object jResponse;
        jResponse = JSONValue.parse(response.toString());
        JSONObject jObject = (JSONObject) jResponse;
        String access_token = jObject.get("access_token").toString();
        return access_token;
    }
}
