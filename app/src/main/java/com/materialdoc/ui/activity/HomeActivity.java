package com.materialdoc.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;
import com.materialdoc.R;
import com.materialdoc.model.data.ChildItem;
import com.materialdoc.model.data.ParentItem;
import com.materialdoc.ui.adapter.ItemAdapter;
import com.materialdoc.ui.data.IViewType;
import com.materialdoc.ui.data.ItemDisplayable;
import com.materialdoc.ui.data.ItemID;
import com.materialdoc.ui.data.TitleDisplayable;
import com.materialdoc.utils.IOUtils;
import com.materialdoc.utils.L;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity {

    private ItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_home);
        initRecycleView();
        loadData();
    }

    private void initRecycleView() {
        mAdapter = new ItemAdapter();
        mAdapter.setListener(new ItemAdapter.Listener() {
            @Override
            public void onDocumentClicked(ItemDisplayable displayable) {
                handleDocumentClick(displayable);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    private void loadData() {
        Observable.create(loadDataFromAssets())
                .map(toDisplayableList())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<IViewType>>() {
                    @Override
                    public void call(List<IViewType> dataList) {
                        mAdapter.setData(dataList);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        L.e("Error during loading json data list", e);
                    }
                });
    }

    @NonNull
    private Observable.OnSubscribe<List<ParentItem>> loadDataFromAssets() {
        return new Observable.OnSubscribe<List<ParentItem>>() {
            @Override
            public void call(Subscriber<? super List<ParentItem>> subscriber) {
                try {
                    InputStream inputStream = getAssets().open("json/data.json");
                    String json = IOUtils.toString(inputStream);
                    Gson gson = new Gson();
                    ParentItem[] parentItemArr = gson.fromJson(json, ParentItem[].class);

                    subscriber.onNext(Arrays.asList(parentItemArr));
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        };
    }

    @NonNull
    private Func1<List<ParentItem>, List<IViewType>> toDisplayableList() {
        return new Func1<List<ParentItem>, List<IViewType>>() {
            @Override
            public List<IViewType> call(List<ParentItem> parentItemList) {
                List<IViewType> typeList = new ArrayList<>();
                for (ParentItem parentItem : parentItemList) {
                    typeList.add(new TitleDisplayable(parentItem.title));
                    if (parentItem.itemsList == null) {
                        continue;
                    }

                    for (ChildItem item : parentItem.itemsList) {
                        typeList.add(toDisplayable(item));
                    }
                }

                return typeList;
            }
        };
    }

    @NonNull
    private ItemDisplayable toDisplayable(ChildItem item) {
        ItemDisplayable displayable = new ItemDisplayable();
        displayable.setId(item.id);
        displayable.setTitle(item.title);
        displayable.setDescription(item.description);
        displayable.setImagePath(item.image);
        return displayable;
    }

    private void handleDocumentClick(ItemDisplayable displayable) {
        int documentId = displayable.getId();
        switch (documentId) {

            //Buttons
            case ItemID.RAISED_BUTTON:
                RaisedButtonActivity.start(this);
                break;
            case ItemID.FLAT_BUTTON:
                FlatButtonActivity.start(this);
                break;

            //Progress
            case ItemID.CIRCULAR_PROGRESS:
                CircularProgressActivity.start(this);
                break;
            case ItemID.LINEAR_PROGRESS:
                LinearProgressActivity.start(this);
                break;

            //Selection controls
            case ItemID.CHECK_BOX:
                CheckBoxActivity.start(this);
                break;
            case ItemID.RADIO_BUTTON:
                RadioButtonActivity.start(this);
                break;
            case ItemID.SWITCH:
                SwitchActivity.start(this);
                break;

            //edit fields
            case ItemID.TEXT_FIELD:
                EditFieldActivity.start(this);
                break;

            //other
            case ItemID.RATING_BAR:
                RatingBarActivity.start(this);
                break;
        }
    }
}
