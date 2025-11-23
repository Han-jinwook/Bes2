package com.bes2.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.bes2.data.model.ImageClusterEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ImageClusterDao_Impl implements ImageClusterDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ImageClusterEntity> __insertionAdapterOfImageClusterEntity;

  private final EntityDeletionOrUpdateAdapter<ImageClusterEntity> __deletionAdapterOfImageClusterEntity;

  private final EntityDeletionOrUpdateAdapter<ImageClusterEntity> __updateAdapterOfImageClusterEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateImageClusterReviewStatus;

  private final SharedSQLiteStatement __preparedStmtOfClearAllImageClusters;

  public ImageClusterDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfImageClusterEntity = new EntityInsertionAdapter<ImageClusterEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `image_clusters` (`id`,`best_image_uri`,`second_best_image_uri`,`creation_time`,`review_status`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ImageClusterEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getBestImageUri() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getBestImageUri());
        }
        if (entity.getSecondBestImageUri() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSecondBestImageUri());
        }
        statement.bindLong(4, entity.getCreationTime());
        statement.bindString(5, entity.getReviewStatus());
      }
    };
    this.__deletionAdapterOfImageClusterEntity = new EntityDeletionOrUpdateAdapter<ImageClusterEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `image_clusters` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ImageClusterEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfImageClusterEntity = new EntityDeletionOrUpdateAdapter<ImageClusterEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `image_clusters` SET `id` = ?,`best_image_uri` = ?,`second_best_image_uri` = ?,`creation_time` = ?,`review_status` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ImageClusterEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getBestImageUri() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getBestImageUri());
        }
        if (entity.getSecondBestImageUri() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSecondBestImageUri());
        }
        statement.bindLong(4, entity.getCreationTime());
        statement.bindString(5, entity.getReviewStatus());
        statement.bindString(6, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateImageClusterReviewStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE image_clusters SET review_status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllImageClusters = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM image_clusters";
        return _query;
      }
    };
  }

  @Override
  public Object insertImageCluster(final ImageClusterEntity imageCluster,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfImageClusterEntity.insertAndReturnId(imageCluster);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteImageCluster(final ImageClusterEntity imageCluster,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfImageClusterEntity.handle(imageCluster);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateImageCluster(final ImageClusterEntity imageCluster,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfImageClusterEntity.handle(imageCluster);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateImageClusterReviewStatus(final String id, final String newStatus,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateImageClusterReviewStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, newStatus);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateImageClusterReviewStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllImageClusters(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllImageClusters.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAllImageClusters.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<ImageClusterEntity> getImageClusterById(final String id) {
    final String _sql = "SELECT * FROM image_clusters WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_clusters"}, new Callable<ImageClusterEntity>() {
      @Override
      @Nullable
      public ImageClusterEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBestImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "best_image_uri");
          final int _cursorIndexOfSecondBestImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "second_best_image_uri");
          final int _cursorIndexOfCreationTime = CursorUtil.getColumnIndexOrThrow(_cursor, "creation_time");
          final int _cursorIndexOfReviewStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "review_status");
          final ImageClusterEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpBestImageUri;
            if (_cursor.isNull(_cursorIndexOfBestImageUri)) {
              _tmpBestImageUri = null;
            } else {
              _tmpBestImageUri = _cursor.getString(_cursorIndexOfBestImageUri);
            }
            final String _tmpSecondBestImageUri;
            if (_cursor.isNull(_cursorIndexOfSecondBestImageUri)) {
              _tmpSecondBestImageUri = null;
            } else {
              _tmpSecondBestImageUri = _cursor.getString(_cursorIndexOfSecondBestImageUri);
            }
            final long _tmpCreationTime;
            _tmpCreationTime = _cursor.getLong(_cursorIndexOfCreationTime);
            final String _tmpReviewStatus;
            _tmpReviewStatus = _cursor.getString(_cursorIndexOfReviewStatus);
            _result = new ImageClusterEntity(_tmpId,_tmpBestImageUri,_tmpSecondBestImageUri,_tmpCreationTime,_tmpReviewStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ImageClusterEntity>> getAllImageClusters() {
    final String _sql = "SELECT * FROM image_clusters ORDER BY creation_time DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_clusters"}, new Callable<List<ImageClusterEntity>>() {
      @Override
      @NonNull
      public List<ImageClusterEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBestImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "best_image_uri");
          final int _cursorIndexOfSecondBestImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "second_best_image_uri");
          final int _cursorIndexOfCreationTime = CursorUtil.getColumnIndexOrThrow(_cursor, "creation_time");
          final int _cursorIndexOfReviewStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "review_status");
          final List<ImageClusterEntity> _result = new ArrayList<ImageClusterEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageClusterEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpBestImageUri;
            if (_cursor.isNull(_cursorIndexOfBestImageUri)) {
              _tmpBestImageUri = null;
            } else {
              _tmpBestImageUri = _cursor.getString(_cursorIndexOfBestImageUri);
            }
            final String _tmpSecondBestImageUri;
            if (_cursor.isNull(_cursorIndexOfSecondBestImageUri)) {
              _tmpSecondBestImageUri = null;
            } else {
              _tmpSecondBestImageUri = _cursor.getString(_cursorIndexOfSecondBestImageUri);
            }
            final long _tmpCreationTime;
            _tmpCreationTime = _cursor.getLong(_cursorIndexOfCreationTime);
            final String _tmpReviewStatus;
            _tmpReviewStatus = _cursor.getString(_cursorIndexOfReviewStatus);
            _item = new ImageClusterEntity(_tmpId,_tmpBestImageUri,_tmpSecondBestImageUri,_tmpCreationTime,_tmpReviewStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ImageClusterEntity>> getImageClustersByReviewStatus(final String reviewStatus) {
    final String _sql = "SELECT * FROM image_clusters WHERE review_status = ? ORDER BY creation_time DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, reviewStatus);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_clusters"}, new Callable<List<ImageClusterEntity>>() {
      @Override
      @NonNull
      public List<ImageClusterEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBestImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "best_image_uri");
          final int _cursorIndexOfSecondBestImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "second_best_image_uri");
          final int _cursorIndexOfCreationTime = CursorUtil.getColumnIndexOrThrow(_cursor, "creation_time");
          final int _cursorIndexOfReviewStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "review_status");
          final List<ImageClusterEntity> _result = new ArrayList<ImageClusterEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageClusterEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpBestImageUri;
            if (_cursor.isNull(_cursorIndexOfBestImageUri)) {
              _tmpBestImageUri = null;
            } else {
              _tmpBestImageUri = _cursor.getString(_cursorIndexOfBestImageUri);
            }
            final String _tmpSecondBestImageUri;
            if (_cursor.isNull(_cursorIndexOfSecondBestImageUri)) {
              _tmpSecondBestImageUri = null;
            } else {
              _tmpSecondBestImageUri = _cursor.getString(_cursorIndexOfSecondBestImageUri);
            }
            final long _tmpCreationTime;
            _tmpCreationTime = _cursor.getLong(_cursorIndexOfCreationTime);
            final String _tmpReviewStatus;
            _tmpReviewStatus = _cursor.getString(_cursorIndexOfReviewStatus);
            _item = new ImageClusterEntity(_tmpId,_tmpBestImageUri,_tmpSecondBestImageUri,_tmpCreationTime,_tmpReviewStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
