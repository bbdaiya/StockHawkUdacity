package com.sam_chordas.android.stockhawk;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by bbdaiya on 05-Sep-16.
 */
public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataProvider(getApplicationContext(), intent);
    }
}
