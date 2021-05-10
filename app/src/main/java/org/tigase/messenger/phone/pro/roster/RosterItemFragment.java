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

package org.tigase.messenger.phone.pro.roster;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

//import androidx.fragment.app.Fragment;

import org.tigase.messenger.jaxmpp.android.roster.RosterDbHelper;
import org.tigase.messenger.phone.pro.API.ApiCLient;
import org.tigase.messenger.phone.pro.API.ApiInterface;
import org.tigase.messenger.phone.pro.DividerItemDecoration;
import org.tigase.messenger.phone.pro.HelpActivity;
import org.tigase.messenger.phone.pro.MainActivity;
import org.tigase.messenger.phone.pro.R;
import org.tigase.messenger.phone.pro.db.DatabaseHelper;
import org.tigase.messenger.phone.pro.db.MyDBHelper;
import org.tigase.messenger.phone.pro.db.RosterProviderExt;
import org.tigase.messenger.phone.pro.models.Verification;
import org.tigase.messenger.phone.pro.roster.contact.EditContactActivity;
import org.tigase.messenger.phone.pro.db.DatabaseContract;
import org.tigase.messenger.phone.pro.providers.RosterProvider;
import org.tigase.messenger.phone.pro.searchbar.SearchActionMode;
import org.tigase.messenger.phone.pro.selectionview.MultiSelectFragment;
import org.tigase.messenger.phone.pro.service.XMPPService;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tigase.jaxmpp.android.Jaxmpp;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.tigase.messenger.phone.pro.service.XMPPService.ACCOUNT_TMP_DISABLED_KEY;

/**
 * A fragment representing a list of Items.
 */
public class RosterItemFragment
		extends MultiSelectFragment {

	final static String TAG = "RosterItemFragment";
	private static final boolean SHOW_OFFLINE_DEFAULT = true;
	RecyclerView recyclerView;
	private MyRosterItemRecyclerViewAdapter adapter;
	private DBUpdateTask dbUpdateTask;
	private SearchActionMode searchActionMode;
	private MyDBHelper mydbHelper;
	final ApiInterface apiCLient_db = ApiCLient.getClient_db().create(ApiInterface.class);
	private SessionObject sessionObject;
	private RosterProviderExt rosterProvider;
	private DatabaseHelper dbHelper;
	private final MainActivity.XMPPServiceConnection mConnection = new MainActivity.XMPPServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			super.onServiceConnected(name, service);
			refreshRoster();
		}
	};
	//	private OnRosterItemDeleteListener mItemLongClickListener = new OnRosterItemDeleteListener() {
//
//		@Override
//		public void onRosterItemDelete(int id, final String account, final String jid, final String name) {
//			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					switch (which) {
//						case DialogInterface.BUTTON_POSITIVE:
//							(new RemoveContactTask(BareJID.bareJIDInstance(account),
//												   BareJID.bareJIDInstance(jid))).execute();
//							break;
//					}
//				}
//			};
//
//			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//			builder.setMessage(getContext().getString(R.string.roster_delete_contact, jid, name))
//					.setPositiveButton(android.R.string.yes, dialogClickListener)
//					.setNegativeButton(android.R.string.no, dialogClickListener)
//					.show();
//		}
//	};
	private SharedPreferences sharedPref;

	// TODO: Customize parameter initialization
	@SuppressWarnings("unused")
	public static Fragment newInstance(MainActivity.XMPPServiceConnection mServiceConnection) {
		RosterItemFragment fragment = new RosterItemFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation
	 * changes).
	 */
	public RosterItemFragment() {
		super();
	}

	@Override
	public void onAttach(Context context) {
		this.sharedPref = getContext().getSharedPreferences("RosterPreferences", Context.MODE_PRIVATE);
		super.onAttach(context);
		mydbHelper = new MyDBHelper(context);
		dbHelper = new DatabaseHelper(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.searchActionMode = new SearchActionMode(getActivity(), txt -> refreshRoster());
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.roster_fragment, menu);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_rosteritem_list, container, false);
		//mydbHelper = new MyDBHelper(container.getContext());
		recyclerView = root.findViewById(R.id.roster_list);

		FloatingActionButton rosterAddContact = root.findViewById(R.id.roster_add_contact);
		rosterAddContact.setOnClickListener(view -> {
			Intent intent = new Intent(getContext(), EditContactActivity.class);
			startActivity(intent);
			//Intent intent = new Intent(getContext(), LoadContacts.class);
			/*Uri uricontact = Uri.parse("content://com.android.contacts/contacts");
			Intent inte = new Intent(Intent.ACTION_DIAL);
			startActivity(inte);*/
			/*Intent intent= new Intent(Intent.ACTION_DEFAULT,  ContactsContract.Contacts.CONTENT_URI);
			startActivity(intent);*/


		});

		// Set the adapter
		recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
		recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));

		this.adapter = new MyRosterItemRecyclerViewAdapter(getContext(), null, this) {
			@Override
			protected void onContentChanged() {
				refreshRoster();
			}
		};
		recyclerView.setAdapter(adapter);

		return root;
	}

	@Override
	public void onDetach() {
		sharedPref = null;
		recyclerView.setAdapter(null);
		adapter.changeCursor(null);
		super.onDetach();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		SharedPreferences.Editor editor;
		switch (item.getItemId()) {
			case R.id.ac_serach:
				ActionMode am = startActionMode(this.searchActionMode);
				return true;
			case R.id.menu_roster_invite:
				/*Intent intentmsg = new Intent(Intent.ACTION_VIEW);
				intentmsg.putExtra("MSG","DONNEES");
				startActivity(intentmsg);*/
				//Intent smsIntent = new Intent(Intent.ACTION_VIEW);
				//smsIntent.setType("vnd.android-dir/mms-sms");
				//smsIntent.putExtra("address","+22234041114");
				/*smsIntent.putExtra("sms_body","your desired message");
				smsIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(smsIntent);*/
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, "Voici le lien de l'application");
				sendIntent.setType("text/plain");

				Intent shareIntent = Intent.createChooser(sendIntent, null);
				startActivity(shareIntent);
				return true;

			case R.id.menu_roster_ajout_contact:
				Intent intent= new Intent(Intent.ACTION_DEFAULT,  ContactsContract.Contacts.CONTENT_URI);
				startActivity(intent);
				return true;

			case R.id.menu_roster_actualiser:

				apiCLient_db.getListenumero().enqueue(new Callback<List<Verification>>() {
					@Override
					public void onResponse(Call<List<Verification>> call, Response<List<Verification>> response) {
						SQLiteDatabase db = dbHelper.getWritableDatabase();
						List<Verification> liste = response.body();
						for(int i=0;i<liste.size();i++){
							String numerodb = liste.get(i).getNumero();
							Log.i(TAG, "onResponse: numero "+i+" : "+numerodb);
							String selectionbynumero = DatabaseContract.RosterItemsCache.FIELD_JID+" LIKE '%"+numerodb+"%'";
							Cursor cursorfriend = getContext().getContentResolver().query(RosterProvider.ROSTER_URI,null, selectionbynumero, null,
									null);
							if(cursorfriend.getCount()==0) {
								Log.i(TAG, "onResponse: if c.getCount = 0");
								String sortbyname = ContactsContract.Contacts.DISPLAY_NAME;
								Cursor cursorcall = getContext().getContentResolver()
										.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, sortbyname);
								if(cursorcall.moveToFirst()){
									do{
										String idcontact = cursorcall.getString(cursorcall.getColumnIndex(ContactsContract.Contacts._ID));
										String namecall = cursorcall.getString(cursorcall.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
										String selectionid = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?";
										Cursor cursorphone = getContext().getContentResolver().
												query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selectionid, new String[]{idcontact}, null);
										if(cursorphone.moveToFirst()){
											String numerocall = cursorphone.getString(cursorphone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
											numerocall = numerocall.replaceAll("\\s","");
											if(numerocall.length()>=9)
												numerocall=numerocall.substring(numerocall.length()-9);
											if (numerocall.equalsIgnoreCase(numerodb)) {
												Log.i(TAG, "onResponse: numerocall = numerodb " + numerocall + " = " + numerodb);
												ContentValues v = new ContentValues();
												v.put(DatabaseContract.RosterItemsCache.FIELD_JID, numerocall + "@xmppsrv.orange-bissau.com");
												/*v.put(DatabaseContract.RosterItemsCache.FIELD_ACCOUNT, sessionObject.getUserBareJid().toString());*/
												v.put(DatabaseContract.RosterItemsCache.FIELD_ACCOUNT, "955294371@xmppsrv.orange-bissau.com");
												v.put(DatabaseContract.RosterItemsCache.FIELD_NAME, namecall);
												v.put(DatabaseContract.RosterItemsCache.FIELD_SUBSCRIPTION, RosterItem.Subscription.both.isTo());
												v.put(DatabaseContract.RosterItemsCache.FIELD_ASK, true);
												v.put(DatabaseContract.RosterItemsCache.FIELD_TIMESTAMP, (new Date()).getTime());
												db.insert(DatabaseContract.RosterItemsCache.TABLE_NAME, null, v);
											}
											else
												Log.i(TAG, "onResponse: numerocall != numerodb " + numerocall + " != " + numerodb);
										} cursorphone.close();
									}while (cursorcall.moveToNext());
								}
							}
							else Log.i(TAG, "onResponse: numero existe");
						}
					}

					@Override
					public void onFailure(Call<List<Verification>> call, Throwable t) {
						Log.i(TAG, "onFailure: ERREUR CHARGEMENT TABLE NUMERO"+t);
					}
				});

			return true;

			case R.id.menu_roste_aide:
				Intent intenthelp = new Intent(getActivity(), HelpActivity.class);
				startActivity(intenthelp);
				return true;

			case R.id.menu_roster_sort_presence:
				item.setChecked(true);

				editor = sharedPref.edit();
				editor.putString("roster_sort", "presence");
				editor.commit();
				refreshRoster();
				return true;

			case R.id.menu_roster_sort_name:
				item.setChecked(true);
				editor = sharedPref.edit();
				editor.putString("roster_sort", "name");
				editor.commit();
				refreshRoster();
				return true;
			case R.id.menu_roster_show_offline:
				final boolean v = !item.isChecked();
				item.setChecked(v);
				editor = sharedPref.edit();
				editor.putBoolean("show_offline", v);
				editor.commit();
				refreshRoster();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean v = sharedPref.getBoolean("show_offline", SHOW_OFFLINE_DEFAULT);
		menu.findItem(R.id.menu_roster_show_offline).setChecked(v);


		String sort = sharedPref.getString("roster_sort", "presence");
		switch (sort) {
			case "name":
				menu.findItem(R.id.menu_roster_sort_name).setChecked(v);
				break;
			case "presence":
				menu.findItem(R.id.menu_roster_sort_presence).setChecked(v);
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mConnection.getService() != null) {
			refreshRoster();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		getActivity().bindService(new Intent(getContext(), XMPPService.class), mConnection, Context.BIND_AUTO_CREATE);
		//dbHelper = new MyDBHelper(getContext());
	}

	@Override
	public void onStop() {
		getActivity().unbindService(mConnection);
		super.onStop();
	}

	@Override
	protected boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.ac_delete:
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage(R.string.roster_delete_contact).setPositiveButton(R.string.yes, (dialog, which) -> {

					final Cursor c = adapter.getCursor();
					for (int pos : getMultiSelector().getSelectedPositions()) {
						c.moveToPosition(pos);
						String account = c.getString(c.getColumnIndex(DatabaseContract.RosterItemsCache.FIELD_ACCOUNT));
						String jid = c.getString(c.getColumnIndex(DatabaseContract.RosterItemsCache.FIELD_JID));
						(new RemoveContactTask(BareJID.bareJIDInstance(account),
								BareJID.bareJIDInstance(jid))).execute();
					}

					mode.finish();
				}).setNegativeButton(R.string.no, null).show();

				return true;
			default:
				return false;
		}
	}

	@Override
	protected boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
		actionMode.getMenuInflater().inflate(R.menu.roster_context, menu);
		return true;
	}

	@Override
	protected void updateActionMode(ActionMode actionMode) {
		final int count = mMultiSelector.getSelectedPositions().size();
		actionMode.setTitle(getContext().getResources().getQuantityString(R.plurals.roster_selected, count, count));
	}

	private void refreshRoster() {
		if (mConnection.getService() != null && dbUpdateTask == null ||
				dbUpdateTask.getStatus() == AsyncTask.Status.FINISHED) {
			String txt = searchActionMode.getSearchText();
			dbUpdateTask = new DBUpdateTask(enabledAccounts());
			dbUpdateTask.execute(txt);
		}
	}

	private String enabledAccounts() {
		final StringBuilder sb = new StringBuilder();
		sb.append("'-'");
		try {
			Collection<JaxmppCore> accounts = mConnection.getService().getMultiJaxmpp().get();
			for (JaxmppCore account : accounts) {
				Boolean disabled = account.getSessionObject().getUserProperty(ACCOUNT_TMP_DISABLED_KEY);
				if (disabled == null || disabled) {
					continue;
				}
				sb.append(",");
				sb.append("'").append(account.getSessionObject().getUserBareJid().toString()).append("'");
			}
		} catch (Exception e) {
			Log.wtf(TAG, "Cannot prepare list of active accounts.", e);
		}
		return sb.toString();
	}

	private class DBUpdateTask
			extends AsyncTask<String, Void, Cursor> {

		private final String enabledAccounts;

		public DBUpdateTask(String enabledAccounts) {
			this.enabledAccounts = enabledAccounts;
		}

		@RequiresApi(api = Build.VERSION_CODES.M)
		@Override
		protected Cursor doInBackground(String... params) {
			if (sharedPref == null) {
				Log.e("RosterItemFragment", "Shared preferences are empty?");
				return null;
			}
			String[] columnsToReturn = new String[]{DatabaseContract.RosterItemsCache.FIELD_ID,
					DatabaseContract.RosterItemsCache.FIELD_ACCOUNT,
					DatabaseContract.RosterItemsCache.FIELD_JID,
					DatabaseContract.RosterItemsCache.FIELD_NAME,
					DatabaseContract.RosterItemsCache.FIELD_STATUS};

			boolean showOffline = sharedPref.getBoolean("show_offline", SHOW_OFFLINE_DEFAULT);
			final String searchText = params != null ? params[0] : null;

			String selection = "1=1 ";
			String selection2 = "1=1 ";
			String[] args = null;

			if (!showOffline) {
				selection += " AND " + DatabaseContract.RosterItemsCache.FIELD_STATUS + ">=5 ";
			}

			if (searchText != null) {
				/*selection += " AND (" + DatabaseContract.RosterItemsCache.FIELD_NAME + " like ? OR " +
						DatabaseContract.RosterItemsCache.FIELD_JID + " like ?" + " )";
				args = new String[]{"%" + searchText + "%", "%" + searchText + "%"};*/

				selection2 += " AND (" + ContactsContract.Contacts.DISPLAY_NAME + " like ? )";
				args = new String[]{"%" + searchText + "%"};
				Log.i("DB","COL : "+selection2);
			}

			if (enabledAccounts != null) {
				selection +=
						" AND (" + DatabaseContract.RosterItemsCache.FIELD_ACCOUNT + " IN (" + enabledAccounts + "))";
			}

			String sort;

			String s = sharedPref.getString("roster_sort", "presence");
			switch (s) {
				case "name":
					sort = DatabaseContract.RosterItemsCache.FIELD_NAME + " COLLATE NOCASE ASC";
					break;
				case "jid":
					sort = DatabaseContract.RosterItemsCache.FIELD_JID + " ASC";
					break;
				case "presence":
					sort = DatabaseContract.RosterItemsCache.FIELD_STATUS + " DESC," +
							DatabaseContract.RosterItemsCache.FIELD_NAME + " COLLATE NOCASE ASC";
					break;
				default:
					sort = "";
			}
/*			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete(DatabaseContract.RosterItemsCache.TABLE_NAME,null,null);*/
			Cursor cursor = getContext().getContentResolver()
					.query(RosterProvider.ROSTER_URI, columnsToReturn, selection, args, sort);
            return cursor;
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			adapter.changeCursor(cursor);
		}
	}

	private class RemoveContactTask
			extends AsyncTask<Void, Void, Void> {

		private final BareJID account;
		private final BareJID jid;

		public RemoveContactTask(BareJID account, BareJID jid) {
			this.jid = jid;
			this.account = account;
		}

		@Override
		protected Void doInBackground(Void... params) {
			final XMPPService mService = mConnection.getService();
			if (mService == null) {
				Log.i("RosterItemFragment", "Service is disconnected!!!");
				return null;
			}
			try {
				Jaxmpp jaxmpp = mService.getJaxmpp(account);
				if (jaxmpp.isConnected()) {
					RosterStore store = RosterModule.getRosterStore(jaxmpp.getSessionObject());
					store.remove(jid);
				}
			} catch (Exception e) {
				Log.e(this.getClass().getSimpleName(), "Can't remove contact from roster", e);
				Toast.makeText(getContext(), "ERROR " + e.getMessage(), Toast.LENGTH_SHORT);
			}
			return null;
		}
	}

}
