package edu.cornell.cs5356.foodjournaling;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.cornell.cs5356.foodjournaling.user.DatabaseHandler;
import edu.cornell.cs5356.foodjournaling.user.UserFunctions;

public class LoginActivity extends ActionBarActivity {

	EditText inputUsername;
	EditText inputPassword;
	TextView loginErrorMsg;
	Button btnLogin;
	TextView registerScreen;
	TextView hackLogin;

	String username, password;
	// JSON Response node names
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_message";
	private static String KEY_UID = "uid";
	private static String KEY_NAME = "username";
	private static String KEY_EMAIL = "email";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		inputUsername = (EditText) findViewById(R.id.loginUsername);
		inputPassword = (EditText) findViewById(R.id.loginPassword);
		loginErrorMsg = (TextView) findViewById(R.id.login_error);
		btnLogin = (Button) findViewById(R.id.btnLogin);

		// Login button Click Event
		btnLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				username = inputUsername.getText().toString();
				password = inputPassword.getText().toString();
				new LoginUserTask().execute(username, password);
			}
		});

		registerScreen = (TextView) findViewById(R.id.link_to_register);
		// Listening to register new account link
		registerScreen.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Switching to Register screen
				Intent i = new Intent(getApplicationContext(),
						RegisterActivity.class);
				startActivity(i);
			}
		});

		hackLogin = (TextView) findViewById(R.id.hack_to_login);
		// Listening to register new account link
		hackLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Switching to Register screen
				Intent i = new Intent(getApplicationContext(),
						MainMenuActivity.class);
				startActivity(i);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class LoginUserTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String result = "0";
			// TODO Auto-generated method stub
			UserFunctions userFunction = new UserFunctions();
			Log.d("Button", "Login");
			JSONObject json = userFunction.loginUser(params[0], params[1]);
			
			// check for login response
			try {
				if (json.getString(KEY_SUCCESS) != null) {
					//loginErrorMsg.setText("");
					String res = json.getString(KEY_SUCCESS);
					if (Integer.parseInt(res) == 1) {
						// user successfully logged in
						// Store user details in SQLite Database

						DatabaseHandler db = new DatabaseHandler(
								getApplicationContext());

						// Clear all previous data in database
						userFunction.logoutUser(getApplicationContext());
						db.addUser(json.getString(KEY_NAME),
								json.getString(KEY_EMAIL),
								json.getString(KEY_UID));
						
						result = "1";
						
					} else {
						System.out.println("!!!: " + json.getString(KEY_ERROR_MSG));
						result = json.getString(KEY_ERROR_MSG);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equals("1")) {
				SharedPreferences prefs = getSharedPreferences("Letseat", MODE_PRIVATE);
				prefs.edit().putString("username", username).commit();
				// Launch Dashboard Screen
				Intent dashboard = new Intent(
						getApplicationContext(),
						MainMenuActivity.class);

				// Close all views before launching Dashboard
				dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(dashboard);
			} else {
				loginErrorMsg.setText(result);
			}
		}
	}
}
