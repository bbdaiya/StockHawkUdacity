package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by bbdaiya on 04-Sep-16.
 */
public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor cursor;
    private int appWidgetId;
    public final String LOG = WidgetDataProvider.class.getSimpleName();

    public WidgetDataProvider(Context mContext, Intent intent){
        this.mContext = mContext;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        try {
//
            cursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDataSetChanged() {

        try {

            cursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        cursor.close();
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
        String symbol,  change, bidprice;
        symbol=change=bidprice="";

        if(cursor.moveToPosition(position)){
            symbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
            change = cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE));
            bidprice = cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE));
        }
        rv.setTextViewText(R.id.stock_symbol, symbol);
        rv.setTextViewText(R.id.change, change);
        rv.setTextViewText(R.id.bid_price, bidprice);
        if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        if (Utils.showPercent) {
            rv.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex("percent_change")));
        } else {
            rv.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex("change")));
        }




        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return cursor.getInt(cursor.getColumnIndex(QuoteColumns._ID));
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
