package com.raffa064.deepnote;

import android.content.Intent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;
import java.util.List;
import android.net.Uri;

public class FilteredWebViewClient extends WebViewClient {
	private List<String> filterList = new ArrayList<>();
	
	public void addFilter(String... filters) {
		for (String filter : filters) {
			filterList.add(filter);
		}
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		boolean matchesWithFilter = false;
		
		for (String filter : filterList) {
			if (url.matches(filter)) {
				matchesWithFilter = true;
				break;
			}
		}
		
		if (matchesWithFilter) {
			return false;
		}
		
		// Open in external browser
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		view.getContext().startActivity(intent);
		
		return true;
	}
}
