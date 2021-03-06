package com.yibao.music.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.yibao.music.R;
import com.yibao.music.activity.SplashActivity;
import com.yibao.music.base.BaseMusicFragment;
import com.yibao.music.base.listener.OnUpdataTitleListener;
import com.yibao.music.fragment.dialogfrag.RelaxDialogFragment;
import com.yibao.music.fragment.dialogfrag.PreviewBigPicDialogFragment;
import com.yibao.music.model.MusicBean;
import com.yibao.music.model.greendao.MusicBeanDao;
import com.yibao.music.util.Constants;
import com.yibao.music.util.FileUtil;
import com.yibao.music.util.LogUtil;
import com.yibao.music.util.ReadFavoriteFileUtil;
import com.yibao.music.util.ToastUtil;
import com.yibao.music.view.CircleImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * @项目名： ArtisanMusic
 * @包名： com.yibao.music.folder
 * @文件名: AboutFragment
 * @author: Stran
 * @Email: www.strangermy@outlook.com / www.strangermy98@gmail.com
 * @创建时间: 2018/2/9 20:51
 * @描述： {TODO}
 */

public class AboutFragment extends BaseMusicFragment {


    @BindView(R.id.about_header_iv)
    CircleImageView mAboutHeaderIv;

    @BindView(R.id.tv_backups_favorite)
    TextView mTvBackupsFavorite;
    @BindView(R.id.tv_recover_favorite)
    TextView mTvRecoverFavorite;
    @BindView(R.id.tv_share)
    TextView mTvShare;
    @BindView(R.id.tv_scanner_media)
    TextView mtScanerMedia;
    private long mCurrentPosition;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.about_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        initListener();
        return view;
    }

    private void initListener() {
        mCompositeDisposable.add(RxView.clicks(mAboutHeaderIv)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> PreviewBigPicDialogFragment.newInstance("")
                        .show(mFragmentManager, "album")));
        mCompositeDisposable.add(RxView.clicks(mTvBackupsFavorite)
                .throttleFirst(3, TimeUnit.SECONDS)
                .subscribe(o -> backupsFavoriteList()));
        mCompositeDisposable.add(RxView.clicks(mTvRecoverFavorite)
                .throttleFirst(3, TimeUnit.SECONDS)
                .subscribe(o -> recoverFavoriteList()));
        mCompositeDisposable.add(RxView.clicks(mtScanerMedia)
                .throttleFirst(3, TimeUnit.SECONDS)
                .subscribe(o -> scannerMedia()));
        mCompositeDisposable.add(RxView.clicks(mTvShare)
                .throttleFirst(3, TimeUnit.SECONDS)
                .subscribe(o -> shareMe()));
        mAboutHeaderIv.setOnLongClickListener(view -> {
            RelaxDialogFragment.newInstance().show(mFragmentManager, "girlsDialog");
            return true;
        });
    }

    private void scannerMedia() {
        Intent intent = new Intent(mActivity, SplashActivity.class);
        intent.putExtra(Constants.SCANNER_MEDIA, Constants.SCANNER_MEDIA);
        startActivity(intent);
    }

    private void shareMe() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, mActivity.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_info));
        shareIntent.setType("text/plain");
        startActivity(shareIntent);
    }

    private void recoverFavoriteList() {
        if (FileUtil.getFavoriteFile()) {
            HashMap<String, String> songInfoMap = new HashMap<>();
            Set<String> stringSet = ReadFavoriteFileUtil.stringToSet();
            for (String s : stringSet) {
                String songName = s.substring(0, s.lastIndexOf("T"));
                String favoriteTime = s.substring(s.lastIndexOf("T") + 1);
                songInfoMap.put(songName, favoriteTime);
            }
            mCompositeDisposable.add(Observable.fromIterable(mSongList).map(musicBean -> {
                //将歌名截取出来进行比较
                String favoriteTime = songInfoMap.get(musicBean.getTitle());
                if (favoriteTime != null) {
                    musicBean.setTime(favoriteTime);
                    musicBean.setIsFavorite(true);
                    mMusicBeanDao.update(musicBean);
                }
                return mCurrentPosition++;
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(currentPostion -> {
                        if (currentPostion == mSongList.size() - 1)
                            if (mActivity instanceof OnUpdataTitleListener) {
                                ((OnUpdataTitleListener) mActivity).checkCurrentFavorite();
                            }
                    }));

        } else {
            ToastUtil.showNotFoundFavoriteFile(mActivity);
        }
    }

    private void backupsFavoriteList() {
        List<MusicBean> list = mMusicBeanDao.queryBuilder().where(MusicBeanDao.Properties.IsFavorite.eq(true)).build().list();
        mCompositeDisposable.add(Observable.fromIterable(list)
                .map(musicBean -> {
                    String songInfo = musicBean.getTitle() + "T" + musicBean.getAddTime();
                    ReadFavoriteFileUtil.writeFile(songInfo);
                    return songInfo;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favoriteName -> LogUtil.d(" 更新本地收藏文件==========   "+favoriteName)));
        ToastUtil.showFavoriteListBackupsDown(mActivity);
    }

    @Override
    protected void changeEditStatus(int currentIndex) {
    }

    public static AboutFragment newInstance() {

        return new AboutFragment();
    }

}
