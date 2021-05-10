/*
 * Stork
 * Copyright (C) 2019 Tigase, Inc. (office@tigase.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */

package org.tigase.messenger.phone.pro.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;

import org.tigase.messenger.phone.pro.API.ApiCLient;
import org.tigase.messenger.phone.pro.API.ApiInterface;
import org.tigase.messenger.phone.pro.R;
import org.tigase.messenger.phone.pro.dynaform.DynamicForm;
import org.tigase.messenger.phone.pro.models.Verification;
import org.tigase.messenger.phone.pro.service.MobileModeFeature;
import org.tigase.messenger.phone.pro.service.SecureTrustManagerFactory;
import org.tigase.messenger.phone.pro.utils.AccountHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.TextPrivateField;
import tigase.jaxmpp.core.client.xmpp.forms.TextSingleField;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule;
import tigase.jaxmpp.core.client.xmpp.modules.registration.UnifiedRegistrationForm;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

import java.lang.ref.WeakReference;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CreateAccountActivity
		extends AppCompatActivity {

	private static final String TAG = "CreateAccountActivity";
	private DynamicForm dynamicForm;
	private View hostSelectorPanel;
	private AccountManager mAccountManager;
	private AccountCreationTask mAuthTask;
	private EditText mHostname;
	private Button nextButton;
	private Button prevButton;
	private View registrationFormPanel;
	private ListView trustedServers;
	private Pinview pinview;
	private View infocodetoken;
	private Button btnenvoisms;
	private View infousers;
	private TextView textviewnumero;
	private  String valeurnumero =null;
	private EditText editextnumero;
	private Button btncontinu2;
	private TextView textviewerrorcode;
	private TextView textviewlaststep;
	private View laststep;
	private Button btnconfirmer;
	private TextView textviewmsginserercode;
	private Button btnrenvoyercode;
	private EditText editextgivepassword;
	private ProgressBar progressBar;
	ArrayList<Verification> verifier = new ArrayList<Verification>();
	ApiInterface apiCLient = ApiCLient.getClient().create(ApiInterface.class);
	final ApiInterface apiCLient_db = ApiCLient.getClient_db().create(ApiInterface.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_account_activity);
		mAccountManager = AccountManager.get(this);

		mHostname = findViewById(R.id.hostname);

		mHostname.setText("xmppsrv.orange-bissau.com");
		dynamicForm = findViewById(R.id.registrationForm);
		hostSelectorPanel = findViewById(R.id.hostSelectPanel);
		registrationFormPanel = findViewById(R.id.registrationFormPanel);
		infocodetoken = findViewById(R.id.infocode);
		pinview = findViewById(R.id.codepinview);
		btnenvoisms = findViewById(R.id.btnenvoisms);
		infousers = findViewById(R.id.infousers);
		textviewnumero = findViewById(R.id.textviewnumero);
		editextnumero = findViewById(R.id.editextnumero);
		btnenvoisms = findViewById(R.id.btnenvoisms);
		btncontinu2 = findViewById(R.id.btncontinu2);
		textviewerrorcode = findViewById(R.id.textviewerrorcode);
		laststep = findViewById(R.id.lastsetp);
		textviewlaststep = findViewById(R.id.textviewlaststep);
		btnconfirmer = findViewById(R.id.btnconfirmer);
		textviewmsginserercode = findViewById(R.id.textviewmsginserercode);
		btnrenvoyercode = findViewById(R.id.btnrenvoyercode);
		editextgivepassword = findViewById(R.id.editextgivepassword);
		progressBar = findViewById(R.id.myProgressBar);
		laststep.setVisibility(View.GONE);
		infocodetoken.setVisibility(View.GONE);


		prevButton = findViewById(R.id.prev_button);
		prevButton.setOnClickListener(v -> showPage1());

		nextButton = findViewById(R.id.next_button);
		//nextButton.setOnClickListener(v -> onNextButton());

		prevButton.setVisibility(View.GONE); nextButton.setVisibility(View.GONE);
		hostSelectorPanel.setVisibility(View.GONE);
		btnenvoisms.setOnClickListener(v->saisienumero());
		btncontinu2.setOnClickListener(v->saisiecode(editextnumero.getText().toString()));
		btnconfirmer.setOnClickListener(v->confirmer());

		trustedServers = findViewById(R.id.trustedServersList);

		final List<String> trustedServerItems = Arrays.asList(getResources().getStringArray(R.array.trusted_servers));

		ArrayAdapter<String> h = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				trustedServerItems);
		trustedServers.setAdapter(h);
		trustedServers.setOnItemClickListener((parent, view, position, id) -> {
			startConnection(trustedServerItems.get(position));
		});

		//showPage1();
	}

	// method difference entre deux dates
	public long differencedate(Date date1, Date date2){
		long diff = date1.getTime() - date2.getTime();
		TimeUnit time = TimeUnit.SECONDS;
		long difference = time.convert(diff, TimeUnit.MILLISECONDS);
		return difference;
	}
	// Champ saisie numero
	private void saisienumero(){
		textviewnumero.setText(editextnumero.getText().toString());
		valeurnumero = editextnumero.getText().toString();
		progressBar.setVisibility(View.VISIBLE);
		if(editextnumero.getText().toString().equals("")){
			editextnumero.setError("Veuillez saisir votre numero téléphone");
		}
		else if(editextgivepassword.getText().toString().equals("")){
			editextgivepassword.setError("Veuillez saisir un mot de passe");
		}
		else {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					progressBar.setVisibility(View.GONE);
				}

			}, 10000);
			String message = "Votre code de confirmation est : " + GenererCode();
			Call<Verification> call = apiCLient_db.getVerificationByNumero(editextnumero.getText().toString());
			call.enqueue(new Callback<Verification>() {
				@Override
				public void onResponse(Call<Verification> call, Response<Verification> response) {
					Log.i(TAG, "onResponse verifier numero en succes1: "+response.isSuccessful());
					Log.i(TAG, "onResponse verifier numero en succes2: "+response.body());
					Log.i(TAG, "onResponse verifier numero en succes3: "+response.errorBody());
					Log.i(TAG, "onResponse verifier numero en succes4: "+response.toString());
					progressBar.setVisibility(View.GONE);
					SendSMS2(editextnumero.getText().toString(), message, "OBIM");

					infousers.setVisibility(View.GONE);
					infocodetoken.setVisibility(View.VISIBLE);
					btnrenvoyercode.setVisibility(View.VISIBLE);
					btncontinu2.setVisibility(View.VISIBLE);
					btnenvoisms.setVisibility(View.GONE);
					btnrenvoyercode.setOnClickListener(v -> SendSMS2(editextnumero.getText().toString(), message, "OBIM"));
					textviewmsginserercode.setText("Inserer le code envoyé par SMS au : ");
				}

				@Override
				public void onFailure(Call<Verification> call, Throwable t) {
					Log.i(TAG, "onFailure verifier numero message1: " + t);
					Log.i(TAG, "onFailure verifier numero message3: " + t.getMessage());

					if(t.getMessage().contains("Failed to connect to")||t.getMessage().contains("timeout")) {
						Toast.makeText(getApplicationContext(),"Erreur connexion serveur, veuillez reessayer",Toast.LENGTH_SHORT).show();
					}
					else {
						if (t.getCause() == null) {
							SendSMS(editextnumero.getText().toString(), message, "OBIM");
							textviewmsginserercode.setText("Inserer le code envoyé par SMS au : ");
							btnenvoisms.setVisibility(View.GONE);
							infocodetoken.setVisibility(View.VISIBLE);
							infousers.setVisibility(View.GONE);
							btncontinu2.setVisibility(View.VISIBLE);
							btnrenvoyercode.setVisibility(View.VISIBLE);
							btnrenvoyercode.setOnClickListener(v -> SendSMS2(editextnumero.getText().toString(), message, "OBIM"));

						}
					}
				}
			});

		}
	}


		// method save verification
	private void SaveVerification(String numero,String code, Integer etat){
		Log.i(TAG, "SaveVerification: numero : "+numero+" code : "+code);

		Call<Verification> call = apiCLient_db.saveVerification1(numero, code,1);
		call.enqueue(new Callback<Verification>() {
			@Override
			public void onResponse(Call<Verification> call, Response<Verification> response) {
				Log.i(TAG, "SAVE TABLE VERIFICATION REUSSIE: ");
			}

			@Override
			public void onFailure(Call<Verification> call, Throwable t) {
				Log.i(TAG, "ECHEC SAVE TABLE VERIFICATION "+t);
			}
		});
	}

	private void UpdateVerification(String numero,String code){

		Call<Verification> call = apiCLient_db.UpdateVerification(numero,code);
		call.enqueue(new Callback<Verification>() {
			@Override
			public void onResponse(Call<Verification> call, Response<Verification> response) {
				Log.i(TAG, "UPDATE VERIFICATION REUSSIE: ");
			}

			@Override
			public void onFailure(Call<Verification> call, Throwable t) {
				Log.i(TAG, "UPDATE VERIFICATION ECHEC: "+t);
			}
		});

	}

	private void SendSMS(String numero, String message, String expediteur){
		Call<String> call = apiCLient.getsendsms(numero, message, expediteur);
		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				Log.i(TAG, "onResponse: ENVOIS SMS REUSSI");
				SaveVerification(numero, message.substring(33),1);

			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				Log.i(TAG, "onFailure: ENVOI SMS ECHEC");
			}
		});

    }

    // send sms second time
	private void SendSMS2(String numero, String message, String expediteur){
		Call<String> call = apiCLient.getsendsms(numero, message, expediteur);
		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				Log.i(TAG, "onResponse: ENVOIS SMS REUSSI");
				UpdateVerification(numero,message.substring(33));

			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				Log.i(TAG, "onFailure: ENVOI SMS ECHEC");
			}
		});

	}
    private String GenererCode(){
		Random rand = new Random();
		String code = "" ;
		for(int i = 0; i<4; i++){
			String a = String.valueOf(rand.nextInt(9));
			code = code+a;
		}
	    return code;
    }

	//methode bouton inserer code
	private void saisiecode(String numero){
		Log.i(TAG,"VALUE PINVIEW = "+pinview.getValue());
		Call<Verification> call = apiCLient_db.getVerificationByNumero(numero);
		call.enqueue(new Callback<Verification>() {
			@Override
			public void onResponse(Call<Verification> call, Response<Verification> response) {
				Log.i(TAG, "onResponse renvoi verfication pour code, CODERESPONSE : "+response.body().getCode()+" CODEPINVEW : "+pinview.getValue());
				if(response.body().getCode().equalsIgnoreCase(pinview.getValue())){
					Log.i(TAG, "onResponse code egal: ");
					onNextButton();
					infocodetoken.setVisibility(View.GONE);
					registrationFormPanel.setVisibility(View.VISIBLE);
				}
				else{
					Log.i(TAG, "onResponse: code incorrecte");
					textviewerrorcode.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onFailure(Call<Verification> call, Throwable t) {

			}
		});
/*		if(!pinview.getValue().equalsIgnoreCase("1234")){
			textviewerrorcode.setVisibility(View.VISIBLE);
		}
		else{
			onNextButton();
			infocodetoken.setVisibility(View.GONE);
			registrationFormPanel.setVisibility(View.VISIBLE);
		}*/
	}


	private void onNextButton() {
		String t = mHostname.getText().toString();
		//String t = hostname;
		if (mAuthTask == null && (t == null || t.isEmpty())) {
			mHostname.setError("Cannot be empty");
			return;
		}
		Log.i(TAG,"TASK : "+mAuthTask);
		if (mAuthTask == null) {
			startConnection(mHostname.getText().toString().trim());
			laststep.setVisibility(View.VISIBLE);
			textviewlaststep.setText("Votre numero telephone est : "+editextnumero.getText());



			//confirmer();


		}/* else {
			try {
				mAuthTask.useForm((UnifiedRegistrationForm) dynamicForm.getJabberDataElement());
			} catch (JaxmppException e) {
				Log.e(TAG, "Something goes wrong", e);
				e.printStackTrace();
			}
		}*/
	}

	private void confirmer(){
		try {

			mAuthTask.useForm((UnifiedRegistrationForm) dynamicForm.getJabberDataElement());
		} catch (JaxmppException e) {
			Log.e(TAG, "Something goes wrong", e);
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void showPage1() {
		if (mAuthTask != null) {
			mAuthTask.cancel(true);
			mAuthTask = null;
		}
		//mHostname.setError(null);
		//hostSelectorPanel.setVisibility(View.VISIBLE);
		registrationFormPanel.setVisibility(View.GONE);
		prevButton.setEnabled(false);
		nextButton.setText("Next");

	}

	private void showPage2() {
		hostSelectorPanel.setVisibility(View.GONE);
		registrationFormPanel.setVisibility(View.GONE);
		prevButton.setEnabled(true);
		nextButton.setText("Register");
	}

	private void startConnection(String host) {
		mAuthTask = new AccountCreationTask(this, getApplicationContext(), mAccountManager, host);
		mAuthTask.execute();
	}

	public static class AccountCreationTask
			extends AsyncTask<Void, Integer, Boolean> {

		private final WeakReference<CreateAccountActivity> activity;
		private final Context context;
		private final AccountManager mAccountManager;
		private final AccountCreator accountCreator;
		private final String hostname;
		private ProgressDialog progress;

		public AccountCreationTask(CreateAccountActivity activity, Context context, AccountManager mAccountManager, String hostname) {
			this.hostname = hostname;
			this.activity = new WeakReference<>(activity);
			this.context = context;
			this.mAccountManager = mAccountManager;
			accountCreator = new AccountCreator(hostname);
		}


		public void useForm(UnifiedRegistrationForm jabberDataElement) throws JaxmppException {
			showProgress("Registering");
			InBandRegistrationModule m = accountCreator.getJaxmpp().getModule(InBandRegistrationModule.class);


			try {
				if (jabberDataElement == null) {
					runOnUiThread(() -> {
						progress.dismiss();
						progress = null;
						AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
						builder.setMessage("Server doesn't support registration.")
								.setPositiveButton(android.R.string.ok, null)
								.show();
					});
					return;
				}

				m.register(jabberDataElement, new AsyncCallback() {
					@Override
					public void onError(Stanza stanza, final XMPPException.ErrorCondition errorCondition)
							throws JaxmppException {
						Log.e(TAG, "Error: " + errorCondition);

						runOnUiThread(() -> {
							progress.dismiss();
							progress = null;
							AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
							builder.setMessage("Registration error: " + errorCondition)
									.setPositiveButton(android.R.string.ok, null)
									.show();
						});

					}

					@Override
					public void onSuccess(Stanza stanza) throws JaxmppException {
						Log.e(TAG, "Success ");
						accountCreator.success();
					}

					@Override
					public void onTimeout() throws JaxmppException {
						runOnUiThread(() -> {
							progress.dismiss();
							progress = null;
							AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
							builder.setMessage("No server response")
									.setPositiveButton(android.R.string.ok, null)
									.show();
						});
					}
				});
			} catch (JaxmppException e) {
				Log.e(TAG, "Cannot send registration form", e);
				e.printStackTrace();
			}

		}

		@Override
		protected Boolean doInBackground(Void... _) {
			accountCreator.getEventBus()
					.addHandler(
							InBandRegistrationModule.ReceivedRequestedFieldsHandler.ReceivedRequestedFieldsEvent.class,
							new InBandRegistrationModule.ReceivedRequestedFieldsHandler() {
								@Override
								public void onReceivedRequestedFields(SessionObject sessionObject, IQ iq,
																	  final UnifiedRegistrationForm unifiedRegistrationForm) {
									Log.w(TAG, "Registration form received");
									runInActivity(ac -> ac.runOnUiThread(() -> {
										ac.showPage2();
										try {
											unifiedRegistrationForm.setUsername(ac.editextnumero.getText().toString());

											unifiedRegistrationForm.setPassword(ac.editextgivepassword.getText().toString());
											unifiedRegistrationForm.setEmail(ac.editextnumero.getText().toString()+"@email.com");
										} catch (XMLException e) {
											e.printStackTrace();
										}
										ac.dynamicForm.setJabberDataElement(unifiedRegistrationForm);
										if (progress != null) {
											progress.dismiss();
											progress = null;
										}
									}));
								}
							});
			return accountCreator.register(context);
		}

		@Override
		protected void onPostExecute(Boolean success) {
			Log.i(TAG, "Registration status= " + success);
			if (progress != null) {
				progress.dismiss();
				progress = null;
			}

			if (!success) {
				runInActivity(CreateAccountActivity::showPage1);

				SecureTrustManagerFactory.DataCertificateException deepException = LoginActivity.getCertException(
						accountCreator.getException());

				if (deepException != null) {
					X509Certificate[] chain = deepException.getChain();
					LoginActivity.showInvalidCertificateDialog(activity.get(), chain,
							() -> runInActivity(a -> a.startConnection(hostname)));
				} else {
					final String msg;
					if (accountCreator.getErrorMessage() == null || accountCreator.getErrorMessage().isEmpty()) {
						msg = "Connection error.";
					} else {
						msg = accountCreator.getErrorMessage();
					}
					runOnUiThread(() -> {
						AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
						builder.setMessage(msg).setPositiveButton(android.R.string.ok, null).show();
					});
				}
				return;
			}

			runInActivity(a -> {
				try {
					UnifiedRegistrationForm form = (UnifiedRegistrationForm) a.dynamicForm.getJabberDataElement();

					final String username = ((TextSingleField) form.getField("username")).getFieldValue();
					final String mPassword = ((TextPrivateField) form.getField("password")).getFieldValue();
					final String mResource = "mobile";
					final Boolean mActive = true;

					String mXmppId = BareJID.bareJIDInstance(username, accountCreator.getJaxmpp()
							.getSessionObject()
							.getProperty(SessionObject.DOMAIN_NAME)).toString();

					Account account = AccountHelper.getAccount(mAccountManager, mXmppId);
					if (account == null) {
						account = new Account(mXmppId, Authenticator.ACCOUNT_TYPE);
						Log.d(TAG, "Adding account " + mXmppId + ":" + mPassword);
						mAccountManager.addAccountExplicitly(account, mPassword, null);
					} else {
						Log.d(TAG, "Updating account " + mXmppId + ":" + mPassword);
						mAccountManager.setPassword(account, mPassword);
					}
					// mAccountManager.setUserData(account, AccountsConstants.FIELD_NICKNAME, mNickname);
//				mAccountManager.setUserData(account, AccountsConstants.FIELD_HOSTNAME, mHostname);
					mAccountManager.setUserData(account, AccountsConstants.FIELD_RESOURCE, mResource);
					mAccountManager.setUserData(account, AccountsConstants.FIELD_ACTIVE, Boolean.toString(mActive));
					mAccountManager.setUserData(account, MobileModeFeature.MOBILE_OPTIMIZATIONS_ENABLED,
							Boolean.toString(true));

					final Intent intent = new Intent();
					intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mXmppId);
					intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE);
					// setAccountAuthenticatorResult(intent.getExtras());
					a.setResult(RESULT_OK, intent);

					Intent i = new Intent();
					i.setAction(LoginActivity.ACCOUNT_MODIFIED_MSG);
					i.putExtra(AccountManager.KEY_ACCOUNT_NAME, mXmppId);
					a.sendBroadcast(i);
				} catch (Exception e) {
					Log.e("LoginActivity", "Can't add account", e);
				}
				a.finish();
			});
		}

		@Override
		protected void onPreExecute() {
			showProgress(context.getResources().getString(R.string.login_checking));
		}

		private void runInActivity(Fnc<CreateAccountActivity> f) {
			final CreateAccountActivity x = activity.get();
			if (x != null && !x.isFinishing()) {
				f.run(x);
			}
		}

		private void runOnUiThread(Runnable runnable) {
			runInActivity(activity1 -> activity1.runOnUiThread(runnable));
		}

		private void showProgress(String msg) {
			if (this.progress == null) {
				runInActivity(a -> {
					this.progress = new ProgressDialog(a);
					progress.setMessage(msg);
					progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progress.setIndeterminate(true);
					progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progress.show();
				});
			}
		}

		interface Fnc<T> {

			void run(T activity);
		}
	}

}
