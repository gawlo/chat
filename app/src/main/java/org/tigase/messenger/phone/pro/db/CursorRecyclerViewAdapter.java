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

package org.tigase.messenger.phone.pro.db;

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;

class CursorFilter
		extends Filter {

	CursorFilterClient mClient;

	CursorFilter(CursorFilterClient client) {
		mClient = client;
	}

	@Override
	public CharSequence convertResultToString(Object resultValue) {
		return mClient.convertToString((Cursor) resultValue);
	}

	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		Cursor cursor = mClient.runQueryOnBackgroundThread(constraint);

		FilterResults results = new FilterResults();
		if (cursor != null) {
			results.count = cursor.getCount();
			results.values = cursor;
		} else {
			results.count = 0;
			results.values = null;
		}
		return results;
	}

	@Override
	protected void publishResults(CharSequence constraint, FilterResults results) {
		Cursor oldCursor = mClient.getCursor();

		if (results.values != null && results.values != oldCursor) {
			mClient.changeCursor((Cursor) results.values);
		}
	}

	interface CursorFilterClient {

		void changeCursor(Cursor cursor);

		CharSequence convertToString(Cursor cursor);

		Cursor getCursor();

		Cursor runQueryOnBackgroundThread(CharSequence constraint);
	}
}

/**
 * Provide a {@link android.support.v7.widget.RecyclerView.Adapter}
 * implementation with cursor support.
 * <p/>
 * Child classes only need to implement
 * {@link #onCreateViewHolder(android.view.ViewGroup, int)} and
 * {@link #onBindViewHolderCursor(android.support.v7.widget.RecyclerView.ViewHolder, android.database.Cursor)}
 * .
 * <p/>
 * This class does not implement deprecated fields and methods from
 * CursorAdapter! Incidentally, only
 * {@link android.widget.CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER} is
 * available, so the flag is implied, and only the Adapter behavior using this
 * flag has been ported.
 *
 * @param <VH> {@inheritDoc}
 *
 * @see android.support.v7.widget.RecyclerView.Adapter
 * @see android.widget.CursorAdapter
 * @see android.widget.Filterable
 */
public abstract class CursorRecyclerViewAdapter<VH extends android.support.v7.widget.RecyclerView.ViewHolder>
		extends RecyclerView.Adapter<VH>
		implements Filterable, CursorFilter.CursorFilterClient {

	private ChangeObserver mChangeObserver;
	private Cursor mCursor;
	private CursorFilter mCursorFilter;
	private DataSetObserver mDataSetObserver;
	private boolean mDataValid;
	private FilterQueryProvider mFilterQueryProvider;
	private int mRowIDColumn;

	public CursorRecyclerViewAdapter(Cursor cursor) {
		init(cursor);
	}

	/**
	 * Change the underlying cursor to a new cursor. If there is an existing
	 * cursor it will be closed.
	 *
	 * @param cursor The new cursor to be used
	 */
	public void changeCursor(Cursor cursor) {
		Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	/**
	 * <p>
	 * Converts the cursor into a CharSequence. Subclasses should override this
	 * method to convert their results. The default implementation returns an
	 * empty String for null values or the default String representation of the
	 * value.
	 * </p>
	 *
	 * @param cursor the cursor to convert to a CharSequence
	 *
	 * @return a CharSequence representing the value
	 */
	public CharSequence convertToString(Cursor cursor) {
		return cursor == null ? "" : cursor.toString();
	}

	public Cursor getCursor() {
		return mCursor;
	}

	public Filter getFilter() {
		if (mCursorFilter == null) {
			mCursorFilter = new CursorFilter(this);
		}
		return mCursorFilter;
	}

	/**
	 * Returns the query filter provider used for filtering. When the provider
	 * is null, no filtering occurs.
	 *
	 * @return the current filter query provider or null if it does not exist
	 *
	 * @see #setFilterQueryProvider(android.widget.FilterQueryProvider)
	 * @see #runQueryOnBackgroundThread(CharSequence)
	 */
	public FilterQueryProvider getFilterQueryProvider() {
		return mFilterQueryProvider;
	}

	/**
	 * Sets the query filter provider used to filter the current Cursor. The
	 * provider's
	 * {@link android.widget.FilterQueryProvider#runQuery(CharSequence)} method
	 * is invoked when filtering is requested by a client of this adapter.
	 *
	 * @param filterQueryProvider the filter query provider or null to remove it
	 *
	 * @see #getFilterQueryProvider()
	 * @see #runQueryOnBackgroundThread(CharSequence)
	 */
	public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
		mFilterQueryProvider = filterQueryProvider;
	}

	@Override
	public int getItemCount() {
		if (mDataValid && mCursor != null) {
			return mCursor.getCount();
		} else {
			return 0;
		}
	}

	/**
	 * @see android.widget.ListAdapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		if (mDataValid && mCursor != null) {
			if (mCursor.moveToPosition(position)) {
				return mCursor.getLong(mRowIDColumn);
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	/**
	 * This method will move the Cursor to the correct position and call
	 * {@link #onBindViewHolderCursor(android.support.v7.widget.RecyclerView.ViewHolder, android.database.Cursor)}
	 * .
	 *
	 * @param holder {@inheritDoc}
	 * @param i {@inheritDoc}
	 */
	@Override
	public void onBindViewHolder(VH holder, int i) {
		if (!mDataValid) {
			throw new IllegalStateException("this should only be called when the cursor is valid");
		}
		if (!mCursor.moveToPosition(i)) {
			throw new IllegalStateException("couldn't move cursor to position " + i);
		}
		onBindViewHolderCursor(holder, mCursor);
	}

	/**
	 * See {@link android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, *
	 * android.database.Cursor)} , {@link #onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder, int)}
	 *
	 * @param holder View holder.
	 * @param cursor The cursor from which to get the data. The cursor is already moved to the correct position.
	 */
	public abstract void onBindViewHolderCursor(VH holder, Cursor cursor);

	/**
	 * Runs a query with the specified constraint. This query is requested by
	 * the filter attached to this adapter.
	 * <p/>
	 * The query is provided by a {@link android.widget.FilterQueryProvider}. If
	 * no provider is specified, the current cursor is not filtered and
	 * returned.
	 * <p/>
	 * After this method returns the resulting cursor is passed to
	 * {@link #changeCursor(Cursor)} and the previous cursor is closed.
	 * <p/>
	 * This method is always executed on a background thread, not on the
	 * application's main thread (or UI thread.)
	 * <p/>
	 * Contract: when constraint is null or empty, the original results, prior
	 * to any filtering, must be returned.
	 *
	 * @param constraint the constraint with which the query must be filtered
	 *
	 * @return a Cursor representing the results of the new query
	 *
	 * @see #getFilter()
	 * @see #getFilterQueryProvider()
	 * @see #setFilterQueryProvider(android.widget.FilterQueryProvider)
	 */
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (mFilterQueryProvider != null) {
			return mFilterQueryProvider.runQuery(constraint);
		}

		return mCursor;
	}

	/**
	 * Swap in a new Cursor, returning the old Cursor. Unlike
	 * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
	 * closed.
	 *
	 * @param newCursor The new cursor to be used.
	 *
	 * @return Returns the previously set Cursor, or null if there wasa not one. If the given new Cursor is the same
	 * instance is the previously set Cursor, null is also returned.
	 */
	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == mCursor) {
			return null;
		}
		Cursor oldCursor = mCursor;
		if (oldCursor != null) {
			if (mChangeObserver != null) {
				oldCursor.unregisterContentObserver(mChangeObserver);
			}
			if (mDataSetObserver != null) {
				oldCursor.unregisterDataSetObserver(mDataSetObserver);
			}
		}
		mCursor = newCursor;
		if (newCursor != null) {
			if (mChangeObserver != null) {
				newCursor.registerContentObserver(mChangeObserver);
			}
			if (mDataSetObserver != null) {
				newCursor.registerDataSetObserver(mDataSetObserver);
			}
			mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
//			mRowIDColumn = newCursor.getColumnIndexOrThrow("_Cid");
			mDataValid = true;
			// notify the observers about the new cursor
			notifyDataSetChanged();
		} else {
			mRowIDColumn = -1;
			mDataValid = false;
			// notify the observers about the lack of a data set
			// notifyDataSetInvalidated();
			notifyItemRangeRemoved(0, getItemCount());
		}
		return oldCursor;
	}

	void init(Cursor c) {
		boolean cursorPresent = c != null;
		mCursor = c;
		mDataValid = cursorPresent;
		mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;

		mChangeObserver = new ChangeObserver();
		mDataSetObserver = new MyDataSetObserver();

		if (cursorPresent) {
			if (mChangeObserver != null) {
				c.registerContentObserver(mChangeObserver);
			}
			if (mDataSetObserver != null) {
				c.registerDataSetObserver(mDataSetObserver);
			}
		}
	}

	protected boolean isDataValid() {
		return mDataValid;
	}

	/**
	 * Called when the {@link ContentObserver} on the cursor receives a change
	 * notification. Can be implemented by sub-class.
	 *
	 * @see ContentObserver#onChange(boolean)
	 */
	protected void onContentChanged() {

	}

	private class ChangeObserver
			extends ContentObserver {

		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			onContentChanged();
		}
	}

	private class MyDataSetObserver
			extends DataSetObserver {

		@Override
		public void onChanged() {
			mDataValid = true;
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			mDataValid = false;
			// notifyDataSetInvalidated();
			notifyItemRangeRemoved(0, getItemCount());
		}
	}

	/**
	 * <p>
	 * The CursorFilter delegates most of the work to the CursorAdapter.
	 * Subclasses should override these delegate methods to run the queries and
	 * convert the results into String that can be used by auto-completion
	 * widgets.
	 * </p>
	 */

}