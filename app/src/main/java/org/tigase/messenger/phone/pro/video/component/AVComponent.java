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

package org.tigase.messenger.phone.pro.video.component;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import org.tigase.messenger.phone.pro.R;
import org.tigase.messenger.phone.pro.db.DatabaseContract;
import org.tigase.messenger.phone.pro.providers.RosterProvider;
import org.tigase.messenger.phone.pro.utils.AvatarHelper;
import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSink;
import tigase.jaxmpp.core.client.JID;

public class AVComponent
		extends FrameLayout {

	private final static String TAG = "AVComponent";
	private String account;
	private ImageView avatar;
	private View callAcceptPanel;
	private View contactInfoPanel;
	private TextView debugStatus;
	private FloatingActionButton hangup;
	private FloatingActionButton speakeron;
	private FloatingActionButton speakeroff;
	private OnHangupHandler hangupHandler;
	private JID jid;
	private SurfaceViewRenderer localVideoView;
	private TextView nameView;
	private SurfaceViewRenderer remoteVideoView;
	private EglBase rootEglBase;

	public AVComponent(@NonNull Context context) {
		super(context);
		init(context, null);
	}

	public AVComponent(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public AVComponent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public AVComponent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	public VideoSink getRemoteVideoView() {
		return remoteVideoView;
	}

	public VideoSink getLocalVideoView() {
		return localVideoView;
	}

	public void initVideos(EglBase eglBase) {
		this.rootEglBase = eglBase;
		localVideoView.init(rootEglBase.getEglBaseContext(), null);
		remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
		localVideoView.setZOrderMediaOverlay(true);
		remoteVideoView.setZOrderMediaOverlay(true);

		// A LINITIATION DE L'APPEL VIDEO
		speakeron.setVisibility(View.VISIBLE);
		// activer haut parleur
		speakeron.setOnClickListener(v1 -> {
			Log.i("HP","HAUT PARLEUR ACTIVE CHEZ RECEVEUR");
			speakeroff.setVisibility(VISIBLE);
			AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
			audioManager.setSpeakerphoneOn(true);
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			speakeron.setVisibility(GONE);
		});
		// désactiver haut parleur
		speakeroff.setOnClickListener(v1 -> {
			Log.i("HP","HAUT PARLEUR DESACTIVE CHEZ RECEVEUR");
			speakeron.setVisibility(VISIBLE);
			AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			audioManager.setSpeakerphoneOn(false);

			speakeroff.setVisibility(GONE);
		});

	}

	public void stop() {
		try {
			localVideoView.release();
		} catch (Exception e) {
			Log.e(TAG, "Cannot release localVideoView", e);
		}
		try {
			remoteVideoView.release();
		} catch (Exception e) {
			Log.e(TAG, "Cannot release remoteVideoView", e);
		}
	}

	public void setRemoteVideoViewVisible(boolean value) {
		/*remoteVideoView.setVisibility(value ? View.VISIBLE : View.GONE);
		contactInfoPanel.setVisibility(!value ? View.VISIBLE : View.GONE);*/
		remoteVideoView.setVisibility(value ? View.VISIBLE : View.VISIBLE);
		contactInfoPanel.setVisibility(!value ? View.VISIBLE : View.VISIBLE);
	}

	public void updateVideoViews(boolean remoteVisible) {
		ViewGroup.LayoutParams params = localVideoView.getLayoutParams();
		if (remoteVisible) {
			params.height = dpToPx(100);
			params.width = dpToPx(100);
		} else {
			params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
		}
		localVideoView.setLayoutParams(params);


	}

	public void setHangupHandler(OnHangupHandler hangupHandler) {
		this.hangupHandler = hangupHandler;
	}

	public EglBase getEglBase() {
		return this.rootEglBase;
	}

	public void askForPickup(OnPickupHandler onPickupHandler) {
		callAcceptPanel.setVisibility(View.VISIBLE);
		hangup.setVisibility(View.GONE);
		//speakeroff.setVisibility(View.GONE);
		/* ACCEPT CALL */
		findViewById(R.id.accept_call).setOnClickListener(v -> {
			callAcceptPanel.setVisibility(View.GONE);
			hangup.setVisibility(View.VISIBLE);
			speakeron.setVisibility(View.VISIBLE);
			// activer haut parleur
			speakeron.setOnClickListener(v1 -> {
				Log.i("HP","HAUT PARLEUR ACTIVE CHEZ RECEVEUR");
				speakeroff.setVisibility(VISIBLE);
				AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
				audioManager.setSpeakerphoneOn(true);
				audioManager.setMode(AudioManager.MODE_IN_CALL);
				speakeron.setVisibility(GONE);
			});
			// désactiver haut parleur
			speakeroff.setOnClickListener(v1 -> {
				Log.i("HP","HAUT PARLEUR DESACTIVE CHEZ RECEVEUR");
				speakeron.setVisibility(VISIBLE);
				AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
				audioManager.setMode(AudioManager.MODE_IN_CALL);
				audioManager.setSpeakerphoneOn(false);

				speakeroff.setVisibility(GONE);
			});

			onPickupHandler.onPickup(true);
		});
		findViewById(R.id.reject_call).setOnClickListener(v -> {
			callAcceptPanel.setVisibility(View.GONE);
			hangup.setVisibility(View.VISIBLE);
			onPickupHandler.onPickup(false);
		});
	}

	public void setRemoteJid(JID jid) {
		this.jid = jid;
//		debugStatus.setText("call with: " + jid);

		AvatarHelper.setAvatarToImageView(jid.getBareJid(), avatar);
		String n = getContactName();
		this.nameView.setText(n == null ? jid.getBareJid().toString() : n);
	}

	public void setAccount(String account) {
		this.account = account;
	}

	private String getContactName() {
		Uri u = Uri.parse(RosterProvider.ROSTER_URI + "/" + account + "/" + jid.getBareJid());

		try (Cursor c = getContext().getContentResolver()
				.query(u, new String[]{DatabaseContract.RosterItemsCache.FIELD_NAME}, null, null, null)) {
			if (c.moveToNext()) {
				return c.getString(c.getColumnIndex(DatabaseContract.RosterItemsCache.FIELD_NAME));
			}
		}
		return null;
	}

	/**
	 * Util Methods
	 */
	private int dpToPx(int dp) {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	}

	private void init(Context context, AttributeSet attributeSet) {
		inflate(context, R.layout.av_component, this);

		this.debugStatus = findViewById(R.id.debug_status);
		this.nameView = findViewById(R.id.name);
		this.avatar = findViewById(R.id.avatar);

		this.contactInfoPanel = findViewById(R.id.contact_info_panel);

		hangup = findViewById(R.id.end_call);
		speakeron = findViewById(R.id.speaker_on);
		speakeroff = findViewById(R.id.speaker_off);
		hangup.setOnClickListener(this::onHangupPressed);

		localVideoView = findViewById(R.id.local_gl_surface_view);
		remoteVideoView = findViewById(R.id.remote_gl_surface_view);

		callAcceptPanel = findViewById(R.id.callAcceptPanel);

		localVideoView.setVisibility(View.VISIBLE);
		localVideoView.setMirror(true);
		remoteVideoView.setMirror(true);
	}

	private void onHangupPressed(View v) {
		if (hangupHandler != null) {
			hangupHandler.onHangup();
		}
	}

	public interface OnHangupHandler {

		void onHangup();
	}

	public interface OnPickupHandler {

		void onPickup(boolean pickedUp);
	}

	// ACTIVER OU DESACTIVER SPEAKER
	public void activehp(boolean enable) {
		AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
		audioManager.setSpeakerphoneOn(enable);
		audioManager.setMode(AudioManager.MODE_IN_CALL);
	}
}
