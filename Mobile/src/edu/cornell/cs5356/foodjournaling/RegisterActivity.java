package edu.cornell.cs5356.foodjournaling;

import org.json.JSONObject;

import edu.cornell.cs5356.foodjournaling.user.DatabaseHandler;
import edu.cornell.cs5356.foodjournaling.user.UserFunctions;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends Activity {
	EditText inputUsername;
	EditText inputEmail;
	EditText inputPassword;
	TextView registerErrorMsg;
	Button btnRegister;

	// JSON Response node names
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_message";
	private static String KEY_UID = "uid";
	private static String KEY_NAME = "username";
	private static String KEY_EMAIL = "email";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set View to register.xml
		setContentView(R.layout.register);

		inputUsername = (EditText) findViewById(R.id.reg_fullname);
		inputEmail = (EditText) findViewById(R.id.reg_email);
		inputPassword = (EditText) findViewById(R.id.reg_password);
		registerErrorMsg = (TextView) findViewById(R.id.reg_error);

		btnRegister = (Button) findViewById(R.id.btnRegister);
		// Login button Click Event
		btnRegister.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				String username = inputUsername.getText().toString();
				String email = inputEmail.getText().toString();
				String password = inputPassword.getText().toString();
				new RegisterUserTask().execute(username, email, password,
						password);
			}
		});

		TextView loginScreen = (TextView) findViewById(R.id.link_to_login);

		// Listening to Login Screen link
		loginScreen.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				// Closing registration screen
				// Switching to Login Screen/closing register screen
				finish();
			}
		});
	}

	private class RegisterUserTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String result = "0";
			// TODO Auto-generated method stub
			UserFunctions userFunction = new UserFunctions();
			Log.d("Button", "Register");
			JSONObject json = userFunction.registerUser(params[0], params[1],
					params[2], params[3]);

			// check for login response
			try {
				if (json.getString(KEY_SUCCESS) != null) {
					// registerErrorMsg.setText("");
					String res = json.getString(KEY_SUCCESS);
					if (Integer.parseInt(res) == 1) {
						// user successfully registred
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
				// Launch Dashboard Screen
				Intent dashboard = new Intent(getApplicationContext(),
						MainMenuActivity.class);
				// Close all views before launching Dashboard
				dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(dashboard);
			} else {
				registerErrorMsg.setText(result);
			}
		}
	}
}
