package com.jastley.exodusnetwork.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import com.jastley.exodusnetwork.app.App;
import com.jastley.exodusnetwork.database.AppManifestDatabase;
import com.jastley.exodusnetwork.database.models.DestinyInventoryItemDefinition;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ManifestRepository {

    @Inject
    AppManifestDatabase manifestDatabase;

    @Inject
    public ManifestRepository() {
        App.getApp().getAppComponent().inject(this);
    }

    public LiveData<PagedList<DestinyInventoryItemDefinition>> getPagedItemsList() {
        return new LivePagedListBuilder<>(manifestDatabase.getInventoryItemDAO().getPagedInventoryItems(), 30).build();
    }
}
